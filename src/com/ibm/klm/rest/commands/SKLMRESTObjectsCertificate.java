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

public class SKLMRESTObjectsCertificate extends SKLMRESTCommand
{
   private static final Logger logger = LogManager.getLogger(SKLMRESTObjectsCertificate.class.getName());

   private static String path   = "/SKLM/rest/v1/objects/certificate";
   private String type          = "POST";
							   
   public SKLMRESTObjectsCertificate(String theRESTURL, String arguments, String userAuthId)
   {
      logger.traceEntry();	  
      respCode = executeRESTCommand(theRESTURL, arguments, userAuthId, path, type);
   }
 
   /*
   {
   	  "clientName": "string",
   	  "prefixName": "string",
   	  "algorithm": "string",
   	  "bitLength": "string",
   	  "certCryptoUsageMask": "string",
   	  "publicKeyCryptoUsageMask": "string",
   	  "certificateBlock": {
   	    "certFormat": "string",
   	    "certMaterial": "string"
   	  },
   	  "publicKeyBlock": {
   	    "publicKeyFormat": "string",
   	    "publicKeyMaterial": "string"
   	  }
   	}
   */   
   //Build as normal, but before you're done, go back and add the block
   public String doBodyArgs()
   {
	  logger.traceEntry();
	        
      String jsonInputString="{";
      String jsonInputStringInner1="\"certificateBlock\": {";
      String jsonInputStringInner2="\"publicKeyBlock\": {";
      int i1=0;
      int i2=0;
      int i3=0;
	  for (String arg: ReadParsedSKLMYaml.getInstance().getBodyArgs())
	  {	    	  
	   	 String[] vals = arg.split("=");
	   	 switch (vals[0])
	   	 {
	   	    case "certFormat":
	   	    case "certMaterial":
		       if (i1!=0)
		    	  jsonInputStringInner1+=",";
			   i1++;
			   jsonInputStringInner1+="\""+vals[0]+"\": \""+vals[1]+"\"";			     
	   		   break;
	   	    case "publicKeyFormat":
	   	    case "publicKeyMaterial":
			    if (i2!=0)
				   jsonInputStringInner2+=",";
				i2++;
				jsonInputStringInner2+="\""+vals[0]+"\": \""+vals[1]+"\"";			     	   	    	
	   	    	break;
	   	    default:
		     if (i3!=0)
		        jsonInputString+=",";
			 i3++;			     
			 jsonInputString+="\""+vals[0]+"\": \""+vals[1]+"\"";
	   		 break;
	   	 }

	  }
	  //if (i3!=0) //It should not be 0...
		//  jsonInputString += ",";
		  
	  //jsonInputString += jsonInputStringInner1 + "},";
	  //jsonInputString += jsonInputStringInner2 + "}";
	  //jsonInputString += "}";
	  boolean addComma=false;
	  
	  if (i3!=0) //It should not be 0...
		  addComma=true;
	  
	  if (i1!=0)
	  {
		  if (addComma==true)
			  jsonInputString += ",";  //Keep as true for next block
		  jsonInputString += jsonInputStringInner1 + "}";
		  addComma=true;
	  }
	  
	  if (i2!=0)
	  {
		  if (addComma==true)
			  jsonInputString += ",";  //Keep as true for next block
		  
	     jsonInputString += jsonInputStringInner2 + "}";
	  }
	  
	  jsonInputString += "}";
	      
      return jsonInputString;
   }  
}