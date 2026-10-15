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


import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PossibleValueEnum;


/*******************************************************************************
 ** Enum-backed possible values with readable labels.
 *******************************************************************************/
public final class ReleaseNotesPossibleValues
{
   public static final String CATEGORY      = "release_notes_category";
   public static final String STATUS        = "release_notes_status";
   public static final String PUBLISH_MODE  = "release_notes_publish_mode";
   public static final String AUDIENCE_TYPE = "release_notes_audience_type";
   public static final String SOURCE_TYPE   = "release_notes_source_type";



   /*******************************************************************************
    ** Utility class.
    *******************************************************************************/
   private ReleaseNotesPossibleValues()
   {
   }



   /*******************************************************************************
    ** Item categories, in display order.
    *******************************************************************************/
   public enum Category implements PossibleValueEnum<String>
   {
      NEW("New"),
      IMPROVED("Improved"),
      FIXED("Fixed"),
      SECURITY("Security"),
      REMOVED("Removed");

      private final String label;



      /*******************************************************************************
       ** Constructor.
       *******************************************************************************/
      Category(String label)
      {
         this.label = label;
      }



      /*******************************************************************************
       ** Stored value.
       *******************************************************************************/
      @Override
      public String getPossibleValueId()
      {
         return (name());
      }



      /*******************************************************************************
       ** Label.
       *******************************************************************************/
      @Override
      public String getPossibleValueLabel()
      {
         return (label);
      }



      /*******************************************************************************
       ** The category for a stored value, or IMPROVED.
       *******************************************************************************/
      public static Category of(String value)
      {
         for(Category category : values())
         {
            if(category.name().equals(value))
            {
               return (category);
            }
         }
         return (IMPROVED);
      }
   }



   /*******************************************************************************
    ** Release status.
    *******************************************************************************/
   public enum Status implements PossibleValueEnum<String>
   {
      DRAFT("Draft"),
      PUBLISHED("Published"),
      SKIPPED("Skipped");

      private final String label;



      /*******************************************************************************
       ** Constructor.
       *******************************************************************************/
      Status(String label)
      {
         this.label = label;
      }



      /*******************************************************************************
       ** Stored value.
       *******************************************************************************/
      @Override
      public String getPossibleValueId()
      {
         return (name());
      }



      /*******************************************************************************
       ** Label.
       *******************************************************************************/
      @Override
      public String getPossibleValueLabel()
      {
         return (label);
      }
   }



   /*******************************************************************************
    ** How drafts become published.
    *******************************************************************************/
   public enum PublishMode implements PossibleValueEnum<String>
   {
      REQUIRE_APPROVAL("Require approval"),
      AUTO_PUBLISH("Publish automatically");

      private final String label;



      /*******************************************************************************
       ** Constructor.
       *******************************************************************************/
      PublishMode(String label)
      {
         this.label = label;
      }



      /*******************************************************************************
       ** Stored value.
       *******************************************************************************/
      @Override
      public String getPossibleValueId()
      {
         return (name());
      }



      /*******************************************************************************
       ** Label.
       *******************************************************************************/
      @Override
      public String getPossibleValueLabel()
      {
         return (label);
      }
   }



   /*******************************************************************************
    ** Who a release is for.
    *******************************************************************************/
   public enum AudienceType implements PossibleValueEnum<String>
   {
      ALL_USERS("All users"),
      PERMISSION("Users with a permission");

      private final String label;



      /*******************************************************************************
       ** Constructor.
       *******************************************************************************/
      AudienceType(String label)
      {
         this.label = label;
      }



      /*******************************************************************************
       ** Stored value.
       *******************************************************************************/
      @Override
      public String getPossibleValueId()
      {
         return (name());
      }



      /*******************************************************************************
       ** Label.
       *******************************************************************************/
      @Override
      public String getPossibleValueLabel()
      {
         return (label);
      }
   }



   /*******************************************************************************
    ** Where an item came from.
    *******************************************************************************/
   public enum SourceType implements PossibleValueEnum<String>
   {
      PR("Pull request"),
      RELEASE("Release notes line"),
      PLATFORM("Platform update"),
      MANUAL("Written by hand");

      private final String label;



      /*******************************************************************************
       ** Constructor.
       *******************************************************************************/
      SourceType(String label)
      {
         this.label = label;
      }



      /*******************************************************************************
       ** Stored value.
       *******************************************************************************/
      @Override
      public String getPossibleValueId()
      {
         return (name());
      }



      /*******************************************************************************
       ** Label.
       *******************************************************************************/
      @Override
      public String getPossibleValueLabel()
      {
         return (label);
      }
   }
}
