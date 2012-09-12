#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


# Cocoon Build Script
#   Invoke maven with necessary options
#
# $Id$

# Java compiler requires at least 128m of memory for cocoon-serializers block,
# plus another 128m for maven itself to fit its dependency tree.
MAVEN_OPTS="-Xmx512m"

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
