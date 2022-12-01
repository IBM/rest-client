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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.net.ssl.HttpsURLConnection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.klm.rest.ReadParsedSKLMYaml;

public class SKLMRESTFileTransferUploadObjectFiles extends SKLMRESTCommand {

	   private static final Logger logger = LogManager.getLogger(SKLMRESTFileTransferUploadObjectFiles.class.getName());

	   private static String path   = "/SKLM/rest/v1/filetransfer/upload/objectfiles";
	   private String type          = "POST";
	   private String boundary      = "";
								   
	   public SKLMRESTFileTransferUploadObjectFiles(String theRESTURL, String arguments, String userAuthId)
	   {
	      logger.traceEntry();	  
	      boundary = generateBoundary();      //"--------------------------749078694904156401931494";
	      setFileUpload();
	      respCode = executeRESTCommand(theRESTURL, arguments, userAuthId, path, type);
	   }
	   
		public String setContentType()
		{
			logger.debug("Setting content type to multipart/formdata");
			return "multipart/form-data; boundary="+boundary;
		}
		
//		-----------------------------293582696224464
//		Content-Disposition: form-data; name="fileName"; filename="clientcert.cer"
		
	   public void handleBodyForFileUpload(HttpsURLConnection conn) throws FileNotFoundException, IOException
	   {
		   logger.traceEntry();
           String jsonInputString = "";
           conn.setDoOutput(true);  //Enable writing to the output stream
           OutputStream os = conn.getOutputStream();
           
		   for (String arg: ReadParsedSKLMYaml.getInstance().getBodyArgs())
		   {
		       String[] vals = arg.split("=");
			        
			   if (vals[0].equals("fileToUpload"))
			   {
			       jsonInputString="--"+boundary+"\r\n";
			       jsonInputString+="Content-Disposition: form-data; name=\""+vals[0]+"\"; filename=\""+vals[1]+"\"\r\n";
			       jsonInputString+="\r\n";
			       logger.debug("jsonInputString="+jsonInputString);
			       			       
			       byte[] input = jsonInputString.getBytes("utf-8");       
			       os.write(input, 0, input.length);
			       
			       filename = vals[1];
			       
				   //System.out.println("Reading the file...");
				   logger.debug("Reading file: "+filename);
				   
				   FileInputStream inputStream = new FileInputStream(filename);
			       byte[] buffer = new byte[4096];
			       int bytesRead = -1;
			       while ((bytesRead = inputStream.read(buffer)) != -1) {
			          os.write(buffer, 0, bytesRead);
				   }
				   inputStream.close();
				   
				   jsonInputString="\r\n";
				   byte[] input2 = jsonInputString.getBytes("utf-8");       
				   os.write(input2, 0, input2.length);
			    }
			    else
			    {
			        jsonInputString="--"+boundary+"\r\n";
			        jsonInputString+="Content-Disposition: form-data; name=\""+vals[0]+"\"\r\n\r\n";
	                jsonInputString+=vals[1]+"\r\n";
	                
	                logger.debug("jsonInputString="+jsonInputString);
	                
                    byte[] input = jsonInputString.getBytes("utf-8");       
				    os.write(input, 0, input.length);
			    }			         
			}
		   
		    String lastLine="--"+boundary+"--\r\n";
		    logger.debug("lastLine="+lastLine);
		    byte[] input2 = lastLine.getBytes("utf-8");       
		    os.write(input2, 0, input2.length);
		    os.flush();  //Needed?
		    os.close();  //Needed?
       }		   
}