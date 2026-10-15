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


import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.releasenotes.api.ReleaseNotesUiApi;
import com.kingsrook.qbits.releasenotes.api.ReleasesSpec;
import com.kingsrook.qbits.releasenotes.api.SeenSpec;
import com.kingsrook.qbits.releasenotes.api.SummarySpec;
import com.kingsrook.qbits.releasenotes.metadata.ReleaseNotesHelp;
import com.kingsrook.qbits.releasenotes.metadata.ReleaseNotesMetaData;
import com.kingsrook.qbits.releasenotes.processes.ReleaseNotesProcesses;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.help.bundles.HelpBundles;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitConfigValidationException;
import com.kingsrook.qqq.backend.core.testing.HelpBundleAssertions;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;
import com.kingsrook.qqq.middleware.javalin.specs.extensions.MiddlewareSpecExtensionsMetaData;
import com.kingsrook.qqq.middleware.javalin.specs.v1.MiddlewareVersionV1;
import com.kingsrook.qqq.middleware.javalin.tools.UiApiContract;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** Registration, changelog, help coverage and the UI API contract.
 *******************************************************************************/
class ReleaseNotesMetaDataTest
{

   /*******************************************************************************
    ** Tables, app, processes and validation.
    *******************************************************************************/
   @Test
   void registration() throws Exception
   {
      QInstance instance = ReleaseNotesTestSupport.instance("1.0.0", List.of());
      for(String table : ReleaseNotesMetaData.TABLES)
      {
         assertThat(instance.getTable(table).getBackendName()).isNotNull();
         assertThat(instance.getTable(table).getPermissionRules().getPermissionBaseName()).isEqualTo("release_notes.manage");
      }
      assertThat(instance.getTable("release_notes_item").getField("text").getBackendName()).isEqualTo("item_text");
      assertThat(instance.getApp("releaseNotes").getLabel()).isEqualTo("Release Notes");
      for(String process : ReleaseNotesProcesses.ALL)
      {
         assertThat(instance.getProcess(process).getTableName()).isEqualTo("release_notes_release");
      }
      assertThat(instance.getProcess(ReleaseNotesProcesses.PUBLISH).getPermissionRules().getPermissionBaseName()).isEqualTo("release_notes.publish");
      assertThat(instance.getQBits()).isNotEmpty();
      assertThat(ReleaseNotesRuntime.of(instance).componentVersions()).containsEntry("qbit-release-notes", ReleaseNotesQBitProducer.VERSION);
      assertThat(ReleaseNotesRuntime.of(instance).withComponentVersion("ui", "1.0").componentVersions()).containsEntry("ui", "1.0").containsKey("qqq");
      assertThat(ReleaseNotesRuntime.mavenVersion("com.kingsrook.qqq", "qqq-backend-core")).isNotBlank();
      assertThat(ReleaseNotesRuntime.mavenVersion("x", "nope")).isNull();
      assertThat(ReleaseNotesRuntime.mavenVersion(null, "nope")).isNull();
      assertThat(new ReleaseNotesQBitConfig().withPermissionBaseName("rn").getAdminPermissionNames()).containsExactly("rn.manage", "rn.publish");

      ReleaseNotesQBitConfig bad = new ReleaseNotesQBitConfig().withBackendName("nope");
      assertThatThrownBy(() -> new ReleaseNotesQBitProducer().withQBitConfig(bad).produce(new QInstance())).isInstanceOf(QBitConfigValidationException.class);
      assertThatThrownBy(() -> new ReleaseNotesQBitProducer().produce(new QInstance())).hasMessageContaining("configuration is required");
      assertThat(new ReleaseNotesQBitProducer().isEnabled()).isFalse();
      assertThat(new ReleaseNotesQBitProducer().withNamespace("x").getNamespace()).isEqualTo("x");
      assertThatThrownBy(() -> ReleaseNotesRuntime.of(new QInstance())).isInstanceOf(IllegalStateException.class);

      assertThat(new ReleaseNotesQBitConfig().withAppVersion("1.2.3").isReleaseVersion()).isTrue();
      assertThat(new ReleaseNotesQBitConfig().withAppVersion("1.2.3-RC.1").isReleaseVersion()).isFalse();
      assertThat(new ReleaseNotesQBitConfig().withAppVersion("abc1234").withAllowNonReleaseVersions(true).isReleaseVersion()).isTrue();
      assertThat(new ReleaseNotesQBitConfig().isReleaseVersion()).isFalse();
      assertThat(new ReleaseNotesQBitConfig().withApplicationName("kof22").getEffectiveApplicationLabel()).isEqualTo("kof22");
   }



   /*******************************************************************************
    ** The changelog runs twice and seeds one settings row.
    *******************************************************************************/
   @Test
   void changelogTwice() throws Exception
   {
      QInstance            instance = ReleaseNotesTestSupport.instance("1.0.0", List.of());
      RDBMSBackendMetaData backend  = (RDBMSBackendMetaData) instance.getBackends().values().iterator().next();
      ReleaseNotesTestSupport.migrate(backend);
      try(Connection connection = DriverManager.getConnection(backend.getJdbcUrl(), "sa", ""); Statement statement = connection.createStatement())
      {
         for(String table : ReleaseNotesMetaData.TABLES)
         {
            try(ResultSet rows = statement.executeQuery("SELECT count(*) FROM " + table))
            {
               rows.next();
               assertThat(rows.getInt(1)).isEqualTo(table.equals("release_notes_settings") ? 1 : 0);
            }
         }
      }
      String script = new String(getClass().getClassLoader().getResourceAsStream(ReleaseNotesMetaData.CHANGELOG).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
      assertThat(script).contains("CONSTRAINT fk_release_notes_item_release_id FOREIGN KEY (release_id) REFERENCES release_notes_release (id)");
   }



   /*******************************************************************************
    ** Every table, field, process and the app have help.
    *******************************************************************************/
   @Test
   void helpCoverage() throws Exception
   {
      QInstance instance = ReleaseNotesTestSupport.instance("1.0.0", List.of());
      HelpBundleAssertions.forInstance(instance).bundles(ReleaseNotesHelp.RESOURCE)
         .owning(target -> "process".equals(target.kind()) ? ReleaseNotesProcesses.ALL.contains(target.name())
            : target.name().startsWith("release_notes_") || target.name().startsWith("releaseNotes")).assertComplete();
      for(String table : ReleaseNotesMetaData.TABLES)
      {
         assertThat(instance.getTable(table).getHelpContent()).containsKeys("help", "emptyState");
      }
      assertThat(instance.getApp("releaseNotes").getHelpContent()).containsKey("help");
      assertThat(HelpBundles.of(instance).getBundles()).isNotEmpty();
   }



   /*******************************************************************************
    ** The committed OpenAPI matches the extension; only customer fields.
    *******************************************************************************/
   @Test
   void uiApiContract() throws Exception
   {
      var    extension = ReleaseNotesUiApi.extension();
      var    version   = new MiddlewareVersionV1();
      String generated = UiApiContract.generateExtensionYaml(version, extension);
      Path   path      = Path.of("src/main/resources/openapi/qqq-v1/release-notes.yaml");
      if(Boolean.getBoolean("releasenotes.openapi.update"))
      {
         Files.createDirectories(path.getParent());
         Files.writeString(path, generated);
      }
      assertThat(UiApiContract.check(generated, Files.readString(path), null, 1, "mvnw -Dtest=ReleaseNotesMetaDataTest -Dreleasenotes.openapi.update=true test")).isEmpty();
      var model = version.generateExtensionOpenAPIModel("qqq", extension);
      assertThat(model.getPaths().keySet()).containsExactlyInAnyOrder("/qqq/v1/qbit/release-notes/releases", "/qqq/v1/qbit/release-notes/summary", "/qqq/v1/qbit/release-notes/seen");
      assertThat(model.getComponents().getSchemas().keySet()).allMatch(name -> name.startsWith("ReleaseNotes") && name.endsWith("V1"));
      assertThat(generated).doesNotContain("sourceRef", "sourceLog", "commitSha", "componentVersions");
      QInstance instance = ReleaseNotesTestSupport.instance("1.0.0", List.of());
      assertThat(MiddlewareSpecExtensionsMetaData.of(instance).getExtensions()).containsOnlyKeys("release-notes");
      assertThat(List.of(new ReleasesSpec(), new SummarySpec(), new SeenSpec())).allSatisfy(spec -> assertThat(spec.defineMethod().getOperationId()).startsWith("releaseNotes"));
      assertThat(Map.of()).isEmpty();
   }
}
