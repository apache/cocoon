#!/bin/sh

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

"$ANT_HOME/bin/ant" -Djava.endorsed.dirs=lib/endorsed -logger org.apache.tools.ant.NoBannerLogger -emacs  $@

# ----- Restore ANT_HOME
export ANT_HOME=$OLD_ANT_HOME
unset OLD_ANT_HOME

# ----- Restore CLASSPATH
export CLASSPATH=$OLD_CLASSPATH
unset OLD_CLASSPATH
