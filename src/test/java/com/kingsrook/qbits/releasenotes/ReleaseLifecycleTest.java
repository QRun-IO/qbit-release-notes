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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesItem;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesRelease;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesSettings;
import com.kingsrook.qqq.backend.core.alerts.NotificationAudience;
import com.kingsrook.qqq.backend.core.alerts.NotificationCategory;
import com.kingsrook.qqq.backend.core.alerts.NotificationSeverity;
import com.kingsrook.qqq.backend.core.alerts.NotificationSignal;
import com.kingsrook.qqq.backend.core.context.CapturedContext;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSystemUserSession;
import com.kingsrook.qqq.backend.core.serviceidentity.QServicePrincipalMetaData;
import com.kingsrook.qqq.backend.core.serviceidentity.ServiceIdentitiesMetaData;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qbits.releasenotes.ReleaseNotesTestSupport.NOTIFICATIONS;
import static com.kingsrook.qbits.releasenotes.ReleaseNotesTestSupport.pr;
import static com.kingsrook.qbits.releasenotes.ReleaseNotesTestSupport.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** Detection, drafting, approval, publishing, notification and What's new.
 *******************************************************************************/
class ReleaseLifecycleTest
{
   private static final List<com.kingsrook.qbits.releasenotes.sources.ReleaseChange> CHANGES = List.of(
      pr(10, "feat(blog): schedule posts for later (#10)", "Adds scheduling."),
      pr(11, "fix: search ignored accented letters", "Release notes: Search now finds words with accented letters."),
      pr(12, "build(qqq): QQQ 4.1.0-RC.13", ""),
      pr(13, "Bump helm chart", "", "dependencies"));



   /*******************************************************************************
    ** One release per version; nothing for development builds.
    *******************************************************************************/
   @Test
   void detectsOnce() throws Exception
   {
      QInstance           instance = ReleaseNotesTestSupport.instance("1.2.3", CHANGES);
      ReleaseNotesRuntime runtime  = ReleaseNotesTestSupport.runtime(instance, "2026-10-15T12:00:00Z");
      Integer             id       = runtime.service().detect();
      assertThat(id).isNotNull();
      assertThat(runtime.service().detect()).isNull();
      QRecord release = ReleaseNotesDb.byId(ReleaseNotesRelease.TABLE_NAME, id);
      assertThat(release.getValueString("status")).isEqualTo("DRAFT");
      assertThat(release.getValueString("title")).isEqualTo("What's new in Acme 1.2.3");
      assertThat(release.getValueString("summary")).isEqualTo("This release brings 1 new feature and 1 fix.");
      assertThat(release.getValueString("sourceLog")).contains("scripted: 4 change(s)");
      assertThat(NOTIFICATIONS).isEmpty();

      List<QRecord> items = ReleaseNotesDb.query(ReleaseNotesItem.TABLE_NAME, ReleaseNotesDb.eq("releaseId", id));
      assertThat(items).hasSize(4);
      assertThat(items.stream().filter(i -> Boolean.TRUE.equals(i.getValueBoolean("included"))).map(i -> i.getValueString("text")))
         .containsExactlyInAnyOrder("Schedule posts for later.", "Search now finds words with accented letters.");
      assertThat(items).allSatisfy(i -> assertThat(i.getValueString("sourceRef")).startsWith("Website-Backend#"));

      ReleaseNotesRuntime dev = ReleaseNotesRuntime.of(ReleaseNotesTestSupport.instance("1.2.4-SNAPSHOT", CHANGES));
      assertThat(dev.service().detect()).isNull();
      assertThat(ReleaseNotesDb.query(ReleaseNotesRelease.TABLE_NAME, new QQueryFilter())).isEmpty();
   }



   /*******************************************************************************
    ** With the release-notes principal declared, qbit work runs as it, never
    ** as the system user.
    *******************************************************************************/
   @Test
   void detectsAsTheDeclaredPrincipal() throws Exception
   {
      QInstance           instance = ReleaseNotesTestSupport.instance("1.2.3", CHANGES);
      ReleaseNotesRuntime runtime  = ReleaseNotesTestSupport.runtime(instance, "2026-10-15T12:00:00Z");
      ServiceIdentitiesMetaData.declare(instance, new QServicePrincipalMetaData().withName("release-notes"));
      String[] ranAs = new String[1];
      runtime.asSystem(() -> ranAs[0] = QContext.getQSession().getUser().getIdReference());
      assertThat(ranAs[0]).isEqualTo("service:release-notes");
      assertThat(runtime.service().detect()).isNotNull();
   }



   /*******************************************************************************
    ** Pods starting together record the version exactly once.
    *******************************************************************************/
   @Test
   void onceUnderConcurrency() throws Exception
   {
      QInstance           instance = ReleaseNotesTestSupport.instance("2.0.0", CHANGES);
      ReleaseNotesRuntime runtime  = ReleaseNotesRuntime.of(instance);
      int                 pods     = 8;
      CountDownLatch      start    = new CountDownLatch(1);
      ExecutorService     pool     = Executors.newFixedThreadPool(pods);
      List<Future<Integer>> results = new ArrayList<>();
      for(int i = 0; i < pods; i++)
      {
         Callable<Integer> pod = () ->
         {
            start.await();
            Integer[] id = new Integer[1];
            QContext.withTemporaryContext(new CapturedContext(instance, new QSystemUserSession(instance)), () -> id[0] = runtime.service().detect());
            return (id[0]);
         };
         results.add(pool.submit(pod));
      }
      start.countDown();
      int recorded = 0;
      for(Future<Integer> result : results)
      {
         if(result.get() != null)
         {
            recorded++;
         }
      }
      pool.shutdown();
      assertThat(recorded).isEqualTo(1);
      assertThat(ReleaseNotesDb.query(ReleaseNotesRelease.TABLE_NAME, new QQueryFilter())).hasSize(1);
      Integer id = ReleaseNotesDb.query(ReleaseNotesRelease.TABLE_NAME, new QQueryFilter()).get(0).getValueInteger("id");
      assertThat(ReleaseNotesDb.query(ReleaseNotesItem.TABLE_NAME, ReleaseNotesDb.eq("releaseId", id))).hasSize(4);
   }



   /*******************************************************************************
    ** Require approval: publishing sends one RELEASE notification and the
    ** release appears on What's new as new until seen.
    *******************************************************************************/
   @Test
   void approvalPublishAndWhatsNew() throws Exception
   {
      QInstance           instance = ReleaseNotesTestSupport.instance("1.2.3", CHANGES);
      ReleaseNotesRuntime runtime  = ReleaseNotesTestSupport.runtime(instance, "2026-10-15T12:00:00Z");
      Integer             id       = runtime.service().detect();

      Map<String, Object> before = runtime.service().whatsNew(user("u1"), 20, 0);
      assertThat((List<?>) before.get("releases")).isEmpty();

      runtime.service().publish(id, "admin-1");
      QRecord release = ReleaseNotesDb.byId(ReleaseNotesRelease.TABLE_NAME, id);
      assertThat(release.getValueString("status")).isEqualTo("PUBLISHED");
      assertThat(release.getValueString("publishedBy")).isEqualTo("admin-1");
      assertThat(release.getValueInstant("notifiedAt")).isNotNull();
      assertThat(release.getValueString("audienceType")).isEqualTo("ALL_USERS");

      assertThat(NOTIFICATIONS).hasSize(1);
      NotificationSignal signal = NOTIFICATIONS.get(0);
      assertThat(signal.getCategory()).isEqualTo(NotificationCategory.RELEASE);
      assertThat(signal.getSeverity()).isEqualTo(NotificationSeverity.INFO);
      assertThat(signal.getAudience()).isEqualTo(NotificationAudience.allUsers());
      assertThat(signal.getTitle()).isEqualTo("What's new in Acme 1.2.3");
      assertThat(signal.getLink().getPath()).isEqualTo("/app/whats-new?release=" + id);
      assertThat(signal.getSourceKey()).isEqualTo("release-notes:acme:1.2.3");

      Map<String, Object> page = runtime.service().whatsNew(user("u1"), 20, 0);
      assertThat(page.get("unreadCount")).isEqualTo(1);
      Map<?, ?> shown = (Map<?, ?>) ((List<?>) page.get("releases")).get(0);
      assertThat(shown.get("isNew")).isEqualTo(true);
      assertThat(shown.toString()).doesNotContain("sourceLog", "commitSha", "componentVersions");
      List<?> items = (List<?>) shown.get("items");
      assertThat(items).hasSize(2);
      assertThat(items.toString()).doesNotContain("#", "Website-Backend", "github");
      assertThat(((Map<?, ?>) items.get(0)).get("category")).isEqualTo("NEW");

      assertThat(runtime.service().summary(user("u1")).get("unreadCount")).isEqualTo(1);
      runtime.service().markSeen(user("u1"));
      runtime.service().markSeen(user("u1"));
      assertThat(runtime.service().summary(user("u1")).get("unreadCount")).isEqualTo(0);
      assertThat(runtime.service().summary(user("u2")).get("unreadCount")).isEqualTo(1);

      assertThatThrownBy(() -> runtime.service().publish(id, "admin-1")).isInstanceOf(QUserFacingException.class).hasMessageContaining("Only drafts");
      assertThatThrownBy(() -> runtime.service().whatsNew(null, 20, 0)).hasMessageContaining("Sign in");
   }



   /*******************************************************************************
    ** Internal references in included text block publishing.
    *******************************************************************************/
   @Test
   void leaksBlockPublishing() throws Exception
   {
      QInstance           instance = ReleaseNotesTestSupport.instance("1.2.3", CHANGES);
      ReleaseNotesRuntime runtime  = ReleaseNotesRuntime.of(instance);
      Integer             id       = runtime.service().detect();
      ReleaseNotesDb.insert(ReleaseNotesItem.TABLE_NAME, new QRecord().withValue("releaseId", id).withValue("category", "FIXED")
         .withValue("text", "Fixed the crash from PR #42 (see https://github.com/acme/app/pull/42).").withValue("included", true).withValue("sourceType", "MANUAL"));
      assertThat(runtime.service().problems(ReleaseNotesDb.byId(ReleaseNotesRelease.TABLE_NAME, id)))
         .anyMatch(p -> p.contains("pull request or issue number")).anyMatch(p -> p.contains("GitHub link"));
      assertThatThrownBy(() -> runtime.service().publish(id, "admin")).isInstanceOf(QUserFacingException.class);
      assertThat(NOTIFICATIONS).isEmpty();
      assertThat(ReleaseNotesDb.byId(ReleaseNotesRelease.TABLE_NAME, id).getValueString("status")).isEqualTo("DRAFT");
   }



   /*******************************************************************************
    ** Auto-publish publishes clean drafts and skips empty ones; permission
    ** audiences are honoured on What's new and in the notification.
    *******************************************************************************/
   @Test
   void autoPublishAndAudience() throws Exception
   {
      QInstance instance = ReleaseNotesTestSupport.instance("3.0.0", CHANGES);
      QRecord   settings = ReleaseNotesDb.first(ReleaseNotesSettings.TABLE_NAME, new QQueryFilter());
      ReleaseNotesDb.update(ReleaseNotesSettings.TABLE_NAME, new QRecord().withValue("id", settings.getValueInteger("id"))
         .withValue("publishMode", "AUTO_PUBLISH").withValue("audienceType", "PERMISSION").withValue("audienceValue", "beta.view"));
      Integer id = ReleaseNotesRuntime.of(instance).service().detect();
      assertThat(ReleaseNotesDb.byId(ReleaseNotesRelease.TABLE_NAME, id).getValueString("status")).isEqualTo("PUBLISHED");
      assertThat(NOTIFICATIONS).hasSize(1);
      assertThat(NOTIFICATIONS.get(0).getAudience()).isEqualTo(NotificationAudience.permission("beta.view"));
      assertThat((List<?>) ReleaseNotesRuntime.of(instance).service().whatsNew(user("u1"), 20, 0).get("releases")).isEmpty();
      assertThat((List<?>) ReleaseNotesRuntime.of(instance).service().whatsNew(user("u2", "beta.view"), 20, 0).get("releases")).hasSize(1);

      QInstance empty = ReleaseNotesTestSupport.instance("3.0.1", List.of(pr(1, "chore: tidy", "")));
      QRecord   s2    = ReleaseNotesDb.first(ReleaseNotesSettings.TABLE_NAME, new QQueryFilter());
      ReleaseNotesDb.update(ReleaseNotesSettings.TABLE_NAME, new QRecord().withValue("id", s2.getValueInteger("id")).withValue("publishMode", "AUTO_PUBLISH"));
      Integer emptyId = ReleaseNotesRuntime.of(empty).service().detect();
      assertThat(ReleaseNotesDb.byId(ReleaseNotesRelease.TABLE_NAME, emptyId).getValueString("status")).isEqualTo("SKIPPED");
      assertThat(NOTIFICATIONS).isEmpty();
   }



   /*******************************************************************************
    ** Redraft keeps hand-written items; skip; drafting off excludes all.
    *******************************************************************************/
   @Test
   void redraftSkipAndDraftingOff() throws Exception
   {
      QInstance           instance = ReleaseNotesTestSupport.instance("1.0.0", CHANGES);
      ReleaseNotesRuntime runtime  = ReleaseNotesRuntime.of(instance);
      Integer             id       = runtime.service().detect();
      ReleaseNotesDb.insert(ReleaseNotesItem.TABLE_NAME, new QRecord().withValue("releaseId", id).withValue("category", "NEW")
         .withValue("text", "Hand-written highlight.").withValue("included", true).withValue("sourceType", "MANUAL"));
      runtime.service().draft(id);
      assertThat(ReleaseNotesDb.query(ReleaseNotesItem.TABLE_NAME, ReleaseNotesDb.eq("releaseId", id))).hasSize(5);

      QRecord settings = ReleaseNotesDb.first(ReleaseNotesSettings.TABLE_NAME, new QQueryFilter());
      ReleaseNotesDb.update(ReleaseNotesSettings.TABLE_NAME, new QRecord().withValue("id", settings.getValueInteger("id")).withValue("draftingEnabled", false).withValue("ingestGithub", false));
      runtime.service().draft(id);
      assertThat(ReleaseNotesDb.query(ReleaseNotesItem.TABLE_NAME, ReleaseNotesDb.eq("releaseId", id))
         .stream().filter(i -> Boolean.TRUE.equals(i.getValueBoolean("included")))).hasSize(1);
      assertThat(ReleaseNotesDb.byId(ReleaseNotesRelease.TABLE_NAME, id).getValueString("draftedBy")).isEqualTo("OFF");

      runtime.service().skip(id, "admin");
      assertThat(ReleaseNotesDb.byId(ReleaseNotesRelease.TABLE_NAME, id).getValueString("status")).isEqualTo("SKIPPED");
      assertThatThrownBy(() -> runtime.service().skip(id, "admin")).hasMessageContaining("Only drafts");
      assertThatThrownBy(() -> runtime.service().draft(id)).hasMessageContaining("Only draft");
      assertThatThrownBy(() -> runtime.service().draft(99999)).hasMessageContaining("not found");
   }
}
