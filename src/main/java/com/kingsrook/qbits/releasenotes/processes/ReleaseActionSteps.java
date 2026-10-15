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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.releasenotes.ReleaseNotesRuntime;
import com.kingsrook.qbits.releasenotes.ReleaseNotesService;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesPossibleValues.Status;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractLoadStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.AbstractTransformStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ProcessSummaryProviderInterface;


/*******************************************************************************
 ** Transform (review) and load (result) for Publish, Skip and Redraft.
 ** Review lists what will happen and why any release cannot proceed; load
 ** performs it through ReleaseNotesService.
 *******************************************************************************/
public final class ReleaseActionSteps
{
   public static final String ACTION  = "releaseAction";
   public static final String PUBLISH = "PUBLISH";
   public static final String SKIP    = "SKIP";
   public static final String REDRAFT = "REDRAFT";

   private static final Map<String, String[]> VERBS = Map.of(
      PUBLISH, new String[] {"published", "published"},
      SKIP, new String[] {"skipped", "skipped"},
      REDRAFT, new String[] {"redrafted", "redrafted"});



   /*******************************************************************************
    ** Utility class.
    *******************************************************************************/
   private ReleaseActionSteps()
   {
   }



   /*******************************************************************************
    ** The action, checked.
    *******************************************************************************/
   static String action(RunBackendStepInput input) throws QException
   {
      String action = input.getValueString(ACTION);
      if(action == null || !VERBS.containsKey(action))
      {
         throw (new QException("Unknown release action"));
      }
      var    config     = ReleaseNotesRuntime.of(QContext.getQInstance()).getConfig();
      String permission = REDRAFT.equals(action) ? config.getManagePermissionName() : config.getPublishPermissionName();
      if(QContext.getQSession() == null || !QContext.getQSession().hasPermission(permission))
      {
         throw (new QException("The " + permission + " permission is required"));
      }
      return (action);
   }



   /*******************************************************************************
    ** Why a release cannot take the action (empty when it can).
    *******************************************************************************/
   static List<String> blockers(ReleaseNotesService service, String action, QRecord release) throws Exception
   {
      if(PUBLISH.equals(action))
      {
         return (service.problems(release));
      }
      if(!Status.DRAFT.name().equals(release.getValueString("status")))
      {
         return (List.of("Only drafts can be " + VERBS.get(action)[1] + "."));
      }
      return (List.of());
   }



   /*******************************************************************************
    ** An OK line.
    *******************************************************************************/
   static ProcessSummaryLine okLine(String action, boolean past)
   {
      ProcessSummaryLine line = new ProcessSummaryLine(com.kingsrook.qqq.backend.core.model.actions.processes.Status.OK, 0, null);
      line.setSingularFutureMessage("release will be " + VERBS.get(action)[0]);
      line.setPluralFutureMessage("releases will be " + VERBS.get(action)[0]);
      line.setSingularPastMessage("release was " + VERBS.get(action)[1]);
      line.setPluralPastMessage("releases were " + VERBS.get(action)[1]);
      return (line);
   }



   /*******************************************************************************
    ** An error line with a reason.
    *******************************************************************************/
   static ProcessSummaryLine errorLine(String reason)
   {
      ProcessSummaryLine line = new ProcessSummaryLine(com.kingsrook.qqq.backend.core.model.actions.processes.Status.ERROR, 0, null);
      line.setSingularFutureMessage("release can't proceed: " + reason);
      line.setPluralFutureMessage("releases can't proceed: " + reason);
      line.setSingularPastMessage("release was not changed: " + reason);
      line.setPluralPastMessage("releases were not changed: " + reason);
      return (line);
   }



   /*******************************************************************************
    ** The signed-in user's id, for publishedBy.
    *******************************************************************************/
   static String actor()
   {
      return (QContext.getQSession() == null || QContext.getQSession().getUser() == null ? null : QContext.getQSession().getUser().getIdReference());
   }



   /*******************************************************************************
    ** Review: count what will happen and group the blockers.
    *******************************************************************************/
   public static class Transform extends AbstractTransformStep implements ProcessSummaryProviderInterface
   {
      private ProcessSummaryLine                    ok;
      private final Map<String, ProcessSummaryLine> errors = new LinkedHashMap<>();



      /*******************************************************************************
       ** Check each release.
       *******************************************************************************/
      @Override
      public void runOnePage(RunBackendStepInput input, RunBackendStepOutput output) throws QException
      {
         String              action  = action(input);
         ReleaseNotesService service = ReleaseNotesRuntime.of(QContext.getQInstance()).service();
         if(ok == null)
         {
            ok = okLine(action, false);
         }
         for(QRecord record : input.getRecords())
         {
            List<String> blockers;
            try
            {
               blockers = blockers(service, action, record);
            }
            catch(Exception e)
            {
               blockers = List.of("it could not be checked");
            }
            if(blockers.isEmpty())
            {
               ok.incrementCountAndAddPrimaryKey(record.getValue("id"));
               output.addRecord(record);
            }
            else
            {
               for(String blocker : blockers)
               {
                  errors.computeIfAbsent(blocker, ReleaseActionSteps::errorLine).incrementCountAndAddPrimaryKey(record.getValue("id"));
               }
            }
         }
      }



      /*******************************************************************************
       ** Review lines.
       *******************************************************************************/
      @Override
      public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput output, boolean isForResultScreen)
      {
         ArrayList<ProcessSummaryLineInterface> lines = new ArrayList<>();
         if(ok != null)
         {
            ok.addSelfToListIfAnyCount(lines);
         }
         errors.values().forEach(line -> line.addSelfToListIfAnyCount(lines));
         return (lines);
      }
   }



   /*******************************************************************************
    ** Result: perform the action on each reviewed release.
    *******************************************************************************/
   public static class Load extends AbstractLoadStep implements ProcessSummaryProviderInterface
   {
      private ProcessSummaryLine                    ok;
      private final Map<String, ProcessSummaryLine> errors = new LinkedHashMap<>();



      /*******************************************************************************
       ** Act on each release.
       *******************************************************************************/
      @Override
      public void runOnePage(RunBackendStepInput input, RunBackendStepOutput output) throws QException
      {
         String              action  = action(input);
         ReleaseNotesService service = ReleaseNotesRuntime.of(QContext.getQInstance()).service();
         if(ok == null)
         {
            ok = okLine(action, true);
         }
         for(QRecord record : input.getRecords())
         {
            Integer id = record.getValueInteger("id");
            try
            {
               switch(action)
               {
                  case PUBLISH -> service.publish(id, actor());
                  case SKIP -> service.skip(id, actor());
                  default -> service.draft(id);
               }
               ok.incrementCountAndAddPrimaryKey(id);
            }
            catch(Exception e)
            {
               String reason = e.getMessage() == null ? "unexpected error" : e.getMessage();
               errors.computeIfAbsent(reason, ReleaseActionSteps::errorLine).incrementCountAndAddPrimaryKey(id);
            }
         }
      }



      /*******************************************************************************
       ** Result lines.
       *******************************************************************************/
      @Override
      public ArrayList<ProcessSummaryLineInterface> getProcessSummary(RunBackendStepOutput output, boolean isForResultScreen)
      {
         ArrayList<ProcessSummaryLineInterface> lines = new ArrayList<>();
         if(ok != null)
         {
            ok.addSelfToListIfAnyCount(lines);
         }
         errors.values().forEach(line -> line.addSelfToListIfAnyCount(lines));
         return (lines);
      }
   }
}
