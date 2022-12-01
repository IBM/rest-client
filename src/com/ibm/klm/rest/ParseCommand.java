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

import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;

public class ParseCommand {
	
	private String cmd = null;
	private String args = null;
	
	private static final Logger logger = LogManager.getLogger(ParseCommand.class.getName());

    public ParseCommand(String theCommand) 
    {    	
    	logger.traceEntry();
    	
    	if (theCommand.trim().equalsIgnoreCase("help") || theCommand.trim().equals("?"))
    	{
    		cmd = "help";
    		return;
    	}
    	
    	if (theCommand.trim().equalsIgnoreCase(""))
    	{
    		cmd = "";
    		return;
    	}
    	
    	//String patternString = "\\s*?(\\w+)\\s*?\\(([\\w,=]+)?\\)";  //Begin and end of string?  v1
    	//Fix 3
    	//String patternString = "^\\s*?(\\w+)\\s*\\(\\s*([\\w,-= ]+)?\\s*\\)\\s*$";  //Begin and end of string?  v2
    	String patternString = "^\\s*?(\\w+)\\s*\\(\\s*([\\w,-=~@/+ \"\\\\]+)?\\s*\\)\\s*$";  //Begin and end of string?
    	//e Fix 3
    	
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(theCommand);

        if (matcher.find())
		{
		   cmd = matcher.group(1);
           args = matcher.group(2);

           //Now try to remove any extraneous spaces from the arguments
           if (args != null)
           {
              args=args.replaceAll("\\s+=", "=");
              args=args.replaceAll("=\\s+", "=");
              args=args.replaceAll("\\s+,", ",");
              args=args.replaceAll(",\\s+", ",");
           }
           logger.debug("Command:"+cmd);
           logger.debug("Arguments:"+args);
		}
    }
	
	public String getCmd() {return cmd;}
	public String getArgs() {return args;}
}