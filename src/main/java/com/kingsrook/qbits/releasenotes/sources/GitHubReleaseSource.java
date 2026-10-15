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


package com.kingsrook.qbits.releasenotes.sources;


import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;


/*******************************************************************************
 ** Changes from a GitHub repository's releases: the releases in scope are
 ** read, the pull requests their bodies link are collected, and each PR's
 ** title, body and labels become one change.
 **
 ** Modes:
 ** - TAG: releases whose tag version is after the previous recorded version
 **   and at most this version (the application's own repository; tags are
 **   tagPrefix + version).
 ** - WINDOW: releases published after the previous release was detected and
 **   up to now (a separately released repository, e.g. a frontend).
 **
 ** The token is read from an environment variable at call time and never
 ** stored. Without a token, private repositories return 404 and the source
 ** contributes nothing but a warning.
 *******************************************************************************/
public class GitHubReleaseSource implements ReleaseSource
{
   private static final Pattern PULL_LINK  = Pattern.compile("/pull/(\\d+)");
   private static final Pattern NOTES_LINE = Pattern.compile("^\\*\\s+(.+?)\\s+by\\s+@\\S+\\s+in\\s+\\S+/pull/(\\d+)\\s*$", Pattern.MULTILINE);
   private static final Pattern VERSION    = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)$");
   private static final int     MAX_PULLS  = 60;

   /*******************************************************************************
    ** How releases are selected.
    *******************************************************************************/
   public enum Mode
   {
      TAG,
      WINDOW
   }

   private String      owner;
   private String      repository;
   private Mode        mode        = Mode.TAG;
   private String      tagPrefix   = "v";
   private String      tokenEnvVar;
   private String      apiBaseUrl  = "https://api.github.com";
   private HttpFetcher httpFetcher = HttpFetcher.standard();

   private transient String tokenOverride;



   /*******************************************************************************
    ** Constructor.
    *******************************************************************************/
   public GitHubReleaseSource()
   {
   }



   /*******************************************************************************
    ** Constructor with the repository.
    *******************************************************************************/
   public GitHubReleaseSource(String owner, String repository, Mode mode)
   {
      this.owner = owner;
      this.repository = repository;
      this.mode = mode;
   }



   /*******************************************************************************
    ** Source name.
    *******************************************************************************/
   @Override
   public String getName()
   {
      return ("github:" + owner + "/" + repository);
   }



   /*******************************************************************************
    ** Calls GitHub.
    *******************************************************************************/
   @Override
   public boolean isGitHub()
   {
      return (true);
   }



   /*******************************************************************************
    ** Read the releases in scope and the pull requests they link.
    *******************************************************************************/
   @Override
   public List<ReleaseChange> collect(ReleaseContext context) throws Exception
   {
      String token = token();
      String base  = apiBaseUrl + "/repos/" + owner + "/" + repository;

      HttpFetcher.Response releasesResponse = httpFetcher.get(base + "/releases?per_page=100", token);
      if(releasesResponse.status() != 200)
      {
         context.warnings().add(getName() + ": releases unavailable (HTTP " + releasesResponse.status() + (token == null ? ", no token" : "") + ")");
         return (List.of());
      }

      List<JSONObject> selected = select(new JSONArray(releasesResponse.body()), context);
      if(selected.isEmpty())
      {
         context.warnings().add(getName() + ": no releases in scope");
         return (List.of());
      }

      ////////////////////////////////////////////////////////////////////
      // PR numbers in release order, with the release-notes line title //
      // as a fallback when the PR itself cannot be read.                //
      ////////////////////////////////////////////////////////////////////
      Map<Integer, String> pulls = new LinkedHashMap<>();
      for(JSONObject release : selected)
      {
         String  body  = release.optString("body", "");
         Matcher lines = NOTES_LINE.matcher(body);
         while(lines.find())
         {
            pulls.putIfAbsent(Integer.parseInt(lines.group(2)), lines.group(1));
         }
         Matcher links = PULL_LINK.matcher(body);
         while(links.find())
         {
            pulls.putIfAbsent(Integer.parseInt(links.group(1)), null);
         }
      }

      List<ReleaseChange> changes = new ArrayList<>();
      int                 read    = 0;
      for(Map.Entry<Integer, String> pull : pulls.entrySet())
      {
         if(read++ >= MAX_PULLS)
         {
            context.warnings().add(getName() + ": more than " + MAX_PULLS + " pull requests; the rest were not read");
            break;
         }
         String ref = repository + "#" + pull.getKey();
         HttpFetcher.Response pr = httpFetcher.get(base + "/pulls/" + pull.getKey(), token);
         if(pr.status() == 200)
         {
            JSONObject   json   = new JSONObject(pr.body());
            List<String> labels = new ArrayList<>();
            JSONArray    array  = json.optJSONArray("labels");
            for(int i = 0; array != null && i < array.length(); i++)
            {
               labels.add(array.getJSONObject(i).optString("name", "").toLowerCase());
            }
            changes.add(new ReleaseChange(ReleaseChange.TYPE_PR, ref, json.optString("title", pull.getValue()), json.optString("body", ""), labels, json.optString("html_url", null)));
         }
         else if(pull.getValue() != null)
         {
            context.warnings().add(ref + ": pull request unavailable (HTTP " + pr.status() + "); used the release line");
            changes.add(new ReleaseChange(ReleaseChange.TYPE_RELEASE, ref, pull.getValue(), "", List.of(), null));
         }
         else
         {
            context.warnings().add(ref + ": pull request unavailable (HTTP " + pr.status() + ")");
         }
      }
      return (changes);
   }



   /*******************************************************************************
    ** The published, non-draft releases in scope, oldest first.
    *******************************************************************************/
   List<JSONObject> select(JSONArray releases, ReleaseContext context)
   {
      List<JSONObject> selected = new ArrayList<>();
      JSONObject       latest   = null;
      for(int i = 0; i < releases.length(); i++)
      {
         JSONObject release = releases.getJSONObject(i);
         if(release.optBoolean("draft", false) || release.optBoolean("prerelease", false))
         {
            continue;
         }
         if(mode == Mode.TAG)
         {
            int[] version = parse(stripPrefix(release.optString("tag_name", "")));
            int[] current = parse(context.version());
            int[] after   = parse(context.previousVersion());
            if(version == null || current == null || compare(version, current) > 0)
            {
               continue;
            }
            boolean inScope = after == null ? compare(version, current) == 0 : compare(version, after) > 0;
            if(inScope)
            {
               selected.add(release);
            }
         }
         else
         {
            Instant published = publishedAt(release);
            if(published == null || published.isAfter(context.now()))
            {
               continue;
            }
            if(context.previousDetectedAt() == null)
            {
               if(latest == null || published.isAfter(publishedAt(latest)))
               {
                  latest = release;
               }
            }
            else if(published.isAfter(context.previousDetectedAt()))
            {
               selected.add(release);
            }
         }
      }
      if(latest != null)
      {
         selected.add(latest);
      }
      selected.sort((a, b) ->
      {
         Instant pa = publishedAt(a);
         Instant pb = publishedAt(b);
         return (pa == null || pb == null ? 0 : pa.compareTo(pb));
      });
      return (selected);
   }



   /*******************************************************************************
    ** A release's published_at, or null.
    *******************************************************************************/
   private static Instant publishedAt(JSONObject release)
   {
      String value = release.optString("published_at", null);
      try
      {
         return (value == null ? null : Instant.parse(value));
      }
      catch(Exception e)
      {
         return (null);
      }
   }



   /*******************************************************************************
    ** The tag without its prefix.
    *******************************************************************************/
   private String stripPrefix(String tag)
   {
      return (tagPrefix != null && tag.startsWith(tagPrefix) ? tag.substring(tagPrefix.length()) : tag);
   }



   /*******************************************************************************
    ** X.Y.Z as numbers, or null.
    *******************************************************************************/
   static int[] parse(String version)
   {
      Matcher matcher = version == null ? null : VERSION.matcher(version);
      if(matcher == null || !matcher.matches())
      {
         return (null);
      }
      return (new int[] {Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3))});
   }



   /*******************************************************************************
    ** Compare two parsed versions.
    *******************************************************************************/
   static int compare(int[] a, int[] b)
   {
      for(int i = 0; i < 3; i++)
      {
         if(a[i] != b[i])
         {
            return (Integer.compare(a[i], b[i]));
         }
      }
      return (0);
   }



   /*******************************************************************************
    ** The token, read now (override first, for tests).
    *******************************************************************************/
   private String token()
   {
      if(tokenOverride != null)
      {
         return (tokenOverride);
      }
      String value = tokenEnvVar == null ? null : System.getenv(tokenEnvVar);
      return (value == null || value.isBlank() ? null : value.trim());
   }



   /*******************************************************************************
    ** Use this token instead of the environment (tests only; never persisted).
    *******************************************************************************/
   public GitHubReleaseSource withTokenOverride(String token)
   {
      this.tokenOverride = token;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for tokenEnvVar: the environment variable holding a
    ** read-only token.
    *******************************************************************************/
   public GitHubReleaseSource withTokenEnvVar(String tokenEnvVar)
   {
      this.tokenEnvVar = tokenEnvVar;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for tagPrefix.
    *******************************************************************************/
   public GitHubReleaseSource withTagPrefix(String tagPrefix)
   {
      this.tagPrefix = tagPrefix;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for apiBaseUrl.
    *******************************************************************************/
   public GitHubReleaseSource withApiBaseUrl(String apiBaseUrl)
   {
      this.apiBaseUrl = apiBaseUrl;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for httpFetcher.
    *******************************************************************************/
   public GitHubReleaseSource withHttpFetcher(HttpFetcher httpFetcher)
   {
      this.httpFetcher = httpFetcher;
      return (this);
   }



   /*******************************************************************************
    ** Getter for owner.
    *******************************************************************************/
   public String getOwner()
   {
      return (this.owner);
   }



   /*******************************************************************************
    ** Getter for repository.
    *******************************************************************************/
   public String getRepository()
   {
      return (this.repository);
   }



   /*******************************************************************************
    ** Getter for mode.
    *******************************************************************************/
   public Mode getMode()
   {
      return (this.mode);
   }
}
