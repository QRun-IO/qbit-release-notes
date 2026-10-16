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


import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Properties;
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
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QSystemUserSession;
import com.kingsrook.qqq.backend.core.serviceidentity.ServiceIdentities;
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
    ** Run qbit work with no signed-in user: as the release-notes service
    ** principal when the host declares it (QQQ service identities; an
    ** unavailable principal fails, never falling back), else as the system user
    ** (previous behavior).
    *******************************************************************************/
   public <T> T asSystem(Work<T> work) throws Exception
   {
      AtomicReference<T> result    = new AtomicReference<>();
      String             principal = config.getServicePrincipal();
      QSession           session   = principal != null && ServiceIdentities.isDeclared(instance, principal)
         ? ServiceIdentities.session(instance, principal) : new QSystemUserSession(instance);
      QContext.withTemporaryContext(new CapturedContext(instance, session), () -> result.set(work.run()));
      return (result.get());
   }



   /*******************************************************************************
    ** Platform components now: every qbit's artifact and version, plus extras
    ** the host adds.
    *******************************************************************************/
   public Map<String, String> componentVersions()
   {
      Map<String, String> versions = new TreeMap<>();
      String              qqq      = mavenVersion("com.kingsrook.qqq", "qqq-backend-core");
      if(qqq != null)
      {
         versions.put("qqq", qqq);
      }
      if(instance.getQBits() != null)
      {
         for(QBitMetaData qbit : instance.getQBits().values())
         {
            if(qbit.getArtifactId() != null)
            {
               String built   = mavenVersion(qbit.getGroupId(), qbit.getArtifactId());
               String version = built != null ? built : qbit.getVersion();
               if(version != null)
               {
                  versions.put(qbit.getArtifactId(), version);
               }
            }
         }
      }
      versions.putAll(extraComponents);
      return (versions);
   }



   /*******************************************************************************
    ** The version Maven recorded in a jar on the classpath
    ** (META-INF/maven/group/artifact/pom.properties), or null.
    *******************************************************************************/
   public static String mavenVersion(String groupId, String artifactId)
   {
      if(groupId == null || artifactId == null)
      {
         return (null);
      }
      String resource = "META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties";
      try(InputStream stream = ReleaseNotesRuntime.class.getClassLoader().getResourceAsStream(resource))
      {
         if(stream == null)
         {
            return (null);
         }
         Properties properties = new Properties();
         properties.load(stream);
         String version = properties.getProperty("version");
         return (version == null || version.isBlank() ? null : version.trim());
      }
      catch(IOException e)
      {
         return (null);
      }
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
