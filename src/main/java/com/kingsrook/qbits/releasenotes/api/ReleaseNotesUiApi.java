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


package com.kingsrook.qbits.releasenotes.api;


import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.middleware.javalin.specs.extensions.MiddlewareSpecExtension;
import com.kingsrook.qqq.middleware.javalin.specs.extensions.MiddlewareSpecExtensionsMetaData;


/*******************************************************************************
 ** The release-notes UI API extension (What's new), discoverable through
 ** /qqq/v1/version.
 *******************************************************************************/
public final class ReleaseNotesUiApi
{
   public static final String NAME    = "release-notes";
   public static final String VERSION = "1.0.0";



   /*******************************************************************************
    ** Utility class.
    *******************************************************************************/
   private ReleaseNotesUiApi()
   {
   }



   /*******************************************************************************
    ** Add the extension once per instance.
    *******************************************************************************/
   public static void register(QInstance instance)
   {
      MiddlewareSpecExtensionsMetaData.ofOrWithNew(instance).withExtension(extension());
   }



   /*******************************************************************************
    ** The public contract.
    *******************************************************************************/
   public static MiddlewareSpecExtension extension()
   {
      return (new MiddlewareSpecExtension().withName(NAME).withSemanticVersion(VERSION)
         .withDescription("What's new: published release notes and each user's unread count")
         .withSpec(ReleasesSpec.class).withSpec(SummarySpec.class).withSpec(SeenSpec.class));
   }
}
