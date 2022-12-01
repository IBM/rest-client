#!/usr/bin/sh

rm -rf ../bin
mkdir ../bin
$JAVA_HOME/bin/javac \
-cp ../libs/javax.json-1.1.4.jar:../libs/disruptor-3.3.4.jar:../libs/log4j-api-2.13.3.jar:../libs/log4j-core-2.13.3.jar:. \
../src/com/ibm/klm/rest/*.java ../src/com/ibm/klm/rest/commands/*.java -d ../bin

$JAVA_HOME/bin/jar -cvf ../bin/rest-client.jar -C ../bin/ .
