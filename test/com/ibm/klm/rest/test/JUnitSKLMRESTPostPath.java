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
import com.ibm.klm.rest.commands.SKLMRESTDeviceGroups;
import com.ibm.klm.rest.commands.SKLMRESTDeviceGroupsDel;

public class JUnitSKLMRESTPostPath extends JUnitSKLMRESTSetup
{
	String args="name=alanLTO,deviceFamily=LTO";
	String argsDel="name=alanLTO";
	
	@Test
	void testDeviceGroupsOK()
	{		
		setup();
		
		SKLMRESTDeviceGroups dg = new SKLMRESTDeviceGroups(SKLMRESTCLI.theURL, args, myToken);
		System.out.println("Resp Code="+dg.getResponseCode());
		if (dg.getResponseCode() != 200)
		{
		   fail("SKLMRESTDeviceGroupsOK failed.  rc="+dg.getResponseCode());
		}
		
		dg = new SKLMRESTDeviceGroups(SKLMRESTCLI.theURL, args, myToken);
		System.out.println("Resp Code="+dg.getResponseCode());
		if (dg.getResponseCode() != 500)
		{
		   fail("SKLMRESTDeviceGroupsBad failed.  rc="+dg.getResponseCode());
		}
		
		SKLMRESTDeviceGroupsDel dgd = new SKLMRESTDeviceGroupsDel(SKLMRESTCLI.theURL, argsDel, myToken);
		System.out.println("Resp Code="+dgd.getResponseCode());
		if (dgd.getResponseCode() != 200)
		{
		   fail("SKLMRESTDeviceGroupDelsOK failed.  rc="+dgd.getResponseCode());
		}

	}
}