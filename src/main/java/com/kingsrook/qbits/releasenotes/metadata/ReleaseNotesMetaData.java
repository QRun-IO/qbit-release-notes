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


package com.kingsrook.qbits.releasenotes.metadata;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qbits.releasenotes.ReleaseNotesQBitConfig;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesItem;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesPossibleValues;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesRelease;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesSettings;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesUserState;
import com.kingsrook.qqq.backend.core.actions.ai.AiExposure;
import com.kingsrook.qqq.backend.core.actions.ai.AiTableMetaData;
import com.kingsrook.qqq.backend.core.actions.dashboard.widgets.ChildRecordListRenderer;
import com.kingsrook.qqq.backend.core.actions.permissions.UseOtherPermissionNameCustomPermissionChecker;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.instances.QInstanceEnricher;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.audits.AuditLevel;
import com.kingsrook.qqq.backend.core.model.metadata.audits.QAuditRules;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QAppSection;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.PermissionLevel;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Capability;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QFieldSection;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Tier;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSTableBackendDetails;


/*******************************************************************************
 ** Tables, possible values, the release-to-items child list and the admin app.
 *******************************************************************************/
public final class ReleaseNotesMetaData
{
   public static final String       CHANGELOG = "db/liquibase/qbit-release-notes/release_notes.changelog.sql";
   public static final List<String> TABLES    = List.of(ReleaseNotesRelease.TABLE_NAME, ReleaseNotesItem.TABLE_NAME, ReleaseNotesSettings.TABLE_NAME, ReleaseNotesUserState.TABLE_NAME);
   public static final String       ITEMS_JOIN = "release_notes_release_release_notes_item";



   /*******************************************************************************
    ** Utility class.
    *******************************************************************************/
   private ReleaseNotesMetaData()
   {
   }



   /*******************************************************************************
    ** Register everything.
    *******************************************************************************/
   public static void addTo(QInstance instance, ReleaseNotesQBitConfig config) throws QException
   {
      instance.addPossibleValueSource(source(ReleaseNotesPossibleValues.CATEGORY, ReleaseNotesPossibleValues.Category.values()));
      instance.addPossibleValueSource(source(ReleaseNotesPossibleValues.STATUS, ReleaseNotesPossibleValues.Status.values()));
      instance.addPossibleValueSource(source(ReleaseNotesPossibleValues.PUBLISH_MODE, ReleaseNotesPossibleValues.PublishMode.values()));
      instance.addPossibleValueSource(source(ReleaseNotesPossibleValues.AUDIENCE_TYPE, ReleaseNotesPossibleValues.AudienceType.values()));
      instance.addPossibleValueSource(source(ReleaseNotesPossibleValues.SOURCE_TYPE, ReleaseNotesPossibleValues.SourceType.values()));

      QTableMetaData release = base(config, ReleaseNotesRelease.TABLE_NAME, "Releases", "new_releases", ReleaseNotesRelease.class);
      release.withRecordLabelFormat("%s %s").withRecordLabelFields("appName", "version");
      release.withUniqueKey(new UniqueKey("appName", "version"));
      release.getField("status").setPossibleValueSourceName(ReleaseNotesPossibleValues.STATUS);
      release.getField("audienceType").setPossibleValueSourceName(ReleaseNotesPossibleValues.AUDIENCE_TYPE);
      release.getField("summary").setType(QFieldType.TEXT);
      release.getField("componentVersions").setType(QFieldType.TEXT);
      release.getField("sourceLog").setType(QFieldType.TEXT);
      for(String readOnly : List.of("appName", "version", "commitSha", "previousVersion", "detectedAt", "status", "publishedAt", "publishedBy", "notifiedAt", "draftedBy", "componentVersions", "sourceLog"))
      {
         release.getField(readOnly).setIsEditable(false);
      }
      release.withoutCapabilities(Capability.TABLE_INSERT, Capability.TABLE_DELETE);
      release.withSection(new QFieldSection("identity", "Release", new QIcon("new_releases"), Tier.T1, List.of("id", "appName", "version", "status", "title")));
      release.withSection(new QFieldSection("note", "Customer note", new QIcon("campaign"), Tier.T2, List.of("summary", "audienceType", "audienceValue", "publishedAt", "publishedBy", "notifiedAt")));
      release.withSection(new QFieldSection("internal", "Internal", new QIcon("lock"), Tier.T2, List.of("commitSha", "previousVersion", "detectedAt", "draftedBy", "sourceLog", "componentVersions")));
      release.withSection(new QFieldSection("system", new QIcon("history"), Tier.T3, List.of("createDate", "modifyDate")));
      instance.addTable(release);

      QTableMetaData item = base(config, ReleaseNotesItem.TABLE_NAME, "Release Items", "list", ReleaseNotesItem.class);
      item.withRecordLabelFormat("%s").withRecordLabelFields("text");
      item.getField("releaseId").setPossibleValueSourceName(ReleaseNotesRelease.TABLE_NAME);
      item.getField("category").setPossibleValueSourceName(ReleaseNotesPossibleValues.CATEGORY);
      item.getField("category").setDefaultValue(ReleaseNotesPossibleValues.Category.IMPROVED.name());
      item.getField("included").setDefaultValue(true);
      item.getField("sortOrder").setDefaultValue(100);
      item.getField("sourceType").setPossibleValueSourceName(ReleaseNotesPossibleValues.SOURCE_TYPE);
      item.getField("sourceType").setDefaultValue(ReleaseNotesPossibleValues.SourceType.MANUAL.name());
      item.getField("sourceType").setIsEditable(false);
      item.getField("sourceRef").setIsEditable(false);
      item.getField("sourceTitle").setIsEditable(false);
      item.getField("text").setType(QFieldType.TEXT);
      item.getField("text").setBackendName("item_text");
      item.withSection(new QFieldSection("identity", "Item", new QIcon("list"), Tier.T1, List.of("id", "releaseId", "category", "text", "included")));
      item.withSection(new QFieldSection("details", "Details", new QIcon("link"), Tier.T2, List.of("sortOrder", "linkUrl", "screenshotUrl")));
      item.withSection(new QFieldSection("internal", "Internal", new QIcon("lock"), Tier.T2, List.of("sourceType", "sourceRef", "sourceTitle")));
      item.withSection(new QFieldSection("system", new QIcon("history"), Tier.T3, List.of("createDate", "modifyDate")));
      instance.addTable(item);

      QTableMetaData settings = base(config, ReleaseNotesSettings.TABLE_NAME, "Settings", "settings", ReleaseNotesSettings.class);
      settings.withRecordLabelFormat("%s").withRecordLabelFields("publishMode");
      settings.getField("publishMode").setPossibleValueSourceName(ReleaseNotesPossibleValues.PUBLISH_MODE);
      settings.getField("audienceType").setPossibleValueSourceName(ReleaseNotesPossibleValues.AUDIENCE_TYPE);
      settings.getField("styleGuide").setType(QFieldType.TEXT);
      settings.withoutCapabilities(Capability.TABLE_INSERT, Capability.TABLE_DELETE);
      settings.withSection(new QFieldSection("identity", "Publishing", new QIcon("settings"), Tier.T1, List.of("id", "publishMode", "audienceType", "audienceValue", "notifyOnPublish")));
      settings.withSection(new QFieldSection("drafting", "Drafting", new QIcon("edit_note"), Tier.T2, List.of("draftingEnabled", "ingestGithub", "includePlatformUpdates", "titleTemplate", "styleGuide")));
      settings.withSection(new QFieldSection("system", new QIcon("history"), Tier.T3, List.of("createDate", "modifyDate")));
      instance.addTable(settings);

      QTableMetaData state = base(config, ReleaseNotesUserState.TABLE_NAME, "Release Read State", "visibility", ReleaseNotesUserState.class);
      state.withRecordLabelFormat("%s").withRecordLabelFields("userId");
      state.withUniqueKey(new UniqueKey("userId"));
      state.withoutCapabilities(Capability.TABLE_INSERT, Capability.TABLE_UPDATE);
      state.withSection(new QFieldSection("identity", "Read state", new QIcon("visibility"), Tier.T1, List.of("id", "userId", "lastSeenAt")));
      state.withSection(new QFieldSection("system", new QIcon("history"), Tier.T3, List.of("createDate", "modifyDate")));
      instance.addTable(state);

      instance.addPossibleValueSource(QPossibleValueSource.newForTable(ReleaseNotesRelease.TABLE_NAME));

      QJoinMetaData join = new QJoinMetaData().withName(ITEMS_JOIN).withLeftTable(ReleaseNotesRelease.TABLE_NAME).withRightTable(ReleaseNotesItem.TABLE_NAME)
         .withType(JoinType.ONE_TO_MANY).withJoinOn(new JoinOn("id", "releaseId"));
      instance.addJoin(join);
      instance.addWidget(ChildRecordListRenderer.widgetMetaDataBuilder(join).withLabel("Items").withCanAddChildRecord(true)
         .withMaxRows(100).withOrderBys(List.of(new QFilterOrderBy("sortOrder", true), new QFilterOrderBy("id", true))).getWidgetMetaData()
         .withPermissionRules(rules(config)));
      release.getSections().add(1, new QFieldSection("items", "Items", new QIcon("list"), Tier.T2, null).withWidgetName(ITEMS_JOIN));

      QAppMetaData app = new QAppMetaData().withName(config.getAppName()).withLabel("Release Notes").withIcon(new QIcon("new_releases"))
         .withPermissionRules(rules(config))
         .withSection(new QAppSection().withName("releaseNotes").withLabel("Release Notes").withIcon(new QIcon("new_releases"))
            .withTables(new ArrayList<>(TABLES)));
      instance.addApp(app);
      if(config.getParentAppName() != null)
      {
         instance.getApp(config.getParentAppName()).withChild(app);
      }
   }



   /*******************************************************************************
    ** Every table and the app need the manage permission.
    *******************************************************************************/
   public static QPermissionRules rules(ReleaseNotesQBitConfig config)
   {
      return (new QPermissionRules().withLevel(PermissionLevel.HAS_ACCESS_PERMISSION).withPermissionBaseName(config.getManagePermissionName())
         .withCustomPermissionChecker(UseOtherPermissionNameCustomPermissionChecker.build(config.getManagePermissionName())));
   }



   /*******************************************************************************
    ** Common storage, permissions and audit.
    *******************************************************************************/
   private static QTableMetaData base(ReleaseNotesQBitConfig config, String name, String label, String icon, Class<? extends QRecordEntity> entity) throws QException
   {
      QTableMetaData table = new QTableMetaData().withName(name).withLabel(label).withIcon(new QIcon(icon))
         .withBackendName(config.getBackendName()).withBackendDetails(new RDBMSTableBackendDetails().withTableName(name))
         .withPrimaryKeyField("id").withFieldsFromEntity(entity)
         .withPermissionRules(rules(config))
         .withAuditRules(new QAuditRules().withAuditLevel(AuditLevel.FIELD))
         .withSupplementalMetaData(new AiTableMetaData().withExposure(AiExposure.NONE));
      QInstanceEnricher.setInferredFieldBackendNames(table);
      return (table);
   }



   /*******************************************************************************
    ** Enum-backed stored values.
    *******************************************************************************/
   private static QPossibleValueSource source(String name, PossibleValueEnum<String>[] values)
   {
      List<QPossibleValue<?>> choices = new ArrayList<>();
      for(PossibleValueEnum<String> value : values)
      {
         choices.add(new QPossibleValue<>(value.getPossibleValueId(), value.getPossibleValueLabel()));
      }
      return (new QPossibleValueSource().withName(name).withType(QPossibleValueSourceType.ENUM).withEnumValues(choices));
   }
}
