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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.klm.rest.ReadParsedSKLMYaml;
import com.ibm.klm.rest.SKLMJSONParser;

public class SKLMRESTAuthToken extends SKLMRESTCommand
{
	
   private static final Logger logger = LogManager.getLogger(SKLMRESTAuthToken.class.getName());
   
   private String authTokenResp = null;
   //private int    respCode      = -1;  
   private boolean isOK         = false;   
   private static String path   = "/SKLM/rest/v1/ckms/login";
   private String theAuthToken  = null;
   private String type          = "POST";

      
   //This one is kind of a special case since we use it at login...we'll make 2 constructors
   public SKLMRESTAuthToken(String theRESTURL, String sklmAdmin, String sklmAdminPwd)
   {
	  logger.traceEntry();
      respCode = -1;
	  isOK = false;
	  logger.debug("Getting token from url:"+theRESTURL+", path:"+path);
	  HttpsURLConnection conn = null;
      try {
        URL url = new URL(theRESTURL+path);
        conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod(type);
        conn.setRequestProperty("Accept-Language", "en");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);  //Enable writing to the output stream??

        String jsonInputString = "{\"userid\": \""+sklmAdmin+"\", \"password\": \""+sklmAdminPwd+"\"}";
        logger.debug("JSONString="+jsonInputString);        
        OutputStream os = conn.getOutputStream();
        byte[] input = jsonInputString.getBytes("utf-8");
        os.write(input, 0, input.length);
        
		respCode = conn.getResponseCode();		
        if (respCode == 200) {		   
           BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		   
		   isOK=true;
           String output;
           authTokenResp="";
		   
           while ((output = br.readLine()) != null) {
              authTokenResp+=output;
           }
		   
           logger.debug("Output from Server: "+authTokenResp);
           
           //Now get the token...
   		   SKLMJSONParser parser = new SKLMJSONParser(authTokenResp);
   	       HashMap<String, String> keyVals = parser.getKeyValuePairs();
   	  
   	       for (String i : keyVals.keySet()) {
   	          logger.debug("key: " + i + ", value: " + keyVals.get(i));
   		   }
   		   //Could just get("UserAuthId")
   		   theAuthToken = keyVals.get(keyVals.keySet().toArray()[0]);
   		
   		   logger.debug("SKLMRESTAuthToken-theAuthToken is:"+theAuthToken);

        }
        else
		{
			readErrorResponseFromServer(conn, respCode);
		}
        conn.disconnect();
		

      } catch (MalformedURLException e) 
        { 
    	    e.printStackTrace();
    	    System.out.println("Malformed URL: "+e.getMessage());
    	    logger.error("Malformed URL: "+e.getMessage());
        } 
        catch (ConnectException ce)
        {
        	System.out.println("Connection exception.  Is SKLM up and running on the server?");
        	System.out.println("Message: "+ce.getMessage());
        	logger.error("Connection exception: "+ce.getMessage());
        }
	    catch (SSLHandshakeException she)           
        {
	    	logger.error("SSL Handshake exception: " + she.getMessage());
	    	System.out.println("SSL Handshake exception: "+she.getMessage());
	    	conn.disconnect();
        }
	    catch (IOException ioe)           
        {
	    	logger.debug("IO exception: "+ioe.getMessage());
	    	//May still be able to get a response here...
	    	readErrorResponseFromServer(conn, respCode);
	    	conn.disconnect();
        }
   }
   
   public SKLMRESTAuthToken(String theRESTURL, String arguments)
   {
	  logger.traceEntry();
      respCode = -1;
	  isOK = false;
	  
	  if (arguments!=null && arguments.trim().equals("help"))
	  {
		  printHelp(path, type);
		  return;
	  }
	  
      if (ReadParsedSKLMYaml.getInstance().checkArguments(path, type, arguments)==false)
    	  return;
      
      HttpsURLConnection conn = null;
      
      try {
    	//Now we parse args
    	ReadParsedSKLMYaml.getInstance().buildArgumentLists(path, type, arguments);
        logger.debug("Arguments: "+arguments);
        //There should only be one of these if it exists
        String newPath = path;
        for (String arg: ReadParsedSKLMYaml.getInstance().getPathArgs())
        {
        	String[] vals = arg.split("=");
        	newPath.replace("\\{"+vals[0]+"\\}", vals[1]);        	
        }
        
        int i=0;
        String queryString="";
        for (String arg: ReadParsedSKLMYaml.getInstance().getQueryArgs())
        {
        	if (i==0)
        		queryString+="?";
        	else
        		queryString+="&";
        	i++;
        	queryString+=arg;
        }

        logger.debug("URL:"+theRESTURL+newPath+queryString);

        URL url = new URL(theRESTURL+newPath+queryString);
        conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod(type);
        //conn.setRequestProperty("Accept-Language", "en"); //take out
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        for (String arg: ReadParsedSKLMYaml.getInstance().getHeaderArgs())
        {
        	String[] vals = arg.split("=");
        	conn.setRequestProperty(vals[0],  vals[1]);
        	logger.debug("Added:"+vals[0]+","+vals[1]+" to header.");
        }
        String jsonInputString = ""; 
        i=0;
        for (String arg: ReadParsedSKLMYaml.getInstance().getBodyArgs())
        {
        	String[] vals = arg.split("=");
        	if (i==0)
        		jsonInputString+="{";
        	else
        		jsonInputString+=", ";
        	i++;
        	jsonInputString+="\""+vals[0]+"\": \""+vals[1]+"\"";
        }
        if (i!=0)
        	jsonInputString+="}";
        logger.debug("jsonInputString="+jsonInputString);

        conn.setDoOutput(true);  //Enable writing to the output stream
        OutputStream os = conn.getOutputStream();
        
        byte[] input = jsonInputString.getBytes("utf-8");       
        os.write(input, 0, input.length);
		respCode = conn.getResponseCode();

        if (respCode == 200) {		   
           BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
		   
		   isOK=true;
           String output;
           authTokenResp="";
		   
           while ((output = br.readLine()) != null) {
              authTokenResp+=output;
           }
		   
		   logger.debug("Output from Server: "+authTokenResp+"\n");		
		   //Now get the token...
		   SKLMJSONParser parser = new SKLMJSONParser(authTokenResp);
		   HashMap<String, String> keyVals = parser.getKeyValuePairs();
		  
		   for (String x : keyVals.keySet()) {
			   logger.debug("key: " + x + ", value: " + keyVals.get(x));
		   }
			//Could just get("UserAuthId")
		   theAuthToken = keyVals.get(keyVals.keySet().toArray()[0]);
			
		   logger.debug("SKLMRESTAuthToken-theAuthToken is:"+theAuthToken);		   
        }
		else
		{
			readErrorResponseFromServer(conn, respCode);
		}
        conn.disconnect();

      } catch (MalformedURLException e) { e.printStackTrace(); } 
	    catch (IOException e)           
        { //e.printStackTrace(); 
	    	readErrorResponseFromServer(conn, respCode);
	    	conn.disconnect();
        }
   }

   
   public String getAuthToken()     { return theAuthToken; }
   public int getResponseCode()     { return respCode; }
   public boolean isOK()            { return isOK; }
   public String getAuthTokenResp() { return authTokenResp; }
}