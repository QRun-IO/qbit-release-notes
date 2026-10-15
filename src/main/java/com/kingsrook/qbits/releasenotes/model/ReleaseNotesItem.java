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
 ** One customer-facing line of a release note.
 *******************************************************************************/
public class ReleaseNotesItem extends QRecordEntity
{
   public static final String TABLE_NAME = "release_notes_item";

   @QField(isPrimaryKey = true, isEditable = false)
   private Integer id;

   @QField(isRequired = true)
   private Integer releaseId;

   @QField(isRequired = true, maxLength = 20)
   private String category;

   @QField(isRequired = true, maxLength = 2000)
   private String text;

   @QField(maxLength = 500)
   private String linkUrl;

   @QField(maxLength = 500)
   private String screenshotUrl;

   @QField
   private Integer sortOrder;

   @QField
   private Boolean included;

   @QField(maxLength = 30)
   private String sourceType;

   @QField(maxLength = 200)
   private String sourceRef;

   @QField(maxLength = 500)
   private String sourceTitle;

   @QField(isEditable = false)
   private Instant createDate;

   @QField(isEditable = false)
   private Instant modifyDate;



   /*******************************************************************************
    ** Default constructor.
    *******************************************************************************/
   public ReleaseNotesItem()
   {
   }



   /*******************************************************************************
    ** Constructor from a record.
    *******************************************************************************/
   public ReleaseNotesItem(QRecord record)
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
   public ReleaseNotesItem withId(Integer id)
   {
      this.id = id;
      return (this);
   }



   /*******************************************************************************
    ** Getter for releaseId.
    *******************************************************************************/
   public Integer getReleaseId()
   {
      return (this.releaseId);
   }



   /*******************************************************************************
    ** Setter for releaseId.
    *******************************************************************************/
   public void setReleaseId(Integer releaseId)
   {
      this.releaseId = releaseId;
   }



   /*******************************************************************************
    ** Fluent setter for releaseId.
    *******************************************************************************/
   public ReleaseNotesItem withReleaseId(Integer releaseId)
   {
      this.releaseId = releaseId;
      return (this);
   }



   /*******************************************************************************
    ** Getter for category.
    *******************************************************************************/
   public String getCategory()
   {
      return (this.category);
   }



   /*******************************************************************************
    ** Setter for category.
    *******************************************************************************/
   public void setCategory(String category)
   {
      this.category = category;
   }



   /*******************************************************************************
    ** Fluent setter for category.
    *******************************************************************************/
   public ReleaseNotesItem withCategory(String category)
   {
      this.category = category;
      return (this);
   }



   /*******************************************************************************
    ** Getter for text.
    *******************************************************************************/
   public String getText()
   {
      return (this.text);
   }



   /*******************************************************************************
    ** Setter for text.
    *******************************************************************************/
   public void setText(String text)
   {
      this.text = text;
   }



   /*******************************************************************************
    ** Fluent setter for text.
    *******************************************************************************/
   public ReleaseNotesItem withText(String text)
   {
      this.text = text;
      return (this);
   }



   /*******************************************************************************
    ** Getter for linkUrl.
    *******************************************************************************/
   public String getLinkUrl()
   {
      return (this.linkUrl);
   }



   /*******************************************************************************
    ** Setter for linkUrl.
    *******************************************************************************/
   public void setLinkUrl(String linkUrl)
   {
      this.linkUrl = linkUrl;
   }



   /*******************************************************************************
    ** Fluent setter for linkUrl.
    *******************************************************************************/
   public ReleaseNotesItem withLinkUrl(String linkUrl)
   {
      this.linkUrl = linkUrl;
      return (this);
   }



   /*******************************************************************************
    ** Getter for screenshotUrl.
    *******************************************************************************/
   public String getScreenshotUrl()
   {
      return (this.screenshotUrl);
   }



   /*******************************************************************************
    ** Setter for screenshotUrl.
    *******************************************************************************/
   public void setScreenshotUrl(String screenshotUrl)
   {
      this.screenshotUrl = screenshotUrl;
   }



   /*******************************************************************************
    ** Fluent setter for screenshotUrl.
    *******************************************************************************/
   public ReleaseNotesItem withScreenshotUrl(String screenshotUrl)
   {
      this.screenshotUrl = screenshotUrl;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sortOrder.
    *******************************************************************************/
   public Integer getSortOrder()
   {
      return (this.sortOrder);
   }



   /*******************************************************************************
    ** Setter for sortOrder.
    *******************************************************************************/
   public void setSortOrder(Integer sortOrder)
   {
      this.sortOrder = sortOrder;
   }



   /*******************************************************************************
    ** Fluent setter for sortOrder.
    *******************************************************************************/
   public ReleaseNotesItem withSortOrder(Integer sortOrder)
   {
      this.sortOrder = sortOrder;
      return (this);
   }



   /*******************************************************************************
    ** Getter for included.
    *******************************************************************************/
   public Boolean getIncluded()
   {
      return (this.included);
   }



   /*******************************************************************************
    ** Setter for included.
    *******************************************************************************/
   public void setIncluded(Boolean included)
   {
      this.included = included;
   }



   /*******************************************************************************
    ** Fluent setter for included.
    *******************************************************************************/
   public ReleaseNotesItem withIncluded(Boolean included)
   {
      this.included = included;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sourceType.
    *******************************************************************************/
   public String getSourceType()
   {
      return (this.sourceType);
   }



   /*******************************************************************************
    ** Setter for sourceType.
    *******************************************************************************/
   public void setSourceType(String sourceType)
   {
      this.sourceType = sourceType;
   }



   /*******************************************************************************
    ** Fluent setter for sourceType.
    *******************************************************************************/
   public ReleaseNotesItem withSourceType(String sourceType)
   {
      this.sourceType = sourceType;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sourceRef.
    *******************************************************************************/
   public String getSourceRef()
   {
      return (this.sourceRef);
   }



   /*******************************************************************************
    ** Setter for sourceRef.
    *******************************************************************************/
   public void setSourceRef(String sourceRef)
   {
      this.sourceRef = sourceRef;
   }



   /*******************************************************************************
    ** Fluent setter for sourceRef.
    *******************************************************************************/
   public ReleaseNotesItem withSourceRef(String sourceRef)
   {
      this.sourceRef = sourceRef;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sourceTitle.
    *******************************************************************************/
   public String getSourceTitle()
   {
      return (this.sourceTitle);
   }



   /*******************************************************************************
    ** Setter for sourceTitle.
    *******************************************************************************/
   public void setSourceTitle(String sourceTitle)
   {
      this.sourceTitle = sourceTitle;
   }



   /*******************************************************************************
    ** Fluent setter for sourceTitle.
    *******************************************************************************/
   public ReleaseNotesItem withSourceTitle(String sourceTitle)
   {
      this.sourceTitle = sourceTitle;
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
   public ReleaseNotesItem withCreateDate(Instant createDate)
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
   public ReleaseNotesItem withModifyDate(Instant modifyDate)
   {
      this.modifyDate = modifyDate;
      return (this);
   }
}
