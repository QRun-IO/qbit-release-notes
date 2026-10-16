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


import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import com.kingsrook.qbits.releasenotes.metadata.ReleaseNotesMetaData;
import com.kingsrook.qbits.releasenotes.sources.ReleaseChange;
import com.kingsrook.qbits.releasenotes.sources.ReleaseContext;
import com.kingsrook.qbits.releasenotes.sources.ReleaseSource;
import com.kingsrook.qqq.backend.core.alerts.AlertResolution;
import com.kingsrook.qqq.backend.core.alerts.AlertSignal;
import com.kingsrook.qqq.backend.core.alerts.AlertSinkInterface;
import com.kingsrook.qqq.backend.core.alerts.AlertsInstanceMetaData;
import com.kingsrook.qqq.backend.core.alerts.NotificationSignal;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.model.session.QSystemUserSession;
import com.kingsrook.qqq.backend.core.model.session.QUser;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;


/*******************************************************************************
 ** H2 instance with the qbit, a scripted source and a capturing alerts sink.
 *******************************************************************************/
public final class ReleaseNotesTestSupport
{
   public static final List<NotificationSignal> NOTIFICATIONS = new CopyOnWriteArrayList<>();



   /*******************************************************************************
    ** Utility class.
    *******************************************************************************/
   private ReleaseNotesTestSupport()
   {
   }



   /*******************************************************************************
    ** A fresh instance running the given version, with these source changes.
    *******************************************************************************/
   public static QInstance instance(String version, List<ReleaseChange> changes) throws Exception
   {
      NOTIFICATIONS.clear();
      String               name    = "rn" + UUID.randomUUID().toString().replace("-", "");
      RDBMSBackendMetaData backend = new RDBMSBackendMetaData().withName(name).withVendor("h2").withHostName("mem").withDatabaseName(name).withUsername("sa");
      backend.setJdbcUrl("jdbc:h2:mem:" + name + ";CASE_INSENSITIVE_IDENTIFIERS=TRUE;DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
      QInstance instance = new QInstance();
      instance.addBackend(backend);
      instance.withInstanceDefaultAuthentication(new QAuthenticationMetaData().withName("mock").withType(QAuthenticationType.MOCK));
      AlertsInstanceMetaData.ofOrWithNew(instance).withSink(new QCodeReference(CapturingSink.class));
      ReleaseNotesQBitConfig config = new ReleaseNotesQBitConfig().withBackendName(name).withApplicationName("acme").withApplicationLabel("Acme")
         .withAppVersion(version).withAppCommit("0123456789abcdef").withSource(new ScriptedSource(changes));
      new ReleaseNotesQBitProducer().withQBitConfig(config).produce(instance);
      migrate(backend);
      QContext.init(instance, new QSystemUserSession(instance));
      return (instance);
   }



   /*******************************************************************************
    ** The runtime of an instance, with a fixed clock.
    *******************************************************************************/
   public static ReleaseNotesRuntime runtime(QInstance instance, String at)
   {
      return (ReleaseNotesRuntime.of(instance).withClock(Clock.fixed(Instant.parse(at), ZoneOffset.UTC)));
   }



   /*******************************************************************************
    ** A signed-in user session.
    *******************************************************************************/
   public static QSession user(String id, String... permissions)
   {
      return (new QSession().withUser(new QUser().withIdReference(id)).withPermissions(permissions));
   }



   /*******************************************************************************
    ** Run the qbit changelog.
    *******************************************************************************/
   public static void migrate(RDBMSBackendMetaData backend) throws Exception
   {
      try(Connection connection = DriverManager.getConnection(backend.getJdbcUrl(), backend.getUsername(), backend.getPassword());
         Liquibase liquibase = new Liquibase(ReleaseNotesMetaData.CHANGELOG, new ClassLoaderResourceAccessor(), DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection))))
      {
         liquibase.getDatabase().setDatabaseChangeLogTableName("databasechangelog");
         liquibase.getDatabase().setDatabaseChangeLogLockTableName("databasechangeloglock");
         liquibase.update("");
      }
   }



   /*******************************************************************************
    ** A PR change.
    *******************************************************************************/
   public static ReleaseChange pr(int number, String title, String body, String... labels)
   {
      return (new ReleaseChange(ReleaseChange.TYPE_PR, "Website-Backend#" + number, title, body, List.of(labels), "https://github.com/acme/app/pull/" + number));
   }



   /*******************************************************************************
    ** Returns fixed changes.
    *******************************************************************************/
   public static class ScriptedSource implements ReleaseSource
   {
      private final ArrayList<ReleaseChange> changes;



      /*******************************************************************************
       ** Constructor.
       *******************************************************************************/
      public ScriptedSource(List<ReleaseChange> changes)
      {
         this.changes = new ArrayList<>(changes);
      }



      /*******************************************************************************
       ** Name.
       *******************************************************************************/
      @Override
      public String getName()
      {
         return ("scripted");
      }



      /*******************************************************************************
       ** The changes.
       *******************************************************************************/
      @Override
      public List<ReleaseChange> collect(ReleaseContext context)
      {
         return (changes);
      }
   }



   /*******************************************************************************
    ** Captures notifications.
    *******************************************************************************/
   public static class CapturingSink implements AlertSinkInterface
   {

      /*******************************************************************************
       ** Ignored.
       *******************************************************************************/
      @Override
      public void raise(AlertSignal signal)
      {
      }



      /*******************************************************************************
       ** Ignored.
       *******************************************************************************/
      @Override
      public void resolve(AlertResolution resolution)
      {
      }



      /*******************************************************************************
       ** Ignored.
       *******************************************************************************/
      @Override
      public void heartbeat(String heartbeatKey, Instant at)
      {
      }



      /*******************************************************************************
       ** Captured.
       *******************************************************************************/
      @Override
      public void notify(NotificationSignal signal)
      {
         NOTIFICATIONS.add(signal);
      }
   }
}
