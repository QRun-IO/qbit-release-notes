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


import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.releasenotes.sources.GitHubReleaseSource;
import com.kingsrook.qbits.releasenotes.sources.HttpFetcher;
import com.kingsrook.qbits.releasenotes.sources.ReleaseChange;
import com.kingsrook.qbits.releasenotes.sources.ReleaseContext;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** The GitHub source against recorded payloads.
 *******************************************************************************/
class GitHubReleaseSourceTest
{
   private static final String BASE = "https://api.github.com/repos/acme/app";

   private static final String RELEASES = """
      [
        {"tag_name":"v0.2.0","published_at":"2026-03-21T00:00:00Z","draft":false,"prerelease":false,"body":"* old by @x in https://github.com/acme/app/pull/1"},
        {"tag_name":"v0.1.111","published_at":"2026-10-15T19:00:48Z","draft":false,"prerelease":false,
         "body":"## What's Changed\\n* feat: new thing by @dev in https://github.com/acme/app/pull/262\\n\\n**Full Changelog**: https://github.com/acme/app/compare/v0.1.110...v0.1.111"},
        {"tag_name":"v0.1.110","published_at":"2026-10-15T17:11:55Z","draft":false,"prerelease":false,"body":"* fix: one by @dev in https://github.com/acme/app/pull/260\\n* fix: two by @dev in https://github.com/acme/app/pull/261"},
        {"tag_name":"v0.1.109","published_at":"2026-10-15T16:29:19Z","draft":false,"prerelease":false,"body":"* chore: old by @dev in https://github.com/acme/app/pull/259"},
        {"tag_name":"v0.1.112","published_at":"2026-10-16T00:00:00Z","draft":true,"prerelease":false,"body":"draft"}
      ]""";



   /*******************************************************************************
    ** A scripted fetcher.
    *******************************************************************************/
   static class Fake implements HttpFetcher
   {
      final Map<String, Response> responses = new HashMap<>();
      final List<String>          tokens    = new ArrayList<>();



      /*******************************************************************************
       ** Scripted GET (404 when unscripted).
       *******************************************************************************/
      @Override
      public Response get(String url, String bearerToken)
      {
         tokens.add(bearerToken);
         return (responses.getOrDefault(url, new Response(404, "{}")));
      }
   }



   /*******************************************************************************
    ** A fetcher with the release list and PRs 260-262.
    *******************************************************************************/
   private static Fake fake()
   {
      Fake fake = new Fake();
      fake.responses.put(BASE + "/releases?per_page=100", new HttpFetcher.Response(200, RELEASES));
      fake.responses.put(BASE + "/pulls/262", new HttpFetcher.Response(200, "{\"title\":\"feat: new thing\",\"body\":\"Release notes: You can do the new thing.\",\"labels\":[{\"name\":\"Enhancement\"}],\"html_url\":\"https://github.com/acme/app/pull/262\"}"));
      fake.responses.put(BASE + "/pulls/261", new HttpFetcher.Response(200, "{\"title\":\"fix: two\",\"body\":null,\"labels\":[]}"));
      return (fake);
   }



   /*******************************************************************************
    ** TAG mode: versions after the previous one up to this one (the stale
    ** higher tag and drafts are ignored); unreadable PRs fall back to the line.
    *******************************************************************************/
   @Test
   void tagMode() throws Exception
   {
      Fake                fake    = fake();
      GitHubReleaseSource source  = new GitHubReleaseSource("acme", "app", GitHubReleaseSource.Mode.TAG).withHttpFetcher(fake).withTokenOverride("t0k");
      ReleaseContext      context = ReleaseContext.of("acme", "0.1.111", "0.1.109", null, Instant.parse("2026-10-15T20:00:00Z"), Map.of(), null);
      List<ReleaseChange> changes = source.collect(context);
      assertThat(changes).extracting(ReleaseChange::ref).containsExactly("app#260", "app#261", "app#262");
      assertThat(changes.get(0).sourceType()).isEqualTo("RELEASE");
      assertThat(changes.get(0).title()).isEqualTo("fix: one");
      assertThat(changes.get(2).labels()).containsExactly("enhancement");
      assertThat(changes.get(2).body()).contains("Release notes:");
      assertThat(context.warnings()).anyMatch(w -> w.contains("app#260"));
      assertThat(fake.tokens).containsOnly("t0k");
      assertThat(source.isGitHub()).isTrue();
      assertThat(source.getName()).isEqualTo("github:acme/app");

      ReleaseContext first = ReleaseContext.of("acme", "0.1.111", null, null, Instant.parse("2026-10-15T20:00:00Z"), Map.of(), null);
      assertThat(source.collect(first)).extracting(ReleaseChange::ref).containsExactly("app#262");
   }



   /*******************************************************************************
    ** WINDOW mode: releases published since the previous release.
    *******************************************************************************/
   @Test
   void windowMode() throws Exception
   {
      GitHubReleaseSource source = new GitHubReleaseSource("acme", "app", GitHubReleaseSource.Mode.WINDOW).withHttpFetcher(fake());
      ReleaseContext context = ReleaseContext.of("acme", "5.0.0", "4.0.0", Instant.parse("2026-10-15T17:00:00Z"), Instant.parse("2026-10-15T20:00:00Z"), Map.of(), null);
      assertThat(source.collect(context)).extracting(ReleaseChange::ref).containsExactly("app#260", "app#261", "app#262");

      ReleaseContext first = ReleaseContext.of("acme", "5.0.0", null, null, Instant.parse("2026-10-15T18:00:00Z"), Map.of(), null);
      assertThat(source.collect(first)).extracting(ReleaseChange::ref).containsExactly("app#260", "app#261");
   }



   /*******************************************************************************
    ** No access and nothing in scope are warnings, not failures.
    *******************************************************************************/
   @Test
   void warnings() throws Exception
   {
      GitHubReleaseSource denied  = new GitHubReleaseSource("acme", "private", GitHubReleaseSource.Mode.TAG).withHttpFetcher(new Fake()).withTokenEnvVar("RELEASE_NOTES_TEST_NO_SUCH_VAR");
      ReleaseContext      context = ReleaseContext.of("acme", "1.0.0", null, null, Instant.now(), Map.of(), null);
      assertThat(denied.collect(context)).isEmpty();
      assertThat(context.warnings()).anyMatch(w -> w.contains("HTTP 404") && w.contains("no token"));

      GitHubReleaseSource source = new GitHubReleaseSource("acme", "app", GitHubReleaseSource.Mode.TAG).withHttpFetcher(fake()).withTagPrefix("v").withApiBaseUrl("https://api.github.com");
      ReleaseContext none = ReleaseContext.of("acme", "9.9.9", "9.9.8", null, Instant.now(), Map.of(), null);
      assertThat(source.collect(none)).isEmpty();
      assertThat(none.warnings()).anyMatch(w -> w.contains("no releases in scope"));
      assertThat(source.getOwner()).isEqualTo("acme");
      assertThat(source.getRepository()).isEqualTo("app");
      assertThat(source.getMode()).isEqualTo(GitHubReleaseSource.Mode.TAG);
   }
}
