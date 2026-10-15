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


import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/*******************************************************************************
 ** What a source needs to know about the release being drafted.
 **
 ** @param applicationName           the released application
 ** @param version                   this version (X.Y.Z)
 ** @param previousVersion           the previously recorded version, or null
 ** @param previousDetectedAt        when the previous version was first seen, or null
 ** @param now                       this release's detection time
 ** @param componentVersions         platform components now (artifact to version)
 ** @param previousComponentVersions platform components at the previous release, or null
 ** @param warnings                  soft problems, kept in the internal source log
 *******************************************************************************/
public record ReleaseContext(String applicationName, String version, String previousVersion, Instant previousDetectedAt, Instant now,
                             Map<String, String> componentVersions, Map<String, String> previousComponentVersions, List<String> warnings)
{

   /*******************************************************************************
    ** A context with an empty warnings list.
    *******************************************************************************/
   public static ReleaseContext of(String applicationName, String version, String previousVersion, Instant previousDetectedAt, Instant now,
                                   Map<String, String> componentVersions, Map<String, String> previousComponentVersions)
   {
      return (new ReleaseContext(applicationName, version, previousVersion, previousDetectedAt, now, componentVersions, previousComponentVersions, new ArrayList<>()));
   }
}
