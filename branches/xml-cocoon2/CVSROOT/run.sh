#!/bin/sh
# -----------------------------------------------------------------------------
# run.sh - Unix Run Script for Apache Cocoon
#
# $Id: run.sh,v 1.1.2.1 2000-09-25 14:02:32 stefano Exp $
# -----------------------------------------------------------------------------

# ----- Verify and Set Required Environment Variables -------------------------
   
if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit installation
  exit 1
fi

# ----- Verify and Set Required Environment Variables -------------------------

if [ "$COCOON_LIB" = "" ] ; then
  COCOON_LIB=./lib
fi

# ----- Set Up The Runtime Classpath ------------------------------------------

CP=`echo $COCOON_LIB/*.jar | tr ' ' ':'`:$JAVA_HOME/lib/tools.jar:$CLASSPATH

# ----- Run Cocoon ------------------------------------------------------------

$JAVA_HOME/bin/java $COCOON_OPTS -classpath $CP org.apache.cocoon.Main $*



