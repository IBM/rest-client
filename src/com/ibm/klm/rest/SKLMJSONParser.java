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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class SKLMJSONParser {
	private static final Logger logger = LogManager.getLogger(SKLMJSONParser.class.getName());

    private HashMap<String, String> keyValuePairs = null;
	
    public HashMap<String, String> getKeyValuePairs()  { return keyValuePairs; }

    public static void printJSONString(String theString)
	{
    	logger.traceEntry();
    	if (theString.trim().equals(""))  //Nothing to print
    		return;
    	
        StringWriter sw = new StringWriter();

        Boolean isArray=false;
        if (theString.startsWith("["))
           isArray=true;
        
        //For some reason and empty JSON string causes the code below to throw an exception
        //when calling readObject, so we will just check for that case here.
        //if (theString.equals("[]"))
        //{
        //	System.out.println(theString);
        //    return;
	    //}
        
        try { 
            JsonObject jobj=null;
            JsonArray jarr=null;

            JsonReader jr = Json.createReader(new StringReader(theString));         
            if (isArray)
            	jarr = jr.readArray();
            else
            	jobj = jr.readObject();
            
            //jr.close();
            Map<String, Object> properties = new HashMap<>(1);
            
            properties.put(JsonGenerator.PRETTY_PRINTING, true);

            JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
            
            JsonWriter jsonWriter = writerFactory.createWriter(sw);

            if (isArray)
               jsonWriter.writeArray(jarr);
            else
               jsonWriter.writeObject(jobj);
            
            jsonWriter.close();
            
            String prettyPrinted = sw.toString();

            logger.info(prettyPrinted);
            System.out.println(prettyPrinted);
        } catch (Exception e) {
        	//e.printStackTrace();
        	logger.error("Formatting error, printing raw JSON...");
        	logger.error(e.getMessage());
        	logger.error(theString);
			System.out.println("Formatting error, printing raw JSON...");
			System.out.println(theString);
        }
    }
	
	public SKLMJSONParser(String theString) 
	{
		logger.traceEntry();
        //theString = "{\"UserAuthId\":\"6672ef7f-6caf-4f06-920b-5ca3625e49b9\"}";
		StringReader reader = new StringReader(theString);
		JsonParser jsonParser = Json.createParser(reader);

		String keyName = null;
		String value = null;
		
		keyValuePairs = new HashMap<String, String>();

		
		while (jsonParser.hasNext()) {
			Event event = jsonParser.next();
			switch (event) {
			case KEY_NAME:			    
				keyName = jsonParser.getString();
				logger.debug("KEY_NAME="+keyName);
				//System.out.println("KEY_NAME="+keyName);
				break;
			case VALUE_STRING:
			    value = jsonParser.getString();
			    logger.debug("VALUE_STRING="+value);
			    //System.out.println("VALUE_STRING="+value);
				if (keyName == null)
				   logger.debug("Error - value without key");
				else
				   keyValuePairs.put(keyName,jsonParser.getString());
				keyName = null;
				break;
			case VALUE_NUMBER:
			    logger.error("VALUE_NUMBER");
				break;
			case VALUE_FALSE:
				logger.error("VALUE_FALSE");			
				break;
			case VALUE_TRUE:
				logger.error("VALUE_TRUE");			
				break;
			case VALUE_NULL:
				logger.error("VALUE_NULL");			
				break;
			case START_OBJECT: //Nothing to do, not an error
			case END_OBJECT:
			case START_ARRAY:
			case END_ARRAY:
				break;
			default:
				logger.error("OTHER : "+event.toString());
			}
		}
		
		jsonParser.close();
		
		for (String i : keyValuePairs.keySet()) {
           logger.debug("key: " + i + ", value: " + keyValuePairs.get(i));
		}
    }
}