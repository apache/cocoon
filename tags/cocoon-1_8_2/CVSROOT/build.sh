#!/bin/sh

echo
echo "Cocoon Build System"
echo "-------------------"

if [ "$JAVA_HOME" = "" ] ; then
  echo "ERROR: JAVA_HOME not found in your environment."
  echo
  echo "Please, set the JAVA_HOME variable in your environment to match the"
  echo "location of the Java Virtual Machine you want to use."
  exit 1
fi

xmlerror(){
 print "ERROR: Please delete any XML jars from $JAVA_HOME/jre/lib/ext,"
 print " otherwise Cocoon will not compile!\n"
}
if [ -e $JAVA_HOME/jre/lib/ext/xml.jar ] ;
then xmlerror;
elif [ -e $JAVA_HOME/jre/lib/ext/jaxp.jar ] ;
then xmlerror;
elif [ -e $JAVA_HOME/jre/lib/ext/parser.jar ] ;
then xmlerror;
elif [ -e $JAVA_HOME/jre/lib/ext/crimson.jar ] ;
then xmlerror;
fi

ANT_HOME=./lib
LOCALCLASSPATH=`echo lib/*.jar | tr ' ' ':'`:$JAVA_HOME/lib/tools.jar:$CLASSPATH

echo
echo Building with classpath $LOCALCLASSPATH

chmod 0755 $ANT_HOME/bin/antRun

echo
echo Starting Ant...

$JAVA_HOME/bin/java -Dant.home=$ANT_HOME -classpath $LOCALCLASSPATH org.apache.tools.ant.Main $*
