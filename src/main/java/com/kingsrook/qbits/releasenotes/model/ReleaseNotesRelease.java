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
 ** One application release and its customer-facing note.
 *******************************************************************************/
public class ReleaseNotesRelease extends QRecordEntity
{
   public static final String TABLE_NAME = "release_notes_release";

   @QField(isPrimaryKey = true, isEditable = false)
   private Integer id;

   @QField(isRequired = true, maxLength = 100)
   private String appName;

   @QField(isRequired = true, maxLength = 50)
   private String version;

   @QField(maxLength = 40)
   private String commitSha;

   @QField(maxLength = 50)
   private String previousVersion;

   @QField
   private Instant detectedAt;

   @QField(isRequired = true, maxLength = 20)
   private String status;

   @QField(maxLength = 200)
   private String title;

   @QField(maxLength = 4000)
   private String summary;

   @QField(maxLength = 30)
   private String audienceType;

   @QField(maxLength = 200)
   private String audienceValue;

   @QField
   private Instant publishedAt;

   @QField(maxLength = 100)
   private String publishedBy;

   @QField
   private Instant notifiedAt;

   @QField(maxLength = 30)
   private String draftedBy;

   @QField(maxLength = 8000)
   private String componentVersions;

   @QField(maxLength = 8000)
   private String sourceLog;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor.
    *******************************************************************************/
   public ReleaseNotesRelease()
   {
   }



   /*******************************************************************************
    ** Constructor from a record.
    *******************************************************************************/
   public ReleaseNotesRelease(QRecord record)
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
   public ReleaseNotesRelease withId(Integer id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for appName.
    *******************************************************************************/
   public String getAppName()
   {
      return (this.appName);
   }



   /*******************************************************************************
    ** Setter for appName.
    *******************************************************************************/
   public void setAppName(String appName)
   {
      this.appName = appName;
   }



   /*******************************************************************************
    ** Fluent setter for appName.
    *******************************************************************************/
   public ReleaseNotesRelease withAppName(String appName)
   {
      this.appName = appName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for version.
    *******************************************************************************/
   public String getVersion()
   {
      return (this.version);
   }



   /*******************************************************************************
    ** Setter for version.
    *******************************************************************************/
   public void setVersion(String version)
   {
      this.version = version;
   }



   /*******************************************************************************
    ** Fluent setter for version.
    *******************************************************************************/
   public ReleaseNotesRelease withVersion(String version)
   {
      this.version = version;
      return (this);
   }



   /*******************************************************************************
    ** Getter for commitSha.
    *******************************************************************************/
   public String getCommitSha()
   {
      return (this.commitSha);
   }



   /*******************************************************************************
    ** Setter for commitSha.
    *******************************************************************************/
   public void setCommitSha(String commitSha)
   {
      this.commitSha = commitSha;
   }



   /*******************************************************************************
    ** Fluent setter for commitSha.
    *******************************************************************************/
   public ReleaseNotesRelease withCommitSha(String commitSha)
   {
      this.commitSha = commitSha;
      return (this);
   }



   /*******************************************************************************
    ** Getter for previousVersion.
    *******************************************************************************/
   public String getPreviousVersion()
   {
      return (this.previousVersion);
   }



   /*******************************************************************************
    ** Setter for previousVersion.
    *******************************************************************************/
   public void setPreviousVersion(String previousVersion)
   {
      this.previousVersion = previousVersion;
   }



   /*******************************************************************************
    ** Fluent setter for previousVersion.
    *******************************************************************************/
   public ReleaseNotesRelease withPreviousVersion(String previousVersion)
   {
      this.previousVersion = previousVersion;
      return (this);
   }



   /*******************************************************************************
    ** Getter for detectedAt.
    *******************************************************************************/
   public Instant getDetectedAt()
   {
      return (this.detectedAt);
   }



   /*******************************************************************************
    ** Setter for detectedAt.
    *******************************************************************************/
   public void setDetectedAt(Instant detectedAt)
   {
      this.detectedAt = detectedAt;
   }



   /*******************************************************************************
    ** Fluent setter for detectedAt.
    *******************************************************************************/
   public ReleaseNotesRelease withDetectedAt(Instant detectedAt)
   {
      this.detectedAt = detectedAt;
      return (this);
   }



   /*******************************************************************************
    ** Getter for status.
    *******************************************************************************/
   public String getStatus()
   {
      return (this.status);
   }



   /*******************************************************************************
    ** Setter for status.
    *******************************************************************************/
   public void setStatus(String status)
   {
      this.status = status;
   }



   /*******************************************************************************
    ** Fluent setter for status.
    *******************************************************************************/
   public ReleaseNotesRelease withStatus(String status)
   {
      this.status = status;
      return (this);
   }



   /*******************************************************************************
    ** Getter for title.
    *******************************************************************************/
   public String getTitle()
   {
      return (this.title);
   }



   /*******************************************************************************
    ** Setter for title.
    *******************************************************************************/
   public void setTitle(String title)
   {
      this.title = title;
   }



   /*******************************************************************************
    ** Fluent setter for title.
    *******************************************************************************/
   public ReleaseNotesRelease withTitle(String title)
   {
      this.title = title;
      return (this);
   }



   /*******************************************************************************
    ** Getter for summary.
    *******************************************************************************/
   public String getSummary()
   {
      return (this.summary);
   }



   /*******************************************************************************
    ** Setter for summary.
    *******************************************************************************/
   public void setSummary(String summary)
   {
      this.summary = summary;
   }



   /*******************************************************************************
    ** Fluent setter for summary.
    *******************************************************************************/
   public ReleaseNotesRelease withSummary(String summary)
   {
      this.summary = summary;
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
   public ReleaseNotesRelease withAudienceType(String audienceType)
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
   public ReleaseNotesRelease withAudienceValue(String audienceValue)
   {
      this.audienceValue = audienceValue;
      return (this);
   }



   /*******************************************************************************
    ** Getter for publishedAt.
    *******************************************************************************/
   public Instant getPublishedAt()
   {
      return (this.publishedAt);
   }



   /*******************************************************************************
    ** Setter for publishedAt.
    *******************************************************************************/
   public void setPublishedAt(Instant publishedAt)
   {
      this.publishedAt = publishedAt;
   }



   /*******************************************************************************
    ** Fluent setter for publishedAt.
    *******************************************************************************/
   public ReleaseNotesRelease withPublishedAt(Instant publishedAt)
   {
      this.publishedAt = publishedAt;
      return (this);
   }



   /*******************************************************************************
    ** Getter for publishedBy.
    *******************************************************************************/
   public String getPublishedBy()
   {
      return (this.publishedBy);
   }



   /*******************************************************************************
    ** Setter for publishedBy.
    *******************************************************************************/
   public void setPublishedBy(String publishedBy)
   {
      this.publishedBy = publishedBy;
   }



   /*******************************************************************************
    ** Fluent setter for publishedBy.
    *******************************************************************************/
   public ReleaseNotesRelease withPublishedBy(String publishedBy)
   {
      this.publishedBy = publishedBy;
      return (this);
   }



   /*******************************************************************************
    ** Getter for notifiedAt.
    *******************************************************************************/
   public Instant getNotifiedAt()
   {
      return (this.notifiedAt);
   }



   /*******************************************************************************
    ** Setter for notifiedAt.
    *******************************************************************************/
   public void setNotifiedAt(Instant notifiedAt)
   {
      this.notifiedAt = notifiedAt;
   }



   /*******************************************************************************
    ** Fluent setter for notifiedAt.
    *******************************************************************************/
   public ReleaseNotesRelease withNotifiedAt(Instant notifiedAt)
   {
      this.notifiedAt = notifiedAt;
      return (this);
   }



   /*******************************************************************************
    ** Getter for draftedBy.
    *******************************************************************************/
   public String getDraftedBy()
   {
      return (this.draftedBy);
   }



   /*******************************************************************************
    ** Setter for draftedBy.
    *******************************************************************************/
   public void setDraftedBy(String draftedBy)
   {
      this.draftedBy = draftedBy;
   }



   /*******************************************************************************
    ** Fluent setter for draftedBy.
    *******************************************************************************/
   public ReleaseNotesRelease withDraftedBy(String draftedBy)
   {
      this.draftedBy = draftedBy;
      return (this);
   }



   /*******************************************************************************
    ** Getter for componentVersions.
    *******************************************************************************/
   public String getComponentVersions()
   {
      return (this.componentVersions);
   }



   /*******************************************************************************
    ** Setter for componentVersions.
    *******************************************************************************/
   public void setComponentVersions(String componentVersions)
   {
      this.componentVersions = componentVersions;
   }



   /*******************************************************************************
    ** Fluent setter for componentVersions.
    *******************************************************************************/
   public ReleaseNotesRelease withComponentVersions(String componentVersions)
   {
      this.componentVersions = componentVersions;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sourceLog.
    *******************************************************************************/
   public String getSourceLog()
   {
      return (this.sourceLog);
   }



   /*******************************************************************************
    ** Setter for sourceLog.
    *******************************************************************************/
   public void setSourceLog(String sourceLog)
   {
      this.sourceLog = sourceLog;
   }



   /*******************************************************************************
    ** Fluent setter for sourceLog.
    *******************************************************************************/
   public ReleaseNotesRelease withSourceLog(String sourceLog)
   {
      this.sourceLog = sourceLog;
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
   public ReleaseNotesRelease withCreateDate(Instant createDate)
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
   public ReleaseNotesRelease withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
   }
}
