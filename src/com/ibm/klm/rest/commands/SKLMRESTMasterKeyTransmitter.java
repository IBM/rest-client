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

public class SKLMRESTMasterKeyTransmitter extends SKLMRESTCommand
{
/*
 	[
	  {
	    "ipHostname": "string",
	    "httpPort": "string",
	    "sklmUsername": "string",
	    "sklmPassword": "string"
	  }
	]			
			[
			 {
			    "ipHostname"   : "sklms1",  
			    "httpPort"     : "9443",
			    "sklmUsername" : "sklmadmin",
			    "sklmPassword" : "SKLM@admin123"
			 },
			 {  "ipHostname"   : "sklms2",  
			    "httpPort"     : "9443",
			    "sklmUsername" : "sklmadmin",
			    "sklmPassword" : "SKLM@admin123"
			  }
			]
*/
   private static final Logger logger = LogManager.getLogger(SKLMRESTMasterKeyTransmitter.class.getName());

   private static String path   = "/SKLM/rest/v1/ckms/masterKey/transmitter";
   private String type          = "POST";

   public SKLMRESTMasterKeyTransmitter(String theRESTURL, String arguments, String userAuthId)
   {
      logger.traceEntry();	  
      respCode = executeRESTCommand(theRESTURL, arguments, userAuthId, path, type);
   }
	   
   public boolean checkArguments(String path, String type, String arguments)
   {
	   //build repeatables, pass in as ArrayList
	   ArrayList<String> repeatableAttributes = new ArrayList<String>();
	   repeatableAttributes.add("ipHostname");
	   repeatableAttributes.add("httpPort");
	   repeatableAttributes.add("sklmUsername");
	   repeatableAttributes.add("sklmPassword");
		    	
	   return ReadParsedSKLMYaml.getInstance().checkArgumentsWithRepeatables(path, type, arguments, repeatableAttributes);
   }
	   
   public String doBodyArgs()
   {
	  logger.traceEntry();
		  
      String jsonInputString = "["; 
      ArrayList<String> argSections = new ArrayList<String>();
      int nextPos = 0;
      //argSections.add(0,"{");
      
      //Build argument sections
	  for (String arg: ReadParsedSKLMYaml.getInstance().getBodyArgs())
	  {	    	  
         boolean inserted=false;
		 String[] vals = arg.split("=");
         for (int i=0; i<nextPos; i++)
         {
        	 if (argSections.get(i) == null || argSections.get(i).contains(vals[0]))
        		 continue;
                argSections.set(i,argSections.get(i) + "\""+vals[0]+"\": \""+vals[1]+"\",");      		 
             inserted=true;
         }
         if (!inserted)
         {
        	 argSections.add("\""+vals[0]+"\": \""+vals[1]+"\",");
        	 nextPos++;
         }
	  }
	  
	  //Now put arg sections back together in a json string	  
	  for (int x=0; x<nextPos; x++)
	  {
		  if (x > 0)
			  jsonInputString += ",";
		  jsonInputString += "{";
          String tmp = argSections.get(x);				  
	      jsonInputString += tmp.substring(0, tmp.length() - 1);
	      jsonInputString += "}";		  
	  }
	  jsonInputString += "]";
      
      return jsonInputString;
   }	
}