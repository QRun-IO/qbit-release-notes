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


import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import com.kingsrook.qbits.releasenotes.drafting.ReleaseNotesDrafter;
import com.kingsrook.qbits.releasenotes.drafting.RuleBasedDrafter;
import com.kingsrook.qqq.backend.core.context.CapturedContext;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QSupplementalInstanceMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSystemUserSession;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Per-instance runtime: config, drafter, clock and startup detection.
 *******************************************************************************/
public class ReleaseNotesRuntime implements QSupplementalInstanceMetaData
{
   public static final String NAME = "releaseNotes";

   private static final QLogger LOG = QLogger.getLogger(ReleaseNotesRuntime.class);

   private final QInstance              instance;
   private final ReleaseNotesQBitConfig config;
   private final Map<String, String>    extraComponents = new TreeMap<>();
   private ReleaseNotesDrafter          drafter         = new RuleBasedDrafter();
   private Clock                        clock           = Clock.systemUTC();



   /*******************************************************************************
    ** Work run with a context.
    *******************************************************************************/
   public interface Work<T>
   {
      /*******************************************************************************
       ** Run.
       *******************************************************************************/
      T run() throws Exception;
   }



   /*******************************************************************************
    ** Constructor.
    *******************************************************************************/
   public ReleaseNotesRuntime(QInstance instance, ReleaseNotesQBitConfig config)
   {
      this.instance = instance;
      this.config = config;
   }



   /*******************************************************************************
    ** The runtime registered in an instance.
    *******************************************************************************/
   public static ReleaseNotesRuntime of(QInstance instance)
   {
      ReleaseNotesRuntime runtime = QSupplementalInstanceMetaData.of(instance, NAME);
      if(runtime == null)
      {
         throw (new IllegalStateException("Release Notes is not registered"));
      }
      return (runtime);
   }



   /*******************************************************************************
    ** Supplemental metadata name.
    *******************************************************************************/
   @Override
   public String getName()
   {
      return (NAME);
   }



   /*******************************************************************************
    ** Register with the instance.
    *******************************************************************************/
   @Override
   public void addSelfToInstance(QInstance instance)
   {
      instance.withSupplementalMetaData(this);
   }



   /*******************************************************************************
    ** Detect the running version on a background thread (call once after the
    ** server starts; tables must already be migrated). Never throws.
    *******************************************************************************/
   public Thread detectInBackground()
   {
      Thread thread = new Thread(() ->
      {
         try
         {
            Integer id = service().detect();
            if(id != null)
            {
               LOG.info("Recorded a new release", logPair("version", config.getAppVersion()),
                  logPair("releaseId", id));
            }
         }
         catch(Exception e)
         {
            LOG.warn("Release detection failed", e);
         }
      }, "release-notes-detect");
      thread.setDaemon(true);
      thread.start();
      return (thread);
   }



   /*******************************************************************************
    ** The service bound to this runtime.
    *******************************************************************************/
   public ReleaseNotesService service()
   {
      return (new ReleaseNotesService(this));
   }



   /*******************************************************************************
    ** Run as the system user.
    *******************************************************************************/
   public <T> T asSystem(Work<T> work) throws Exception
   {
      AtomicReference<T> result = new AtomicReference<>();
      QContext.withTemporaryContext(new CapturedContext(instance, new QSystemUserSession(instance)), () -> result.set(work.run()));
      return (result.get());
   }



   /*******************************************************************************
    ** Platform components now: every qbit's artifact and version, plus extras
    ** the host adds.
    *******************************************************************************/
   public Map<String, String> componentVersions()
   {
      Map<String, String> versions = new TreeMap<>();
      if(instance.getQBits() != null)
      {
         for(QBitMetaData qbit : instance.getQBits().values())
         {
            if(qbit.getArtifactId() != null && qbit.getVersion() != null)
            {
               versions.put(qbit.getArtifactId(), qbit.getVersion());
            }
         }
      }
      versions.putAll(extraComponents);
      return (versions);
   }



   /*******************************************************************************
    ** Add a platform component (e.g. the UI or QQQ version) to compare between
    ** releases.
    *******************************************************************************/
   public ReleaseNotesRuntime withComponentVersion(String name, String version)
   {
      if(name != null && version != null)
      {
         extraComponents.put(name, version);
      }
      return (this);
   }



   /*******************************************************************************
    ** Now.
    *******************************************************************************/
   public Instant now()
   {
      return (clock.instant());
   }



   /*******************************************************************************
    ** Fluent setter for clock (tests).
    *******************************************************************************/
   public ReleaseNotesRuntime withClock(Clock clock)
   {
      this.clock = clock;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for drafter.
    *******************************************************************************/
   public ReleaseNotesRuntime withDrafter(ReleaseNotesDrafter drafter)
   {
      this.drafter = drafter;
      return (this);
   }



   /*******************************************************************************
    ** Getter for drafter.
    *******************************************************************************/
   public ReleaseNotesDrafter getDrafter()
   {
      return (drafter);
   }



   /*******************************************************************************
    ** Getter for config.
    *******************************************************************************/
   public ReleaseNotesQBitConfig getConfig()
   {
      return (config);
   }



   /*******************************************************************************
    ** Getter for instance.
    *******************************************************************************/
   public QInstance getInstance()
   {
      return (instance);
   }
}
