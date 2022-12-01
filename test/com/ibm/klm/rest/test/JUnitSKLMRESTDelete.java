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
import com.ibm.klm.rest.commands.SKLMRESTLogout;

public class JUnitSKLMRESTDelete extends JUnitSKLMRESTSetup
{
	String user="SKLMAdmin";
	
	@Test
	void testLogoutGood()
	{
		setup();	
		
		SKLMRESTLogout l = new SKLMRESTLogout(SKLMRESTCLI.theURL, "userAuthId="+myToken, myToken);
		System.out.println("Resp Code="+l.getResponseCode());
		if (l.getResponseCode() != 200)
		{
		   fail("SKLMRESTBackupInfo failed.  rc="+l.getResponseCode());
		}
	}
	
	//Try to logout again...should fail
	@Test
	void testLogoutBad()
	{
		SKLMRESTLogout l = new SKLMRESTLogout(SKLMRESTCLI.theURL, "userAuthId="+myToken, myToken);
		System.out.println("Resp Code="+l.getResponseCode());
		if (l.getResponseCode() != 500)
		{
		   fail("SKLMRESTBackupInfo failed.  rc="+l.getResponseCode());
		}
	}
}