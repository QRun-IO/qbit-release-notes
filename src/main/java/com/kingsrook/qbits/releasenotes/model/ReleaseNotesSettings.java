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


package com.kingsrook.qbits.releasenotes.model;


import java.time.Instant;
import com.kingsrook.qqq.backend.core.model.data.QField;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.data.QRecordEntity;


/*******************************************************************************
 ** Release notes settings: how releases are drafted, approved and announced.
 *******************************************************************************/
public class ReleaseNotesSettings extends QRecordEntity
{
   public static final String TABLE_NAME = "release_notes_settings";

   @QField(isPrimaryKey = true, isEditable = false)
   private Integer id;

   @QField(isRequired = true, maxLength = 30)
   private String publishMode;

   @QField(isRequired = true, maxLength = 30)
   private String audienceType;

   @QField(maxLength = 200)
   private String audienceValue;

   @QField
   private Boolean notifyOnPublish;

   @QField
   private Boolean draftingEnabled;

   @QField
   private Boolean includePlatformUpdates;

   @QField
   private Boolean ingestGithub;

   @QField(maxLength = 200)
   private String titleTemplate;

   @QField(maxLength = 4000)
   private String styleGuide;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor.
    *******************************************************************************/
   public ReleaseNotesSettings()
   {
   }



   /*******************************************************************************
    ** Constructor from a record.
    *******************************************************************************/
   public ReleaseNotesSettings(QRecord record)
   {
      populateFromQRecord(record);
   }



   /*******************************************************************************
    ** Getter for id.
    *******************************************************************************/
   public Integer getId()
   {
      return (this.id);
   }



   /*******************************************************************************
    ** Setter for id.
    *******************************************************************************/
   public void setId(Integer id)
   {
      this.id = id;
   }



   /*******************************************************************************
    ** Fluent setter for id.
    *******************************************************************************/
   public ReleaseNotesSettings withId(Integer id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for publishMode.
    *******************************************************************************/
   public String getPublishMode()
   {
      return (this.publishMode);
   }



   /*******************************************************************************
    ** Setter for publishMode.
    *******************************************************************************/
   public void setPublishMode(String publishMode)
   {
      this.publishMode = publishMode;
   }



   /*******************************************************************************
    ** Fluent setter for publishMode.
    *******************************************************************************/
   public ReleaseNotesSettings withPublishMode(String publishMode)
   {
      this.publishMode = publishMode;
      return (this);
   }



   /*******************************************************************************
    ** Getter for audienceType.
    *******************************************************************************/
   public String getAudienceType()
   {
      return (this.audienceType);
   }



   /*******************************************************************************
    ** Setter for audienceType.
    *******************************************************************************/
   public void setAudienceType(String audienceType)
   {
      this.audienceType = audienceType;
   }



   /*******************************************************************************
    ** Fluent setter for audienceType.
    *******************************************************************************/
   public ReleaseNotesSettings withAudienceType(String audienceType)
   {
      this.audienceType = audienceType;
      return (this);
   }



   /*******************************************************************************
    ** Getter for audienceValue.
    *******************************************************************************/
   public String getAudienceValue()
   {
      return (this.audienceValue);
   }



   /*******************************************************************************
    ** Setter for audienceValue.
    *******************************************************************************/
   public void setAudienceValue(String audienceValue)
   {
      this.audienceValue = audienceValue;
   }



   /*******************************************************************************
    ** Fluent setter for audienceValue.
    *******************************************************************************/
   public ReleaseNotesSettings withAudienceValue(String audienceValue)
   {
      this.audienceValue = audienceValue;
      return (this);
   }



   /*******************************************************************************
    ** Getter for notifyOnPublish.
    *******************************************************************************/
   public Boolean getNotifyOnPublish()
   {
      return (this.notifyOnPublish);
   }



   /*******************************************************************************
    ** Setter for notifyOnPublish.
    *******************************************************************************/
   public void setNotifyOnPublish(Boolean notifyOnPublish)
   {
      this.notifyOnPublish = notifyOnPublish;
   }



   /*******************************************************************************
    ** Fluent setter for notifyOnPublish.
    *******************************************************************************/
   public ReleaseNotesSettings withNotifyOnPublish(Boolean notifyOnPublish)
   {
      this.notifyOnPublish = notifyOnPublish;
      return (this);
   }



   /*******************************************************************************
    ** Getter for draftingEnabled.
    *******************************************************************************/
   public Boolean getDraftingEnabled()
   {
      return (this.draftingEnabled);
   }



   /*******************************************************************************
    ** Setter for draftingEnabled.
    *******************************************************************************/
   public void setDraftingEnabled(Boolean draftingEnabled)
   {
      this.draftingEnabled = draftingEnabled;
   }



   /*******************************************************************************
    ** Fluent setter for draftingEnabled.
    *******************************************************************************/
   public ReleaseNotesSettings withDraftingEnabled(Boolean draftingEnabled)
   {
      this.draftingEnabled = draftingEnabled;
      return (this);
   }



   /*******************************************************************************
    ** Getter for includePlatformUpdates.
    *******************************************************************************/
   public Boolean getIncludePlatformUpdates()
   {
      return (this.includePlatformUpdates);
   }



   /*******************************************************************************
    ** Setter for includePlatformUpdates.
    *******************************************************************************/
   public void setIncludePlatformUpdates(Boolean includePlatformUpdates)
   {
      this.includePlatformUpdates = includePlatformUpdates;
   }



   /*******************************************************************************
    ** Fluent setter for includePlatformUpdates.
    *******************************************************************************/
   public ReleaseNotesSettings withIncludePlatformUpdates(Boolean includePlatformUpdates)
   {
      this.includePlatformUpdates = includePlatformUpdates;
      return (this);
   }



   /*******************************************************************************
    ** Getter for ingestGithub.
    *******************************************************************************/
   public Boolean getIngestGithub()
   {
      return (this.ingestGithub);
   }



   /*******************************************************************************
    ** Setter for ingestGithub.
    *******************************************************************************/
   public void setIngestGithub(Boolean ingestGithub)
   {
      this.ingestGithub = ingestGithub;
   }



   /*******************************************************************************
    ** Fluent setter for ingestGithub.
    *******************************************************************************/
   public ReleaseNotesSettings withIngestGithub(Boolean ingestGithub)
   {
      this.ingestGithub = ingestGithub;
      return (this);
   }



   /*******************************************************************************
    ** Getter for titleTemplate.
    *******************************************************************************/
   public String getTitleTemplate()
   {
      return (this.titleTemplate);
   }



   /*******************************************************************************
    ** Setter for titleTemplate.
    *******************************************************************************/
   public void setTitleTemplate(String titleTemplate)
   {
      this.titleTemplate = titleTemplate;
   }



   /*******************************************************************************
    ** Fluent setter for titleTemplate.
    *******************************************************************************/
   public ReleaseNotesSettings withTitleTemplate(String titleTemplate)
   {
      this.titleTemplate = titleTemplate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for styleGuide.
    *******************************************************************************/
   public String getStyleGuide()
   {
      return (this.styleGuide);
   }



   /*******************************************************************************
    ** Setter for styleGuide.
    *******************************************************************************/
   public void setStyleGuide(String styleGuide)
   {
      this.styleGuide = styleGuide;
   }



   /*******************************************************************************
    ** Fluent setter for styleGuide.
    *******************************************************************************/
   public ReleaseNotesSettings withStyleGuide(String styleGuide)
   {
      this.styleGuide = styleGuide;
      return (this);
   }



   /*******************************************************************************
    ** Getter for createDate.
    *******************************************************************************/
   public Instant getCreateDate()
   {
      return (this.createDate);
   }



   /*******************************************************************************
    ** Setter for createDate.
    *******************************************************************************/
   public void setCreateDate(Instant createDate)
   {
      this.createDate = createDate;
   }



   /*******************************************************************************
    ** Fluent setter for createDate.
    *******************************************************************************/
   public ReleaseNotesSettings withCreateDate(Instant createDate)
   {
      this.createDate = createDate;
      return (this);
   }



   /*******************************************************************************
    ** Getter for modifyDate.
    *******************************************************************************/
   public Instant getModifyDate()
   {
      return (this.modifyDate);
   }



   /*******************************************************************************
    ** Setter for modifyDate.
    *******************************************************************************/
   public void setModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
   }



   /*******************************************************************************
    ** Fluent setter for modifyDate.
    *******************************************************************************/
   public ReleaseNotesSettings withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
   }
}
