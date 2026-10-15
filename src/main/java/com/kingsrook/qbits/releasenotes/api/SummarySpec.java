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


import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.middleware.javalin.specs.BasicOperation;
import com.kingsrook.qqq.openapi.model.HttpMethod;


/*******************************************************************************
 ** GET the caller's unread count and the latest release.
 *******************************************************************************/
public class SummarySpec extends ReleaseNotesJsonSpec
{

   /*******************************************************************************
    ** Method and path relative to /qqq/v1.
    *******************************************************************************/
   @Override
   public BasicOperation defineBasicOperation()
   {
      return (new BasicOperation().withHttpMethod(HttpMethod.GET).withPath("/qbit/release-notes/summary")
         .withShortSummary("What's new summary").withLongDescription("How many published releases are new to the signed-in user, and the latest one, for a menu badge."));
   }



   /*******************************************************************************
    ** Response schema.
    *******************************************************************************/
   @Override
   protected String getResponseSchemaName()
   {
      return ("ReleaseNotesSummaryV1");
   }



   /*******************************************************************************
    ** Summary.
    *******************************************************************************/
   @Override
   public Object handle(JsonInput input) throws Exception
   {
      return (service().summary(QContext.getQSession()));
   }
}
