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


import java.io.Serializable;
import java.util.List;


/*******************************************************************************
 ** Where release changes come from (SPI). Implementations are serializable
 ** metadata: no secrets in fields (read tokens at call time).
 *******************************************************************************/
public interface ReleaseSource extends Serializable
{

   /*******************************************************************************
    ** Short name used in the internal source log.
    *******************************************************************************/
   String getName();

   /*******************************************************************************
    ** True for sources that call GitHub (the ingestGithub setting turns them off).
    *******************************************************************************/
   default boolean isGitHub()
   {
      return (false);
   }

   /*******************************************************************************
    ** The changes in this release. Add soft problems to context.warnings();
    ** throw only when the source cannot run at all.
    *******************************************************************************/
   List<ReleaseChange> collect(ReleaseContext context) throws Exception;
}
