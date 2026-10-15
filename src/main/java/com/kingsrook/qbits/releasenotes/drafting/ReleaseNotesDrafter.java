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


package com.kingsrook.qbits.releasenotes.drafting;


import java.io.Serializable;
import java.util.List;
import com.kingsrook.qbits.releasenotes.sources.ReleaseChange;


/*******************************************************************************
 ** Turns raw changes into customer-facing items (SPI). The default is
 ** RuleBasedDrafter. A drafter never publishes; publishing is a separate,
 ** settings-controlled step.
 *******************************************************************************/
public interface ReleaseNotesDrafter extends Serializable
{

   /*******************************************************************************
    ** Name stored in the release's draftedBy field.
    *******************************************************************************/
   String getName();

   /*******************************************************************************
    ** One item per change (excluded ones with included=false, so admins can
    ** still pick them).
    *******************************************************************************/
   List<DraftItem> draft(List<ReleaseChange> changes, DraftOptions options);



   /*******************************************************************************
    ** One drafted item.
    **
    ** @param category    NEW, IMPROVED, FIXED, SECURITY or REMOVED
    ** @param text        customer-facing text
    ** @param included    whether it is in the published note
    ** @param sourceType  PR, RELEASE or PLATFORM (internal)
    ** @param sourceRef   internal reference (internal)
    ** @param sourceTitle original title (internal)
    *******************************************************************************/
   record DraftItem(String category, String text, boolean included, String sourceType, String sourceRef, String sourceTitle)
   {
   }



   /*******************************************************************************
    ** Drafting choices from settings.
    **
    ** @param includePlatformUpdates turn component upgrades into one item
    *******************************************************************************/
   record DraftOptions(boolean includePlatformUpdates)
   {
   }
}
