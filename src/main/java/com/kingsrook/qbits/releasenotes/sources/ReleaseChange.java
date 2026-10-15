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


import java.util.List;


/*******************************************************************************
 ** One raw change from a source. Everything here is internal: the drafter
 ** turns it into customer-facing text, and ref/url/title stay in admin fields.
 **
 ** @param sourceType PR, RELEASE or PLATFORM
 ** @param ref        internal reference such as Website-Backend#262
 ** @param title      original title
 ** @param body       original body (may hold a "Release notes:" section)
 ** @param labels     labels, lowercase
 ** @param url        internal link
 *******************************************************************************/
public record ReleaseChange(String sourceType, String ref, String title, String body, List<String> labels, String url)
{
   public static final String TYPE_PR       = "PR";
   public static final String TYPE_RELEASE  = "RELEASE";
   public static final String TYPE_PLATFORM = "PLATFORM";
}
