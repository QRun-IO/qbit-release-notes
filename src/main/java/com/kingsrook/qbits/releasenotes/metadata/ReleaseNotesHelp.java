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


package com.kingsrook.qbits.releasenotes.metadata;


import com.kingsrook.qbits.releasenotes.ReleaseNotesQBitConfig;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.help.QHelpContent;
import com.kingsrook.qqq.backend.core.model.metadata.help.bundles.HelpBundle;
import com.kingsrook.qqq.backend.core.model.metadata.help.bundles.HelpBundleEntry;
import com.kingsrook.qqq.backend.core.model.metadata.help.bundles.HelpBundles;


/*******************************************************************************
 ** Registers the help bundle and applies its table and app help (host help
 ** already set wins).
 *******************************************************************************/
public final class ReleaseNotesHelp
{
   public static final String RESOURCE = "help/qbit-release-notes.yaml";



   /*******************************************************************************
    ** Utility class.
    *******************************************************************************/
   private ReleaseNotesHelp()
   {
   }



   /*******************************************************************************
    ** Register.
    *******************************************************************************/
   public static void register(QInstance instance, ReleaseNotesQBitConfig config)
   {
      HelpBundles.register(instance, RESOURCE);
      HelpBundle bundle = HelpBundles.of(instance).getBundles().getLast();
      for(HelpBundleEntry entry : bundle.getEntries())
      {
         String key = entry.getKey();
         if(key.startsWith("app:releaseNotes;"))
         {
            entry.setKey(key.replace("app:releaseNotes;", "app:" + config.getAppName() + ";"));
         }
         if(key.startsWith("table:") && key.contains(";slot:"))
         {
            String name  = key.substring(6, key.indexOf(';'));
            String slot  = key.substring(key.indexOf(";slot:") + 6);
            var    table = instance.getTable(name);
            if(table != null && (table.getHelpContent() == null || !table.getHelpContent().containsKey(slot)))
            {
               table.withHelpContent(slot, content(entry));
            }
         }
         else if(key.equals("app:releaseNotes;slot:help"))
         {
            var app = instance.getApp(config.getAppName());
            if(app.getHelpContent() == null || !app.getHelpContent().containsKey("help"))
            {
               app.withHelpContent("help", content(entry));
            }
         }
      }
   }



   /*******************************************************************************
    ** Entry as markdown help.
    *******************************************************************************/
   private static QHelpContent content(HelpBundleEntry entry)
   {
      return (new QHelpContent().withContentAsMarkdown(entry.getSummary() + "\n\n" + (entry.getContent() == null ? "" : entry.getContent())));
   }
}
