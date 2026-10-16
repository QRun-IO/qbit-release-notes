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
import java.util.Map;
import com.kingsrook.qbits.releasenotes.ReleaseNotesRuntime;
import com.kingsrook.qbits.releasenotes.ReleaseNotesService;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.middleware.javalin.specs.BasicResponse;
import com.kingsrook.qqq.middleware.javalin.specs.extensions.AbstractJsonEndpointSpec;
import com.kingsrook.qqq.openapi.model.Method;
import com.kingsrook.qqq.openapi.model.Parameter;
import com.kingsrook.qqq.openapi.model.Schema;
import io.javalin.http.HttpStatus;


/*******************************************************************************
 ** Common schemas and error contract for the release-notes operations.
 *******************************************************************************/
public abstract class ReleaseNotesJsonSpec extends AbstractJsonEndpointSpec
{

   /*******************************************************************************
    ** Stable operation ids for clients.
    *******************************************************************************/
   @Override
   public Method defineMethod()
   {
      return (super.defineMethod().withOperationId("releaseNotes" + getClass().getSimpleName().replace("Spec", "")));
   }



   /*******************************************************************************
    ** All component schemas.
    *******************************************************************************/
   @Override
   public Map<String, Schema> defineComponentSchemas()
   {
      return (ReleaseNotesSchemas.all());
   }



   /*******************************************************************************
    ** No parameters by default.
    *******************************************************************************/
   @Override
   public List<Parameter> defineRequestParameters()
   {
      return (List.of());
   }



   /*******************************************************************************
    ** Error statuses.
    *******************************************************************************/
   @Override
   public List<BasicResponse> defineAdditionalBasicResponses()
   {
      return (List.of(new BasicResponse(HttpStatus.BAD_REQUEST, "Invalid request", "ReleaseNotesErrorV1"),
         new BasicResponse(HttpStatus.UNAUTHORIZED, "Sign in required", "ReleaseNotesErrorV1")));
   }



   /*******************************************************************************
    ** The service for the current instance.
    *******************************************************************************/
   protected ReleaseNotesService service()
   {
      return (ReleaseNotesRuntime.of(QContext.getQInstance()).service());
   }
}
