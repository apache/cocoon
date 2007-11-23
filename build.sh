#!/bin/sh
#
# Cocoon Build Script
#   Invoke maven with necessary options
#
# $Id$

# Java compiler requires at least 128m of memory for cocoon-serializers block,
# plus another 128m for maven itself to fit its dependency tree.
MAVEN_OPTS="-Xmx256m"

# Parse command line
ARGS=""
while [ "$#" -gt "0" ]
do
  case "$1" in
    debug)
      MAVEN_OPTS="-Dmaven.surefire.debug $MAVEN_OPTS"
      ;;

    notest | notests)
      MAVEN_OPTS="-Dmaven.test.skip=true $MAVEN_OPTS"
      ;;

    build)
      ARGS="$ARGS install"
      ;;

    *)
      ARGS="$ARGS $1"
  esac

  shift
done
export MAVEN_OPTS

# Invoke maven
mvn -P allblocks $ARGS
