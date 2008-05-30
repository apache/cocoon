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

CWD=`pwd`
MAVEN_OPTS="$MAVEN_OPTS -Djava.awt.headless=true -Dorg.apache.cocoon.mode=dev"

ARGS=""
while [ "$#" -gt "0" ]
do
  case "$1" in
    debug)
      MAVEN_OPTS="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=5005,server=y,suspend=n $MAVEN_OPTS -Djava.compiler=NONE"
      ;;
    *)
      ARGS="$ARGS $1"
  esac

  shift
done

export MAVEN_OPTS
cd core/cocoon-webapp && echo "Starting in `pwd`" && mvn jetty:run

cd $CWD
