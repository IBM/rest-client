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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReadParsedSKLMYaml {
	private static final Logger logger = LogManager.getLogger(ReadParsedSKLMYaml.class.getName());

	private static ReadParsedSKLMYaml theParsedFile = null;
	private HashMap<String,SKLMRESTSpec> theSKLMRESTData = new HashMap<String,SKLMRESTSpec>();
	
	private ArrayList<String> bodyArgs=new ArrayList<String>();
	private ArrayList<String> queryArgs=new ArrayList<String>();
	private ArrayList<String> pathArgs=new ArrayList<String>();
	private ArrayList<String> headerArgs=new ArrayList<String>();
	
	public ArrayList<String> getBodyArgs()   {return bodyArgs;}
	public ArrayList<String> getQueryArgs()  {return queryArgs;}
	public ArrayList<String> getPathArgs()   {return pathArgs;}
	public ArrayList<String> getHeaderArgs() {return headerArgs;}
	
	//public HashMap<String,SKLMRESTSpec> getTheSKLMRESTData() {return theSKLMRESTData;}
	
	public SKLMRESTSpec getSKLMRESTSpec(String path)
	{
	   logger.traceEntry();
	   if (theSKLMRESTData != null)
	   {
		   return theSKLMRESTData.get(path);
	   }
	   return null;
	}
	
	public static ReadParsedSKLMYaml getInstance()
	{
	   logger.traceEntry();
	   if (theParsedFile==null)
		   theParsedFile=new ReadParsedSKLMYaml();
	   return theParsedFile;
	}
	
	private ReadParsedSKLMYaml() 
	{
		logger.traceEntry();
		logger.info("Reading YAML...");
		boolean isOK = false;
		try {
			File f = new File("ParsedSKLMYaml.txt");
			Scanner reader = new Scanner(f);
			
			SKLMRESTSpec curspec=null;
			String curtype="";
			
			mainloop:
			while (reader.hasNextLine())
			{
				String line = reader.nextLine();
				logger.trace("Parsing: "+line);
				
				String[] keyvals = line.split(":");
				//for (String a : keyvals)
		        //    System.out.println(a);
				
				switch (keyvals[0]) {
				case "Path":  //Done
					isOK=false;
					if (curspec!=null) {
						logger.error("Path found but not expectied in parsed YAML.");						
						break mainloop;
					}
					if (theSKLMRESTData.containsKey(keyvals[1])) {
						logger.error("Duplicate path found: "+keyvals[1]);
						break mainloop;
					}
					SKLMRESTSpec spec = new SKLMRESTSpec();
					spec.setPath(keyvals[1]);
					theSKLMRESTData.put(keyvals[1],spec);
					curspec=spec;
					break;
					
				case "Type":
					if (curspec!=null)
					{
						curtype=keyvals[1];
						break;
					}
					else {
						logger.error("Type found but not expectied in parsed YAML.");
						isOK=false;
						break mainloop;
					}
				case "Summary":
					if (curspec!=null && curtype != "")
					{
						curspec.setSummary(curtype, keyvals[1]);
						break;
					}
					else {
						logger.error("Summary found but not expectied in parsed YAML.");
						isOK=false;
						break mainloop;
					}

				case "Attributes":
					if (curspec!=null && curtype != "")
					{
					   if (keyvals.length>1)
					   {
						   String[] attributes = keyvals[1].split(",");
						   for (String a: attributes)
							   curspec.addAttribute(curtype, a, false);
					   }
					   break;
					}
					else {
						logger.error("Attributes found but not expectied in parsed YAML.");
						isOK=false;
						break mainloop;
					}

				case "RequiredAttributes":
					if (curspec!=null)
					{
						if (keyvals.length>1)
						{
							String[] reqattributes = keyvals[1].split(",");
							for (String r: reqattributes)
								curspec.addAttribute(curtype, r, true);
						}
							break;
					}
					else {
						logger.error("Req Attributes found but not expectied in parsed YAML.");
						isOK=false;
						break mainloop;
					}
					
				case "AttributeLocs":
					if (curspec!=null && curtype != "")
					{
					   if (keyvals.length>1)
					   {
						   String[] attributeLocs = keyvals[1].split(",");
						   for (String l: attributeLocs)
							   curspec.addAttributeLoc(curtype, l);
					   }
					   break;
					}
					else {
						logger.error("Attributes found but not expectied in parsed YAML.");
						isOK=false;
						break mainloop;
					}


				case "--END--":  //DONE
					if (curspec!=null) {
						isOK=true;
						curspec=null;
						curtype="";
					}
					else {
						logger.error("End found but not expectied in parsed YAML.");
						isOK=false;
						break mainloop;
					}
				}				
			} //end of while
			reader.close();
		} catch (FileNotFoundException e) {
			logger.error("File not found, proceeding without params.");
			e.printStackTrace();
		}
		//If things didn't go well, we are done....
		if (!isOK) {
			theSKLMRESTData=null;
			logger.fatal("Unable to parse YAML file, exiting.");
			System.out.println("Unable to read data from YAML config file, see log for more details.");
			System.exit(1);
		}		
	}
	
	//Fix 6
	/*
	 * Common function to do the argument split so we don't need the regex in multiple places
	 */
	public String[] doCmdSplit(String args)
	{
		return args.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);  //Drop empty strings (-1)
		//                   !"  "   !"  "
		//?= positive look ahead
		//?: non capturing group
		

		//for (String arg: argList)
		//{
            //System.out.println("Arg:"+arg);
		//}				
		
	}

	/*
	 * Common function to handle the splitting of a single arg.  Need to guarantee exactly 2 items.
	 */
	public String[] doArgSplit(String arg)
	{
		 String tmpVals[] = arg.split("=");
         String vals[] = new String[2];
         vals[0] = tmpVals[0];
         vals[1] = "";
         if (tmpVals.length != 1)
            vals[1]=tmpVals[1];

         logger.debug("param:"+vals[0]);
         logger.debug("value:"+vals[1]);
         
         return vals;
	}
	//e Fix6
	
	
	
	/*
	 * checkArguments will verify that passed in arguments match what the command accepts and
	 * that all required parameters were used
	 */
	public boolean checkArguments(String path, String type, String args)
	{
		logger.traceEntry();
		logger.debug("Path:"+path);
		logger.debug("Type:"+type);
		logger.debug("Args:"+args);
		SKLMRESTSpec theSpec = null;
		ArrayList<String> attributes = null;
		ArrayList<String> reqAttributes = null;
		
		if (theParsedFile!=null)
		{
			theSpec = theParsedFile.getSKLMRESTSpec(path);
			if (theSpec!=null)
			{
				attributes = theSpec.getAttributes(type);
				reqAttributes = theSpec.getRequiredAttributes(type);
			}
			else {
				logger.error("How is the spec null?  Invalid path...");
				return false;
			}
			if (args!=null)
			{
				String argList[] = doCmdSplit(args);				
				
				for (String arg: argList)
				{
					long quoteCnt = arg.chars().filter(c -> c == '\"').count();  //It might be better to just loop...
					if (quoteCnt % 2 !=0)
					{
						logger.error("Mismatched quotes in arguments");
						System.out.println("Mismatched quotes in arguments");
						return false;
					}					
					
					arg=arg.replaceAll("\"","");  //If the string was quoted, get rid of the quotes
					
					logger.debug("Found arg:"+arg);
					if (!arg.contains("="))
					{
						logger.error("Invalid argument:"+arg);
						System.out.println("Invalid argument:"+arg);
						return false;
					}
					
					String vals[] = doArgSplit(arg);

					if (attributes!=null)
					{
						if (reqAttributes!=null && reqAttributes.contains(vals[0]))
						{
							reqAttributes.remove(vals[0]);
							attributes.remove(vals[0]);
						}
						else if (attributes.contains(vals[0]))
						{
							attributes.remove(vals[0]);
						}
						else {
							logger.error("Invalid argument:"+arg);
							System.out.println("Invalid argument:"+arg);
							return false;
						}
					}
				}
			}
			
			if (reqAttributes.isEmpty())
				return true;
			else
			{
				logger.error("Missing required attributes:");
				System.out.println("Missing required attributes:");
				for (String s: reqAttributes)
				{
					System.out.println(s);
					logger.error(s);
				}
			}
		}
		else
		{
			logger.error("The parsed file is null...we should not get here.");
		}
		return false;		
	}
	
	
	/*
	 * checkArguments will verify that passed in arguments match what the command accepts and
	 * that all required parameters were used
	 */
	public boolean checkArgumentsWithRepeatables(String path, String type, String args, ArrayList<String> repeatableAttributes)
	{
		logger.traceEntry();
		logger.debug("Path:"+path);
		logger.debug("Type:"+type);
		logger.debug("Args:"+args);
		logger.debug("Repeatables:"+repeatableAttributes);
		SKLMRESTSpec theSpec = null;
		ArrayList<String> attributes = null;
		ArrayList<String> reqAttributes = null;
		
		//Init the count of repeated attributes
		Map<String, Integer> map = new HashMap<>();		
		for (String repAttr: repeatableAttributes)
			map.put(repAttr, 0);
		
		if (theParsedFile!=null)
		{
			theSpec = theParsedFile.getSKLMRESTSpec(path);
			if (theSpec!=null)
			{
				attributes = theSpec.getAttributes(type);
				reqAttributes = theSpec.getRequiredAttributes(type);
			}
			else {
				logger.error("How is the spec null?  Invalid path...");
				return false;
			}
			if (args!=null)
			{
				String argList[] = doCmdSplit(args);

				for (String arg: argList)
				{
					long quoteCnt = arg.chars().filter(c -> c == '\"').count();  //It might be better to just loop...
					if (quoteCnt % 2 !=0)
					{
						logger.error("Mismatched quotes in arguments");
						System.out.println("Mismatched quotes in arguments");
						return false;
					}					
					
					arg=arg.replaceAll("\"","");  //If the string was quoted, get rid of the quotes
					
					logger.debug("Found arg:"+arg);
					if (!arg.contains("="))
					{
						logger.error("Invalid argument:"+arg);
						System.out.println("Invalid argument:"+arg);
						return false;
					}
					
					String vals[] = doArgSplit(arg);
					
					if (attributes!=null)
					{
						if (reqAttributes!=null && reqAttributes.contains(vals[0]))
						{
							reqAttributes.remove(vals[0]);
							attributes.remove(vals[0]);
						}
						else if (attributes.contains(vals[0]))
						{
							attributes.remove(vals[0]);
						}
						//Check repeatable
						else if (repeatableAttributes!=null && repeatableAttributes.contains(vals[0]))
						{
							int count = map.get(vals[0]);
							map.put(vals[0], count+1);
						}
						else {
							logger.error("Invalid argument:"+arg);
							System.out.println("Invalid argument:"+arg);
							return false;
						}
					}
				}
			}
			
			//Check repeatables
			int count = -1;
			for (String repAttr: repeatableAttributes)
			{
				if (count==-1)
					count = map.get(repAttr);
				else
					if (count!=map.get(repAttr))
					{
						System.out.println("Mismatch in number of arguments for repeatable section.");
						logger.error("Mismatch in number of arguments for repeatable section. "+repAttr+" "+count);
						return false;
					}
			}
			
			
			if (reqAttributes.isEmpty())
				return true;
			else
			{
				logger.error("Missing required attributes:");
				System.out.println("Missing required attributes:");
				for (String s: reqAttributes)
				{
					System.out.println(s);
					logger.error(s);
				}
			}
		}
		else
		{
			logger.error("The parsed file is null...we should not get here.");
		}
		return false;		
	}

	
	//This function is for special cases where you can put random things in for parameters like config file attributes
	//that aren't listed in the data we have as checkable items.  We will just verify the format.
	public boolean checkArgumentsForFormatOnly(String path, String type, String args)
	{
		logger.traceEntry();
		logger.debug("Path:"+path);
		logger.debug("Type:"+type);
		logger.debug("Args:"+args);
		
		SKLMRESTSpec theSpec = null;		
		boolean attributesRequired = false;		
		ArrayList<String> reqAttributes = null;
		
		if (theParsedFile!=null)
		{
			theSpec = theParsedFile.getSKLMRESTSpec(path);
			if (theSpec!=null)
			{
				reqAttributes = theSpec.getAttributes(type);
				if (reqAttributes!=null)
					attributesRequired = true;
				logger.debug("Attributes required for this cmd? "+attributesRequired);
			}
			else {
				logger.error("How is the spec null?  Invalid path...");
				return false;
			}
			
			if (args!=null)
			{
				String argList[] = doCmdSplit(args);				

				for (String arg: argList)
				{
					long quoteCnt = arg.chars().filter(c -> c == '\"').count();  //It might be better to just loop...
					if (quoteCnt % 2 !=0)
					{
						logger.error("Mismatched quotes in arguments");
						System.out.println("Mismatched quotes in arguments");
						return false;
					}					
					
					arg=arg.replaceAll("\"","");  //If the string was quoted, get rid of the quotes
					
					logger.debug("Found arg:"+arg);
					if (!arg.contains("="))
					{
						logger.error("Invalid argument:"+arg);
						System.out.println("Invalid argument:"+arg);
						return false;
					}
					
					String vals[] = doArgSplit(arg);
				}
				return true;
			}
		}
		else
		{
			logger.error("The parsed file is null...we should not get here.");
		}
		return false;				
	}
	
	/*
	 * buildArgumentList will take the passed in arguments, which should have already been validated
	 * and attach the location that the parameter should appear in the request
	 * 
	 * Items come in as:  arg1=val1,arg2=val2,arg3=val3
	 * Go out as:         loc1=arg1=val1,loc2=arg2=val2,loc3=arg3=val3
	 * 
	 * We should not need to do the same argument checks since they have already been done
	 */
	public void buildArgumentLists(String path, String type, String args)
	{
		logger.traceEntry();
		SKLMRESTSpec theSpec = theParsedFile.getSKLMRESTSpec(path);
		ArrayList<String> attributes = theSpec.getAttributes(type);
		for (String a: attributes)
			logger.debug("Attribute:"+a);
		ArrayList<String> attributeLocs = theSpec.getAttributeLocs(type);
		
		bodyArgs.clear();
		queryArgs.clear();
		pathArgs.clear();
		headerArgs.clear();

		if (args != null)
		{
		   String argList[] = doCmdSplit(args);
			
		   for (String arg: argList)
		   {
			   arg=arg.replaceAll("\"","");  //If the string was quoted, get rid of the quotes
			   logger.debug("Found arg:"+arg);
			   String vals[] = doArgSplit(arg);
			   
			   int pos=attributes.indexOf(vals[0]);
			   logger.debug("Found at pos:"+pos);
			
			   //Added for special case /SKLM/rest/v1/configProperties Put
			   String loc = "body";
			   if (pos==-1)
			   {
				   logger.warn("Possibly a special case, ensure command is run correctly.");
			   }
			   else
			      loc = attributeLocs.get(pos);
			   
			   if (loc.equals("header"))
			   {
				   headerArgs.add(arg);
			   }
			   else if (loc.equals("body"))
			   {
				   bodyArgs.add(arg);
			   }
			   else if (loc.equals("query"))
			   {
				   queryArgs.add(arg);
			   }
			   else if (loc.equals("path"))
			   {
				   pathArgs.add(arg);
			   }
			   else 
			   {
				   logger.warn("Unknown location for attribute:"+loc+".  Adding to body.");
				   bodyArgs.add(arg);
			   }
		   }
		}
		
		
		for (String s: headerArgs)
			logger.debug("Header:"+s);
		for (String s: bodyArgs)
			logger.debug("Body:"+s);
		for (String s: queryArgs)
			logger.debug("Query:"+s);
		for (String s: pathArgs)
			logger.debug("Path:"+s);
	}
	
	
	//public static void main(String[] args)
	//{
	//	ReadParsedSKLMYaml X = new ReadParsedSKLMYaml();
	//}

}