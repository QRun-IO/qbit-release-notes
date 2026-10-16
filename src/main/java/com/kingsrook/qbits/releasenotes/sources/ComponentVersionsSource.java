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


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;


/*******************************************************************************
 ** The platform components (QQQ qbits and other libraries the host registers)
 ** that changed since the previous release, as one internal PLATFORM change.
 *******************************************************************************/
public class ComponentVersionsSource implements ReleaseSource
{

   /*******************************************************************************
    ** Source name.
    *******************************************************************************/
   @Override
   public String getName()
   {
      return ("platform");
   }



   /*******************************************************************************
    ** One change listing every added, removed or upgraded component.
    *******************************************************************************/
   @Override
   public List<ReleaseChange> collect(ReleaseContext context)
   {
      Map<String, String> now    = context.componentVersions() == null ? Map.of() : context.componentVersions();
      Map<String, String> before = context.previousComponentVersions();
      if(before == null || now.isEmpty())
      {
         return (List.of());
      }
      TreeSet<String> names = new TreeSet<>(now.keySet());
      names.addAll(before.keySet());
      List<String> lines = new ArrayList<>();
      for(String name : names)
      {
         String from = before.get(name);
         String to   = now.get(name);
         if(!Objects.equals(from, to))
         {
            lines.add(name + " " + (from == null ? "(new)" : from) + " -> " + (to == null ? "(removed)" : to));
         }
      }
      if(lines.isEmpty())
      {
         return (List.of());
      }
      return (List.of(new ReleaseChange(ReleaseChange.TYPE_PLATFORM, "platform", "Platform updates", String.join("\n", lines), List.of(), null)));
   }
}
