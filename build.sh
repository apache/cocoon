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
chmod u+x ./tools/bin/antRun
chmod u+x ./tools/bin/ant

# ----- Verify and Set Required Environment Variables -------------------------

if [ "$TERM" = "cygwin" ] ; then
  S=';'
else
  S=':'
fi

# ----- Ignore system CLASSPATH variable
OLD_CLASSPATH=$CLASSPATH
unset CLASSPATH
CLASSPATH="`echo lib/endorsed/*.jar | tr ' ' $S`"
export CLASSPATH

# ----- Use Ant shipped with Cocoon. Ignore installed in the system Ant
OLD_ANT_HOME="$ANT_HOME"
export ANT_HOME=tools
OLD_ANT_OPTS="$ANT_OPTS"
export ANT_OPTS="-Djava.endorsed.dirs=lib/endorsed"

"$ANT_HOME/bin/ant" -logger org.apache.tools.ant.NoBannerLogger -emacs  $@

# ----- Restore ANT_HOME and ANT_OPTS
export ANT_HOME=$OLD_ANT_HOME
unset OLD_ANT_HOME
export ANT_OPTS=$OLD_ANT_OPTS
unset OLD_ANT_OPTS

# ----- Restore CLASSPATH
export CLASSPATH=$OLD_CLASSPATH
unset OLD_CLASSPATH
