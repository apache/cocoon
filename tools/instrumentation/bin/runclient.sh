#!/bin/sh
#
# CVS $Id: runclient.sh,v 1.1 2003/03/09 00:11:34 pier Exp $
#
# Script to run instrumentation client (run from the bin directory).

FRAMEWORK=../lib/avalon-framework-4.1.3.jar
CLIENT=../lib/excalibur-instrument-client-20030116.jar:../lib/excalibur-instrument-manager-interfaces-20030116.jar
ALTRMI=../lib/excalibur-altrmi-client-impl-20030116.jar:../lib/excalibur-altrmi-client-interfaces-20030116.jar:../lib/excalibur-altrmi-common-20030116.jar

CLASSPATH=$FRAMEWORK:$CLIENT:$ALTRMI

java -classpath $CLASSPATH org.apache.excalibur.instrument.client.Main

