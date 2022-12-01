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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.ibm.klm.rest.SKLMRESTCLI;
import com.ibm.klm.rest.commands.SKLMRESTAuthToken;

class JUnitSKLMRESTAuthToken extends JUnitSKLMRESTSetup {
	
	@Test
	void testSKLMRESTAuthTokenStringStringString()
	{
	   setup();
	   SKLMRESTAuthToken sklmAuthTok = new SKLMRESTAuthToken(SKLMRESTCLI.theURL,SKLMRESTCLI.sklmAdmin,SKLMRESTCLI.sklmAdminPwd);
	   if (!sklmAuthTok.isOK()) 
	   {
		 fail("Can't get token.");
	   }
	   System.out.println("Token="+sklmAuthTok.getAuthToken());
	}

	@Test
	void testSKLMRESTAuthTokenStringString()
	{
	   String args = "userid="+SKLMRESTCLI.sklmAdmin+",password="+SKLMRESTCLI.sklmAdminPwd;
		  	  			  		  		 
	   SKLMRESTAuthToken at = new SKLMRESTAuthToken(SKLMRESTCLI.theURL, args);
	   String myToken = at.getAuthToken();
	   System.out.println("Token="+myToken);
	   assertNotEquals(myToken,"");		   
	}
}