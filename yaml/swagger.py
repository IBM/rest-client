#!/usr/bin/env python

"""swagger.py: parses the swagger_en.yaml file"""

__author__    = "Alan Watkins"
__email__     = "Alan.Watkins1@us.ibm.com"
__copyright__ = "Copytight 2021, IBM"

import os
import shutil
import yaml
import re
import sys

def getInfo(theData,theSchema):
   theSummary = theData.get('summary')
   print ("Summary:"+theSummary)

   theAttributes=""
   theReqAttributes=""
   theAttrLocations=""

   #Some parameters can be specified in the paramters area
   theRef = theData.get('parameters')
   if (theRef) :
      for z in theRef :
         #print (z.get('name'))
         theAttrLocations+=","+z.get('in')
         isReq=str(z.get('required'))
         theAttributes+=","+z.get('name')
         if (isReq=="True"):
            theReqAttributes+=","+z.get('name')

   #Doesn't have to have a request body or multipart either
   theRef =  theData.get('requestBody')
   if (theRef):
      theRef=theRef.get('content')
      theRefTemp=theRef.get('application/json')
      if (theRefTemp is None):
         theRefTemp=theRef.get('multipart/form-data')
      theRef=theRefTemp.get('schema').get('$ref')
      theRef = theRef.split("/")[-1]
      #print ("The Ref Temp:"+theRef)
      theSchemaRef = theSchema.get(theRef);
      theProperties = theSchemaRef.get('properties');
      theRequired = theSchemaRef.get('required');
      if (theProperties is None):
         #print ("Another special case....look for items and it will have them")
         theRef= theSchemaRef.get('items').get('$ref')
         theRef = theRef.split("/")[-1]
         theSchemaRef = theSchema.get(theRef);
         theProperties = theSchemaRef.get('properties');
         theRequired = theSchemaRef.get('required');

      for x in theProperties:
         #New Stuff
         theSubRef = theProperties.get(x).get('$ref')
         if (theSubRef) :
            #Dont add this, process it
            #print ("Found one...")
            theSubRef = theSubRef.split("/")[-1]
            theSubSchemaRef = theSchema.get(theSubRef);
            theSubProperties = theSubSchemaRef.get('properties');
            theSubRequired = theSubSchemaRef.get('required');
            for y in theSubProperties :
               theAttributes+=","+y
               theAttrLocations+=",body"
         else :
            #endofnew
            theAttributes+=","+x
            theAttrLocations+=",body"

      if (theRequired) :
         for y in theRequired:
            theReqAttributes+=","+y

   theReqAttributes=theReqAttributes.lstrip(",")
   theAttributes=theAttributes.lstrip(",")
   theAttrLocations=theAttrLocations.lstrip(",")
   print ("Attributes:"+theAttributes)
   print ("RequiredAttributes:"+theReqAttributes)
   print ("AttributeLocs:"+theAttrLocations)


def main():
    yamlFile = "swagger_en.yaml"
    if (len(sys.argv) == 2):
       yamlFile=str(sys.argv[1])
    elif (len(sys.argv) != 1):
       #print (len(sys.argv))
       print ("Provide filename to parse or none for swagger_en.yaml")
       exit(0) 

    #There are 2 types of parameter specs.  The one most common with post is
    #to have a reference, but most gets dont do it that way - we need to account for both
    with open(yamlFile) as f:
        spec = yaml.safe_load(f.read())
        target = spec.get('paths')
        for i in target:
           print ("Path:"+i)
           thePost = target.get(i).get('post')
           theGet  = target.get(i).get('get')
           theDel  = target.get(i).get('delete')
           thePut  = target.get(i).get('put')
           theSchema =  spec.get('components').get('schemas')
           if (thePost) :
              print ("Type:post")
              getInfo(thePost, theSchema)

           if (theGet) :
              print ("Type:get")
              getInfo(theGet, theSchema)

           if (theDel) :
              print ("Type:delete")
              getInfo(theDel, theSchema)

           if (thePut) :
              print ("Type:put")
              getInfo(thePut, theSchema)

           print ("--END--")



if __name__ == "__main__":
    main()
