@echo off

if "%JAVA_HOME%" == "" goto error

if not "%LIB_HOME%" == "" goto skip

set LIB_HOME=.\lib

:skip

echo.
echo Cocoon Build System
echo -------------------

if exist "%JAVA_HOME%\jre\lib\ext\xml.jar" goto xmlerror
if exist "%JAVA_HOME%\jre\lib\ext\parser.jar" goto xmlerror
if exist "%JAVA_HOME%\jre\lib\ext\jaxp.jar" goto xmlerror
if exist "%JAVA_HOME%\jre\lib\ext\crimson.jar" goto xmlerror

set ANT_HOME=.\lib

set LOCALCLASSPATH=%JAVA_HOME%\lib\tools.jar
for %%i in (%LIB_HOME%\*.jar) do call lcp.bat %%i

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
goto end

:xmlerror

echo "ERROR: Please delete any XML jars from %JAVA_HOME%\jre\lib\ext,"
echo "otherwise Cocoon will not compile!"

:end

set LOCALCLASSPATH=
set ANT_HOME=
set LIB_HOME=
