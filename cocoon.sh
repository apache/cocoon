#!/bin/sh
# -----------------------------------------------------------------------------
# Cocoon Unix Shell Script
#
# $Id: cocoon.sh,v 1.1 2003/03/09 00:01:33 pier Exp $
# -----------------------------------------------------------------------------

# Configuration variables
#
# COCOON_LIB
#   Folder containing all the library files needed by the Cocoon CLI
#
# JAVA_HOME
#   Home of Java installation.
#
# JAVA_OPTIONS
#   Extra options to pass to the JVM
#
# JAVA_DEBUG_PORT
#   The location where the JVM debug server should listen to
#
# JETTY_PORT
#   Override the default port for Jetty
# 
# JETTY_ADMIN_PORT
#   The port where the jetty web administration should bind
#
# JETTY_WEBAPP
#   The directory where the webapp that jetty has to execute is located
#

usage()
{
    echo "Usage: $0 (action)"
    echo "actions:"
    echo "  cli             Run Cocoon from command line"
    echo "  servlet         Run Cocoon in a servlet container"
    echo "  servlet-admin   Run Cocoon in a servlet container and turn container web administration on"
    echo "  servlet-debug   Run Cocoon in a servlet container and turn remote debug on"
    exit 1
}

[ $# -gt 0 ] || usage

ACTION=$1
shift
ARGS="$*"

# ----- Verify and Set Required Environment Variables -------------------------

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit installation
  exit 1
fi

if [ "$COCOON_LIB" = "" ] ; then
  COCOON_LIB=build/webapp/WEB-INF/lib
fi

if [ "$JETTY_PORT" = "" ] ; then
  JETTY_PORT=8888
fi

if [ "$JETTY_ADMIN_PORT" = "" ] ; then
  JETTY_ADMIN_PORT=8889
fi

if [ "$JETTY_WEBAPP" = "" ] ; then
  JETTY_WEBAPP=build/webapp
fi

if [ "$JAVA_DEBUG_PORT" = "" ] ; then
  JAVA_DEBUG_PORT=8000
fi

# ----- Set Classpath ----------------------------------------------------------

CP=./tools/loader

# ----- Do the action ----------------------------------------------------------

case "$ACTION" in
  cli)
        $JAVA_HOME/bin/java $JAVA_OPT -classpath %CP% -Djava.endorsed.dirs=lib/endorsed -Dloader.jar.repositories=%COCOON_LIB% -Dloader.main.class=org.apache.cocoon.Main Loader $ARGS
        ;;

  servlet)
        $JAVA_HOME/bin/java $JAVA_OPT -classpath $CP -Djava.endorsed.dirs=lib/endorsed -Dwebapp=$JETTY_WEBAPP -Dorg.xml.sax.parser=org.apache.xerces.parsers.SAXParser -Djetty.port=$JETTY_PORT -Djetty.admin.port=$JETTY_ADMIN_PORT -Dloader.jar.repositories=tools/jetty/lib,lib/endorsed -Dloader.main.class=org.mortbay.jetty.Server Loader tools/jetty/conf/main.xml
        ;;

  servlet-admin)
        $JAVA_HOME/bin/java $JAVA_OPT -classpath $CP -Djava.endorsed.dirs=lib/endorsed -Dwebapp=$JETTY_WEBAPP -Dorg.xml.sax.parser=org.apache.xerces.parsers.SAXParser -Djetty.port=$JETTY_PORT -Djetty.admin.port=$JETTY_ADMIN_PORT -Dloader.jar.repositories=tools/jetty/lib,lib/endorsed -Dloader.main.class=org.mortbay.jetty.Server Loader tools/jetty/conf/main.xml tools/jetty/conf/admin.xml
        ;;

  servlet-debug)
        $JAVA_HOME/bin/java $JAVA_OPT -Xdebug -Xrunjdwp:transport=dt_socket,address=$JAVA_DEBUG_PORT,server=y,suspend=n -classpath $CP -Djava.endorsed.dirs=lib/endorsed -Dwebapp=$JETTY_WEBAPP -Dorg.xml.sax.parser=org.apache.xerces.parsers.SAXParser -Djetty.port=$JETTY_PORT -Djetty.admin.port=$JETTY_ADMIN_PORT -Dloader.jar.repositories=tools/jetty/lib,lib/endorsed -Dloader.main.class=org.mortbay.jetty.Server Loader tools/jetty/conf/main.xml tools/jetty/conf/admin.xml
        ;;

  *)
        usage
        ;;
esac

exit 0
