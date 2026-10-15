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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import com.kingsrook.qbits.releasenotes.drafting.ReleaseNotesDrafter;
import com.kingsrook.qbits.releasenotes.drafting.ReleaseNotesDrafter.DraftItem;
import com.kingsrook.qbits.releasenotes.drafting.ReleaseNotesSanitizer;
import com.kingsrook.qbits.releasenotes.drafting.RuleBasedDrafter;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesItem;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesPossibleValues.AudienceType;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesPossibleValues.Category;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesPossibleValues.PublishMode;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesPossibleValues.SourceType;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesPossibleValues.Status;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesRelease;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesSettings;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesUserState;
import com.kingsrook.qbits.releasenotes.sources.ReleaseChange;
import com.kingsrook.qbits.releasenotes.sources.ReleaseContext;
import com.kingsrook.qbits.releasenotes.sources.ReleaseSource;
import com.kingsrook.qqq.backend.core.alerts.AlertLink;
import com.kingsrook.qqq.backend.core.alerts.NotificationAudience;
import com.kingsrook.qqq.backend.core.alerts.NotificationCategory;
import com.kingsrook.qqq.backend.core.alerts.NotificationSeverity;
import com.kingsrook.qqq.backend.core.alerts.NotificationSignal;
import com.kingsrook.qqq.backend.core.alerts.QAlerts;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Release lifecycle: detect a new version once, gather and draft, publish
 ** (with one notification), skip, and the per-user What's new view. Writes run
 ** as the system user; callers check permissions first.
 *******************************************************************************/
public class ReleaseNotesService
{
   private static final QLogger LOG = QLogger.getLogger(ReleaseNotesService.class);

   public static final String SOURCE_NAME = "release-notes";

   private final ReleaseNotesRuntime    runtime;
   private final ReleaseNotesQBitConfig config;



   /*******************************************************************************
    ** Constructor.
    *******************************************************************************/
   public ReleaseNotesService(ReleaseNotesRuntime runtime)
   {
      this.runtime = runtime;
      this.config = runtime.getConfig();
   }



   /*******************************************************************************
    ** Record the running version once (unique app + version: concurrent pods
    ** race on the insert and only the winner drafts). Returns the new
    ** release id, or null when there is nothing new.
    *******************************************************************************/
   public Integer detect() throws Exception
   {
      if(!config.isReleaseVersion())
      {
         return (null);
      }
      String version = config.getAppVersion().trim();
      Integer id = runtime.asSystem(() ->
      {
         if(findRelease(version) != null)
         {
            return (null);
         }
         QRecord previous = ReleaseNotesDb.first(ReleaseNotesRelease.TABLE_NAME, appFilter()
            .withOrderBy(new QFilterOrderBy("detectedAt", false)).withOrderBy(new QFilterOrderBy("id", false)));
         QRecord record = new QRecord()
            .withValue("appName", config.getApplicationName())
            .withValue("version", version)
            .withValue("commitSha", truncate(config.getAppCommit(), 40))
            .withValue("previousVersion", previous == null ? null : previous.getValueString("version"))
            .withValue("detectedAt", runtime.now())
            .withValue("status", Status.DRAFT.name())
            .withValue("componentVersions", JsonUtils.toJson(runtime.componentVersions()));
         try
         {
            return (ReleaseNotesDb.insert(ReleaseNotesRelease.TABLE_NAME, record).getValueInteger("id"));
         }
         catch(Exception e)
         {
            if(findRelease(version) != null)
            {
               LOG.info("Another node recorded this release first", logPair("version", version));
               return (null);
            }
            throw (e);
         }
      });
      if(id == null)
      {
         return (null);
      }

      draft(id);
      ReleaseNotesSettings settings = settings();
      if(PublishMode.AUTO_PUBLISH.name().equals(settings.getPublishMode()))
      {
         boolean      empty    = runtime.asSystem(() -> includedItems(id).isEmpty());
         List<String> problems = runtime.asSystem(() -> problems(ReleaseNotesDb.byId(ReleaseNotesRelease.TABLE_NAME, id)));
         if(empty)
         {
            skip(id, "system");
         }
         else if(problems.isEmpty())
         {
            publish(id, "system");
         }
      }
      return (id);
   }



   /*******************************************************************************
    ** Gather sources and (re)draft a release's items, title and summary. Items
    ** written by hand are kept.
    *******************************************************************************/
   public void draft(Integer releaseId) throws Exception
   {
      ReleaseNotesSettings settings = settings();
      runtime.asSystem(() ->
      {
         QRecord release = ReleaseNotesDb.byId(ReleaseNotesRelease.TABLE_NAME, releaseId);
         if(release == null)
         {
            throw (new QUserFacingException("The release was not found."));
         }
         if(!Status.DRAFT.name().equals(release.getValueString("status")))
         {
            throw (new QUserFacingException("Only draft releases can be redrafted."));
         }

         QRecord previous = release.getValueString("previousVersion") == null ? null : findRelease(release.getValueString("previousVersion"));
         ReleaseContext context = ReleaseContext.of(config.getApplicationName(), release.getValueString("version"), release.getValueString("previousVersion"),
            previous == null ? null : previous.getValueInstant("detectedAt"), release.getValueInstant("detectedAt"),
            readVersions(release.getValueString("componentVersions")), previous == null ? null : readVersions(previous.getValueString("componentVersions")));

         List<ReleaseChange> changes = new ArrayList<>();
         for(ReleaseSource source : config.getSources())
         {
            if(source.isGitHub() && !Boolean.TRUE.equals(settings.getIngestGithub()))
            {
               context.warnings().add(source.getName() + ": skipped (GitHub ingestion is off)");
               continue;
            }
            try
            {
               List<ReleaseChange> found = source.collect(context);
               changes.addAll(found);
               context.warnings().add(source.getName() + ": " + found.size() + " change(s)");
            }
            catch(Exception e)
            {
               LOG.warn("Release source failed", e, logPair("source", source.getName()));
               context.warnings().add(source.getName() + ": failed (" + e.getClass().getSimpleName() + ")");
            }
         }

         boolean drafting = !Boolean.FALSE.equals(settings.getDraftingEnabled());
         ReleaseNotesDrafter drafter = runtime.getDrafter();
         List<DraftItem> items = drafter.draft(changes, new ReleaseNotesDrafter.DraftOptions(Boolean.TRUE.equals(settings.getIncludePlatformUpdates())));

         QQueryFilter generated = ReleaseNotesDb.eq("releaseId", releaseId)
            .withCriteria(new QFilterCriteria("sourceType", QCriteriaOperator.NOT_EQUALS, SourceType.MANUAL.name()));
         ReleaseNotesDb.delete(ReleaseNotesItem.TABLE_NAME, generated);
         List<QRecord> rows = new ArrayList<>();
         int order = 10;
         for(DraftItem item : items)
         {
            rows.add(new QRecord().withValue("releaseId", releaseId).withValue("category", item.category()).withValue("text", item.text())
               .withValue("included", drafting && item.included()).withValue("sortOrder", order)
               .withValue("sourceType", item.sourceType()).withValue("sourceRef", truncate(item.sourceRef(), 200)).withValue("sourceTitle", truncate(item.sourceTitle(), 500)));
            order += 10;
         }
         ReleaseNotesDb.insertAll(ReleaseNotesItem.TABLE_NAME, rows);

         List<String> categories = new ArrayList<>();
         for(QRecord item : includedItems(releaseId))
         {
            categories.add(item.getValueString("category"));
         }
         QRecord patch = new QRecord().withValue("id", releaseId)
            .withValue("title", title(settings, release.getValueString("version")))
            .withValue("summary", drafting ? RuleBasedDrafter.summarize(categories) : release.getValueString("summary"))
            .withValue("draftedBy", drafting ? drafter.getName() : "OFF")
            .withValue("sourceLog", truncate(String.join("\n", context.warnings()), 8000));
         ReleaseNotesDb.update(ReleaseNotesRelease.TABLE_NAME, patch);
         return (null);
      });
   }



   /*******************************************************************************
    ** What blocks publishing a release (empty when it can be published).
    *******************************************************************************/
   public List<String> problems(QRecord release) throws Exception
   {
      List<String> problems = new ArrayList<>();
      if(release == null)
      {
         problems.add("The release was not found.");
         return (problems);
      }
      if(!Status.DRAFT.name().equals(release.getValueString("status")))
      {
         problems.add("Only drafts can be published.");
      }
      if(release.getValueString("title") == null || release.getValueString("title").isBlank())
      {
         problems.add("Add a title.");
      }
      List<QRecord> items = includedItems(release.getValueInteger("id"));
      if(items.isEmpty() && (release.getValueString("summary") == null || release.getValueString("summary").isBlank()))
      {
         problems.add("Include at least one item or write a summary.");
      }
      ReleaseNotesSanitizer.problems(release.getValueString("title")).forEach(p -> problems.add("Title " + p + "."));
      ReleaseNotesSanitizer.problems(release.getValueString("summary")).forEach(p -> problems.add("Summary " + p + "."));
      for(QRecord item : items)
      {
         String label = "Item " + item.getValueInteger("id") + " ";
         ReleaseNotesSanitizer.problems(item.getValueString("text")).forEach(p -> problems.add(label + p + "."));
         ReleaseNotesSanitizer.linkProblems(item.getValueString("linkUrl")).forEach(p -> problems.add(label + p + "."));
         ReleaseNotesSanitizer.linkProblems(item.getValueString("screenshotUrl")).forEach(p -> problems.add(label + "screenshot " + p + "."));
      }
      if(AudienceType.PERMISSION.name().equals(audienceType(release)) && audienceValue(release) == null)
      {
         problems.add("Name the permission for the audience.");
      }
      return (problems);
   }



   /*******************************************************************************
    ** Publish a draft: mark it published and send one notification.
    *******************************************************************************/
   public void publish(Integer releaseId, String actor) throws Exception
   {
      ReleaseNotesSettings settings = settings();
      runtime.asSystem(() ->
      {
         QRecord      release  = ReleaseNotesDb.byId(ReleaseNotesRelease.TABLE_NAME, releaseId);
         List<String> problems = problems(release);
         if(!problems.isEmpty())
         {
            throw (new QUserFacingException(String.join(" ", problems)));
         }
         Instant now = runtime.now();
         ReleaseNotesDb.update(ReleaseNotesRelease.TABLE_NAME, new QRecord().withValue("id", releaseId).withValue("status", Status.PUBLISHED.name())
            .withValue("publishedAt", now).withValue("publishedBy", truncate(actor, 100))
            .withValue("audienceType", audienceType(release)).withValue("audienceValue", audienceValue(release)));
         if(!Boolean.FALSE.equals(settings.getNotifyOnPublish()))
         {
            QAlerts.notify(signal(release));
            ReleaseNotesDb.update(ReleaseNotesRelease.TABLE_NAME, new QRecord().withValue("id", releaseId).withValue("notifiedAt", now));
         }
         LOG.info("Published release notes", logPair("version", release.getValueString("version")), logPair("releaseId", releaseId));
         return (null);
      });
   }



   /*******************************************************************************
    ** Skip a draft: it is never announced or listed.
    *******************************************************************************/
   public void skip(Integer releaseId, String actor) throws Exception
   {
      runtime.asSystem(() ->
      {
         QRecord release = ReleaseNotesDb.byId(ReleaseNotesRelease.TABLE_NAME, releaseId);
         if(release == null || !Status.DRAFT.name().equals(release.getValueString("status")))
         {
            throw (new QUserFacingException("Only drafts can be skipped."));
         }
         ReleaseNotesDb.update(ReleaseNotesRelease.TABLE_NAME, new QRecord().withValue("id", releaseId).withValue("status", Status.SKIPPED.name())
            .withValue("publishedBy", truncate(actor, 100)));
         return (null);
      });
   }



   /*******************************************************************************
    ** The notification for a published release (category RELEASE; the source
    ** key makes a resend update the same notification).
    *******************************************************************************/
   NotificationSignal signal(QRecord release) throws Exception
   {
      Integer id = release.getValueInteger("id");
      return (new NotificationSignal(release.getValueString("title"))
         .withBody(release.getValueString("summary"))
         .withCategory(NotificationCategory.RELEASE)
         .withSeverity(NotificationSeverity.INFO)
         .withSourceName(SOURCE_NAME)
         .withSourceKey(SOURCE_NAME + ":" + config.getApplicationName() + ":" + release.getValueString("version"))
         .withSourceTableName(ReleaseNotesRelease.TABLE_NAME)
         .withSourceRecordId(String.valueOf(id))
         .withLink(AlertLink.toPath(config.getWhatsNewPath() + "?release=" + id, "See what's new"))
         .withAudience(AudienceType.PERMISSION.name().equals(audienceType(release))
            ? NotificationAudience.permission(audienceValue(release)) : NotificationAudience.allUsers()));
   }



   /*******************************************************************************
    ** Published releases the session may see, newest first, with their
    ** customer fields only, plus the unread count.
    *******************************************************************************/
   public Map<String, Object> whatsNew(QSession session, int limit, int offset) throws Exception
   {
      String userId = userId(session);
      return (runtime.asSystem(() ->
      {
         Instant         lastSeen = lastSeen(userId);
         List<QRecord>   visible  = visibleReleases(session);
         List<Object>    rows     = new ArrayList<>();
         int             unread   = 0;
         for(QRecord release : visible)
         {
            if(isNew(release, lastSeen))
            {
               unread++;
            }
         }
         for(QRecord release : visible.subList(Math.min(offset, visible.size()), Math.min(offset + limit, visible.size())))
         {
            rows.add(customerView(release, isNew(release, lastSeen)));
         }
         Map<String, Object> result = new LinkedHashMap<>();
         result.put("releases", rows);
         result.put("total", visible.size());
         result.put("unreadCount", unread);
         result.put("lastSeenAt", lastSeen == null ? null : lastSeen.toString());
         return (result);
      }));
   }



   /*******************************************************************************
    ** The unread count and latest release, for the user-menu badge.
    *******************************************************************************/
   public Map<String, Object> summary(QSession session) throws Exception
   {
      String userId = userId(session);
      return (runtime.asSystem(() ->
      {
         Instant       lastSeen = lastSeen(userId);
         List<QRecord> visible  = visibleReleases(session);
         int           unread   = (int) visible.stream().filter(r -> isNew(r, lastSeen)).count();
         Map<String, Object> result = new LinkedHashMap<>();
         result.put("unreadCount", unread);
         if(!visible.isEmpty())
         {
            QRecord latest = visible.get(0);
            Map<String, Object> view = new LinkedHashMap<>();
            view.put("id", latest.getValueInteger("id"));
            view.put("version", latest.getValueString("version"));
            view.put("title", latest.getValueString("title"));
            view.put("publishedAt", String.valueOf(latest.getValueInstant("publishedAt")));
            result.put("latest", view);
         }
         else
         {
            result.put("latest", null);
         }
         return (result);
      }));
   }



   /*******************************************************************************
    ** Mark everything seen for the session's user.
    *******************************************************************************/
   public Map<String, Object> markSeen(QSession session) throws Exception
   {
      String userId = userId(session);
      runtime.asSystem(() ->
      {
         Instant now   = runtime.now();
         QRecord state = ReleaseNotesDb.first(ReleaseNotesUserState.TABLE_NAME, ReleaseNotesDb.eq("userId", userId));
         if(state == null)
         {
            try
            {
               ReleaseNotesDb.insert(ReleaseNotesUserState.TABLE_NAME, new QRecord().withValue("userId", userId).withValue("lastSeenAt", now));
               return (null);
            }
            catch(Exception e)
            {
               state = ReleaseNotesDb.first(ReleaseNotesUserState.TABLE_NAME, ReleaseNotesDb.eq("userId", userId));
               if(state == null)
               {
                  throw (e);
               }
            }
         }
         ReleaseNotesDb.update(ReleaseNotesUserState.TABLE_NAME, new QRecord().withValue("id", state.getValueInteger("id")).withValue("lastSeenAt", now));
         return (null);
      });
      Map<String, Object> result = new LinkedHashMap<>();
      result.put("unreadCount", 0);
      return (result);
   }



   /*******************************************************************************
    ** The settings row (defaults when it is missing).
    *******************************************************************************/
   public ReleaseNotesSettings settings() throws Exception
   {
      QRecord row = runtime.asSystem(() -> ReleaseNotesDb.first(ReleaseNotesSettings.TABLE_NAME, new QQueryFilter().withOrderBy(new QFilterOrderBy("id", true))));
      if(row != null)
      {
         return (new ReleaseNotesSettings(row));
      }
      return (new ReleaseNotesSettings().withPublishMode(PublishMode.REQUIRE_APPROVAL.name()).withAudienceType(AudienceType.ALL_USERS.name())
         .withNotifyOnPublish(true).withDraftingEnabled(true).withIncludePlatformUpdates(true).withIngestGithub(true));
   }



   /*******************************************************************************
    ** Published, visible releases for the session, newest first.
    *******************************************************************************/
   private List<QRecord> visibleReleases(QSession session) throws QException
   {
      List<QRecord> published = ReleaseNotesDb.query(ReleaseNotesRelease.TABLE_NAME, appFilter()
         .withCriteria(new QFilterCriteria("status", QCriteriaOperator.EQUALS, Status.PUBLISHED.name()))
         .withOrderBy(new QFilterOrderBy("publishedAt", false)).withOrderBy(new QFilterOrderBy("id", false)));
      List<QRecord> visible = new ArrayList<>();
      for(QRecord release : published)
      {
         String type  = release.getValueString("audienceType");
         String value = release.getValueString("audienceValue");
         if(!AudienceType.PERMISSION.name().equals(type) || (value != null && session != null && session.hasPermission(value)))
         {
            visible.add(release);
         }
      }
      return (visible);
   }



   /*******************************************************************************
    ** Customer fields of a release and its included items.
    *******************************************************************************/
   private Map<String, Object> customerView(QRecord release, boolean isNew) throws QException
   {
      Map<String, Object> view = new LinkedHashMap<>();
      view.put("id", release.getValueInteger("id"));
      view.put("version", release.getValueString("version"));
      view.put("title", release.getValueString("title"));
      view.put("summary", release.getValueString("summary"));
      view.put("publishedAt", String.valueOf(release.getValueInstant("publishedAt")));
      view.put("isNew", isNew);
      List<Object> items = new ArrayList<>();
      for(QRecord item : includedItems(release.getValueInteger("id")))
      {
         Map<String, Object> row = new LinkedHashMap<>();
         Category category = Category.of(item.getValueString("category"));
         row.put("category", category.name());
         row.put("categoryLabel", category.getPossibleValueLabel());
         row.put("text", item.getValueString("text"));
         row.put("linkUrl", item.getValueString("linkUrl"));
         row.put("screenshotUrl", item.getValueString("screenshotUrl"));
         items.add(row);
      }
      view.put("items", items);
      return (view);
   }



   /*******************************************************************************
    ** Included items in display order (category, then sort order).
    *******************************************************************************/
   private List<QRecord> includedItems(Integer releaseId) throws QException
   {
      List<QRecord> items = new ArrayList<>(ReleaseNotesDb.query(ReleaseNotesItem.TABLE_NAME, ReleaseNotesDb.eq("releaseId", releaseId)
         .withCriteria(new QFilterCriteria("included", QCriteriaOperator.EQUALS, true))
         .withOrderBy(new QFilterOrderBy("sortOrder", true)).withOrderBy(new QFilterOrderBy("id", true))));
      items.sort((a, b) -> Integer.compare(Category.of(a.getValueString("category")).ordinal(), Category.of(b.getValueString("category")).ordinal()));
      return (items);
   }



   /*******************************************************************************
    ** Whether a release is newer than what the user last saw.
    *******************************************************************************/
   private static boolean isNew(QRecord release, Instant lastSeen)
   {
      Instant publishedAt = release.getValueInstant("publishedAt");
      return (publishedAt != null && (lastSeen == null || publishedAt.isAfter(lastSeen)));
   }



   /*******************************************************************************
    ** When the user last looked, or null.
    *******************************************************************************/
   private static Instant lastSeen(String userId) throws QException
   {
      QRecord state = ReleaseNotesDb.first(ReleaseNotesUserState.TABLE_NAME, ReleaseNotesDb.eq("userId", userId));
      return (state == null ? null : state.getValueInstant("lastSeenAt"));
   }



   /*******************************************************************************
    ** The session's user id; What's new needs a signed-in user.
    *******************************************************************************/
   private static String userId(QSession session) throws QUserFacingException
   {
      String userId = session == null || session.getUser() == null ? null : session.getUser().getIdReference();
      if(userId == null || userId.isBlank())
      {
         throw (new QUserFacingException("Sign in to see what's new."));
      }
      return (userId);
   }



   /*******************************************************************************
    ** The release's audience type, else the settings default.
    *******************************************************************************/
   private String audienceType(QRecord release) throws Exception
   {
      String type = release.getValueString("audienceType");
      return (type != null ? type : settings().getAudienceType() == null ? AudienceType.ALL_USERS.name() : settings().getAudienceType());
   }



   /*******************************************************************************
    ** The release's audience value, else the settings default.
    *******************************************************************************/
   private String audienceValue(QRecord release) throws Exception
   {
      String value = release.getValueString("audienceValue");
      if((value == null || value.isBlank()) && release.getValueString("audienceType") == null)
      {
         value = settings().getAudienceValue();
      }
      return (value == null || value.isBlank() ? null : value.trim());
   }



   /*******************************************************************************
    ** The release row for a version of this application, or null.
    *******************************************************************************/
   private QRecord findRelease(String version) throws QException
   {
      return (ReleaseNotesDb.first(ReleaseNotesRelease.TABLE_NAME, appFilter().withCriteria(new QFilterCriteria("version", QCriteriaOperator.EQUALS, version))));
   }



   /*******************************************************************************
    ** Rows of this application.
    *******************************************************************************/
   private QQueryFilter appFilter()
   {
      return (ReleaseNotesDb.eq("appName", config.getApplicationName()));
   }



   /*******************************************************************************
    ** The title from the settings template.
    *******************************************************************************/
   private String title(ReleaseNotesSettings settings, String version)
   {
      String template = settings.getTitleTemplate() == null || settings.getTitleTemplate().isBlank() ? "What's new in {app} {version}" : settings.getTitleTemplate();
      return (truncate(template.replace("{app}", config.getEffectiveApplicationLabel()).replace("{version}", version).replaceAll("\\s+", " ").trim(), 200));
   }



   /*******************************************************************************
    ** Stored component versions, or null.
    *******************************************************************************/
   private static Map<String, String> readVersions(String json)
   {
      try
      {
         return (json == null || json.isBlank() ? null : JsonUtils.toObject(json, new TypeReference<Map<String, String>>()
         {
         }));
      }
      catch(Exception e)
      {
         return (null);
      }
   }



   /*******************************************************************************
    ** At most max characters.
    *******************************************************************************/
   private static String truncate(String text, int max)
   {
      return (text == null || text.length() <= max ? text : text.substring(0, max));
   }
}
