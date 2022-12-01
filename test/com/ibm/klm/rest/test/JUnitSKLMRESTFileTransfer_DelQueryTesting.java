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

package com.ibm.klm.rest.test;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.ibm.klm.rest.SKLMRESTCLI;
import com.ibm.klm.rest.commands.SKLMRESTDeviceGroupsExportDeleteExportFile;
import com.ibm.klm.rest.commands.SKLMRESTFileTransferDownloadObjectfiles;
import com.ibm.klm.rest.commands.SKLMRESTFileTransferUploadObjectFiles;

public class JUnitSKLMRESTFileTransfer_DelQueryTesting extends JUnitSKLMRESTSetup
{
	//Upload
	//Download
	//Delete
	
    //password=Passw0rd_1
	String filepathOnServer="/opt/IBM/WebSphere/AppServer/products/sklm/data/";
	String filenameBase="sklm_v4.1.0.1_20220120124947-0500_export.exp";
	String filename = filepathOnServer + filenameBase;
	String delArgs = "exportFilePath="+filename;
	
	String upArgs = "fileToUpload=" + filenameBase;
	String downArgs = "fileName=" + filenameBase;
	
	
	@Test
	void testUploadDownloadDeviceGroupsExportFileDel()
	{				   
		setup();

		//File upload
		SKLMRESTFileTransferUploadObjectFiles upload = new SKLMRESTFileTransferUploadObjectFiles(SKLMRESTCLI.theURL, upArgs, myToken);
		System.out.println("Upload Resp Code="+upload.getResponseCode());
		if (upload.getResponseCode() != 200) 
		{
		   fail("SKLMRESTFIleTransferUploadObjectfilesOK failed.  rc="+upload.getResponseCode());
		}

		//File Download
		SKLMRESTFileTransferDownloadObjectfiles download = new SKLMRESTFileTransferDownloadObjectfiles(SKLMRESTCLI.theURL, downArgs, myToken);
		System.out.println("Download Resp Code="+download.getResponseCode());
		if (download.getResponseCode() != 200) 
		{
		   fail("SKLMRESTFIleTransferDownloadObjectfiles failed.  rc="+download.getResponseCode());
		}

		//Del export file tests delete with query
		SKLMRESTDeviceGroupsExportDeleteExportFile del = new SKLMRESTDeviceGroupsExportDeleteExportFile(SKLMRESTCLI.theURL, delArgs, myToken);
		System.out.println("Del Exp File Resp Code="+del.getResponseCode());
		if (del.getResponseCode() != 204) 
		{
		   fail("SKLMRESTDeviceGroupsExportDeleteExportFileOK failed.  rc="+del.getResponseCode());
		}

		del = new SKLMRESTDeviceGroupsExportDeleteExportFile(SKLMRESTCLI.theURL, delArgs, myToken);
		System.out.println("Bad Del Exp File Resp Code="+del.getResponseCode());
		if (del.getResponseCode() != 404) 
		{
		   fail("SKLMRESTDeviceGroupsExportDeleteExportFileOK failed.  rc="+del.getResponseCode());
		}
	}
}