#!/usr/bin/sh

$JAVA_HOME/bin/java -cp ../bin/rest-client.jar:../libs/javax.json-1.1.4.jar:../libs/log4j-api-2.17.1.jar:../libs/log4j-core-2.17.1.jar com.ibm.klm.rest.SKLMRESTCLI "$@"
