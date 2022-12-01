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
import com.ibm.klm.rest.commands.SKLMRESTClients;
import com.ibm.klm.rest.commands.SKLMRESTClientsDel;
import com.ibm.klm.rest.commands.SKLMRESTClientsGet;
import com.ibm.klm.rest.commands.SKLMRESTClientsGroups;
import com.ibm.klm.rest.commands.SKLMRESTClientsGroupsAssignClients;
import com.ibm.klm.rest.commands.SKLMRESTClientsGroupsDel;
import com.ibm.klm.rest.commands.SKLMRESTClientsGroupsRemoveClients;
import com.ibm.klm.rest.commands.SKLMRESTClientsUpdateClientName;


public class JUnitSKLMRESTDelPath_Put_PutPath_GetPath_Post extends JUnitSKLMRESTSetup 
{
	String cliName1      ="alanclient997";
	String cliName2      ="alanclient997_2";
	String grpName       ="alangroup997";
	
	String args          = "clientName="+cliName1;
	String argsUpdate    = args+",newClientName="+cliName2;
	String argsDelete    =  "clientName="+cliName2;
	String argsGrp       = "groupName="+grpName;
	String argsAssnGrp   = argsGrp+",clients="+cliName1;
	
	@Test
	void testa_CreateClients()
	{				   
		setup();
		SKLMRESTClients c = new SKLMRESTClients(SKLMRESTCLI.theURL, args, myToken);
		System.out.println("Create Resp Code="+c.getResponseCode());
		if (c.getResponseCode() != 201) 
		{
		   fail("SKLMRESTCreateClientOK failed.  rc="+c.getResponseCode());
		}

		c = new SKLMRESTClients(SKLMRESTCLI.theURL, args, myToken);
		System.out.println("Create bad Resp Code="+c.getResponseCode());
		if (c.getResponseCode() != 409)
		{
		   fail("SKLMRESTCreateClientBad failed.  rc="+c.getResponseCode());
		}
	}
	@Test
	void testb_GetClients()
	{	
		SKLMRESTClientsGet cg = new SKLMRESTClientsGet(SKLMRESTCLI.theURL, args, myToken);
		System.out.println("Get Resp Code="+cg.getResponseCode());
		if (cg.getResponseCode() != 200)
		{
		   fail("SKLMRESTGetClientOK failed.  rc="+cg.getResponseCode());
		}
	}
		
	@Test
	void testc_CreateGroup()
	{
		SKLMRESTClientsGroups cg2 = new SKLMRESTClientsGroups(SKLMRESTCLI.theURL, argsGrp, myToken);
		System.out.println("Create Group Resp Code="+cg2.getResponseCode());
		if (cg2.getResponseCode() != 201)
		{
		   fail("SKLMRESTClientsGroupsOK failed.  rc="+cg2.getResponseCode());
		}
	}
	
	@Test
	void testd_AssignClient()
	{
		//Assign Client Good
		SKLMRESTClientsGroupsAssignClients cgac = new SKLMRESTClientsGroupsAssignClients(SKLMRESTCLI.theURL, argsAssnGrp, myToken);
		System.out.println("Assign Client Resp Code="+cgac.getResponseCode());
		if (cgac.getResponseCode() != 200)
		{
		   fail("SKLMRESTClientsGroupsAssignClientsOK failed.  rc="+cgac.getResponseCode());
		}

		//Assign Client Bad - just try again and it should give you an error in the message, although code is success
		cgac = new SKLMRESTClientsGroupsAssignClients(SKLMRESTCLI.theURL, argsAssnGrp, myToken);
		System.out.println("Assign Client 2 Resp Code="+cgac.getResponseCode());
		if (cgac.getResponseCode() != 200)
		{
		   fail("SKLMRESTClientsGroupsAssignClientsBAD failed.  rc="+cgac.getResponseCode());
		}
	}
	
	@Test
	void teste_RemoveClientFomGroup()
	{
		//Remove Client from group
		SKLMRESTClientsGroupsRemoveClients cgrc = new SKLMRESTClientsGroupsRemoveClients(SKLMRESTCLI.theURL, argsAssnGrp, myToken);
		System.out.println("Rem from group Resp Code="+cgrc.getResponseCode());
		if (cgrc.getResponseCode() != 200)
		{
		   fail("SKLMRESTClientsGroupsRemoveClientsOK failed.  rc="+cgrc.getResponseCode());
		}
	}
	
	@Test
	void testf_RemoveGroup()
	{
		//Remove Group
		SKLMRESTClientsGroupsDel cgd = new SKLMRESTClientsGroupsDel(SKLMRESTCLI.theURL, argsGrp, myToken);
		System.out.println("Remove group Resp Code="+cgd.getResponseCode());
		if (cgd.getResponseCode() != 204)
		{
		   fail("SKLMRESTClientsGroupsDelOK failed.  rc="+cgd.getResponseCode());
		}
	}
	
	@Test
	void testg_RenameClient()
	{		
		//Rename the client
		SKLMRESTClientsUpdateClientName cucn = new SKLMRESTClientsUpdateClientName(SKLMRESTCLI.theURL, argsUpdate, myToken);
		System.out.println("Rename Client Resp Code="+cucn.getResponseCode());
		if (cucn.getResponseCode() != 200)
		{
		   fail("SKLMRESTClientsUpdateClientNameOK failed.  rc="+cucn.getResponseCode());
		}
		
		//Rename again, should fail
		cucn = new SKLMRESTClientsUpdateClientName(SKLMRESTCLI.theURL, argsUpdate, myToken);
		System.out.println("Rename bad Resp Code="+cucn.getResponseCode());
		if (cucn.getResponseCode() != 404)
		{
		   fail("SKLMRESTClientsUpdateClientNameOK failed.  rc="+cucn.getResponseCode());
		}

	}

	@Test
	void testh_DeleteClients()
	{		
		SKLMRESTClientsDel cd = new SKLMRESTClientsDel(SKLMRESTCLI.theURL, argsDelete, myToken);
		System.out.println("Client del Resp Code="+cd.getResponseCode());
		if (cd.getResponseCode() != 204)
		{
		   fail("SKLMRESTDeleteClientOK failed.  rc="+cd.getResponseCode());
		}

		cd = new SKLMRESTClientsDel(SKLMRESTCLI.theURL, args, myToken);
		System.out.println("Cli Del bad Resp Code="+cd.getResponseCode());
		if (cd.getResponseCode() != 404)
		{
		   fail("SKLMRESTDeleteClientBad failed.  rc="+cd.getResponseCode());
		}
	}
}