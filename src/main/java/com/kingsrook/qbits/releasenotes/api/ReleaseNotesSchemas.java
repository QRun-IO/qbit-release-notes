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


package com.kingsrook.qbits.releasenotes.api;


import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.kingsrook.qqq.openapi.model.Schema;
import com.kingsrook.qqq.openapi.model.Type;


/*******************************************************************************
 ** Versioned response schemas. Only customer-facing fields: internal
 ** references never leave the admin tables.
 *******************************************************************************/
public final class ReleaseNotesSchemas
{

   /*******************************************************************************
    ** Utility class.
    *******************************************************************************/
   private ReleaseNotesSchemas()
   {
   }



   /*******************************************************************************
    ** All component schemas.
    *******************************************************************************/
   public static Map<String, Schema> all()
   {
      Map<String, Schema> schemas = new TreeMap<>();
      schemas.put("ReleaseNotesErrorV1", object(Map.of("error", text("What went wrong."))));
      schemas.put("ReleaseNotesItemV1", object(Map.of(
         "category", new Schema().withType(Type.STRING).withEnumValues(List.of("NEW", "IMPROVED", "FIXED", "SECURITY", "REMOVED")).withDescription("Item category."),
         "categoryLabel", text("Category label, e.g. New."),
         "text", text("Customer-facing text."),
         "linkUrl", text("Optional app path or https link.").withNullable(true),
         "screenshotUrl", text("Optional screenshot URL.").withNullable(true))));
      schemas.put("ReleaseNotesReleaseV1", object(Map.of(
         "id", integer("Release id."),
         "version", text("Application version."),
         "title", text("Release title."),
         "summary", text("Short summary.").withNullable(true),
         "publishedAt", text("When it was published (ISO-8601)."),
         "isNew", new Schema().withType(Type.BOOLEAN).withDescription("Published after the user last looked."),
         "items", array(ref("ReleaseNotesItemV1"), "Included items in category order."))));
      schemas.put("ReleaseNotesReleasesV1", object(Map.of(
         "releases", array(ref("ReleaseNotesReleaseV1"), "This page of releases, newest first."),
         "total", integer("Visible published releases."),
         "unreadCount", integer("Releases new to the user."),
         "lastSeenAt", text("When the user last marked What's new as seen.").withNullable(true))));
      schemas.put("ReleaseNotesLatestV1", object(Map.of(
         "id", integer("Release id."),
         "version", text("Application version."),
         "title", text("Release title."),
         "publishedAt", text("When it was published (ISO-8601)."))));
      schemas.put("ReleaseNotesSummaryV1", object(Map.of(
         "unreadCount", integer("Releases new to the user."),
         "latest", ref("ReleaseNotesLatestV1").withNullable(true).withDescription("The newest visible release, or null."))));
      schemas.put("ReleaseNotesSeenV1", object(Map.of("unreadCount", integer("Always 0 after marking seen."))));
      return (schemas);
   }



   /*******************************************************************************
    ** An object with described properties.
    *******************************************************************************/
   private static Schema object(Map<String, Schema> properties)
   {
      return (new Schema().withType(Type.OBJECT).withProperties(new TreeMap<>(properties)));
   }



   /*******************************************************************************
    ** A described string.
    *******************************************************************************/
   private static Schema text(String description)
   {
      return (new Schema().withType(Type.STRING).withDescription(description));
   }



   /*******************************************************************************
    ** A described integer.
    *******************************************************************************/
   private static Schema integer(String description)
   {
      return (new Schema().withType(Type.INTEGER).withDescription(description));
   }



   /*******************************************************************************
    ** A described array.
    *******************************************************************************/
   private static Schema array(Schema items, String description)
   {
      return (new Schema().withType(Type.ARRAY).withItems(items).withDescription(description));
   }



   /*******************************************************************************
    ** A component reference.
    *******************************************************************************/
   private static Schema ref(String name)
   {
      return (new Schema().withRefToSchema(name));
   }
}
