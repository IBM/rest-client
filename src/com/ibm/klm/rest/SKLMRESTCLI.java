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

package com.ibm.klm.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ibm.klm.rest.commands.*;

public class SKLMRESTCLI
{

   public static String theURL                    = "https://localhost:9443";
   public static String sklmAdmin                 = "SKLMAdmin";
   public static String sklmAdminPwd              = "";
   public static String theCertFile               = "";
   public static String theCommand                = "";
   public static String theCommandFile            = "";
   public static ArrayList<String> theCommandList = new ArrayList<String>();
   
   public static String quickTest() {return "Working";}
   
   
   //Logging
   private static final Logger logger = LogManager.getLogger(SKLMRESTCLI.class.getName());
   
   //TrustManager Stuff....
   private static X509Certificate caCertificate;
   public static X509Certificate getMyCert()
   {
	   return caCertificate;
   }
   
   public static void setupTrustAllForREST()
   {
	   logger.traceEntry();
	   
	   // Allows you to use https without verifying cert....
       // Create a trust manager that does not validate certificate chains
	   TrustManager[] trustAllCerts = new TrustManager[] {
	       new X509TrustManager() {
	          public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
	          public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
	          public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
	       }
	   };

	   // Install the all-trusting trust manager
	   try {
	      SSLContext sc = SSLContext.getInstance("SSL");
	      sc.init(null, trustAllCerts, new java.security.SecureRandom());
	      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	   } catch (Exception e) {
	    	logger.fatal("Unable to install trust manager.");
	        System.out.println("Unable to install trust manager.");
	        System.out.println("All trusting failed.");
	        System.exit(1);		   	      
	   }	   	   
	
	   //a Fix2
	   // Create all-trusting host name verifier
       HostnameVerifier allHostsValid = new HostnameVerifier() {
           public boolean verify(String hostname, SSLSession session) {
               return true;
           }
       };	   
       // Install the all-trusting host verifier
       HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
       //e Fix2
   }
   
   public static void setupTrustMgrForREST()
   {
	  logger.traceEntry();
	  
	  //If no cert was specified, this means they want to use the java truststore on the system, so skip all of this
	  if (theCertFile.equals(""))
		  return;
	  
	  if (theCertFile.equals("idontwanttouseacertfile"))
	  {
		  logger.info("Trusting all certs...");
		  setupTrustAllForREST();
		  return;
	  }

	  
   	  //Load the cert
	  try {
         CertificateFactory cf = CertificateFactory.getInstance("X.509");  
         // openssl s_client -showcerts -connect localhost:9443 - I needed the 2nd cert		 
         FileInputStream finStream = new FileInputStream(theCertFile); 
         caCertificate = (X509Certificate)cf.generateCertificate(finStream);
	  }
	  catch (FileNotFoundException fnfe)
	  {
		  System.out.println("Cannot find cert file: " + theCertFile);
		  logger.error("Cannot find cert file: " + theCertFile);
		  System.exit(1);
	  }
	  catch (CertificateException ce) 
	  {
		  System.out.println("Something is wrong with the certificate material in: "+theCertFile);
		  logger.error("Something is wrong with the certificate material in: "+theCertFile);
		  System.exit(1);
      }
	  catch (Exception e) 
	  {
		  System.out.println("Unable to process cert file: "+theCertFile);
		  logger.error("Unable to process cert file: "+theCertFile);
		  System.exit(1);
	  }
	  
	  logger.debug("My cert...");
	  //System.out.println("My cert...");
	  logger.debug(caCertificate.toString());
	  //System.out.println(caCertificate.toString());

      // Create a trust manager that does not validate certificate chains
      // Allows you to use https without verifying cert....
      TrustManager[] trustAllCerts = new TrustManager[] {
         new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers()
            { 
			   //System.out.println("In getAcceptedIssuers()...");
			   logger.traceEntry();
			   X509Certificate[] issuers = new X509Certificate[1];
			   issuers[0]=SKLMRESTCLI.getMyCert();
			   return issuers;
			}

            public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
            { System.out.println("In checkClientTrusted()..."); }
         
            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
            { 
			  Boolean certOK = false;
			  logger.traceEntry();
			  //System.out.println("In checkServerTrusted()...");
			  logger.debug("Array size="+certs.length);
			  //System.out.println("Array size="+certs.length);
			  //System.out.println("This is my cert's public key..");
			  //System.out.println(SKLMRESTCLI.getMyCert().getPublicKey());
			  for (int i=0; i<certs.length; i++)
			  {
				logger.debug("Item#"+(i+1));
			    //System.out.println("Item#"+(i+1));
				logger.debug("==========");
			    //System.out.println("==========");
				logger.debug(certs[i].toString());
			    //System.out.println(certs[i].toString());
			    //System.out.println("AuthType="+authType);
			    try {
			      certs[i].verify(SKLMRESTCLI.getMyCert().getPublicKey());
			      logger.info("Verified with cert#"+(i+1));
				  //System.out.println("Verified with cert#"+(i+1));
				  certOK = true;
				  break;
			    }
			    catch (NoSuchAlgorithmException e1) {
			    	logger.info("NoSuchAlgorithmException");
			    	//System.out.println("NoSuchAlgorithmException");			    	
			    }
			    catch (InvalidKeyException e2)      {
			    	logger.info("InvalidKeyException");
			    	//System.out.println("InvalidKeyException");
			    }
			    catch (NoSuchProviderException e3)  {
			    	logger.info("NoSuchProviderException");
			    	//System.out.println("NoSuchProviderException");
			    }
			    catch (SignatureException e4)       {
			    	logger.info("SignatureException");
			    	//System.out.println("SignatureException");
			    }
			    catch (CertificateException e5)     {
			    	logger.info("CertificateException");
			    	//System.out.println("CertificateException");
			    }
			    catch (Exception e)                 {
			    	logger.info("Did not verify with cert#"+(i+1));
			    	//System.out.println("Did not verify with cert#"+(i+1));
			    }
			  }
			  if (!certOK)
			  {
				  logger.fatal("Unable to verify certificate from server.");
				  System.out.println("Unable to verify certificate from server.");
				  System.exit(1);
			  }
			}
         }
      };

      // Install the all-trusting trust manager
      try {
         SSLContext sc = SSLContext.getInstance("SSL");
         sc.init(null, trustAllCerts, new java.security.SecureRandom());
         HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      } catch (Exception e) {
    	 logger.fatal("Unable to install trust manager.");
         System.out.println("Unable to install trust manager.");
         System.exit(1);
      }
   }

   public static void printUsage()
   {
	   System.out.println("Usage:  sklmRESTCLI -user SKLM_ADMIN_ID -password SKLM_ADMIN_PWD [-certfile path_to_cert] [-cmd \"cmd to execute\"] [-cmdFile \"path_to_commandFile\"]");
	   System.exit(1);
   }
    
   public static void printAvailableCommands()
   {
      System.out.println("\nAvailable Commands\n");
                        //---------------------------Z-------------------------Z--------------------------
                        //-------------------------------------------X------------------------------------
      System.out.println("Login:\n" 
      		           + "sklmLogin                  sklmLogout\n"
      		           + "\n"
      		           + "Backup and Restore:\n"
      		           + "sklmBackupInfo             sklmBackups               sklmBackupsGet\n"
      		           + "sklmBackupsIsRunning       sklmBackupsNeed           sklmBackupsProgress\n"
      		           + "sklmBackupsResult          sklmRestore               sklmRestoreIsRunning\n"
      		           + "sklmRestoreProgress        sklmRestoreResult\n"
      		           + "\n"
    		           + "Certificate Management:\n"
      		           + "sklmCertificateAttributes                 sklmCertificatesGet\n"
    		           + "sklmCertificates                          sklmCertificatesPut\n"
      		           + "sklmCertificatesBulkCertUpdate            sklmCertificatesClient\n"
    		           + "sklmCertificatesExport                    sklmCertificatesImport\n"
      		           + "sklmCertificatesRollover                  sklmCertificatesRolloverGet\n"
    		           + "sklmCertificatesRolloverDel               sklmCertificatesDel\n"    		           
    		           + "\n"
    		           + "Client Management:\n"
    		           + "sklmClients                               sklmClientsGetAll\n"     		           
    		           + "sklmClientsGroups                         sklmClientsGroupsGetAll\n"
    		           + "sklmClientsGroupsClientList               sklmClientsGroupsUpdateGroupName\n"
    		           + "sklmClientsGroupsDel                      sklmClientsGroupsGet\n"
    		           + "sklmClientsGroupsAssignClients            sklmClientsGroupsRemoveClients\n"
    		           + "sklmClientsUpdateClientName               sklmClientsDel\n"
    		           + "sklmClientsGet                            sklmClientsAssignCertificate\n"
    		           + "sklmClientsAssignUsers                    sklmClientsListObjectsToClient\n"
    		           + "sklmClientsRemoveUsers\n"
    		           + "\n"
    		           + "Conflict Resolution:\n"
    		           + "sklmConflictResolutionChangeCertificateAlias\n"
      		           + "sklmConflictResolutionChangeName\n"
    		           + "sklmConflictResolutionGetChangeHistory\n"    		           
    		           + "sklmConflictResolutionRenewKeyAlias\n"
    		           + "sklmConflictResolutionRenewUUID\n" 
    		           + "\n"
    		           + "Device Group Management:\n"
    		           + "sklmDeviceGroupsExport                    sklmDeviceGroupsExportGet\n"
    		           + "sklmDeviceGroupsExportDeleteExportFile\n"
    		           + "sklmDeviceGroupsExportViewSummaryOfExportFileInADirectory\n"
    		           + "sklmDeviceGroupsImport                    sklmDeviceGroupsImportConflicts\n"
    		           + "sklmDeviceGroupAttributes                 sklmDeviceGroupAttributesPut\n"
    		           + "sklmDeviceGroupAttributesDel              sklmDeviceGroupsGet\n"
    		           + "sklmDeviceGroupsBase                      sklmDeviceGroups\n"
    		           + "sklmDeviceGroupsDel                       sklmDeviceTypes\n"   //sklmDeviceTypesGet
    		           + "\n"
    		           + "Device Management:\n"
    		           + "sklmDevices                sklmDevicesGet            sklmDevicesPut\n"
    		           + "sklmDevicesDel             sklmMachines              sklmMachinesDel\n"
    		           + "sklmMachinesGet            sklmMachinesPut           sklmMachinesDevice\n"
    		           + "sklmMachinesDeviceDel      sklmMachinesDeviceGet\n"
    		           + "\n"
    		           + "File Transfer:\n"
    		           + "sklmDownloadLogFiles                      sklmDownloadObjectfiles\n"
    		           + "sklmUploadLicense                         sklmUploadObjectFiles\n"
    		           + "\n"    		           
    		           + "Key Group Management:\n"
    		           + "sklmKeygroupEntry          sklmKeygroupentryDel      sklmKeygroupsGet\n"
    		           + "sklmKeygroupsPut           sklmKeygroupsRollover     sklmKeygroupsRolloverGet\n"
    		           + "sklmKeygroupsRolloverDel   sklmKeygroups             sklmKeygroupsDel\n"
    		           + "\n"
    		           + "Key Management:\n"   		           
    		           + "sklmKeyAttributes          sklmKeys                  sklmKeysGet\n"
    		           + "sklmKeysPut                sklmKeysExport            sklmKeysImport\n"
    		           + "sklmKeysDel\n"
    		           + "\n"
    		           + "KMIP Secret Data Management:\n"
    		           + "sklmSecretData                            sklmSecretDataDel\n"
    		           + "\n"    		           
    		           + "KMIP Template Management:\n"
    		           + "sklmKMIPTemplate                          sklmKMIPTemplateDel\n"
    		           + "\n"    		           
    		           + "Master Key Management:\n"
    		           + "sklmDeviceGroupMasterKeyGetAll            sklmDeviceGroupMasterKeyRotateAll\n"
    		           + "sklmDeviceGroupMasterKey                  sklmDeviceGroupMasterKeyGet\n"
    		           + "sklmDeviceGroupMasterKeyRotate            sklmMasterKey\n"
    		           + "sklmMasterKeyTransmitter\n"
    		           + "\n"
    		           + "Multi-Master Cluster Management:\n"
    		           + "sklmMultiMasterAddNodes                   sklmMultiMasterGetClusterName\n"
    		           + "sklmMultiMasterIsNodeIsolatedFromCluster  sklmMultiMasterJoinBackTheCluster\n"
    		           + "sklmMultiMasterReconfig                   sklmMultiMasterRegenerateClusterName\n"
    		           + "sklmMultiMasterRemoveNode                 sklmMultiMasterRestartCluster\n"
    		           + "sklmMultiMasterSetupAsReadWriteMaster     sklmMultiMasterStopCluster\n"
    		           + "sklmMultiMasterTakeoverAsPrimary          sklmMUltiMasterUpdateMaster\n"
    		           + "sklmMultiMasterExpiringCertificates       sklmMultiMasterState\n"
    		           + "sklmMultiMasterAgentStatus                sklmMultiMasterStartAgent\n"
    		           + "sklmMultiMasterStopAgent                  sklmMultiMasterNodes\n"
    		           + "sklmMultiMasterAllDBNodeStatus            sklmMultiMasterAllNodeStatus\n"
    		           + "sklmMultiMasterCheckPreRequisite          sklmMultiMasterGetClusterHADRStatus\n"
    		           + "sklmMultiMasterGetThisNodeDetails\n"
    		           + "\n"
    		           + "Object Management:\n"
    		           + "sklmObjects                sklmObjectsCertificate    sklmObjectsKeypair\n"
    		           + "sklmObjectsOpaque          sklmObjectsSecret         sklmObjectsSymmetricKey\n"
    		           + "sklmObjectsGet             sklmObjectsDel\n"
    		           + "\n"
    		           + "Pending Object Management:\n"
    		           + "sklmPendingClientCertificatesGet          sklmPendingClientCertificates\n"
    		           + "sklmPendingClientCertificatesDel          sklmPendingDevices\n"
    		           + "sklmPendingDevicesGet                     sklmPendingDevicesDel\n"
    		           + "sklmPendingMachineDevices                 sklmPendingMachineDevicesAccept\n"
    		           + "sklmPendingMachineDevicesReject\n"
    		           + "\n"
    		           + "Replication:\n"
    		           + "sklmReplicateNow                          sklmReplicateStart\n"
    		           + "sklmReplicateStatus                       sklmReplicateStop\n"
    		           + "sklmReplicationConfigProperties           sklmReplicationConfigPropertiesPut\n"
    		           + "sklmReplicationConfigPropertiesDel\n"
    		           + "\n"
    		           + "Served Data Archival:\n"
    		           + "sklmServedData                            sklmServedDataGet\n"
    		           + "\n"
    		           + "Server Configuration:\n"
    		           + "sklmConfigPropertiesGetAll                sklmConfigPropertiesPut\n"
    		           + "sklmConfigPropertiesDel                   sklmConfigPropertiesGet\n"
    		           + "\n"
    		           + "Server Management:\n"
    		           + "sklmServerChangePasswordDB2MultiMaster    sklmServerChangePasswordDB2StandAlone\n"
    		           + "sklmServerCounts                          sklmServerKerberosConfigure\n"
    		           + "sklmServerKerberosConfigureOnMM           sklmServerKerberosGetConfiguration\n"
    		           + "sklmServerKerberosRemove                  sklmServerRestartServer\n"
    		           + "sklmServerVersionInfo                     sklmServerKeyServerStatus\n"
    		           + "sklmServerSystemDetails\n"
    		           + "\n" 		           
    		           + "Truststore Management:\n"
    		           + "sklmTruststoreCertificates                sklmAddCertToTruststore\n"
    		           + "sklmDeleteCertFromTruststore\n"
    		           + "\n"
    		           + "User Administration:\n"
    		           + "sklmUserPassword\n"
    		           +"\n");
    		      
      System.out.println("\nFor more info on a particular command, enter 'command(help)'\n");
   }
   
   public static void readConfigFile()
   {
	   //Current config file options are
	   //username:SKLMAdmin
	   //password:
	   //certfile:
	   //url:
	   try {
          File f = new File("SKLMRESTCLIConfig.properties");
		  Scanner reader = new Scanner(f);
			
		  while (reader.hasNextLine())
		  {
             String line = reader.nextLine().trim();
			 logger.debug("Parsing: "+line);
			 
			 //Skip blank lines and lines that start with #
			 if (line.startsWith("#") || line.equals(""))
				 continue;
				
			 String[] keyvals = line.split(":",2);
				
             switch (keyvals[0].trim()) {
				case "user":
				   sklmAdmin = keyvals[1].trim();
                   break;
					
				case "password":
				   sklmAdminPwd = keyvals[1].trim();
				   break;
					
				case "certfile":
					theCertFile = keyvals[1].trim();
					break;

				case "url":
					theURL = keyvals[1].trim();
					break;
					
				default:  //Unrecognized line
					System.out.println("Unrecognized parameter in config file:"+keyvals[0]);
					break;
			 }				
		  } //end of while
	      reader.close();
	   } catch (FileNotFoundException e) {
	      //File not specified...that's ok
		  logger.info("No SKLMRESTCLIConfig.properties found.");
	   }
   }
   
   public static void consumeCommandFile()
   {
       try {
          File f = new File(theCommandFile);
	      Scanner reader = new Scanner(f);
				
          while (reader.hasNextLine())
          {
             String line = reader.nextLine().trim();
		     logger.debug("CmdFile Found: "+line);
				 
             //Skip blank lines and lines that start with #
	         if (line.startsWith("#") || line.equals(""))
		        continue;
		  
		     theCommandList.add(line);
	     } //end of while
         reader.close();
      } catch (FileNotFoundException e) {
		  //File not specified...that's ok
		  logger.error("Unable to find command file: "+theCommandFile);
		  System.out.println("Unable to find command file: "+theCommandFile);
	  }	   
   }
   
   public static void readConfigAndparseArgs(String[] args)
   {
	   int i=0;

	   readConfigFile();
	   
	   logger.debug("Command line args:");
       for (String x: args)
          logger.debug("arg:"+x);
	   
	   if (args.length % 2 != 0) 
	   {
		   System.out.println("Incorrect number of arguments");
		   logger.info("Incorrect number of arguments:" + args.length);
		   System.exit(0);
	   }

	   
	   while (i < args.length-1)
	   {
		   if (args[i].equals(""))
		   {
			   i++;
			   continue;  //This is a Windows hack
		   }
		   
		   if (args[i].equalsIgnoreCase("-url"))
           {
			   theURL=args[i+1];
               i+=2;			   
               //System.out.println("Set URL to:"+theURL);
           }
           else if (args[i].equalsIgnoreCase("-user"))
           {
			   sklmAdmin=args[i+1];
               i+=2;			   			   
           }				   
           else if (args[i].equalsIgnoreCase("-password"))
           {
   			   sklmAdminPwd=args[i+1];
               i+=2;
           }
           else if (args[i].equalsIgnoreCase("-certfile"))
           {
        	   theCertFile=args[i+1];
               i+=2;
           }
           else if (args[i].equalsIgnoreCase("-cmd"))
           {
        	   theCommandList.add(args[i+1]);
        	   theCommand=args[i+1];
               i+=2;        	   
           }
           else if (args[i].equalsIgnoreCase("-cmdFile"))
           {
        	   theCommandFile = args[i+1];
               i+=2;
               consumeCommandFile();
           }
		   else
		   {
			   logger.error("Unrecognized parameter: "+args[i]);
			   System.out.println("Unrecognized parameter: "+args[i]);
			   printUsage();
		   }
	   }
	   
	   if (theURL.equals("") || sklmAdmin.equals("") || sklmAdminPwd.equals("")) { // || theCertFile.equals("")) { - allow java truststore
		  logger.error("Missing required parameter...");
		  System.out.println("Missing required parameter...");
	      printUsage();
	   }
	   else
	   {
		   logger.debug("Parameters:");
		   logger.debug("===========");
		   logger.debug("username:"+sklmAdmin);
		   //logger.debug("password"+sklmAdminPwd);
		   logger.debug("certfile:"+theCertFile);
		   logger.debug("url:"+theURL);
		   logger.debug("cmd:"+theCommand);
		   logger.debug("cmdFile:"+theCommandFile);
	   }
   }
   
   /*
   public static void getAuthToken()
   {
      try {
        URL url = new URL(theURL+"/SKLM/rest/v1/ckms/login");
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Accept-Language", "en");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);  //Enable writing to the output stream??


        String jsonInputString = "{\"userid\": \""+sklmAdmin+"\", \"password\": \""+sklmAdminPwd+"\"}";
        OutputStream os = conn.getOutputStream();
        byte[] input = jsonInputString.getBytes("utf-8");
        os.write(input, 0, input.length);
        
        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

        String output;
        System.out.println("Output from Server .... \n");
        while ((output = br.readLine()) != null) {
            System.out.println(output);
        }

        conn.disconnect();

      } catch (MalformedURLException e) { e.printStackTrace(); } 
	    catch (IOException e)           { e.printStackTrace(); }
   }
*/

   public static void main(String[] args)
   {
	  logger.info("V41 Program starting...");
      readConfigAndparseArgs(args);  //This will set up the cmd list
      setupTrustMgrForREST();
	  SKLMRESTAuthToken sklmAuthTok = new SKLMRESTAuthToken(theURL,sklmAdmin,sklmAdminPwd);
	  if (sklmAuthTok.isOK()) {
		  String myToken = sklmAuthTok.getAuthToken();
		  
		  logger.debug("theAuthToken is:"+myToken);
		  //System.out.println("SKLMRESTCLI-theAuthToken is:"+myToken);
		  
		  boolean keepGoing = true;		  
		  Scanner scan= new Scanner(System.in);
		  		  		 
		  String text = "";
		  //Get the first command
		  try {  theCommand = theCommandList.remove(0); }
		  catch (IndexOutOfBoundsException e) { theCommand=""; }
		  
		  while (keepGoing)
		  {			  
			  if (theCommand.equals(""))
			  {
			     System.out.print("sklmRestCLI>");
		         //scan= new Scanner(System.in);
                 text = scan.nextLine();
                 text = text.trim();
		      }
			  else
			  {
				  text = theCommand.trim();
				  System.out.println("\nExecuting: " + text);
			  }
              logger.debug("Data entered: "+text);
		      if (text.equalsIgnoreCase("quit")) {
                 keepGoing = false;
		      }
              else {
				  ParseCommand theCmd = new ParseCommand(text);
				  String cmd = theCmd.getCmd();				  
				  if (cmd != null)
				  {
					 //Login
					 if (cmd.equalsIgnoreCase("sklmLogin"))
					 {
						 SKLMRESTAuthToken at = new SKLMRESTAuthToken(theURL, theCmd.getArgs());
						 myToken = at.getAuthToken();
					 }
					 else if (cmd.equalsIgnoreCase("sklmLogout")) 
					 {
					     SKLMRESTLogout lo = new SKLMRESTLogout(theURL, theCmd.getArgs(), myToken);						 
					 }
					 
					 
					 //Backup and restore
					 else if (cmd.equalsIgnoreCase("sklmBackupInfo"))
					 {
						SKLMRESTBackupInfo bi = new SKLMRESTBackupInfo(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmBackups"))
					 {
						 SKLMRESTBackups b = new SKLMRESTBackups(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmBackupsGet"))
					 {
						 SKLMRESTBackupsGet bg = new SKLMRESTBackupsGet(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmBackupsIsrunning"))
					 {
						 SKLMRESTBackupsIsRunning bir = new SKLMRESTBackupsIsRunning(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmBackupsNeed"))
					 {
						 SKLMRESTBackupsNeed bn = new SKLMRESTBackupsNeed(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmBackupsProgress"))
					 {
						 SKLMRESTBackupsProgress bp = new SKLMRESTBackupsProgress(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmBackupsResult"))
					 {
						 SKLMRESTBackupsResult br = new SKLMRESTBackupsResult(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmRestore"))
					 {
						 SKLMRESTRestore r = new SKLMRESTRestore(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmRestoreIsrunning"))
					 {
						 SKLMRESTRestoreIsRunning rir = new SKLMRESTRestoreIsRunning(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmRestoreProgress"))
					 {
						 SKLMRESTRestoreProgress rp = new SKLMRESTRestoreProgress(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmRestoreResult"))
					 {
						 SKLMRESTRestoreResult rr = new SKLMRESTRestoreResult(theURL, theCmd.getArgs(), myToken);
					 }					 
					 
					 
					 //Certificate management
					 else if (cmd.equalsIgnoreCase("sklmCertificateAttributes"))
					 {
						SKLMRESTCertificateAttributes ca = new SKLMRESTCertificateAttributes(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmCertificatesGet"))
					 {
						 SKLMRESTCertificatesGet cg = new SKLMRESTCertificatesGet(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmCertificates"))
					 {
						 SKLMRESTCertificates c = new SKLMRESTCertificates(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmCertificatesPut"))
					 {
						 SKLMRESTCertificatesPut cp = new SKLMRESTCertificatesPut(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmCertificatesBulkCertUpdate"))
					 {
						 SKLMRESTCertificatesBulkCertUpdate cbcu = new SKLMRESTCertificatesBulkCertUpdate(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmCertificatesClient"))
					 {
						 SKLMRESTCertificatesClient cc = new SKLMRESTCertificatesClient(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmCertificatesExport"))
					 {
						 SKLMRESTCertificatesExport ce = new SKLMRESTCertificatesExport(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmCertificatesImport"))
					 {
						 SKLMRESTCertificatesImport ce = new SKLMRESTCertificatesImport(theURL, theCmd.getArgs(), myToken);
					 }					 					 
					 else if (cmd.equalsIgnoreCase("sklmCertificatesRollover"))
					 {
						SKLMRESTCertMgmtRollover cr = new SKLMRESTCertMgmtRollover(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmCertificatesRolloverGet"))
					 {
						 SKLMRESTCertMgmtRolloverGet crg = new SKLMRESTCertMgmtRolloverGet(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmCertificatesRolloverDel"))
					 {
						 SKLMRESTCertMgmtRolloverDel crd = new SKLMRESTCertMgmtRolloverDel(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmCertificatesDel"))
					 {
						 SKLMRESTCertificatesDel ce = new SKLMRESTCertificatesDel(theURL, theCmd.getArgs(), myToken);
					 }					 
					 
					 
                     //Client management
					 else if (cmd.equalsIgnoreCase("sklmClients"))
					 {
						SKLMRESTClients c = new SKLMRESTClients(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmClientsGetAll"))
					 {
						 SKLMRESTClientsGetAll cga = new SKLMRESTClientsGetAll(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmClientsGroups"))
					 {
						SKLMRESTClientsGroups cg = new SKLMRESTClientsGroups(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmClientsGroupsGetAll"))
					 {
						 SKLMRESTClientsGroupsGetAll cgga = new SKLMRESTClientsGroupsGetAll(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmClientsGroupsClientList"))
					 {
						SKLMRESTClientsGroupsClientList cgcl = new SKLMRESTClientsGroupsClientList(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmClientsGroupsUpdateGroupName"))
					 {
						SKLMRESTClientsGroupsUpdateGroupName  cgugn = new SKLMRESTClientsGroupsUpdateGroupName(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmClientsGroupsDel"))
					 {
						 SKLMRESTClientsGroupsDel cgd = new SKLMRESTClientsGroupsDel(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmClientsGroupsGet"))
					 {
						 SKLMRESTClientsGroupsGet cgc = new SKLMRESTClientsGroupsGet(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmClientsGroupsAssignClients"))
					 {
						 SKLMRESTClientsGroupsAssignClients cgac = new SKLMRESTClientsGroupsAssignClients(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmClientsGroupsRemoveClients"))
					 {
						 SKLMRESTClientsGroupsRemoveClients cgrc = new SKLMRESTClientsGroupsRemoveClients(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmClientsUpdateClientName"))
					 {
						 SKLMRESTClientsUpdateClientName cucn = new SKLMRESTClientsUpdateClientName(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmClientsDel"))
					 {
						 SKLMRESTClientsDel cd = new SKLMRESTClientsDel(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmClientsGet"))
					 {
						 SKLMRESTClientsGet cg = new SKLMRESTClientsGet(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmClientsAssignCertificate"))
					 {
						 SKLMRESTClientsAssignCertificate cac = new SKLMRESTClientsAssignCertificate(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmClientsAssignUsers"))
					 {
						SKLMRESTClientsAssignUsers cau = new SKLMRESTClientsAssignUsers(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmClientsListObjectsToClient"))
					 {
						 SKLMRESTClientsListObjectsToClient clotc = new SKLMRESTClientsListObjectsToClient(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmClientsRemoveUsers"))
					 {
						 SKLMRESTClientsRemoveUsers cru = new SKLMRESTClientsRemoveUsers(theURL, theCmd.getArgs(), myToken);
					 }
					 
					 
					 //Conflict resolution					 
					 else if (cmd.equalsIgnoreCase("sklmConflictResolutionChangeCertificateAlias"))
					 {
						 SKLMRESTConflictResolutionChangeCertificateAlias crcca = new SKLMRESTConflictResolutionChangeCertificateAlias(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmConflictResolutionChangeName"))
					 {
						 SKLMRESTConflictResolutionChangeName crcn = new SKLMRESTConflictResolutionChangeName(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmConflictResolutionGetChangeHistory"))
					 {
						 SKLMRESTConflictResolutionGetChangeHistory cegch = new SKLMRESTConflictResolutionGetChangeHistory(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmConflictResolutionRenewKeyAlias"))
					 {
						 SKLMRESTConflictResolutionRenewKeyAlias crrka = new SKLMRESTConflictResolutionRenewKeyAlias(theURL, theCmd.getArgs(), myToken); 
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmConflictResolutionRenewUUID"))
					 {
						 SKLMRESTConflictResolutionRenewUUID crru = new SKLMRESTConflictResolutionRenewUUID(theURL, theCmd.getArgs(), myToken);
					 }			
					 
					 
					 //Device group management
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroupsExport"))
					 {
						 SKLMRESTDeviceGroupsExport dge = new  SKLMRESTDeviceGroupsExport(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroupsExportGet"))
					 {
						 SKLMRESTDeviceGroupsExportGet dge = new SKLMRESTDeviceGroupsExportGet(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroupsExportDeleteExportFile"))
					 {
						 SKLMRESTDeviceGroupsExportDeleteExportFile dgedef = new SKLMRESTDeviceGroupsExportDeleteExportFile(theURL, theCmd.getArgs(), myToken);
					 }					 					 
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroupsExportViewSummaryOfExportFileInADirectory"))
					 {
						 SKLMRESTDeviceGroupsExportViewSummaryOfExportFileInADirectory dgevsoefiad = new  SKLMRESTDeviceGroupsExportViewSummaryOfExportFileInADirectory(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroupsImport"))
					 {
						 SKLMRESTDeviceGroupsImport dgi = new SKLMRESTDeviceGroupsImport(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroupsImportConflicts"))
					 {
						 SKLMRESTDeviceGroupsImportImportConflicts dgiic = new  SKLMRESTDeviceGroupsImportImportConflicts(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroupAttributes"))
					 {
						 SKLMRESTDeviceGroupAttributes dga = new SKLMRESTDeviceGroupAttributes(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroupAttributesPut"))
					 {
						 SKLMRESTDeviceGroupAttributesPut dgap = new SKLMRESTDeviceGroupAttributesPut(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroupAttributesDel"))
					 {
						SKLMRESTDeviceGroupAttributesDel dgag = new  SKLMRESTDeviceGroupAttributesDel(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroupsGet"))
					 {
						 SKLMRESTDeviceGroupsGet dgg = new  SKLMRESTDeviceGroupsGet(theURL, theCmd.getArgs(), myToken);
					 }						 
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroupsBase"))
					 {
						 SKLMRESTDeviceGroupsBase dgb = new SKLMRESTDeviceGroupsBase(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroups"))
					 {
						 SKLMRESTDeviceGroups dg = new SKLMRESTDeviceGroups(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroupsDel"))
					 {
						 SKLMRESTDeviceGroupsDel dgd = new SKLMRESTDeviceGroupsDel(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmDeviceTypes"))
				     {
					     SKLMRESTDeviceTypes dt = new SKLMRESTDeviceTypes(theURL, theCmd.getArgs(), myToken);
				     }					 					 
					 //sklmDeviceTypesGet is the same as sklmDeviceTypes but requires a parameter that's optional in that command
					 //else if (cmd.equalsIgnoreCase("sklmDeviceTypesGet")) //??? - Do we need this?  No...
					 //{
						//(theURL, theCmd.getArgs(), myToken);
					 //}
					 
					 
					 //Device management
					 else if (cmd.equalsIgnoreCase("sklmDevices"))
					 {
						 SKLMRESTDevices d = new SKLMRESTDevices(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmDevicesGet"))
					 {
						 SKLMRESTDevicesGet dg = new SKLMRESTDevicesGet(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmDevicesPut"))
				     {
					     SKLMRESTDevicesPut dp = new SKLMRESTDevicesPut(theURL, theCmd.getArgs(), myToken);
				     }					 					 
					 else if (cmd.equalsIgnoreCase("sklmDevicesDel"))
					 {
						 SKLMRESTDevicesDel dd = new SKLMRESTDevicesDel(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmMachines"))
					 {
						 SKLMRESTMachines m = new SKLMRESTMachines(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMachinesDel"))
					 {
						 SKLMRESTMachinesDel md = new SKLMRESTMachinesDel(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMachinesGet"))
					 {
						 SKLMRESTMachinesGet mg = new SKLMRESTMachinesGet(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMachinesPut"))
					 {
						 SKLMRESTMachinesPut md = new SKLMRESTMachinesPut(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmMachinesDevice"))
					 {
						 SKLMRESTMachinesDevice md = new SKLMRESTMachinesDevice(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMachinesDeviceDel"))
					 {
						 SKLMRESTMachinesDeviceDel mdd = new SKLMRESTMachinesDeviceDel(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMachinesDeviceGet"))
					 {
						 SKLMRESTMachinesDeviceGet mdg = new SKLMRESTMachinesDeviceGet(theURL, theCmd.getArgs(), myToken);
					 }
					 
					 
					 //File transfer
					 else if (cmd.equalsIgnoreCase("sklmDownloadLogs"))
					 {
						 SKLMRESTFileTransferDownloadLogs ftdl = new SKLMRESTFileTransferDownloadLogs(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmDownloadObjectfiles"))
					 {
						SKLMRESTFileTransferDownloadObjectfiles ftdof = new SKLMRESTFileTransferDownloadObjectfiles(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmUploadLicense"))
					 {
						SKLMRESTFileTransferUploadLicense ul = new SKLMRESTFileTransferUploadLicense(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmUploadObjectfiles"))
					 {
						SKLMRESTFileTransferUploadObjectFiles uof = new SKLMRESTFileTransferUploadObjectFiles(theURL, theCmd.getArgs(), myToken);
					 }
					 
					 
					 //Key group management
					 else if (cmd.equalsIgnoreCase("sklmKeygroupentry"))
					 {
						 SKLMRESTKeygroupentry kge = new SKLMRESTKeygroupentry(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmKeygroupentryDel"))
					 {
						 SKLMRESTKeygroupentryDel kged = new SKLMRESTKeygroupentryDel(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmKeygroupsGet"))
					 {
						 SKLMRESTKeygroupsGet kgg = new SKLMRESTKeygroupsGet(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmKeygroupsPut"))
					 {
						 SKLMRESTKeygroupsPut kgp = new SKLMRESTKeygroupsPut(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmKeygroupsRollover"))
					 {
						 SKLMRESTKeygroupsRollover kgr = new SKLMRESTKeygroupsRollover(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmKeygroupsRolloverGet"))
					 {
						 SKLMRESTKeygroupsRolloverGet kgrg = new SKLMRESTKeygroupsRolloverGet(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmKeygroupsRolloverDel"))
					 {
						 SKLMRESTKeygroupsRolloverDel kgrd = new SKLMRESTKeygroupsRolloverDel(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmKeygroups"))
					 {
					     SKLMRESTKeygroups kg = new SKLMRESTKeygroups(theURL, theCmd.getArgs(), myToken);	 
					 }
					 else if (cmd.equalsIgnoreCase("sklmKeygroupsDel"))
				     {
						 SKLMRESTKeygroupsDel kgd = new SKLMRESTKeygroupsDel(theURL, theCmd.getArgs(), myToken);
				     }			
					 
					 
					 //Key management
					 else if (cmd.equalsIgnoreCase("sklmKeyAttributes"))
					 {
						SKLMRESTKeyAttributes ka = new SKLMRESTKeyAttributes(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmKeys"))
					 {
						 SKLMRESTKeys k = new SKLMRESTKeys(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmKeysGet"))
					 {
						 SKLMRESTKeysGet kg = new SKLMRESTKeysGet(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmKeysPut"))
					 {
						 SKLMRESTKeysPut kp = new SKLMRESTKeysPut(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmKeysExport"))
					 {
						 SKLMRESTKeysExport ke = new SKLMRESTKeysExport(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmKeysImport"))
					 {
						 SKLMRESTKeysImport ki = new SKLMRESTKeysImport(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmKeysDel"))
					 {
						 SKLMRESTKeysDel kd = new SKLMRESTKeysDel(theURL, theCmd.getArgs(), myToken);
					 }					 
					 
					 
					 //KMIP secret data management
					 else if (cmd.equalsIgnoreCase("sklmSecretData"))
					 {
						SKLMRESTSecretData sd = new SKLMRESTSecretData(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmSecretDataDel"))
					 {
						 SKLMRESTSecretDataDel sd = new SKLMRESTSecretDataDel(theURL, theCmd.getArgs(), myToken);
					 }					 
					 
					 
					 //KMIP template management
					 else if (cmd.equalsIgnoreCase("sklmKMIPTemplate"))
					 {
						SKLMRESTKMIPTemplate kt = new SKLMRESTKMIPTemplate(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmKMIPTemplateDel"))
					 {
						 SKLMRESTKMIPTemplateDel ktd = new SKLMRESTKMIPTemplateDel(theURL, theCmd.getArgs(), myToken);
					 }


					 //Master key management
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroupMasterKeyGetAll"))
					 {
						SKLMRESTDeviceGroupMasterKeyGetAll dgmkga = new SKLMRESTDeviceGroupMasterKeyGetAll(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroupMasterKeyRotateAll"))
					 {
						 SKLMRESTDeviceGroupMasterKeyRotateAll dgmkra = new  SKLMRESTDeviceGroupMasterKeyRotateAll(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroupMasterKey"))
					 {
						 SKLMRESTDeviceGroupMasterKey dgmk = new SKLMRESTDeviceGroupMasterKey(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroupMasterKeyGet"))
					 {
						 SKLMRESTDeviceGroupMasterKeyGet dgmkg = new SKLMRESTDeviceGroupMasterKeyGet(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmDeviceGroupMasterKeyRotate"))
					 {
						 SKLMRESTDeviceGroupMasterKeyRotate dgmkr = new SKLMRESTDeviceGroupMasterKeyRotate(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMasterKey"))
					 {
						 SKLMRESTMasterKey mk = new SKLMRESTMasterKey(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMasterKeyTransmitter"))
					 {
						 SKLMRESTMasterKeyTransmitter mkt = new SKLMRESTMasterKeyTransmitter(theURL, theCmd.getArgs(), myToken);
					 }
					 
					 
					 //Multi-Master cluster management
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterAddNodes"))
					 {
						SKLMRESTMultiMasterAddNodes mman = new SKLMRESTMultiMasterAddNodes(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterGetClusterName"))
					 {
						SKLMRESTMultiMasterGetClusterName mmgcn = new SKLMRESTMultiMasterGetClusterName(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterIsNodeIsolatedFromCluster"))
					 {
						SKLMRESTMultiMasterIsNodeIsolatedFromCluster mminifc = new SKLMRESTMultiMasterIsNodeIsolatedFromCluster(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterJoinBackTheCluster"))
					 {
						SKLMRESTMultiMasterJoinBackTheCluster mmjbtc = new SKLMRESTMultiMasterJoinBackTheCluster(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterReconfig"))
					 {
						SKLMRESTMultiMasterReconfig mmr = new SKLMRESTMultiMasterReconfig(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterRegenerateClusterName"))
					 {
						SKLMRESTMultiMasterRegenerateClusterName mmrcn = new SKLMRESTMultiMasterRegenerateClusterName(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterRemoveNode"))
					 {
						SKLMRESTMultiMasterRemoveNode mmrn = new SKLMRESTMultiMasterRemoveNode(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterRestartCluster"))
					 {
						SKLMRESTMultiMasterRestartCluster mmrc = new SKLMRESTMultiMasterRestartCluster(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterSetupAsReadWriteMaster"))
					 {
						SKLMRESTMultiMasterSetupAsReadWriteMaster mmsarwm = new SKLMRESTMultiMasterSetupAsReadWriteMaster(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterStopCluster"))
					 {
						 SKLMRESTMultiMasterStopCluster mmsc = new SKLMRESTMultiMasterStopCluster(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterTakeoverAsPrimary"))
					 {
						SKLMRESTMultiMasterTakeoverAsPrimary mmtoap = new SKLMRESTMultiMasterTakeoverAsPrimary(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterUpdateMaster"))
					 {
						SKLMRESTMultiMasterUpdateMaster mmum = new SKLMRESTMultiMasterUpdateMaster(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterExpiringCertificates"))
					 {
						SKLMRESTMultiMasterExpiringCertificates mmec = new SKLMRESTMultiMasterExpiringCertificates(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterState"))
					 {
						SKLMRESTMultiMasterState mms = new SKLMRESTMultiMasterState(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterAgentStatus"))
					 {
						 SKLMRESTMultiMasterAgentStatus mmas = new SKLMRESTMultiMasterAgentStatus(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterStartAgent"))
					 {
						 SKLMRESTMultiMasterStartAgent mmsa = new SKLMRESTMultiMasterStartAgent(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterStopAgent"))
					 {
						SKLMRESTMultiMasterStopAgent mmsa = new SKLMRESTMultiMasterStopAgent(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterNodes"))
					 {
						SKLMRESTMultiMasterNodes mmn = new SKLMRESTMultiMasterNodes(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterAllDBNodeStatus"))
					 {
						 SKLMRESTMultiMasterAllDBNodeStatus mmans = new SKLMRESTMultiMasterAllDBNodeStatus(theURL, theCmd.getArgs(), myToken);
					 }					 					 
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterAllNodeStatus"))
					 {
						 SKLMRESTMultiMasterAllNodeStatus mmans = new SKLMRESTMultiMasterAllNodeStatus(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterCheckPreRequisite"))
					 {
						SKLMRESTMultiMasterCheckPreRequisite mmcpr = new SKLMRESTMultiMasterCheckPreRequisite(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterGetClusterHADRStatus"))
					 {
						 SKLMRESTMultiMasterGetClusterHADRStatus mmgcs = new SKLMRESTMultiMasterGetClusterHADRStatus(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmMultiMasterGetThisNodeDetails"))
					 {
						SKLMRESTMultiMasterGetThisNodeDetails mmgtnd = new SKLMRESTMultiMasterGetThisNodeDetails(theURL, theCmd.getArgs(), myToken);
					 }
					 
					 
					 //Object management
					 else if (cmd.equalsIgnoreCase("sklmObjects"))
					 {
						SKLMRESTObjects o = new SKLMRESTObjects(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmObjectsCertificate"))
					 {
						 SKLMRESTObjectsCertificate oc = new SKLMRESTObjectsCertificate(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmObjectsKeypair"))
					 {
						 SKLMRESTObjectsKeypair ok = new SKLMRESTObjectsKeypair(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmObjectsOpaque"))
					 {
						 SKLMRESTObjectsOpaque oo = new SKLMRESTObjectsOpaque(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmObjectsSecret"))
					 {
						 SKLMRESTObjectsSecret os = new SKLMRESTObjectsSecret(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmObjectsSymmetricKey"))
					 {
						 SKLMRESTObjectsSymmetricKey osk = new SKLMRESTObjectsSymmetricKey(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmObjectsGet"))
					 {
						 SKLMRESTObjectsGet og = new SKLMRESTObjectsGet(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmObjectsDel"))
					 {
						 SKLMRESTObjectsDel od = new SKLMRESTObjectsDel(theURL, theCmd.getArgs(), myToken);
					 }
					 
					 
					 //Pending object management
					 else if (cmd.equalsIgnoreCase("sklmPendingClientCertificates"))
					 {
						 SKLMRESTPendingClientCertificates pcc = new  SKLMRESTPendingClientCertificates(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmPendingClientCertificatesGet"))
					 {
						 SKLMRESTPendingClientCertificatesGet pccg = new SKLMRESTPendingClientCertificatesGet(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmPendingClientCertificatesDel"))
					 {
						 SKLMRESTPendingClientCertificatesDel pccd = new SKLMRESTPendingClientCertificatesDel(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmPendingDevices"))
					 {
						 SKLMRESTPendingDevices pd = new SKLMRESTPendingDevices(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmPendingDevicesGet"))
					 {
						 SKLMRESTPendingDevicesGet pdg = new  SKLMRESTPendingDevicesGet(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmPendingDevicesDel"))
					 {
						 SKLMRESTPendingDevicesDel pdd = new SKLMRESTPendingDevicesDel(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmPendingMachineDevices"))
					 {
						 SKLMRESTPendingMachineDevices pmd = new SKLMRESTPendingMachineDevices(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmPendingMachineDevicesAccept"))
					 {
						 SKLMRESTPendingMachineDevicesAccept pmda = new SKLMRESTPendingMachineDevicesAccept(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmPendingMachineDevicesReject"))
					 {
						 SKLMRESTPendingMachineDevicesReject pmr = new SKLMRESTPendingMachineDevicesReject(theURL, theCmd.getArgs(), myToken);
					 }
					 
					 
					 //Replication
					 else if (cmd.equalsIgnoreCase("sklmReplicateNow"))
					 {
						SKLMRESTReplicateNow rn = new SKLMRESTReplicateNow(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmReplicateStart"))
					 {
						SKLMRESTReplicateStart rs = new SKLMRESTReplicateStart(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmReplicateStatus"))
					 {
						SKLMRESTReplicateStatus rs2 = new SKLMRESTReplicateStatus(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmReplicateStop"))
					 {
						SKLMRESTReplicateStop rs3 = new SKLMRESTReplicateStop(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmReplicationConfigProperties"))
					 {
						 SKLMRESTReplicationConfigProperties rcpg = new SKLMRESTReplicationConfigProperties(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmReplicationConfigPropertiesPut"))
					 {
						 SKLMRESTReplicationConfigPropertiesPut rcpp = new SKLMRESTReplicationConfigPropertiesPut(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmReplicationConfigPropertiesDel"))
					 {
						 SKLMRESTReplicationConfigPropertiesDel rcpd = new SKLMRESTReplicationConfigPropertiesDel(theURL, theCmd.getArgs(), myToken);
					 }					 
					 //else if (cmd.equalsIgnoreCase("sklmReplicationConfigPropertiesGet"))
					 //{
						 //I don't think we need this
					 //}	
					 
					 
					 //Served data archival
					 else if (cmd.equalsIgnoreCase("sklmServedData"))
					 {
						SKLMRESTServedData sd = new SKLMRESTServedData(theURL, theCmd.getArgs(), myToken); 
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmServedDataGet"))
					 {
						 SKLMRESTServedDataGet sd = new SKLMRESTServedDataGet(theURL, theCmd.getArgs(), myToken);
					 }					 
					 
					 
					 //Server configuration
					 else if (cmd.equalsIgnoreCase("sklmConfigPropertiesGetAll"))
					 {
						 SKLMRESTConfigPropertiesGetAll cpga = new SKLMRESTConfigPropertiesGetAll(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmConfigPropertiesPut"))
					 {
						 SKLMRESTConfigPropertiesPut cpp = new SKLMRESTConfigPropertiesPut(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmConfigPropertiesDel"))
					 {
						 SKLMRESTConfigPropertiesDel cpd = new SKLMRESTConfigPropertiesDel(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmConfigPropertiesGet"))  //?? - I don't think we need this one
					 {
						 SKLMRESTConfigPropertiesGet cpg = new SKLMRESTConfigPropertiesGet(theURL, theCmd.getArgs(), myToken);
					 }
					 
					 
					 //Server management
					 else if (cmd.equalsIgnoreCase("sklmServerChangePasswordDB2MultiMaster"))
					 {
						 SKLMRESTServerChangePasswordDB2MultiMaster scpdmm = new SKLMRESTServerChangePasswordDB2MultiMaster(theURL, theCmd.getArgs(), myToken);
					 }			
					 else if (cmd.equalsIgnoreCase("sklmServerChangePasswordDB2Standalone"))
					 {
						 SKLMRESTServerChangePasswordDB2Standalone scpdsa = new SKLMRESTServerChangePasswordDB2Standalone(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmServerCounts"))
					 {
						 SKLMRESTServerCounts sc = new SKLMRESTServerCounts(theURL, theCmd.getArgs(), myToken);
					 }		
					//else if (cmd.equalsIgnoreCase("sklmCounts2"))  //??? - Is this needed??  No
					 //{
						//(theURL, theCmd.getArgs(), myToken);
					 //}	
					 else if (cmd.equalsIgnoreCase("sklmServerKerberosConfigure"))
					 {
						 SKLMRESTServerKerberosConfigure skc = new SKLMRESTServerKerberosConfigure(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmServerKerberosConfigureOnMM"))
					 {
						 SKLMRESTServerKerberosConfigureOnMM skcomm = new SKLMRESTServerKerberosConfigureOnMM(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmServerKerberosGetConfiguration"))
					 {
						 SKLMRESTServerKerberosGetConfiguration skc = new SKLMRESTServerKerberosGetConfiguration(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmServerKerberosRemove"))
					 {
						 SKLMRESTServerKerberosRemove skr = new SKLMRESTServerKerberosRemove(theURL, theCmd.getArgs(), myToken);						 
					 }
					 else if (cmd.equalsIgnoreCase("sklmServerRestartServer"))
					 {
						 SKLMRESTServerRestartServer srs = new SKLMRESTServerRestartServer(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmServerVersionInfo"))
					 {
						 SKLMRESTServerVersionInfo svi = new SKLMRESTServerVersionInfo(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("sklmServerKeyServerStatus"))
					 {
						 SKLMRESTServerKeyServerStatus skss = new SKLMRESTServerKeyServerStatus(theURL, theCmd.getArgs(), myToken);
					 }					 					 				 
					 else if (cmd.equalsIgnoreCase("sklmServerSystemDetails"))
					 {
						 SKLMRESTServerSystemDetails ssd = new SKLMRESTServerSystemDetails(theURL, theCmd.getArgs(), myToken);
					 }					 
					 

					 //Truststore management
					 else if (cmd.equalsIgnoreCase("sklmTruststoreCertificates"))
					 {
						SKLMRESTTruststoreCertificates tsc = new SKLMRESTTruststoreCertificates(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmAddCertToTruststore"))
					 {
						 SKLMRESTTruststoreAddCertToTruststore actt= new SKLMRESTTruststoreAddCertToTruststore(theURL, theCmd.getArgs(), myToken);
					 }
					 else if (cmd.equalsIgnoreCase("sklmDeleteCertFromTruststore"))
					 {
						 SKLMRESTTruststoreDeleteCertFromTruststore dcft = new SKLMRESTTruststoreDeleteCertFromTruststore(theURL, theCmd.getArgs(), myToken);
					 }
					 
					 
					 //User Administration
					 else if (cmd.equalsIgnoreCase("sklmUserPassword"))
					 {
						SKLMRESTUserPassword up = new SKLMRESTUserPassword(theURL, theCmd.getArgs(), myToken);
					 }					 
					 else if (cmd.equalsIgnoreCase("help") || cmd.equals("?"))
				     {
						 printAvailableCommands();
				     }
					 else if (cmd.equals("")) {} //Do nothing here...
				     else
				     {
				         System.out.println(cmd + " not implemented.");
				         logger.debug("" + cmd + " not implemented.");
				     }
                     if (!(theCommand.equals("")))
					 {					  
					    try {  theCommand = theCommandList.remove(0); }
					    catch (IndexOutOfBoundsException e)
					    { 
					        theCommand=""; 
					    	keepGoing=false;
					    }
					 }
				  }
				  else
				  {
					  System.out.println("Invalid command.");
					  logger.debug(cmd + " not implemented.");
					  
					  //a fix1 - invalid command on command line causes loop
                      if (!(theCommand.equals("")))
 	                  {					  
					      try {  theCommand = theCommandList.remove(0); }
						  catch (IndexOutOfBoundsException e)
						  { 
						     theCommand=""; 
						     keepGoing=false;
						  }
					  }
                      //e fix1
				  }
			  }
		  }
		  scan.close();
	  }
	  else {
		  logger.debug("Unable to login");
	  }
	     
	  //getAuthToken()
   }
}