@echo off

set LOCALCLASSPATH="lib\xerces_1_0_1.jar;lib\xalan_0_19_2;lib\fop_0_12_0;lib\servlet_2_2.jar;lib\ant.jar;lib\xml.jar"

%JAVA_HOME%\bin\java.exe -classpath "%JAVA_HOME%\lib\tools.jar;%CLASSPATH%;%LOCALCLASSPATH%" org.apache.tools.ant.Main %1 %2 %3 %4 %5

