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


import java.util.List;
import java.util.Map;
import com.kingsrook.qbits.releasenotes.api.ReleasesSpec;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesItem;
import com.kingsrook.qbits.releasenotes.model.ReleaseNotesRelease;
import com.kingsrook.qbits.releasenotes.processes.CheckForReleaseStep;
import com.kingsrook.qbits.releasenotes.processes.ReleaseActionSteps;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;
import com.kingsrook.qqq.backend.core.model.actions.processes.Status;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qbits.releasenotes.ReleaseNotesTestSupport.NOTIFICATIONS;
import static com.kingsrook.qbits.releasenotes.ReleaseNotesTestSupport.pr;
import static com.kingsrook.qbits.releasenotes.ReleaseNotesTestSupport.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


/*******************************************************************************
 ** Publish, Skip, Redraft and Check for new release as record actions.
 *******************************************************************************/
class ReleaseActionsTest
{

   /*******************************************************************************
    ** Review then result for Publish; blockers are grouped by reason.
    *******************************************************************************/
   @Test
   void publishAction() throws Exception
   {
      QInstance instance = ReleaseNotesTestSupport.instance("1.0.0", List.of(pr(1, "feat: calendars", "")));
      Integer   good     = ReleaseNotesRuntime.of(instance).service().detect();
      QRecord   release  = ReleaseNotesDb.byId(ReleaseNotesRelease.TABLE_NAME, good);
      QRecord   leaky    = new QRecord().withValue("id", 777).withValue("status", "DRAFT").withValue("title", "Fixed #4");

      QContext.setQSession(user("admin-7", "release_notes.publish", "release_notes.manage"));
      RunBackendStepInput input = new RunBackendStepInput();
      input.setRecords(List.of(release, leaky));
      input.addValue(ReleaseActionSteps.ACTION, ReleaseActionSteps.PUBLISH);

      ReleaseActionSteps.Transform transform = new ReleaseActionSteps.Transform();
      RunBackendStepOutput         preview   = new RunBackendStepOutput();
      transform.runOnePage(input, preview);
      List<ProcessSummaryLineInterface> review = transform.getProcessSummary(preview, false);
      assertThat(review).anySatisfy(line -> assertThat(((ProcessSummaryLine) line).getStatus()).isEqualTo(Status.OK));
      assertThat(review).anySatisfy(line -> assertThat(((ProcessSummaryLine) line).getPluralFutureMessage()).contains("pull request or issue number"));
      assertThat(preview.getRecords()).hasSize(1);

      input.setRecords(preview.getRecords());
      ReleaseActionSteps.Load load   = new ReleaseActionSteps.Load();
      RunBackendStepOutput    result = new RunBackendStepOutput();
      load.runOnePage(input, result);
      ProcessSummaryLine done = (ProcessSummaryLine) load.getProcessSummary(result, true).get(0);
      assertThat(done.getCount()).isEqualTo(1);
      assertThat(done.getSingularPastMessage()).isEqualTo("release was published");
      assertThat(ReleaseNotesDb.byId(ReleaseNotesRelease.TABLE_NAME, good).getValueString("publishedBy")).isEqualTo("admin-7");
      assertThat(NOTIFICATIONS).hasSize(1);

      load = new ReleaseActionSteps.Load();
      load.runOnePage(input, result);
      assertThat(load.getProcessSummary(result, true)).anySatisfy(line -> assertThat(((ProcessSummaryLine) line).getStatus()).isEqualTo(Status.ERROR));
   }



   /*******************************************************************************
    ** Skip and Redraft; permissions; unknown actions.
    *******************************************************************************/
   @Test
   void skipRedraftAndPermissions() throws Exception
   {
      QInstance instance = ReleaseNotesTestSupport.instance("1.0.0", List.of(pr(1, "feat: calendars", "")));
      Integer   id       = ReleaseNotesRuntime.of(instance).service().detect();
      ReleaseNotesDb.delete(ReleaseNotesItem.TABLE_NAME, ReleaseNotesDb.eq("releaseId", id));

      RunBackendStepInput input = new RunBackendStepInput();
      input.setRecords(List.of(ReleaseNotesDb.byId(ReleaseNotesRelease.TABLE_NAME, id)));
      input.addValue(ReleaseActionSteps.ACTION, ReleaseActionSteps.REDRAFT);
      QContext.setQSession(user("viewer", "release_notes.publish"));
      assertThatThrownBy(() -> new ReleaseActionSteps.Transform().runOnePage(input, new RunBackendStepOutput())).hasMessageContaining("release_notes.manage");

      QContext.setQSession(user("admin", "release_notes.publish", "release_notes.manage"));
      new ReleaseActionSteps.Load().runOnePage(input, new RunBackendStepOutput());
      assertThat(ReleaseNotesDb.query(ReleaseNotesItem.TABLE_NAME, ReleaseNotesDb.eq("releaseId", id))).hasSize(1);

      input.addValue(ReleaseActionSteps.ACTION, ReleaseActionSteps.SKIP);
      ReleaseActionSteps.Transform transform = new ReleaseActionSteps.Transform();
      RunBackendStepOutput         preview   = new RunBackendStepOutput();
      transform.runOnePage(input, preview);
      assertThat(((ProcessSummaryLine) transform.getProcessSummary(preview, false).get(0)).getSingularFutureMessage()).isEqualTo("release will be skipped");
      new ReleaseActionSteps.Load().runOnePage(input, new RunBackendStepOutput());
      assertThat(ReleaseNotesDb.byId(ReleaseNotesRelease.TABLE_NAME, id).getValueString("status")).isEqualTo("SKIPPED");

      input.setRecords(List.of(ReleaseNotesDb.byId(ReleaseNotesRelease.TABLE_NAME, id)));
      ReleaseActionSteps.Transform again = new ReleaseActionSteps.Transform();
      again.runOnePage(input, new RunBackendStepOutput());
      assertThat(again.getProcessSummary(new RunBackendStepOutput(), false)).anySatisfy(line -> assertThat(((ProcessSummaryLine) line).getStatus()).isEqualTo(Status.ERROR));

      input.addValue(ReleaseActionSteps.ACTION, "EXPLODE");
      assertThatThrownBy(() -> new ReleaseActionSteps.Load().runOnePage(input, new RunBackendStepOutput())).hasMessageContaining("Unknown");
   }



   /*******************************************************************************
    ** Check for new release reports what it did.
    *******************************************************************************/
   @Test
   void checkForRelease() throws Exception
   {
      ReleaseNotesTestSupport.instance("4.5.6", List.of());
      QContext.setQSession(user("admin", "release_notes.manage"));
      RunBackendStepOutput first = new RunBackendStepOutput();
      new CheckForReleaseStep().run(new RunBackendStepInput(), first);
      assertThat(first.getValues().get("processResults").toString()).contains("release recorded as a draft: 4.5.6");
      RunBackendStepOutput second = new RunBackendStepOutput();
      new CheckForReleaseStep().run(new RunBackendStepInput(), second);
      assertThat(second.getValues().get("processResults").toString()).contains("already recorded");

      ReleaseNotesTestSupport.instance("dev", List.of());
      QContext.setQSession(user("admin", "release_notes.manage"));
      RunBackendStepOutput dev = new RunBackendStepOutput();
      new CheckForReleaseStep().run(new RunBackendStepInput(), dev);
      assertThat(dev.getValues().get("processResults").toString()).contains("not a release version");

      QContext.setQSession(user("nobody"));
      assertThatThrownBy(() -> new CheckForReleaseStep().run(new RunBackendStepInput(), new RunBackendStepOutput())).hasMessageContaining("permission");
   }



   /*******************************************************************************
    ** Background detection and the HTTP handlers' paging rules.
    *******************************************************************************/
   @Test
   void backgroundAndApi() throws Exception
   {
      QInstance instance = ReleaseNotesTestSupport.instance("7.0.0", List.of(pr(1, "feat: calendars", "")));
      ReleaseNotesRuntime.of(instance).detectInBackground().join(30000);
      assertThat(ReleaseNotesDb.query(ReleaseNotesRelease.TABLE_NAME, ReleaseNotesDb.eq("version", "7.0.0"))).hasSize(1);
      Map<String, Object> summary = ReleaseNotesRuntime.of(instance).service().summary(user("u"));
      assertThat(summary.get("latest")).isNull();
      assertThat(new ReleasesSpec().defineBasicOperation().getPath()).isEqualTo("/qbit/release-notes/releases");
   }
}
