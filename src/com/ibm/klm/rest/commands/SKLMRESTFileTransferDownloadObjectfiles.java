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

public class SKLMRESTFileTransferDownloadObjectfiles extends SKLMRESTCommand
{
   private static final Logger logger = LogManager.getLogger(SKLMRESTFileTransferDownloadObjectfiles.class.getName());

   private static String path   = "/SKLM/rest/v1/filetransfer/download/objectfiles";
   private String type          = "GET";

   public SKLMRESTFileTransferDownloadObjectfiles(String theRESTURL, String arguments, String userAuthId)
   {
      logger.traceEntry();
      respCode = executeRESTCommand(theRESTURL, arguments, userAuthId, path, type);
   }
   
   public String getAcceptType()
   {
	   logger.debug("Accept application/octet-stream");
	   return "application/octet-stream";
   }
}
/*
curl -X GET "https://sklmrhel411.fyre.ibm.com:9443/SKLM/rest/v1/filetransfer/download/objectfiles?fileName=sklmCert.p12" 
-H  "accept: application/octet-stream" 
-H  "Accept-Language: en" 
-H  "Authorization: SKLMAuth userAuthId=a11a699c-5bd0-413a-8e11-ca2867d68cb5"

curl -X GET "https://sklmrhel411.fyre.ibm.com:9443/SKLM/rest/v1/devices" 
-H  "accept: application/json" 
-H  "Authorization: SKLMAuth userAuthId=a11a699c-5bd0-413a-8e11-ca2867d68cb5"
*/