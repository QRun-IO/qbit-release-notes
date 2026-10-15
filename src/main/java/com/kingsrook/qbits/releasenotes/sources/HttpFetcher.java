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


package com.kingsrook.qbits.releasenotes.sources;


import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


/*******************************************************************************
 ** Minimal GET for JSON APIs; replaceable in tests.
 *******************************************************************************/
public interface HttpFetcher extends Serializable
{

   /*******************************************************************************
    ** GET a URL with an optional bearer token.
    *******************************************************************************/
   Response get(String url, String bearerToken) throws IOException, InterruptedException;

   /*******************************************************************************
    ** The default: java.net.http with short timeouts.
    *******************************************************************************/
   static HttpFetcher standard()
   {
      return (new StandardHttpFetcher());
   }



   /*******************************************************************************
    ** A response: status and body.
    **
    ** @param status HTTP status
    ** @param body   response body
    *******************************************************************************/
   record Response(int status, String body)
   {
   }



   /*******************************************************************************
    ** java.net.http implementation.
    *******************************************************************************/
   class StandardHttpFetcher implements HttpFetcher
   {

      /*******************************************************************************
       ** GET with the GitHub JSON accept header.
       *******************************************************************************/
      @Override
      public Response get(String url, String bearerToken) throws IOException, InterruptedException
      {
         HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
         HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(20))
            .header("Accept", "application/vnd.github+json").header("X-GitHub-Api-Version", "2022-11-28").header("User-Agent", "qbit-release-notes");
         if(bearerToken != null && !bearerToken.isBlank())
         {
            request.header("Authorization", "Bearer " + bearerToken);
         }
         HttpResponse<String> response = client.send(request.GET().build(), HttpResponse.BodyHandlers.ofString());
         return (new Response(response.statusCode(), response.body()));
      }
   }
}
