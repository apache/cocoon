#!/bin/sh
#
# Cocoon Build Script
#   Invoke maven with necessary options
#
# $Id$

# Java compiler requires at least 128m of memory for cocoon-serializers block,
# plus another 128m for maven itself to fit its dependency tree.
export MAVEN_OPTS=-Xmx256m

# Unit tests debugging option
# -Dmaven.surefire.debug

# Unit test skip option
# -Dmaven.test.skip=true

# Invoke maven
mvn -P allblocks $*

