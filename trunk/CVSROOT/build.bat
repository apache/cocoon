@echo off

echo Cocoon Build System
echo -------------------

if "%JAVA_HOME%" == "" goto error

set LOCALCLASSPATH=%CLASSPATH%;%JAVA_HOME%\lib\tools.jar;.\lib\xerces_1_0_1.jar;.\lib\xalan_0_19_4.jar;.\lib\fop_0_12_1.jar;.\lib\servlet_2_2.jar;.\lib\ant.jar;.\lib\xml.jar

echo Building with classpath %LOCALCLASSPATH%

echo Starting Ant...

%JAVA_HOME%\bin\java.exe -classpath "%LOCALCLASSPATH%" org.apache.tools.ant.Main %1 %2 %3 %4 %5

goto end

:error

echo "ERROR: JAVA_HOME not found in your environment."
echo "Please, set the JAVA_HOME variable in your environment to match the"
echo "location of the Java Virtual Machine you want to use."

:end

set LOCALCLASSPATH=