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

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SKLMRESTSpec {

	public class SKLMRESTData {
		private String summary;
		private ArrayList<String> attributes;
		private ArrayList<String> required;
		private ArrayList<String> attributeLocs;

		public SKLMRESTData()
		{
			summary       = "";
			attributes    = new ArrayList<String>();
			required      = new ArrayList<String>();
			attributeLocs = new ArrayList<String>();
		}
		
		public String getSummary()       { return summary; }
		public void setSummary(String s) { summary = s; }
		
		public ArrayList<String> getAttributes() { return attributes; }
		public void addAttribute(String a)       { attributes.add(a); }
		
		public ArrayList<String> getRequired() { return required; }
		public void addRequired(String r)      { required.add(r); }
		
		public ArrayList<String> getAttributeLoc() { return attributeLocs; }
		public void addAttributeLoc(String l)      { attributeLocs.add(l); }

	}
	
	private static final Logger logger = LogManager.getLogger(SKLMRESTSpec.class.getName());

	private String path              = null;
	private SKLMRESTData getBlock    = null;
	private SKLMRESTData putBlock    = null;
	private SKLMRESTData deleteBlock = null;
	private SKLMRESTData postBlock   = null;

	public String getPath()       {return path;}
	public void setPath(String p) {path=p;}

	public ArrayList<String> getAttributes(String type)
	{
		logger.traceEntry();
		switch (type) {
           case "post": 
           case "POST":
			   if (postBlock!=null) 
				   return new ArrayList<String>(postBlock.getAttributes());
			   else
				   return null;
		   case "get":
		   case "GET":
			   if (getBlock!=null)
				   //return getBlock.getAttributes();
			   return new ArrayList<String>(getBlock.getAttributes());
			   else
				   return null;
		   case "delete":
		   case "DELETE":
			   if (deleteBlock!=null)
				   return new ArrayList<String>(deleteBlock.getAttributes());
			   else
				   return null;
		   case "put":
		   case "PUT":
			   if (putBlock!=null)
				   return new ArrayList<String>(putBlock.getAttributes());
			   else
				   return null;
		   default: 
			  System.out.println ("Unknown method "+type+" getting attributes.");
			  return null;
		}
	}

	public ArrayList<String> getRequiredAttributes(String type)
	{
	    logger.traceEntry();
		switch (type) {
		   case "post": 
		   case "POST":
			   if (postBlock!=null)
				   return new ArrayList<String>(postBlock.getRequired());
			   else
				   return null;
		   case "get":
		   case "GET":
			   if (getBlock!=null)
				   return new ArrayList<String>(getBlock.getRequired());
			   else
				   return null;
		   case "delete": 
		   case "DELETE":
			   if (deleteBlock!=null)
				   return new ArrayList<String>(deleteBlock.getRequired());
			   else
				   return null;
		   case "put":
		   case "PUT":
			   if (putBlock!=null)
				   return new ArrayList<String>(putBlock.getRequired());
			   else
				   return null;			   
		   default: 
			  logger.error("Unknown method "+type+" getting req attributes.");
			  return null;
		}
	}
	
	//We never would alter these so return the actual obj
	public ArrayList<String> getAttributeLocs(String type)
	{
		logger.traceEntry();
		switch (type) {
		   case "post":
		   case "POST":
			   if (postBlock!=null)
				   return postBlock.getAttributeLoc();
			   else
				   return null;
		   case "get":
		   case "GET":
			   if (getBlock!=null)
				   return getBlock.getAttributeLoc();
			   else
				   return null;
		   case "delete":
		   case "DELETE":
			   if (deleteBlock!=null)
				   return deleteBlock.getAttributeLoc();
			   else
				   return null;
		   case "put":
		   case "PUT":
			   if (putBlock!=null)
				   return putBlock.getAttributeLoc();
			   else
				   return null;			   
		   default: 
			   logger.error("Unknown method "+type+" getting attribute locs.");
			  return null;
		}
	}
	

	public boolean setSummary(String type, String summary)
	{
		logger.traceEntry();
		switch (type) {
		   case "post":
			   if (postBlock==null)
				   postBlock=new SKLMRESTData();
			   postBlock.setSummary(summary);
			   break;
			   
		   case "get":
			   if (getBlock==null)
				   getBlock=new SKLMRESTData();
			   getBlock.setSummary(summary);
			   break;


		   case "delete":
			   if (deleteBlock==null)
				   deleteBlock=new SKLMRESTData();
			   deleteBlock.setSummary(summary);
			   break;

		   case "put":
			   if (putBlock==null)
				   putBlock=new SKLMRESTData();
			   putBlock.setSummary(summary);
			   break;

		   default: 
			  logger.error("Unknown method "+type+" setting summary.");
			  return false;
		}
		return true;
	}

	public String getSummary(String type)
	{
		logger.traceEntry();
		String summary="";
		switch (type) {
		   case "post":
		   case "POST":
			   if (postBlock!=null)
				   summary=postBlock.getSummary();
			   break;
			   
		   case "get":
		   case "GET":
			   if (getBlock!=null)
				   summary=getBlock.getSummary();
			   break;

		   case "delete":
		   case "DELETE":			   
			   if (deleteBlock!=null)
				   summary=deleteBlock.getSummary();
			   break;

		   case "put":
		   case "PUT":			   
			   if (putBlock!=null)
				   summary=putBlock.getSummary();
			   break;

		   default: 
			   logger.error("Unknown method "+type+" getting summary.");
		}
		return summary;
	}

	public boolean addAttribute(String type, String attribute, boolean required)
	{
		logger.traceEntry();
		switch (type) {
		   case "post":
			   if (postBlock==null)
				   postBlock=new SKLMRESTData();
			   if (required)
				   postBlock.addRequired(attribute);
			   else
				   postBlock.addAttribute(attribute);
			   break;
			   
		   case "get":
			   if (getBlock==null)
				   getBlock=new SKLMRESTData();
			   if (required)
				   getBlock.addRequired(attribute);
			   else
				   getBlock.addAttribute(attribute);
			   break;

		   case "delete":
			   if (deleteBlock==null)
				   deleteBlock=new SKLMRESTData();
			   if (required)
				   deleteBlock.addRequired(attribute);
			   else
				   deleteBlock.addAttribute(attribute);
			   break;

		   case "put":			   
			   if (putBlock==null)
				   putBlock=new SKLMRESTData();
			   if (required)
				   putBlock.addRequired(attribute);
			   else
				   putBlock.addAttribute(attribute);
			   break;

		   default: 
			   logger.error("Unknown method "+type+" reading attributes.");
			   return false;
		}
		return true;
	}
	
	public boolean addAttributeLoc(String type, String attributeLoc)
	{
		logger.traceEntry();
		switch (type) {
		   case "post":
			   if (postBlock==null)
				   postBlock=new SKLMRESTData();
				   postBlock.addAttributeLoc(attributeLoc);
			   break;
			   
		   case "get":
			   if (getBlock==null)
				   getBlock=new SKLMRESTData();
				   getBlock.addAttributeLoc(attributeLoc);
			   break;

		   case "delete":
			   if (deleteBlock==null)
				   deleteBlock=new SKLMRESTData();
				   deleteBlock.addAttributeLoc(attributeLoc);
			   break;

		   case "put":
			   if (putBlock==null)
				   putBlock=new SKLMRESTData();
				   putBlock.addAttributeLoc(attributeLoc);
			   break;

		   default: 
			   logger.error("Unknown method "+type+" reading attribute locs.");
			   return false;
		}
		return true;
	}
}