#!/bin/sh

java -classpath "$CLASSPATH:./lib/ant.jar:./lib/xml.jar" org.apache.tools.ant.Main $*
