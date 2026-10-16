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


package com.kingsrook.qbits.releasenotes.drafting;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


/*******************************************************************************
 ** Guards customer-facing text: finds internal references (PR and issue
 ** numbers, GitHub links, commit hashes, ticket keys), personal data (e-mail
 ** and IP addresses), internal hosts and secret-looking values. Drafted items
 ** that fail are excluded; publishing is refused while included text fails.
 *******************************************************************************/
public final class ReleaseNotesSanitizer
{
   private static final Map<String, Pattern> RULES = new LinkedHashMap<>();

   static
   {
      RULES.put("a pull request or issue number", Pattern.compile("(?<![\\w&])#\\d+\\b"));
      RULES.put("a GitHub link", Pattern.compile("(?i)github\\.com"));
      RULES.put("a commit hash", Pattern.compile("\\b(?=[0-9a-f]*\\d)(?=[0-9a-f]*[a-f])[0-9a-f]{7,40}\\b"));
      RULES.put("a ticket key", Pattern.compile("\\b[A-Z][A-Z0-9]{1,9}-\\d{1,6}\\b"));
      RULES.put("an e-mail address", Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.-]+"));
      RULES.put("an IP address", Pattern.compile("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b"));
      RULES.put("an internal host name", Pattern.compile("(?i)\\b(localhost|[\\w-]+\\.(lan|internal|local|svc|cluster\\.local))\\b"));
      RULES.put("a secret", Pattern.compile("(?i)(\\b(api[_-]?key|token|secret|password|passwd)\\s*[:=]\\s*\\S+|AKIA[0-9A-Z]{16}|\\bgh[pousr]_[A-Za-z0-9]{20,}|\\bsk-[A-Za-z0-9_-]{20,}|-----BEGIN)"));
   }



   /*******************************************************************************
    ** Utility class.
    *******************************************************************************/
   private ReleaseNotesSanitizer()
   {
   }



   /*******************************************************************************
    ** What in the text must not reach customers (empty when clean).
    *******************************************************************************/
   public static List<String> problems(String text)
   {
      List<String> problems = new ArrayList<>();
      if(text == null || text.isBlank())
      {
         return (problems);
      }
      for(Map.Entry<String, Pattern> rule : RULES.entrySet())
      {
         if(rule.getValue().matcher(text).find())
         {
            problems.add("contains " + rule.getKey());
         }
      }
      return (problems);
   }



   /*******************************************************************************
    ** What is wrong with a customer-facing link (empty when it is an app path
    ** or an https URL that is not a GitHub link).
    *******************************************************************************/
   public static List<String> linkProblems(String url)
   {
      List<String> problems = new ArrayList<>();
      if(url == null || url.isBlank())
      {
         return (problems);
      }
      boolean appPath = url.startsWith("/") && !url.startsWith("//");
      boolean https   = url.startsWith("https://") && url.length() > 8;
      if(!(appPath || https) || url.contains("\\") || url.chars().anyMatch(c -> Character.isWhitespace(c) || Character.isISOControl(c)))
      {
         problems.add("link must be an app path or an https URL");
      }
      if(url.toLowerCase().contains("github.com"))
      {
         problems.add("link points at GitHub");
      }
      return (problems);
   }
}
