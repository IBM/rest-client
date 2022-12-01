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

public class SKLMRESTObjectsOpaque extends SKLMRESTCommand
{
   private static final Logger logger = LogManager.getLogger(SKLMRESTObjectsOpaque.class.getName());

   private static String path   = "/SKLM/rest/v1/objects/opaque";
   private String type          = "POST";
							   
   public SKLMRESTObjectsOpaque(String theRESTURL, String arguments, String userAuthId)
   {
      logger.traceEntry();	  
      respCode = executeRESTCommand(theRESTURL, arguments, userAuthId, path, type);
   }
   /*
   {
	  "clientName": "string",
	  "keyBlock": {
	    "keyMaterial": "string"
	  }
	}
    */  
   //Build as normal, but before you're done, go back and add the block
   public String doBodyArgs()
   {
	  logger.traceEntry();
	        
      String jsonInputString="{";
      String jsonInputStringInner="\"keyBlock\": {";
      int i1=0;
      int i2=0;
	  for (String arg: ReadParsedSKLMYaml.getInstance().getBodyArgs())
	  {	    	  
	   	 String[] vals = arg.split("=");
	   	 switch (vals[0])
	   	 {
	   	    case "keyMaterial":
		    if (i1!=0)
		       jsonInputStringInner+=",";
			i1++;
		    jsonInputStringInner+="\""+vals[0]+"\": \""+vals[1]+"\"";			     
	   		break;
	   	 default:
		     if (i2!=0)
		        jsonInputString+=",";
			 i2++;			     
			 jsonInputString+="\""+vals[0]+"\": \""+vals[1]+"\"";
	   		 break;
	   	 }
	  }
	  if (i2!=0) //It should not be 0...
		  jsonInputString += ",";
	  
	  if (i1!=0)
	     jsonInputString += jsonInputStringInner + "}";
	  
      jsonInputString += "}";
	      
      return jsonInputString;
   }  
}