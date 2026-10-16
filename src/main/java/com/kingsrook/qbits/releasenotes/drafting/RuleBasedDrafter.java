/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2026.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.kingsrook.qbits.releasenotes.drafting;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesPossibleValues.Category;
import com.kingsrook.qbits.releasenotes.sources.ReleaseChange;


/*******************************************************************************
 ** Deterministic drafting from PR titles, bodies and labels (no LLM).
 **
 ** - A "Release notes:" line or "## Release notes" section in the body is the
 **   author's customer text and is used as written ("none" excludes the PR).
 ** - Otherwise the title is cleaned: Conventional Commit prefix and scope,
 **   PR/issue numbers, links and code marks are removed.
 ** - Internal work is excluded: labels such as internal, chore, ci,
 **   dependencies, skip-release-notes; types chore, ci, build, test, refactor,
 **   docs, style; and titles about infrastructure or version bumps.
 ** - Category from the type (feat, fix, perf, security, remove) or labels.
 ** - Text that still fails the sanitizer is excluded.
 *******************************************************************************/
public class RuleBasedDrafter implements ReleaseNotesDrafter
{
   public static final String NAME          = "RULES";
   public static final String PLATFORM_TEXT = "Platform and reliability improvements.";

   private static final Set<String> SKIP_LABELS = Set.of("internal", "chore", "ci", "dependencies", "deps", "infrastructure", "infra",
      "skip-release-notes", "no-release-notes", "release-notes:skip", "documentation", "test", "tests", "refactor");
   private static final Set<String> SKIP_TYPES  = Set.of("chore", "ci", "build", "test", "tests", "refactor", "docs", "doc", "style", "deps", "release", "revert", "wip");

   private static final Map<String, Category> TYPE_CATEGORIES = Map.ofEntries(Map.entry("feat", Category.NEW), Map.entry("feature", Category.NEW),
      Map.entry("fix", Category.FIXED), Map.entry("bugfix", Category.FIXED), Map.entry("hotfix", Category.FIXED),
      Map.entry("perf", Category.IMPROVED), Map.entry("improve", Category.IMPROVED), Map.entry("improvement", Category.IMPROVED),
      Map.entry("ux", Category.IMPROVED), Map.entry("ui", Category.IMPROVED),
      Map.entry("security", Category.SECURITY), Map.entry("sec", Category.SECURITY),
      Map.entry("remove", Category.REMOVED), Map.entry("removed", Category.REMOVED), Map.entry("deprecate", Category.REMOVED));

   private static final Pattern CONVENTIONAL  = Pattern.compile("^\\s*([A-Za-z]+)(\\([^)]*\\))?!?:\\s*(.+)$");
   private static final Pattern NOTES_INLINE  = Pattern.compile("(?im)^\\s*(?:[-*]\\s*)?release[ -]?notes?\\s*:\\s*(\\S.*)$");
   private static final Pattern NOTES_SECTION = Pattern.compile("(?ims)^\\s*#{1,6}\\s*release[ -]?notes?\\s*:?\\s*$\\s*(.+?)(?=^\\s*#{1,6}\\s|\\z)");
   private static final Pattern INTERNAL      = Pattern.compile("(?i)\\b(bump|bumps|dependabot|renovate|liquibase|changelog|migrations?|kubernetes|k8s|helm|terraform|opentofu|tofu|circleci|ci|pipeline|docker|dockerfile|pods?|deploy|deployment|infisical|secrets?|snapshot|checkstyle|jacoco|lint|merge|rc\\.\\d+)\\b");
   private static final Pattern NONE          = Pattern.compile("(?i)^\\s*(none|n/?a|-|no|nothing|internal( only)?)\\.?\\s*$");



   /*******************************************************************************
    ** Drafter name.
    *******************************************************************************/
   @Override
   public String getName()
   {
      return (NAME);
   }



   /*******************************************************************************
    ** Draft one item per change, sorted by category, duplicates excluded.
    *******************************************************************************/
   @Override
   public List<DraftItem> draft(List<ReleaseChange> changes, DraftOptions options)
   {
      List<DraftItem> items = new ArrayList<>();
      Set<String>     seen  = new HashSet<>();
      for(ReleaseChange change : changes)
      {
         DraftItem item = ReleaseChange.TYPE_PLATFORM.equals(change.sourceType())
            ? new DraftItem(Category.IMPROVED.name(), PLATFORM_TEXT, options.includePlatformUpdates(), change.sourceType(), change.ref(), truncate(change.body(), 500))
            : draftOne(change);
         if(item.included() && !seen.add(item.text().toLowerCase(Locale.ROOT)))
         {
            item = new DraftItem(item.category(), item.text(), false, item.sourceType(), item.sourceRef(), item.sourceTitle());
         }
         items.add(item);
      }
      items.sort(Comparator.comparing((DraftItem item) -> !item.included()).thenComparing(item -> Category.of(item.category()).ordinal()));
      return (items);
   }



   /*******************************************************************************
    ** Draft one PR or release line.
    *******************************************************************************/
   DraftItem draftOne(ReleaseChange change)
   {
      String       title  = change.title() == null ? "" : change.title().trim();
      List<String> labels = change.labels() == null ? List.of() : change.labels();
      Matcher      conv   = CONVENTIONAL.matcher(title);
      String       type   = conv.matches() ? conv.group(1).toLowerCase(Locale.ROOT) : null;
      String       rest   = conv.matches() ? conv.group(3) : title;

      String authored = authoredText(change.body());
      boolean excluded = labels.stream().anyMatch(SKIP_LABELS::contains)
         || (type != null && SKIP_TYPES.contains(type))
         || (authored != null && NONE.matcher(authored).matches())
         || (authored == null && INTERNAL.matcher(rest).find());

      String text = clean(authored != null && !NONE.matcher(authored).matches() ? authored : rest);
      if(text.isEmpty() || !ReleaseNotesSanitizer.problems(text).isEmpty())
      {
         excluded = true;
         if(text.isEmpty())
         {
            text = "(no customer-facing text)";
         }
      }
      return (new DraftItem(category(type, labels).name(), text, !excluded, change.sourceType(), change.ref(), truncate(title, 500)));
   }



   /*******************************************************************************
    ** The author's customer text from the body, or null.
    *******************************************************************************/
   static String authoredText(String body)
   {
      if(body == null || body.isBlank())
      {
         return (null);
      }
      Matcher section = NOTES_SECTION.matcher(body);
      if(section.find())
      {
         String text = section.group(1).replaceAll("(?m)^\\s*[-*]\\s+", "").replaceAll("<!--.*?-->", "").trim();
         if(!text.isEmpty())
         {
            return (text.split("\\R\\s*\\R")[0].replaceAll("\\s*\\R\\s*", " ").trim());
         }
      }
      Matcher inline = NOTES_INLINE.matcher(body);
      return (inline.find() ? inline.group(1).trim() : null);
   }



   /*******************************************************************************
    ** Category from the commit type, then labels, else Improved.
    *******************************************************************************/
   static Category category(String type, List<String> labels)
   {
      if(type != null && TYPE_CATEGORIES.containsKey(type))
      {
         return (TYPE_CATEGORIES.get(type));
      }
      if(labels.contains("security"))
      {
         return (Category.SECURITY);
      }
      if(labels.contains("bug") || labels.contains("fix"))
      {
         return (Category.FIXED);
      }
      if(labels.contains("feature") || labels.contains("new feature"))
      {
         return (Category.NEW);
      }
      return (Category.IMPROVED);
   }



   /*******************************************************************************
    ** Plain customer text: references, links and code marks removed, first
    ** letter capitalised, ending with a period.
    *******************************************************************************/
   static String clean(String raw)
   {
      String text = raw == null ? "" : raw;
      text = text.replaceAll("\\[([^\\]]*)\\]\\([^)]*\\)", "$1");
      text = text.replaceAll("https?://\\S+", "");
      text = text.replaceAll("\\(\\s*#\\d+\\s*\\)", "");
      text = text.replaceAll("(?i)\\b(closes|fixes|resolves|refs?)\\s+#\\d+", "");
      text = text.replaceAll("(?<![\\w&])#\\d+\\b", "");
      text = text.replaceAll("\\b[A-Z][A-Z0-9]{1,9}-\\d{1,6}\\b:?", "");
      text = text.replace("`", "").replace("**", "").replace("__", "");
      text = text.replaceAll("\\(\\s*\\)", "");
      text = text.replaceAll("\\s+", " ").trim();
      text = text.replaceAll("^[-:;,\\s]+|[-:;,\\s]+$", "");
      if(text.isEmpty())
      {
         return (text);
      }
      text = Character.toUpperCase(text.charAt(0)) + text.substring(1);
      if(!text.matches(".*[.!?]$"))
      {
         text = text + ".";
      }
      return (truncate(text, 2000));
   }



   /*******************************************************************************
    ** The customer summary: counts by kind, or a maintenance line.
    *******************************************************************************/
   public static String summarize(List<String> includedCategories)
   {
      long added    = includedCategories.stream().filter(Category.NEW.name()::equals).count();
      long improved = includedCategories.stream().filter(c -> Category.IMPROVED.name().equals(c) || Category.REMOVED.name().equals(c)).count();
      long fixed    = includedCategories.stream().filter(Category.FIXED.name()::equals).count();
      long security = includedCategories.stream().filter(Category.SECURITY.name()::equals).count();
      List<String> parts = new ArrayList<>();
      addPart(parts, added, "new feature", "new features");
      addPart(parts, improved, "improvement", "improvements");
      addPart(parts, fixed, "fix", "fixes");
      addPart(parts, security, "security update", "security updates");
      if(parts.isEmpty())
      {
         return ("Maintenance and reliability work behind the scenes.");
      }
      String joined = parts.size() == 1 ? parts.get(0) : String.join(", ", parts.subList(0, parts.size() - 1)) + " and " + parts.get(parts.size() - 1);
      return ("This release brings " + joined + ".");
   }



   /*******************************************************************************
    ** "2 fixes" style part.
    *******************************************************************************/
   private static void addPart(List<String> parts, long count, String singular, String plural)
   {
      if(count > 0)
      {
         parts.add(count + " " + (count == 1 ? singular : plural));
      }
   }



   /*******************************************************************************
    ** At most max characters.
    *******************************************************************************/
   static String truncate(String text, int max)
   {
      if(text == null)
      {
         return (null);
      }
      return (text.length() <= max ? text : text.substring(0, max - 1) + "…");
   }
}
