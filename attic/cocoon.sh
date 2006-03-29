#!/bin/sh
#  Copyright 1999-2004 The Apache Software Foundation
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
# -----------------------------------------------------------------------------
# Cocoon Unix Shell Script
#
# $Id$
# -----------------------------------------------------------------------------

# Configuration variables
#
# COCOON_HOME
#   The root of the Cocoon distribution
#
# COCOON_WEBAPP_HOME
#   The root of the Cocoon web application
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
# JAVA_DEBUG_ARGS
#   The command line arguments for the internal JVM debugger
#
# JAVA_PROFILE_ARGS
#   The command line arguments for the internal JVM profiler
#
# JETTY_PORT
#   Override the default port for Jetty
# 
# JETTY_ADMIN_PORT
#   The port where the jetty web administration should bind
# 
# JETTY_JMX_PORT
#   The port where the jetty JMX HTTP Adapter should bind
#


usage()
{
    echo "Usage: $0 (action)"
    echo "actions:"
    echo "  cli               Run Cocoon from the command line"
    echo "  servlet           Run Cocoon in a servlet container (default)"
    echo "  servlet-admin     Run Cocoon in a servlet container and turn on container web administration"
    echo "  servlet-jmx       Run Cocoon in a servlet container and turn on container web JMX adapter"
    echo "  servlet-admin-jmx Run Cocoon in a servlet container and turn on container web administration and JMX adapter"
    if [ "$JAVA_VERSION" -gt 4 ];then
      echo "  servlet-pms       Run Cocoon in a servlet container and turn on platform MBean server"
      echo "  servlet-admin-pms Run Cocoon in a servlet container and turn on container web administration and platform MBean server"
    fi
    echo "  servlet-debug     Run Cocoon in a servlet container and turn on JVM remote debug"
    echo "  servlet-profile   Run Cocoon in a servlet container and turn on JVM profiling"
    echo "  osgi              Run Cocoon with the experimental OSGI kernel"
    echo "  osgi-debug        Run Cocoon with the experimental OSGI kernel and turn on JVM remote debug"
    exit 1
}

# ----- Handle action parameter ------------------------------------------------
DEFAULT_ACTION="servlet"
ACTION="$1"
if [ -n "$ACTION" ]
then
  shift
else
  ACTION=$DEFAULT_ACTION
  echo "$0: executing default action '$ACTION', use -h to see other actions"
fi
ARGS="$*"

# ----- Verify and Set Required Environment Variables -------------------------

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit installation
  exit 1
fi

if [ "$JAVA_OPTIONS" = "" ] ; then
#  JAVA_OPTIONS='-Xms32M -Xmx512M'
  JAVA_OPTIONS='-Xms32M -Xmx512M -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.LogKitLogger'
#  JAVA_OPTIONS='-Xms32M -Xmx512M -Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog'
fi

if [ "$COCOON_HOME" = "" ] ; then
  COCOON_HOME='.'
fi

if [ "$COCOON_WEBAPP_HOME" = "" ] ; then
  STANDALONE_WEBAPP=../webapp
  if [ -d $STANDALONE_WEBAPP ] ; then
    # for standalone-webapp setup
    COCOON_WEBAPP_HOME=$STANDALONE_WEBAPP
  else
    # when in the build environment
    # COCOON_WEBAPP_HOME="$COCOON_HOME/build/webapp"
    # For mvn war:inplace
    COCOON_WEBAPP_HOME="$COCOON_HOME/cocoon-webapp/src/main/webapp"
  fi
fi
echo "$0: using $COCOON_WEBAPP_HOME as the webapp directory"

if [ "$COCOON_LIB" = "" ] ; then
  COCOON_LIB="$COCOON_WEBAPP_HOME/WEB-INF/lib"
fi

if [ "$JETTY_PORT" = "" ] ; then
  JETTY_PORT='8888'
fi

if [ "$JETTY_ADMIN_PORT" = "" ] ; then
  JETTY_ADMIN_PORT='8889'
fi

if [ "$JETTY_JMX_PORT" = "" ] ; then
  JETTY_JMX_PORT='8890'
fi

if [ "$JAVA_DEBUG_ARGS" = "" ] ; then
  JAVA_DEBUG_ARGS='-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n'
fi

if [ "$JAVA_PROFILE_ARGS" = "" ] ; then
  JAVA_PROFILE_ARGS='-Xrunhprof:heap=all,cpu=samples,thread=y,depth=3'
fi


# ----- Set platform specific variables

PATHSEP=":";
case "`uname`" in
   CYGWIN*) PATHSEP=";" ;;
esac

# ----- Set Local Variables ( used to minimize cut/paste) ---------------------

JAVA="$JAVA_HOME/bin/java"
JAVA_VERSION=`$JAVA -version 2>&1 | awk '/java version/ {print substr($3,4,1)}'`
if [ "$JAVA_VERSION" -gt 4 ]; then
  JAVA_PLATFORM_SERVER="-Dcom.sun.management.jmxremote"
fi
ENDORSED_LIBS="$COCOON_HOME/lib/endorsed"
ENDORSED="-Djava.endorsed.dirs=$ENDORSED_LIBS"
PARSER=-Dorg.xml.sax.parser=org.apache.xerces.parsers.SAXParser
LOADER=Loader
LOADER_LIB="${COCOON_HOME}/tools/loader"
BLOCKS="-Dorg.apache.cocoon.processor=org.apache.cocoon.blocks.BlocksManager -Dorg.apache.cocoon.configuration=/wiring.xml"

CLI=-Dloader.main.class=org.apache.cocoon.Main
CLI_LIBRARIES="-Dloader.jar.repositories=$COCOON_LIB"

JETTY=-Dloader.main.class=org.mortbay.jetty.Server
JETTY_CONF="$COCOON_HOME/tools/jetty/conf"
JETTY_MAIN="$JETTY_CONF/main.xml"
JETTY_JMX_MAIN="$JETTY_CONF/main-jmx.xml"
JETTY_ADMIN="$JETTY_CONF/admin.xml"
JETTY_JMX_ADMIN="$JETTY_CONF/admin-jmx.xml"
JETTY_WEBAPP="-Dwebapp=$COCOON_WEBAPP_HOME"
JETTY_HOME="-Dhome=$COCOON_HOME"
JETTY_PORT_ARGS="-Djetty.port=$JETTY_PORT"
JETTY_ADMIN_ARGS="-Djetty.admin.port=$JETTY_ADMIN_PORT"
JETTY_JMX_ARGS="-Djetty.jmx.port=$JETTY_JMX_PORT"
JETTY_START_CONF="-DSTART=$COCOON_HOME/tools/jetty/conf/jetty-start.config"
JETTY_START="-jar $COCOON_HOME/tools/jetty/jetty-start-5.1.8.jar"

# ----- Do the action ----------------------------------------------------------

case "$ACTION" in
  cli)
        $JAVA $JAVA_OPTIONS -cp $LOADER_LIB $ENDORSED $CLI_LIBRARIES $CLI $LOADER $ARGS
        ;;

  servlet)
        $JAVA $JAVA_OPTIONS $ENDORSED $PARSER $JETTY_PORT_ARGS $JETTY_START_CONF $JETTY_WEBAPP $JETTY_HOME $JETTY $JETTY_START $JETTY_MAIN
        ;;

  servlet-admin)
        $JAVA $JAVA_OPTIONS $ENDORSED $PARSER $JETTY_PORT_ARGS $JETTY_ADMIN_ARGS $JETTY_START_CONF $JETTY_WEBAPP $JETTY_HOME $JETTY $JETTY_START $JETTY_MAIN $JETTY_ADMIN
        ;;

  servlet-jmx)
        $JAVA $JAVA_OPTIONS $ENDORSED $PARSER $JETTY_PORT_ARGS $JETTY_JMX_ARGS $JETTY_START_CONF $JETTY_WEBAPP $JETTY_HOME $JETTY $JETTY_START $JETTY_JMX_MAIN
        ;;

  servlet-pms)
        $JAVA $JAVA_OPTIONS $JAVA_PLATFORM_SERVER $ENDORSED $PARSER $JETTY_PORT_ARGS $JETTY_JMX_ARGS $JETTY_START_CONF $JETTY_WEBAPP $JETTY_HOME $JETTY $JETTY_START $JETTY_MAIN
        ;;

  servlet-admin-jmx)
        $JAVA $JAVA_OPTIONS $ENDORSED $PARSER $JETTY_PORT_ARGS $JETTY_ADMIN_ARGS $JETTY_JMX_ARGS $JETTY_START_CONF $JETTY_WEBAPP $JETTY_HOME $JETTY $JETTY_START $JETTY_JMX_ADMIN
        ;;

  servlet-admin-pms)
        $JAVA $JAVA_OPTIONS $JAVA_PLATFORM_SERVER $ENDORSED $PARSER $JETTY_PORT_ARGS $JETTY_ADMIN_ARGS $JETTY_JMX_ARGS $JETTY_START_CONF $JETTY_WEBAPP $JETTY_HOME $JETTY $JETTY_START $JETTY_MAIN $JETTY_ADMIN
        ;;

  servlet-debug)
        $JAVA $JAVA_OPTIONS $JAVA_DEBUG_ARGS $ENDORSED $PARSER $JETTY_PORT_ARGS $JETTY_START_CONF $JETTY_WEBAPP $JETTY_HOME $JETTY $JETTY_START $JETTY_MAIN
        ;;

  servlet-profile)
        $JAVA $JAVA_OPTIONS $JAVA_PROFILE_ARGS $ENDORSED $PARSER $JETTY_PORT_ARGS $JETTY_START_CONF $JETTY_WEBAPP $JETTY_HOME $JETTY $JETTY_START $JETTY_MAIN
        ;;

  blocks)
        $JAVA $JAVA_OPTIONS $BLOCKS $ENDORSED $PARSER $JETTY_PORT_ARGS $JETTY_START_CONF $JETTY_WEBAPP $JETTY_HOME $JETTY $JETTY_START $JETTY_MAIN
        ;;

  osgi)
        # -init prevents knopflerfish from using its persistent info about bundles, seems safer for now
        KNOP_OPTIONS="-init"
        $JAVA $JAVA_OPTIONS -jar lib/core/knopflerfish-framework-1.3.3.jar $KNOP_OPTIONS $ARGS
        ;;

  osgi-debug)
        # -init prevents knopflerfish from using its persistent info about bundles, seems safer for now
        KNOP_OPTIONS="-init"
        $JAVA $JAVA_OPTIONS $JAVA_DEBUG_ARGS -jar lib/core/knopflerfish-framework-1.3.3.jar $KNOP_OPTIONS $ARGS
        ;;

  *)
        usage
        ;;
esac

exit 0
