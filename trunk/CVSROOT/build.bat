@echo off

java -classpath "%CLASSPATH%;lib\ant.jar;lib\xml.jar" org.apache.tools.ant.Main %1 %2 %3 %4 %5
