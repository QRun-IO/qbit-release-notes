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


package com.kingsrook.qbits.releasenotes;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qbits.releasenotes.api.ReleaseNotesUiApi;
import com.kingsrook.qbits.releasenotes.metadata.ReleaseNotesHelp;
import com.kingsrook.qbits.releasenotes.metadata.ReleaseNotesMetaData;
import com.kingsrook.qbits.releasenotes.processes.ReleaseNotesProcesses;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.MetaDataProducerMultiOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitConfigValidationException;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaDataProducer;


/*******************************************************************************
 ** Produces the Release Notes QBit.
 *******************************************************************************/
public class ReleaseNotesQBitProducer implements QBitMetaDataProducer<ReleaseNotesQBitConfig>
{
   public static final String GROUP_ID    = "com.kingsrook.qbits";
   public static final String ARTIFACT_ID = "qbit-release-notes";
   public static final String VERSION     = "0.1.0";

   private ReleaseNotesQBitConfig qBitConfig;
   private String                 namespace;



   /*******************************************************************************
    ** Fluent setter for the config.
    *******************************************************************************/
   public ReleaseNotesQBitProducer withQBitConfig(ReleaseNotesQBitConfig config)
   {
      this.qBitConfig = config;
      return (this);
   }



   /*******************************************************************************
    ** Getter for the config.
    *******************************************************************************/
   @Override
   public ReleaseNotesQBitConfig getQBitConfig()
   {
      return (qBitConfig);
   }



   /*******************************************************************************
    ** Fluent setter for the namespace.
    *******************************************************************************/
   public ReleaseNotesQBitProducer withNamespace(String namespace)
   {
      this.namespace = namespace;
      return (this);
   }



   /*******************************************************************************
    ** Getter for the namespace.
    *******************************************************************************/
   @Override
   public String getNamespace()
   {
      return (namespace);
   }



   /*******************************************************************************
    ** The qbit's identity.
    *******************************************************************************/
   @Override
   public QBitMetaData getQBitMetaData()
   {
      return (new QBitMetaData().withGroupId(GROUP_ID).withArtifactId(ARTIFACT_ID).withVersion(VERSION).withNamespace(namespace).withConfig(qBitConfig));
   }



   /*******************************************************************************
    ** Producers found by package scanning have no config: skip them.
    *******************************************************************************/
   @Override
   public boolean isEnabled()
   {
      return (qBitConfig != null);
   }



   /*******************************************************************************
    ** Validate and register.
    *******************************************************************************/
   @Override
   public MetaDataProducerMultiOutput produce(QInstance instance) throws QException
   {
      if(qBitConfig == null)
      {
         throw (new QException("Release Notes configuration is required"));
      }
      List<String> errors = new ArrayList<>();
      qBitConfig.validate(instance, errors);
      for(String table : ReleaseNotesMetaData.TABLES)
      {
         if(instance.getTable(table) != null)
         {
            errors.add("Table already registered: " + table);
         }
      }
      if(!errors.isEmpty())
      {
         throw (new QBitConfigValidationException(qBitConfig, errors));
      }
      instance.addQBit(getQBitMetaData());
      ReleaseNotesMetaData.addTo(instance, qBitConfig);
      ReleaseNotesProcesses.addTo(instance, qBitConfig);
      new ReleaseNotesRuntime(instance, qBitConfig).addSelfToInstance(instance);
      registerUiApi(instance);
      ReleaseNotesHelp.register(instance, qBitConfig);
      return (new MetaDataProducerMultiOutput());
   }



   /*******************************************************************************
    ** Middleware is optional for hosts without the HTTP API.
    *******************************************************************************/
   private static void registerUiApi(QInstance instance)
   {
      try
      {
         Class.forName("com.kingsrook.qqq.middleware.javalin.specs.extensions.MiddlewareSpecExtensionsMetaData");
         ReleaseNotesUiApi.register(instance);
      }
      catch(ClassNotFoundException e)
      {
         // Core-only hosts have no HTTP API to register.
      }
   }
}
