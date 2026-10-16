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


import java.util.ArrayList;
import com.kingsrook.qbits.releasenotes.ReleaseNotesRuntime;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;


/*******************************************************************************
 ** Record the running version now if it has no release yet (the same
 ** once-only detection the host runs at startup).
 *******************************************************************************/
public class CheckForReleaseStep implements BackendStep
{

   /*******************************************************************************
    ** Detect and report.
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput input, RunBackendStepOutput output) throws QException
   {
      ReleaseNotesRuntime runtime = ReleaseNotesRuntime.of(QContext.getQInstance());
      String              version = runtime.getConfig().getAppVersion();
      if(QContext.getQSession() == null || !QContext.getQSession().hasPermission(runtime.getConfig().getManagePermissionName()))
      {
         throw (new QException("The " + runtime.getConfig().getManagePermissionName() + " permission is required"));
      }
      ArrayList<ProcessSummaryLineInterface> lines = new ArrayList<>();
      try
      {
         Integer id = runtime.service().detect();
         if(id != null)
         {
            lines.add(new ProcessSummaryLine(Status.OK, 1, "release recorded as a draft: " + version));
         }
         else if(!runtime.getConfig().isReleaseVersion())
         {
            lines.add(new ProcessSummaryLine(Status.INFO, 0, "releases recorded; this build (" + version + ") is not a release version"));
         }
         else
         {
            lines.add(new ProcessSummaryLine(Status.INFO, 0, "new releases; " + version + " is already recorded"));
         }
      }
      catch(Exception e)
      {
         throw (new QException("Checking for a new release failed: " + e.getMessage(), e));
      }
      output.addValue("processResults", lines);
   }
}
