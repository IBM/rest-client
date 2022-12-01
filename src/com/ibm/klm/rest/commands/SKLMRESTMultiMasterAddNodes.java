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

/*
[
  {
    "clusterName": "string",
    "primaryHadrPort": "string"
  },
  {
    "type": "string",
    "httpPort": "string",
    "ipHostname": "string",
    "sklmUsername": "string",
    "sklmPassword": "string",
    "wasUsername": "string",
    "wasPassword": "string",
    "standbyPriorityIndex": "string",
    "autoaccept": "string"
  }
]
 */
public class SKLMRESTMultiMasterAddNodes extends SKLMRESTCommand 
{
   private static final Logger logger = LogManager.getLogger(SKLMRESTMultiMasterAddNodes.class.getName());

   private static String path   = "/SKLM/rest/v1/ckms/config/nodes/addNodes";
   private String type          = "POST";
					   
   public SKLMRESTMultiMasterAddNodes(String theRESTURL, String arguments, String userAuthId)
   {
      logger.traceEntry();	  
      respCode = executeRESTCommand(theRESTURL, arguments, userAuthId, path, type);
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
	   	 case "clusterName":
	   	 case "primaryHadrPort":
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