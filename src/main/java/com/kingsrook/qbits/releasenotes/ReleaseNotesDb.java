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


import java.io.Serializable;
import java.util.List;
import com.kingsrook.qqq.backend.core.actions.tables.DeleteAction;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.actions.tables.QueryAction;
import com.kingsrook.qqq.backend.core.actions.tables.UpdateAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.delete.DeleteInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QueryInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.update.UpdateInput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Small table helpers over the standard QQQ actions (current context).
 *******************************************************************************/
public final class ReleaseNotesDb
{

   /*******************************************************************************
    ** Utility class.
    *******************************************************************************/
   private ReleaseNotesDb()
   {
   }



   /*******************************************************************************
    ** Query a table.
    *******************************************************************************/
   public static List<QRecord> query(String tableName, QQueryFilter filter) throws QException
   {
      return (new QueryAction().execute(new QueryInput(tableName).withFilter(filter)).getRecords());
   }



   /*******************************************************************************
    ** The first matching row, or null.
    *******************************************************************************/
   public static QRecord first(String tableName, QQueryFilter filter) throws QException
   {
      List<QRecord> records = query(tableName, filter.withLimit(1));
      return (records.isEmpty() ? null : records.get(0));
   }



   /*******************************************************************************
    ** A row by id, or null.
    *******************************************************************************/
   public static QRecord byId(String tableName, Serializable id) throws QException
   {
      return (id == null ? null : first(tableName, eq("id", id)));
   }



   /*******************************************************************************
    ** Insert one row; throws on any record error.
    *******************************************************************************/
   public static QRecord insert(String tableName, QRecord record) throws QException
   {
      QRecord result = new InsertAction().execute(new InsertInput(tableName).withRecord(record)).getRecords().get(0);
      throwIfErrors(tableName, result);
      return (result);
   }



   /*******************************************************************************
    ** Insert rows; throws on any record error.
    *******************************************************************************/
   public static void insertAll(String tableName, List<QRecord> records) throws QException
   {
      if(records.isEmpty())
      {
         return;
      }
      for(QRecord result : new InsertAction().execute(new InsertInput(tableName).withRecords(records)).getRecords())
      {
         throwIfErrors(tableName, result);
      }
   }



   /*******************************************************************************
    ** Update one row; throws on any record error.
    *******************************************************************************/
   public static void update(String tableName, QRecord record) throws QException
   {
      QRecord result = new UpdateAction().execute(new UpdateInput(tableName).withRecord(record)).getRecords().get(0);
      throwIfErrors(tableName, result);
   }



   /*******************************************************************************
    ** Delete matching rows.
    *******************************************************************************/
   public static void delete(String tableName, QQueryFilter filter) throws QException
   {
      new DeleteAction().execute(new DeleteInput(tableName).withQueryFilter(filter));
   }



   /*******************************************************************************
    ** field = value.
    *******************************************************************************/
   public static QQueryFilter eq(String field, Serializable value)
   {
      return (new QQueryFilter(new QFilterCriteria(field, QCriteriaOperator.EQUALS, value)));
   }



   /*******************************************************************************
    ** Throw the first record error.
    *******************************************************************************/
   private static void throwIfErrors(String tableName, QRecord record) throws QException
   {
      if(record.getErrors() != null && !record.getErrors().isEmpty())
      {
         throw (new QException("Could not write " + tableName + ": " + record.getErrors().get(0).getMessage()));
      }
   }
}
