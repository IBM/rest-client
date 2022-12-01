@echo off

if "%~1" == "" GOTO NOARGS
%JAVA_HOME%\bin\java.exe -classpath ..\bin\rest-client.jar;..\libs\javax.json-1.1.4.jar;..\libs\log4j-api-2.17.1.jar;..\libs\log4j-core-2.17.1.jar com.ibm.klm.rest.SKLMRESTCLI "%~1" "%~2" "%~3" "%~4" "%~5" "%~6" "%~7" "%~8"

goto END

:NOARGS
%JAVA_HOME%\bin\java.exe -classpath ..\bin\rest-client.jar;..\libs\javax.json-1.1.4.jar;..\libs\log4j-api-2.17.1.jar;..\libs\log4j-core-2.17.1.jar com.ibm.klm.rest.SKLMRESTCLI

:END
