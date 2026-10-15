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


package com.kingsrook.qbits.releasenotes.processes;


import java.util.List;
import com.kingsrook.qbits.releasenotes.ReleaseNotesQBitConfig;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesRelease;
import com.kingsrook.qqq.backend.core.actions.permissions.UseOtherPermissionNameCustomPermissionChecker;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.layout.QIcon;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.PermissionLevel;
import com.kingsrook.qqq.backend.core.model.metadata.permissions.QPermissionRules;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QComponentType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ExtractViaQueryStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;


/*******************************************************************************
 ** Record actions on Releases (standard ETL review and result): Publish,
 ** Skip, Redraft; and the table action Check for new release. None are in
 ** the nav.
 *******************************************************************************/
public final class ReleaseNotesProcesses
{
   public static final String PUBLISH = "releaseNotesPublish";
   public static final String SKIP    = "releaseNotesSkip";
   public static final String REDRAFT = "releaseNotesRedraft";
   public static final String DETECT  = "releaseNotesCheckForRelease";

   public static final List<String> ALL = List.of(PUBLISH, SKIP, REDRAFT, DETECT);



   /*******************************************************************************
    ** Utility class.
    *******************************************************************************/
   private ReleaseNotesProcesses()
   {
   }



   /*******************************************************************************
    ** Register the processes.
    *******************************************************************************/
   public static void addTo(QInstance instance, ReleaseNotesQBitConfig config)
   {
      instance.addProcess(recordAction(PUBLISH, "Publish", "campaign", ReleaseActionSteps.PUBLISH, config.getPublishPermissionName(),
         "Review the selected drafts. Publishing shows them on What's new and notifies their audience."));
      instance.addProcess(recordAction(SKIP, "Skip", "do_not_disturb_on", ReleaseActionSteps.SKIP, config.getPublishPermissionName(),
         "Review the selected drafts. Skipped releases are never announced or listed."));
      instance.addProcess(recordAction(REDRAFT, "Redraft", "refresh", ReleaseActionSteps.REDRAFT, config.getManagePermissionName(),
         "Review the selected drafts. Redrafting reads the sources again and replaces drafted items; items written by hand are kept."));

      instance.addProcess(new QProcessMetaData().withName(DETECT).withLabel("Check for new release").withIcon(new QIcon("new_releases"))
         .withTableName(ReleaseNotesRelease.TABLE_NAME)
         .withPermissionRules(rules(config.getManagePermissionName()))
         .withStep(new QBackendStepMetaData().withName("run").withCode(new QCodeReference(CheckForReleaseStep.class)))
         .withStep(new QFrontendStepMetaData().withName("result").withLabel("Result")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.PROCESS_SUMMARY_RESULTS))));
   }



   /*******************************************************************************
    ** A record action on selected releases.
    *******************************************************************************/
   private static QProcessMetaData recordAction(String name, String label, String icon, String action, String permission, String preview)
   {
      QProcessMetaData process = StreamedETLWithFrontendProcess.processMetaDataBuilder()
         .withName(name).withLabel(label).withIcon(new QIcon(icon))
         .withTableName(ReleaseNotesRelease.TABLE_NAME).withSourceTable(ReleaseNotesRelease.TABLE_NAME).withDestinationTable(ReleaseNotesRelease.TABLE_NAME)
         .withExtractStepClass(ExtractViaQueryStep.class).withTransformStepClass(ReleaseActionSteps.Transform.class).withLoadStepClass(ReleaseActionSteps.Load.class)
         .withSupportsFullValidation(true).withDoFullValidation(true).withMinInputRecords(1)
         .withPreviewMessage(preview)
         .withFields(List.of(new QFieldMetaData(ReleaseActionSteps.ACTION, QFieldType.STRING).withDefaultValue(action)))
         .getProcessMetaData();
      process.withPermissionRules(rules(permission));
      return (process);
   }



   /*******************************************************************************
    ** Exactly one permission.
    *******************************************************************************/
   private static QPermissionRules rules(String permission)
   {
      return (new QPermissionRules().withLevel(PermissionLevel.HAS_ACCESS_PERMISSION).withPermissionBaseName(permission)
         .withCustomPermissionChecker(UseOtherPermissionNameCustomPermissionChecker.build(permission)));
   }
}
