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

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.klm.rest.ReadParsedSKLMYaml;

public class SKLMRESTServerKerberosConfigureOnMM extends SKLMRESTCommand
{
/*
	[
	  {
	    "userId": "string",
	    "password": "string",
	    "wasUserName": "string",
	    "wasUserPassword": "string"
	  },
	  {  <---This section can repeat
	    "ipHostName": "string",
	    "dbServiceName": "string"
	  }  (, if repeated)
	]
*/

   private static final Logger logger = LogManager.getLogger(SKLMRESTServerKerberosConfigureOnMM.class.getName());

   private static String path   = "/SKLM/rest/v1/ckms/kerberos/configureOnMM";
   private String type          = "POST";
							   
   public SKLMRESTServerKerberosConfigureOnMM(String theRESTURL, String arguments, String userAuthId)
   {
      logger.traceEntry();	  
      respCode = executeRESTCommand(theRESTURL, arguments, userAuthId, path, type);
   }
   
   public boolean checkArguments(String path, String type, String arguments)
   {
	   logger.traceEntry();
	   //build repeatables, pass in as ArrayList
       ArrayList<String> repeatableAttributes = new ArrayList<String>();
	   repeatableAttributes.add("ipHostName");
	   repeatableAttributes.add("dbServiceName");
	    	
	   return ReadParsedSKLMYaml.getInstance().checkArgumentsWithRepeatables(path, type, arguments, repeatableAttributes);
   }
   
   public String doBodyArgs()
   {
	  logger.traceEntry();
	  
      String jsonInputString = "["; 
      String jsonInputStringInner1="{";
      String jsonInputStringInner2="{";
      int i1=0;
      int i2=0;
	  for (String arg: ReadParsedSKLMYaml.getInstance().getBodyArgs())
	  {	    	  
	   	 String[] vals = arg.split("=");
	   	 switch (vals[0])
	   	 {
	   	    case "userId":
	   	    case "password":
	   	    case "wasUserName":
	   	    case "wasUserPassword":
		     if (i1!=0)
		    	jsonInputStringInner1+=",";
			 i1++;
			 jsonInputStringInner1+="\""+vals[0]+"\": \""+vals[1]+"\"";			     
	   		 break;
	   	 default:
		     if (i2!=0)
		        jsonInputStringInner2+=",";
			 i2++;			     
			 jsonInputStringInner2+="\""+vals[0]+"\": \""+vals[1]+"\"";
	   		 break;
	   	 }

	  }
	  jsonInputString += jsonInputStringInner1 + "}," + jsonInputStringInner2 + "}]";
	      
      return jsonInputString;
   }
}