#!/bin/sh
# -----------------------------------------------------------------------------
# build.sh - Unix Build Script for Apache Cocoon
#
# $Id: build.sh,v 1.10.2.11 2000-09-19 00:25:32 stefano Exp $
# -----------------------------------------------------------------------------

ANT_HOME=.

CP=$JAVA_HOME/lib/tools.jar:$ANT_HOME/lib/ant.jar:./lib/xerces_1_2.jar

# ----- Execute The Requested Build -------------------------------------------

$JAVA_HOME/bin/java $ANT_OPTS -classpath $CP org.apache.tools.ant.Main -Dant.home=$ANT_HOME $*



