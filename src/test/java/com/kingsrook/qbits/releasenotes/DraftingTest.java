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



package com.kingsrook.qbits.releasenotes;


import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.releasenotes.drafting.ReleaseNotesDrafter.DraftItem;
import com.kingsrook.qbits.releasenotes.drafting.ReleaseNotesDrafter.DraftOptions;
import com.kingsrook.qbits.releasenotes.drafting.ReleaseNotesSanitizer;
import com.kingsrook.qbits.releasenotes.drafting.RuleBasedDrafter;
import com.kingsrook.qbits.releasenotes.sources.ComponentVersionsSource;
import com.kingsrook.qbits.releasenotes.sources.ReleaseChange;
import com.kingsrook.qbits.releasenotes.sources.ReleaseContext;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qbits.releasenotes.ReleaseNotesTestSupport.pr;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Rule-based drafting, the sanitizer and platform changes.
 *******************************************************************************/
class DraftingTest
{

   /*******************************************************************************
    ** Categories, exclusions, authored text and cleaning.
    *******************************************************************************/
   @Test
   void drafts()
   {
      List<DraftItem> items = new RuleBasedDrafter().draft(List.of(
         pr(1, "feat(crm)!: Bulk edit contacts (#1)", ""),
         pr(2, "fix: totals were wrong on invoices, closes #77", ""),
         pr(3, "perf: faster search", ""),
         pr(4, "security: tighter session timeouts", ""),
         pr(5, "remove: legacy export", ""),
         pr(6, "Make the editor remember drafts", "", "bug"),
         pr(7, "Add a calendar", "", "enhancement", "feature"),
         pr(8, "Something", "", "security"),
         pr(9, "chore: tidy", ""),
         pr(10, "ci: speed up pipeline", ""),
         pr(11, "Update the image for k8s deploy", ""),
         pr(12, "feat: thing", "## Release notes\n\nYou can now pin `favourite` reports.\n\n## Testing\nran it"),
         pr(13, "feat: other", "Release notes: none"),
         pr(14, "fix: crash on save ABC-123", "", "skip-release-notes"),
         pr(15, "feat: Bulk edit contacts", ""),
         pr(16, "fix: leaked e-mail", "Release notes: Contact owner@example.com for help."),
         new ReleaseChange("RELEASE", "Website-Frontend#3", "[Docs](https://x) improved **layout**", "", null, null)), new DraftOptions(true));

      Map<String, DraftItem> byRef = new java.util.HashMap<>();
      items.forEach(item -> byRef.put(item.sourceRef(), item));
      assertThat(byRef.get("Website-Backend#1")).extracting(DraftItem::category, DraftItem::text, DraftItem::included).containsExactly("NEW", "Bulk edit contacts.", true);
      assertThat(byRef.get("Website-Backend#2")).extracting(DraftItem::category, DraftItem::text).containsExactly("FIXED", "Totals were wrong on invoices.");
      assertThat(byRef.get("Website-Backend#3").category()).isEqualTo("IMPROVED");
      assertThat(byRef.get("Website-Backend#4").category()).isEqualTo("SECURITY");
      assertThat(byRef.get("Website-Backend#5").category()).isEqualTo("REMOVED");
      assertThat(byRef.get("Website-Backend#6").category()).isEqualTo("FIXED");
      assertThat(byRef.get("Website-Backend#7").category()).isEqualTo("NEW");
      assertThat(byRef.get("Website-Backend#8").category()).isEqualTo("SECURITY");
      assertThat(byRef.get("Website-Backend#9").included()).isFalse();
      assertThat(byRef.get("Website-Backend#10").included()).isFalse();
      assertThat(byRef.get("Website-Backend#11").included()).isFalse();
      assertThat(byRef.get("Website-Backend#12").text()).isEqualTo("You can now pin favourite reports.");
      assertThat(byRef.get("Website-Backend#13").included()).isFalse();
      assertThat(byRef.get("Website-Backend#14").included()).isFalse();
      assertThat(byRef.get("Website-Backend#15").included()).as("duplicate").isFalse();
      assertThat(byRef.get("Website-Backend#16").included()).as("e-mail").isFalse();
      assertThat(byRef.get("Website-Frontend#3").text()).isEqualTo("Docs improved layout.");
      assertThat(items.get(0).included()).isTrue();
      assertThat(items.get(items.size() - 1).included()).isFalse();
      assertThat(items.stream().filter(DraftItem::included).map(DraftItem::text)).allMatch(text -> ReleaseNotesSanitizer.problems(text).isEmpty());
   }



   /*******************************************************************************
    ** Summary wording.
    *******************************************************************************/
   @Test
   void summaries()
   {
      assertThat(RuleBasedDrafter.summarize(List.of())).isEqualTo("Maintenance and reliability work behind the scenes.");
      assertThat(RuleBasedDrafter.summarize(List.of("NEW"))).isEqualTo("This release brings 1 new feature.");
      assertThat(RuleBasedDrafter.summarize(List.of("NEW", "NEW", "IMPROVED", "FIXED", "SECURITY")))
         .isEqualTo("This release brings 2 new features, 1 improvement, 1 fix and 1 security update.");
   }



   /*******************************************************************************
    ** What the sanitizer catches and lets through.
    *******************************************************************************/
   @Test
   void sanitizer()
   {
      assertThat(ReleaseNotesSanitizer.problems("Search is faster.")).isEmpty();
      assertThat(ReleaseNotesSanitizer.problems(null)).isEmpty();
      assertThat(ReleaseNotesSanitizer.problems("Fixed #12")).containsExactly("contains a pull request or issue number");
      assertThat(ReleaseNotesSanitizer.problems("see github.com/x")).containsExactly("contains a GitHub link");
      assertThat(ReleaseNotesSanitizer.problems("built from 6b496676")).containsExactly("contains a commit hash");
      assertThat(ReleaseNotesSanitizer.problems("Done in TECH-7")).containsExactly("contains a ticket key");
      assertThat(ReleaseNotesSanitizer.problems("ask a@b.co")).containsExactly("contains an e-mail address");
      assertThat(ReleaseNotesSanitizer.problems("host 10.100.22.64")).containsExactly("contains an IP address");
      assertThat(ReleaseNotesSanitizer.problems("on traefik.galaxy.lan")).containsExactly("contains an internal host name");
      assertThat(ReleaseNotesSanitizer.problems("token=abc")).containsExactly("contains a secret");
      assertThat(ReleaseNotesSanitizer.problems("ghp_abcdefghijklmnopqrstuvwxyz")).containsExactly("contains a secret");
      assertThat(ReleaseNotesSanitizer.problems("Colour #ffffff and decade 2026")).isEmpty();
      assertThat(ReleaseNotesSanitizer.linkProblems("/app/blog")).isEmpty();
      assertThat(ReleaseNotesSanitizer.linkProblems("https://kof22.com/x")).isEmpty();
      assertThat(ReleaseNotesSanitizer.linkProblems(null)).isEmpty();
      assertThat(ReleaseNotesSanitizer.linkProblems("//evil.com")).isNotEmpty();
      assertThat(ReleaseNotesSanitizer.linkProblems("javascript:alert(1)")).isNotEmpty();
      assertThat(ReleaseNotesSanitizer.linkProblems("https://github.com/a/b")).containsExactly("link points at GitHub");
   }



   /*******************************************************************************
    ** Platform changes: none without a previous release; one internal change
    ** listing upgrades; drafted as one optional item.
    *******************************************************************************/
   @Test
   void platform()
   {
      ComponentVersionsSource source = new ComponentVersionsSource();
      assertThat(source.getName()).isEqualTo("platform");
      assertThat(source.collect(ReleaseContext.of("a", "1.0.1", null, null, null, Map.of("x", "1"), null))).isEmpty();
      assertThat(source.collect(ReleaseContext.of("a", "1.0.1", "1.0.0", null, null, Map.of("x", "1"), Map.of("x", "1")))).isEmpty();
      List<ReleaseChange> changes = source.collect(ReleaseContext.of("a", "1.0.1", "1.0.0", null, null, Map.of("x", "2", "y", "1"), Map.of("x", "1", "z", "3")));
      assertThat(changes).hasSize(1);
      assertThat(changes.get(0).body()).contains("x 1 -> 2", "y (new) -> 1", "z 3 -> (removed)");
      DraftItem item = new RuleBasedDrafter().draft(changes, new DraftOptions(false)).get(0);
      assertThat(item.text()).isEqualTo(RuleBasedDrafter.PLATFORM_TEXT);
      assertThat(item.included()).isFalse();
      assertThat(new RuleBasedDrafter().draft(changes, new DraftOptions(true)).get(0).included()).isTrue();
   }
}
