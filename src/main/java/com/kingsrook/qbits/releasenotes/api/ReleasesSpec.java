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


import java.util.List;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QBadRequestException;
import com.kingsrook.qqq.middleware.javalin.specs.BasicOperation;
import com.kingsrook.qqq.openapi.model.HttpMethod;
import com.kingsrook.qqq.openapi.model.In;
import com.kingsrook.qqq.openapi.model.Parameter;
import com.kingsrook.qqq.openapi.model.Schema;
import com.kingsrook.qqq.openapi.model.Type;


/*******************************************************************************
 ** GET published releases the caller may see, newest first.
 *******************************************************************************/
public class ReleasesSpec extends ReleaseNotesJsonSpec
{

   /*******************************************************************************
    ** Method and path relative to /qqq/v1.
    *******************************************************************************/
   @Override
   public BasicOperation defineBasicOperation()
   {
      return (new BasicOperation().withHttpMethod(HttpMethod.GET).withPath("/qbit/release-notes/releases")
         .withShortSummary("Published release notes").withLongDescription("Published releases visible to the signed-in user, newest first, with their customer-facing items and whether each is new to the user."));
   }



   /*******************************************************************************
    ** Paging.
    *******************************************************************************/
   @Override
   public List<Parameter> defineRequestParameters()
   {
      return (List.of(
         new Parameter().withName("limit").withIn(In.QUERY).withRequired(false).withSchema(new Schema().withType(Type.INTEGER)).withDescription("Releases per page (1-50, default 20)."),
         new Parameter().withName("offset").withIn(In.QUERY).withRequired(false).withSchema(new Schema().withType(Type.INTEGER)).withDescription("Releases to skip (default 0).")));
   }



   /*******************************************************************************
    ** Response schema.
    *******************************************************************************/
   @Override
   protected String getResponseSchemaName()
   {
      return ("ReleaseNotesReleasesV1");
   }



   /*******************************************************************************
    ** List.
    *******************************************************************************/
   @Override
   public Object handle(JsonInput input) throws Exception
   {
      Integer limit  = input.getQueryParamInteger("limit");
      Integer offset = input.getQueryParamInteger("offset");
      if((limit != null && (limit < 1 || limit > 50)) || (offset != null && offset < 0))
      {
         throw (new QBadRequestException("limit must be 1-50 and offset at least 0."));
      }
      return (service().whatsNew(QContext.getQSession(), limit == null ? 20 : limit, offset == null ? 0 : offset));
   }
}
