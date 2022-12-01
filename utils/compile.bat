rmdir /S /Q ..\bin\
mkdir ..\bin\

%JAVA_HOME%\bin\\javac -cp ..\libs\javax.json-1.1.4.jar;..\libs\log4j-api-2.17.1.jar;..\libs\log4j-core-2.17.1.jar ..\src\com\ibm\klm\rest\*.java ..\src\com\ibm\klm\rest\commands\*.java -d ..\bin\
%JAVA_HOME%\bin\\jar -cvf ..\bin\rest-client.jar -C ..\bin\ .
