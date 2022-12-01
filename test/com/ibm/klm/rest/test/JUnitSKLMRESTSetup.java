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

import com.ibm.klm.rest.ReadParsedSKLMYaml;
import com.ibm.klm.rest.SKLMRESTCLI;
import com.ibm.klm.rest.commands.SKLMRESTAuthToken;

class JUnitSKLMRESTSetup {

	public static String myToken="";
	void setup()
	{
		//Set these up prior to running any tests
		SKLMRESTCLI.theURL = "https://sklmrhel411.fyre.ibm.com:9443";
		SKLMRESTCLI.sklmAdmin                 = "SKLMAdmin";
		SKLMRESTCLI.sklmAdminPwd              = "Passw0rd_12";
		//Note - move ParsedSKLMYaml.txt to SKLMRESTCLI
			  
		SKLMRESTCLI.setupTrustAllForREST();  //For JUNit, trust everything	   
	    ReadParsedSKLMYaml.getInstance();  //prime it...
	    
	    SKLMRESTAuthToken sklmAuthTok = new SKLMRESTAuthToken(SKLMRESTCLI.theURL,
	    		                                              SKLMRESTCLI.sklmAdmin,
	    		                                              SKLMRESTCLI.sklmAdminPwd);
	    myToken = sklmAuthTok.getAuthToken();
	}		
}