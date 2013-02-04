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

chmod u+x ./tools/bin/antRun
chmod u+x ./tools/bin/ant

# ----- Verify and Set Required Environment Variables -------------------------

S=":";
case "`uname`" in
   CYGWIN*) S=";" ;;
esac

# ----- Ignore system CLASSPATH variable
OLD_CLASSPATH="$CLASSPATH"
unset CLASSPATH
CLASSPATH="`echo lib/endorsed/*.jar | tr ' ' $S`"
export CLASSPATH

# ----- Use Ant shipped with Cocoon. Ignore installed in the system Ant
OLD_ANT_HOME="$ANT_HOME"
ANT_HOME=tools
OLD_ANT_OPTS="$ANT_OPTS"
ANT_OPTS="-Xms32M -Xmx512M -Djava.endorsed.dirs=lib/endorsed"
export ANT_HOME ANT_OPTS

if [ ! -f "$ANT_HOME/lib/ant.jar" ]
then
    echo ====================================================================
    echo                WARNING : dependencies not found
    echo ====================================================================
    echo  Third party libraries must be present in order to build Cocoon.
    echo  You may download the cocoon-2.1.12-deps.zip and unzip
    echo  its content in this directory to get these libraries.
    echo ====================================================================
    exit
fi

"$ANT_HOME/bin/ant" -logger org.apache.tools.ant.NoBannerLogger --noconfig -emacs "$@"
ERR=$?

# ----- Restore ANT_HOME and ANT_OPTS
ANT_HOME="$OLD_ANT_HOME"
ANT_OPTS="$OLD_ANT_OPTS"
export ANT_HOME ANT_OPTS
unset OLD_ANT_HOME
unset OLD_ANT_OPTS

# ----- Restore CLASSPATH
CLASSPATH="$OLD_CLASSPATH"
export CLASSPATH
unset OLD_CLASSPATH
exit $ERR
