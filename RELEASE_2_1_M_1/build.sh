#!/bin/sh

echo
echo "Apache Cocoon Build System"
echo "--------------------------"

cp ./lib/*/xalan*.jar ./tools/lib
cp ./lib/*/xerces*.jar ./tools/lib
cp ./lib/*/xml-api*.jar ./tools/lib

chmod u+x ./tools/bin/antRun
chmod u+x ./tools/bin/ant

unset ANT_HOME

CP=$CLASSPATH
export CP
unset CLASSPATH

$PWD/tools/bin/ant -logger org.apache.tools.ant.NoBannerLogger -emacs $@ 

CLASSPATH=$CP
export CLASSPATH
