#!/bin/sh
# -----------------------------------------------------------------------------
# build.sh - Unix Build Script for Apache Cocoon
#
# $Id: build.sh,v 1.10.2.27 2001-04-25 20:48:33 giacomo Exp $
# -----------------------------------------------------------------------------

# ----- Verify and Set Required Environment Variables -------------------------
   
if [ "$ANT_HOME" = "" ] ; then
  ANT_HOME=.
fi

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit installation
  exit 1
fi

# ----- Set Up The Runtime Classpath ------------------------------------------

CP=$JAVA_HOME/lib/tools.jar:$ANT_HOME/lib/ant_1_3.jar:$ANT_HOME/lib/ant_1_3-optional.jar:./lib/xerces_1_3_1.jar:./lib/xalan-2.0.1.jar
 
# ----- Make sure Ant script is executable ------------------------------------

if [ ! -x $ANT_HOME/bin/antRun ] ; then
	chmod 0755 $ANT_HOME/bin/antRun
fi

# ----- Execute The Requested Build -------------------------------------------

$JAVA_HOME/bin/java $ANT_OPTS -classpath $CP org.apache.tools.ant.Main -Dant.home=$ANT_HOME -logger org.apache.tools.ant.NoBannerLogger -emacs $*



