@echo off

if "%JAVA_HOME%" == "" goto error

echo.
echo Cocoon Build System
echo -------------------

set ANT_HOME=.\lib
set ANT=.\lib\ant.jar
set JAVAC=%JAVA_HOME%\lib\tools.jar
set XERCES=.\lib\xerces_1_0_3.jar
set XALAN=.\lib\xalan_1_0_1.jar
set FOP=.\lib\fop_0_12_1.jar
set SERVLETS=.\lib\servlet_2_2.jar
set TURBINE=.\lib\turbine-2.0.jar
set LOCALCLASSPATH=%ANT%;%JAVAC%;%XERCES%;%XALAN%;%FOP%;%SERVLETS%;%TURBINE%;%CLASSPATH%

echo.
echo Building with classpath %LOCALCLASSPATH%

echo.
echo Starting Ant...

%JAVA_HOME%\bin\java.exe -Dant.home="%ANT_HOME%" -classpath "%LOCALCLASSPATH%" org.apache.tools.ant.Main %1 %2 %3 %4 %5

goto end

:error

echo "ERROR: JAVA_HOME not found in your environment."
echo.
echo "Please, set the JAVA_HOME variable in your environment to match the"
echo "location of the Java Virtual Machine you want to use."

:end

set LOCALCLASSPATH=
set XERCES=
set XALAN=
set FOP=
set SERVLETS=
set ANT=
set ANT_HOME=
set JAVAC=
