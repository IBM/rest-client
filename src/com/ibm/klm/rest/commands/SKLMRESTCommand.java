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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.klm.rest.ReadParsedSKLMYaml;
import com.ibm.klm.rest.SKLMJSONParser;
import com.ibm.klm.rest.SKLMRESTSpec;

public abstract class SKLMRESTCommand 
{
	private static final Logger logger = LogManager.getLogger(SKLMRESTCommand.class.getName());
		
	//Abstract class for common functions shared by several commands - REST commands should extend this class
	boolean fileUpload = false;
	public String filename = "";
	public int respCode=0;
	public int getResponseCode() {return respCode;}
		
	public void setFileUpload() {fileUpload = true;}
	
	//Reads an error response when success isn't returned from the REST server
	public void readErrorResponseFromServer(HttpsURLConnection conn,int respCode)
	{
	   logger.traceEntry();
	   if (conn != null)
	   try {	   
	      //BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
	      BufferedReader br = new BufferedReader(new InputStreamReader((conn.getErrorStream())));
		   
		  String errorResp = "";
	      String output;           
			   
	      while ((output = br.readLine()) != null) {        	  
	         errorResp+=output;
	      }
		  logger.error("Error output from Server in catch: "+respCode+" "+errorResp);
		  //System.out.println("Error output from Server: "+respCode+" "+errorResp+"\n");		  
		  System.out.println("Error: "+respCode);
		  SKLMJSONParser.printJSONString(errorResp);
       }
       catch (IOException e)
       {
          e.printStackTrace();
       }
	   catch (Exception e2)
	   {
		   e2.printStackTrace();
		   System.out.println(e2.getMessage());
		   System.out.println("An unknown error has occurred.  Response code:"+respCode);
		   logger.error("Unknown error, rc="+respCode);
		   logger.error(e2.getMessage());
	   }
	}
	
	public void printHelp(String path, String type)
	{
	   logger.traceEntry();
	   //Print the summary here
	   SKLMRESTSpec theSpec = ReadParsedSKLMYaml.getInstance().getSKLMRESTSpec(path);
	   if (theSpec!=null)
	   {
	      System.out.println();      
	      System.out.println(theSpec.getSummary(type));
	      System.out.println();      
          System.out.println("Attributes: " + theSpec.getAttributes(type));
          System.out.println("Required Attributes: " + theSpec.getRequiredAttributes(type));
          System.out.println();
       }
	   else
	   {
		   System.out.println("There is no avaialble help for this command.");
	   }
	}
	
	//The purpose of this function is to allow individual commands to override
	//what it means for arguments to be valid
	public boolean checkArguments(String path, String type, String arguments)
	{
		return ReadParsedSKLMYaml.getInstance().checkArguments(path, type, arguments);
	}

	//The purpose of this function is to allow individual commands to override
	//accepted types for things like file xfers
	public String getAcceptType()
	{
		return "application/json";
	}
	
	public String setContentType()
	{
		return "application/json";
	}

	
	//Execute a REST command
	public int executeRESTCommand(String theRESTURL, String arguments, String userAuthId, String path, String type)
	{
	   logger.traceEntry();
		  
	   if (arguments!=null && (arguments.trim().equalsIgnoreCase("help")))
	   {
		  printHelp(path, type);
		  return 0;
	   }
			  
      int respCode = -1;
		  
      //if (ReadParsedSKLMYaml.getInstance().checkArguments(path, type, arguments)==false)
      if (checkArguments(path, type, arguments)==false)
    	  return -1;

	  HttpsURLConnection conn = null;
	  //String filename="";
		  
	  try {
		  //Now we parse args
	      ReadParsedSKLMYaml.getInstance().buildArgumentLists(path, type, arguments);
	      logger.debug("Arguments: "+arguments);
	      //There should only be one of these if it exists
	      String newPath = path;
	      
	      for (String arg: ReadParsedSKLMYaml.getInstance().getPathArgs())
	      {
	      	  String[] vals = arg.split("=");
	      	  //Fix6
	      	  String replacement = "";
	      	  
	      	  if (vals.length != 1)
	      		  replacement=vals[1];
	      	  
			  newPath = newPath.replace("{"+vals[0]+"}", replacement);
              //e Fix6			  
              logger.debug("newPath is now:" +newPath);
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
	      conn.setRequestProperty("Accept", getAcceptType());
	      conn.setRequestProperty("Content-Type", setContentType());
   	      conn.setRequestProperty("Authorization", "SKLMAuth userAuthId="+userAuthId);
	      
	      for (String arg: ReadParsedSKLMYaml.getInstance().getHeaderArgs())
	      {
             String[] vals = arg.split("=");
             //Fix6
	      	 String replacement = "";
	      	  
	      	 if (vals.length != 1)
	      	    replacement=vals[1];
             
             conn.setRequestProperty(vals[0],  replacement);
             logger.debug("Added:"+vals[0]+","+replacement+" to header.");
             //e Fix6
		  }
	      
	      setAdditionalRequestProperties(conn);
	      
	      if (fileUpload==true)
	      {
	    	  handleBodyForFileUpload(conn);
	      }
	      else
	      {
			  String jsonInputString = doBodyArgs();

			  logger.debug("jsonInputString="+jsonInputString);

			  //Don't do the following for GET	  
			  if (!type.equalsIgnoreCase("GET"))
	          {
	              conn.setDoOutput(true);  //Enable writing to the output stream	              
	              OutputStream os = conn.getOutputStream();
		          byte[] input = jsonInputString.getBytes("utf-8");       
		          os.write(input, 0, input.length);
			      //additionalWriteIfNeeded(os);  //I dont think we'll need this
			      os.flush();  //Needed?
			      os.close();  //Needed?
			  }	    	  
	      }
		   
	      //Process a response here...
          respCode = conn.getResponseCode();
          if (respCode <= 299 || respCode == 300) {
        	 System.out.println("Response code: " + respCode);
        	 logger.debug("Response code from server:"+respCode);        	 		     
        	 
        	 String contentDisp = conn.getHeaderField("content-disposition");
        	 if (contentDisp != null && contentDisp.startsWith("attachment; filename="))
        	 {
        		 ///This is a file....we need to read it
        		 filename = contentDisp.split("=")[1];
        		 logger.debug("Filename is:"+filename);
        		 
                 InputStream inStream = conn.getInputStream();
        		 byte[] buffer = new byte[4096];
        		 int n;

        		 OutputStream output = new FileOutputStream( filename );
        		 while ((n = inStream.read(buffer)) != -1) 
        		 {
        		     output.write(buffer, 0, n);
        		 }
        		 output.close();   
        		 System.out.println("Wrote file: "+filename);
        	 }
        	 else
        	 {
        		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        		
		        String output;
		        String response = "";
				   
		        while ((output = br.readLine()) != null) {
		           response+=output;
		        }		          
		        logger.debug("Output from Server: "+response);
		 		   		        
		 	    SKLMJSONParser.printJSONString(response);
        	 }
		 }
		 else
		 {
		    readErrorResponseFromServer(conn, respCode);
		 }
		 conn.disconnect();	  
	  } catch (MalformedURLException e) { e.printStackTrace(); }
	    catch (FileNotFoundException e)
	    {
	    	System.out.println("Unable to write or find file:" + filename);
	    	logger.error("Unable to write or find file:" + filename);
	    }
		catch (IOException e)           
	    { //e.printStackTrace(); 
		   readErrorResponseFromServer(conn, respCode);
		   conn.disconnect();
	    }
	  
	  
		return respCode;
   }
	
	
   public String doBodyArgs()
   {
	  logger.traceEntry();
      String jsonInputString = ""; 
      int i=0;
      for (String arg: ReadParsedSKLMYaml.getInstance().getBodyArgs())
      {
         String[] vals = arg.split("=");
         //Fix6
     	 String replacement = "";
      	  
     	 if (vals.length != 1)
     	    replacement=vals[1];
         //e Fix6
         
         if (i==0)
             jsonInputString+="{";
         else
             jsonInputString+=", ";
         i++;
         jsonInputString+="\""+vals[0]+"\": \""+replacement+"\"";
      }
      if (i!=0)
         jsonInputString+="}";
      
      return jsonInputString;
   }
   
   public void additionalWriteIfNeeded(OutputStream os) throws FileNotFoundException, IOException {}  //For file uploads
   public void setAdditionalRequestProperties(HttpsURLConnection conn) {} //For fileuploads
   public void handleBodyForFileUpload(HttpsURLConnection conn) throws IOException  {}  //For fileuploads
   public String generateBoundary()
   {
	   logger.traceEntry();
	   return "-------"+UUID.randomUUID().toString(); 
   }
   
   
}
