@echo off

if "%JAVA_HOME%" == "" goto error

echo.
echo Cocoon Build System
echo -------------------

set ANT_HOME=.\lib
set ANT=.\lib\ant.jar
set JAVAC=%JAVA_HOME%\lib\tools.jar
set JAXP=.\lib\jaxp_1_0_1.jar
set XERCES=.\lib\xerces_1_1_1.jar
set XALAN=.\lib\xalan_1_0_1.jar
set FOP=.\lib\fop_0_13_0.jar
set SVG=.\lib\svgv_0_8.jar
set JS=./lib/js.jar
set JSTYLE=./lib/jstyle.jar
set SERVLETS=.\lib\servlet_2_2.jar
set LOCALCLASSPATH=%ANT%;%JAVAC%;%JAXP%;%XERCES%;%XALAN%;%FOP%;%SVG%;%JS%;%JSTYLE%;%SERVLETS%;%CLASSPATH%

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
set JAXP=
set XERCES=
set XALAN=
set FOP=
set SVG=
set SERVLETS=
set ANT=
set ANT_HOME=
set JAVAC=
