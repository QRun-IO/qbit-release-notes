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
import java.util.regex.Pattern;
import com.kingsrook.qbits.releasenotes.sources.ReleaseSource;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.qbits.QBitConfig;
import com.kingsrook.qqq.backend.module.rdbms.model.metadata.RDBMSBackendMetaData;


/*******************************************************************************
 ** Host configuration. Runtime choices (approval, audience, drafting) live in
 ** the release_notes_settings table so admins can change them.
 *******************************************************************************/
public class ReleaseNotesQBitConfig implements QBitConfig
{
   public static final String TABLE_NAME_PREFIX = "release_notes_";

   private static final Pattern RELEASE_VERSION = Pattern.compile("^\\d+\\.\\d+\\.\\d+$");

   private String backendName;
   private String appName = "releaseNotes";
   private String parentAppName;
   private String permissionBaseName = "release_notes";
   private String applicationName;
   private String applicationLabel;
   private String appVersion;
   private String appCommit;
   private String whatsNewPath = "/app/whats-new";
   private Boolean allowNonReleaseVersions = false;
   private List<ReleaseSource> sources = new ArrayList<>();



   /*******************************************************************************
    ** Validate backend and identity before registration.
    *******************************************************************************/
   @Override
   public void validate(QInstance qInstance, List<String> errors)
   {
      assertCondition(backendName != null && qInstance.getBackend(backendName) instanceof RDBMSBackendMetaData, "backendName must name an existing RDBMS backend", errors);
      assertCondition(appName != null && !appName.isBlank() && qInstance.getApp(appName) == null, "appName must be nonblank and unused", errors);
      assertCondition(permissionBaseName != null && !permissionBaseName.isBlank(), "permissionBaseName is required", errors);
      assertCondition(applicationName != null && !applicationName.isBlank(), "applicationName is required", errors);
      assertCondition(parentAppName == null || qInstance.getApp(parentAppName) != null, "parentAppName must name an existing app", errors);
      assertCondition(whatsNewPath != null && whatsNewPath.startsWith("/") && !whatsNewPath.startsWith("//"), "whatsNewPath must be an app path", errors);
   }



   /*******************************************************************************
    ** Default storage backend.
    *******************************************************************************/
   @Override
   public String getDefaultBackendNameForTables()
   {
      return (backendName);
   }



   /*******************************************************************************
    ** True when appVersion is one this qbit should record a release for.
    *******************************************************************************/
   public boolean isReleaseVersion()
   {
      if(appVersion == null || appVersion.isBlank())
      {
         return (false);
      }
      return (Boolean.TRUE.equals(allowNonReleaseVersions) || RELEASE_VERSION.matcher(appVersion).matches());
   }



   /*******************************************************************************
    ** The customer-facing application name (label, else name).
    *******************************************************************************/
   public String getEffectiveApplicationLabel()
   {
      return (applicationLabel != null && !applicationLabel.isBlank() ? applicationLabel : applicationName);
   }



   /*******************************************************************************
    ** Permission to edit releases, items and settings.
    *******************************************************************************/
   public String getManagePermissionName()
   {
      return (permissionBaseName + ".manage");
   }



   /*******************************************************************************
    ** Permission to publish or skip releases.
    *******************************************************************************/
   public String getPublishPermissionName()
   {
      return (permissionBaseName + ".publish");
   }



   /*******************************************************************************
    ** Every permission this qbit checks, for the host's admin role.
    *******************************************************************************/
   public List<String> getAdminPermissionNames()
   {
      return (List.of(getManagePermissionName(), getPublishPermissionName()));
   }



   /*******************************************************************************
    ** Fluent adder for a source.
    *******************************************************************************/
   public ReleaseNotesQBitConfig withSource(ReleaseSource source)
   {
      this.sources.add(source);
      return (this);
   }



   /*******************************************************************************
    ** Getter for backendName: The RDBMS backend that holds the tables.
    *******************************************************************************/
   public String getBackendName()
   {
      return (this.backendName);
   }



   /*******************************************************************************
    ** Setter for backendName.
    *******************************************************************************/
   public void setBackendName(String backendName)
   {
      this.backendName = backendName;
   }



   /*******************************************************************************
    ** Fluent setter for backendName.
    *******************************************************************************/
   public ReleaseNotesQBitConfig withBackendName(String backendName)
   {
      this.backendName = backendName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for appName: The admin app name.
    *******************************************************************************/
   public String getAppName()
   {
      return (this.appName);
   }



   /*******************************************************************************
    ** Setter for appName.
    *******************************************************************************/
   public void setAppName(String appName)
   {
      this.appName = appName;
   }



   /*******************************************************************************
    ** Fluent setter for appName.
    *******************************************************************************/
   public ReleaseNotesQBitConfig withAppName(String appName)
   {
      this.appName = appName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for parentAppName: Optional app to nest the admin app under.
    *******************************************************************************/
   public String getParentAppName()
   {
      return (this.parentAppName);
   }



   /*******************************************************************************
    ** Setter for parentAppName.
    *******************************************************************************/
   public void setParentAppName(String parentAppName)
   {
      this.parentAppName = parentAppName;
   }



   /*******************************************************************************
    ** Fluent setter for parentAppName.
    *******************************************************************************/
   public ReleaseNotesQBitConfig withParentAppName(String parentAppName)
   {
      this.parentAppName = parentAppName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for permissionBaseName: Prefix of the qbit's permissions.
    *******************************************************************************/
   public String getPermissionBaseName()
   {
      return (this.permissionBaseName);
   }



   /*******************************************************************************
    ** Setter for permissionBaseName.
    *******************************************************************************/
   public void setPermissionBaseName(String permissionBaseName)
   {
      this.permissionBaseName = permissionBaseName;
   }



   /*******************************************************************************
    ** Fluent setter for permissionBaseName.
    *******************************************************************************/
   public ReleaseNotesQBitConfig withPermissionBaseName(String permissionBaseName)
   {
      this.permissionBaseName = permissionBaseName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for applicationName: Stable key of the released application, e.g. kof22.
    *******************************************************************************/
   public String getApplicationName()
   {
      return (this.applicationName);
   }



   /*******************************************************************************
    ** Setter for applicationName.
    *******************************************************************************/
   public void setApplicationName(String applicationName)
   {
      this.applicationName = applicationName;
   }



   /*******************************************************************************
    ** Fluent setter for applicationName.
    *******************************************************************************/
   public ReleaseNotesQBitConfig withApplicationName(String applicationName)
   {
      this.applicationName = applicationName;
      return (this);
   }



   /*******************************************************************************
    ** Getter for applicationLabel: Customer-facing application name used in titles.
    *******************************************************************************/
   public String getApplicationLabel()
   {
      return (this.applicationLabel);
   }



   /*******************************************************************************
    ** Setter for applicationLabel.
    *******************************************************************************/
   public void setApplicationLabel(String applicationLabel)
   {
      this.applicationLabel = applicationLabel;
   }



   /*******************************************************************************
    ** Fluent setter for applicationLabel.
    *******************************************************************************/
   public ReleaseNotesQBitConfig withApplicationLabel(String applicationLabel)
   {
      this.applicationLabel = applicationLabel;
      return (this);
   }



   /*******************************************************************************
    ** Getter for appVersion: The running release version (X.Y.Z); null or a development version disables detection.
    *******************************************************************************/
   public String getAppVersion()
   {
      return (this.appVersion);
   }



   /*******************************************************************************
    ** Setter for appVersion.
    *******************************************************************************/
   public void setAppVersion(String appVersion)
   {
      this.appVersion = appVersion;
   }



   /*******************************************************************************
    ** Fluent setter for appVersion.
    *******************************************************************************/
   public ReleaseNotesQBitConfig withAppVersion(String appVersion)
   {
      this.appVersion = appVersion;
      return (this);
   }



   /*******************************************************************************
    ** Getter for appCommit: The running commit (internal only).
    *******************************************************************************/
   public String getAppCommit()
   {
      return (this.appCommit);
   }



   /*******************************************************************************
    ** Setter for appCommit.
    *******************************************************************************/
   public void setAppCommit(String appCommit)
   {
      this.appCommit = appCommit;
   }



   /*******************************************************************************
    ** Fluent setter for appCommit.
    *******************************************************************************/
   public ReleaseNotesQBitConfig withAppCommit(String appCommit)
   {
      this.appCommit = appCommit;
      return (this);
   }



   /*******************************************************************************
    ** Getter for whatsNewPath: The UI path of the What's new page, used in notification links.
    *******************************************************************************/
   public String getWhatsNewPath()
   {
      return (this.whatsNewPath);
   }



   /*******************************************************************************
    ** Setter for whatsNewPath.
    *******************************************************************************/
   public void setWhatsNewPath(String whatsNewPath)
   {
      this.whatsNewPath = whatsNewPath;
   }



   /*******************************************************************************
    ** Fluent setter for whatsNewPath.
    *******************************************************************************/
   public ReleaseNotesQBitConfig withWhatsNewPath(String whatsNewPath)
   {
      this.whatsNewPath = whatsNewPath;
      return (this);
   }



   /*******************************************************************************
    ** Getter for allowNonReleaseVersions: Detect versions that are not X.Y.Z (tests and demos only).
    *******************************************************************************/
   public Boolean getAllowNonReleaseVersions()
   {
      return (this.allowNonReleaseVersions);
   }



   /*******************************************************************************
    ** Setter for allowNonReleaseVersions.
    *******************************************************************************/
   public void setAllowNonReleaseVersions(Boolean allowNonReleaseVersions)
   {
      this.allowNonReleaseVersions = allowNonReleaseVersions;
   }



   /*******************************************************************************
    ** Fluent setter for allowNonReleaseVersions.
    *******************************************************************************/
   public ReleaseNotesQBitConfig withAllowNonReleaseVersions(Boolean allowNonReleaseVersions)
   {
      this.allowNonReleaseVersions = allowNonReleaseVersions;
      return (this);
   }



   /*******************************************************************************
    ** Getter for sources: Where release changes come from.
    *******************************************************************************/
   public List<ReleaseSource> getSources()
   {
      return (this.sources);
   }



   /*******************************************************************************
    ** Setter for sources.
    *******************************************************************************/
   public void setSources(List<ReleaseSource> sources)
   {
      this.sources = sources;
   }



   /*******************************************************************************
    ** Fluent setter for sources.
    *******************************************************************************/
   public ReleaseNotesQBitConfig withSources(List<ReleaseSource> sources)
   {
      this.sources = sources;
      return (this);
   }
}
