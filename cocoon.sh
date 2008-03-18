#!/bin/sh
#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
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
# LOADER_LIB
#   The classpath where to lookup the Loader class, defaults to
#   ${COCOON_HOME}/tools/loader
#
# CLI_CLASSPATH
#   The additional classpath for cli and precompile, defaults to
#   lib/core/servlet_2_2.jar:$COCOON_WEBAPP_HOME/WEB-INF/classes
#


usage()
{
    echo "Usage: $0 (action)"
    echo "actions:"
    echo "  cli               Run Cocoon from the command line"
    echo "  precompile        Crawl your webapp to compile all XSP files (requires the xsp block)"
    echo "  servlet           Run Cocoon in a servlet container (default)"
    echo "  servlet-admin     Run Cocoon in a servlet container and turn on container web administration"
    echo "  servlet-debug     Run Cocoon in a servlet container and turn on JVM remote debug"
    echo "  servlet-profile   Run Cocoon in a servlet container and turn on JVM profiling"
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


# ----- OS specific support ----------------------------------------------------

cygwin=false;
darwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true
           if [ -z "$JAVA_HOME" ] ; then
             JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Home
           fi
           ;;
esac


# ----- Verify and Set Required Environment Variables -------------------------

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit installation
  exit 1
fi

if [ "$JAVA_OPTIONS" = "" ] ; then
  JAVA_OPTIONS='-Xms32M -Xmx512M -Dorg.apache.cocoon.settings=cocoon.properties'
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
    COCOON_WEBAPP_HOME="$COCOON_HOME/build/webapp"
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

if [ "$JAVA_DEBUG_ARGS" = "" ] ; then
  JAVA_DEBUG_ARGS='-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n'
fi

if [ "$JAVA_PROFILE_ARGS" = "" ] ; then
  JAVA_PROFILE_ARGS='-Xrunhprof:heap=all,cpu=samples,thread=y,depth=3'
fi


# ----- Set platform specific variables

PATHSEP=":";
if $cygwin; then
  PATHSEP=";"
  JAVA_HOME=`cygpath --unix "$JAVA_HOME"`;
fi

# ----- Set Local Variables ( used to minimize cut/paste) ---------------------

JAVA="exec $JAVA_HOME/bin/java"
ENDORSED_LIBS="$COCOON_HOME/lib/endorsed"
ENDORSED="-Djava.endorsed.dirs=$ENDORSED_LIBS"
PARSER=-Dorg.xml.sax.parser=org.apache.xerces.parsers.SAXParser
LOADER=Loader
if [ "$LOADER_LIB" = "" ] ; then
	LOADER_LIB="${COCOON_HOME}/tools/loader"
fi

CLI="-Dloader.main.class=org.apache.cocoon.Main"
CLI_LIBRARIES="-Dloader.jar.repositories=$COCOON_LIB"
if [ "$CLI_CLASSPATH" = "" ] ; then
	CLI_CLASSPATH="lib/core/servlet_2_2.jar:$COCOON_WEBAPP_HOME/WEB-INF/classes"
fi
CLI_VERBOSE="-Dloader.verbose=false"
CLI_PROPERTIES="$CLI_LIBRARIES -Dloader.class.path=$CLI_CLASSPATH $CLI_VERBOSE"

PRECOMPILE=-Dloader.main.class=org.apache.cocoon.bean.XSPPrecompileWrapper

JETTY=-Dloader.main.class=org.mortbay.jetty.Server
JETTY_CONF="$COCOON_HOME/tools/jetty/conf"
JETTY_MAIN="$JETTY_CONF/main.xml"
JETTY_ADMIN="$JETTY_CONF/admin.xml"
JETTY_WEBAPP="-Dwebapp=$COCOON_WEBAPP_HOME"
JETTY_HOME="-Dhome=$COCOON_HOME"
JETTY_PORT_ARGS="-Djetty.port=$JETTY_PORT"
JETTY_ADMIN_ARGS="-Djetty.admin.port=$JETTY_ADMIN_PORT"
JETTY_LIBRARIES="-Dloader.jar.repositories=$COCOON_HOME/tools/jetty/lib${PATHSEP}${ENDORSED_LIBS}"

# ----- Do the action ----------------------------------------------------------

case "$ACTION" in
  cli)
        $JAVA $JAVA_OPTIONS -cp $LOADER_LIB $ENDORSED $CLI_PROPERTIES $CLI $LOADER $ARGS
        ;;

  precompile)
        $JAVA $JAVA_OPTIONS -cp $LOADER_LIB $ENDORSED $CLI_PROPERTIES $PRECOMPILE $LOADER $ARGS
        ;;

  servlet)
        $JAVA $JAVA_OPTIONS -cp $LOADER_LIB $ENDORSED $PARSER $JETTY_PORT_ARGS $JETTY_LIBRARIES $JETTY_WEBAPP $JETTY_HOME $JETTY $LOADER $JETTY_MAIN
        ;;

  servlet-admin)
        $JAVA $JAVA_OPTIONS -cp $LOADER_LIB $ENDORSED $PARSER $JETTY_PORT_ARGS $JETTY_ADMIN_ARGS $JETTY_LIBRARIES $JETTY_WEBAPP $JETTY_HOME $JETTY $LOADER $JETTY_MAIN $JETTY_ADMIN
        ;;

  servlet-debug)
        $JAVA $JAVA_OPTIONS $JAVA_DEBUG_ARGS -cp $LOADER_LIB $ENDORSED $PARSER $JETTY_PORT_ARGS $JETTY_LIBRARIES $JETTY_WEBAPP $JETTY_HOME $JETTY $LOADER $JETTY_MAIN
        ;;

  servlet-profile)
        $JAVA $JAVA_OPTIONS $JAVA_PROFILE_ARGS -cp $LOADER_LIB $ENDORSED $PARSER $JETTY_ARGS $JETTY_LIBRARIES $JETTY_WEBAPP $JETTY_HOME $JETTY $LOADER $JETTY_MAIN
        ;;

  *)
        usage
        ;;
esac

exit 0
