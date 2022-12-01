/**
 * (C) Copyright IBM Corp. 2022.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.klm.rest.commands;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.klm.rest.ReadParsedSKLMYaml;

public class SKLMRESTMultiMasterTakeoverAsPrimary extends SKLMRESTCommand
{
/*
 * Note that this is one of a handful off commands that requires am array to be sent to the REST server,
 * so it is being handled as a special case.

 [
  {
    "clusterName": "string"
  },
  {
    "ipHostname": "string"
  }
]	
 */
   private static final Logger logger = LogManager.getLogger(SKLMRESTMultiMasterTakeoverAsPrimary.class.getName());

   private static String path   = "/SKLM/rest/v1/ckms/config/nodes/takeoverAsPrimary";
   private String type          = "POST";
				   
   public SKLMRESTMultiMasterTakeoverAsPrimary(String theRESTURL, String arguments, String userAuthId)
   {
      logger.traceEntry();	  
      respCode = executeRESTCommand(theRESTURL, arguments, userAuthId, path, type);      
   }
   
   
   //This is the first one of these we've overridden....try to still abstract as much as possible
   public String doBodyArgs()
   {
	  logger.traceEntry();
	  
      String jsonInputString = "["; 
      int i=0;
	  for (String arg: ReadParsedSKLMYaml.getInstance().getBodyArgs())
	  {	    	  
	   	 String[] vals = arg.split("=");
	     if (i!=0)
	        jsonInputString+=",";
	     i++;
	     jsonInputString+="{";
	     jsonInputString+="\""+vals[0]+"\": \""+vals[1]+"\"";
	     jsonInputString+="}";
	  }
	  jsonInputString+="]";
	      
      return jsonInputString;
   }
}