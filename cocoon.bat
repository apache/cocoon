@echo off
:: -----------------------------------------------------------------------------
:: Cocoon Win32 Shell Script
::
:: $Id: cocoon.bat,v 1.1 2003/03/09 00:01:33 pier Exp $
:: -----------------------------------------------------------------------------

:: Configuration variables
::
:: COCOON_LIB
::   Folder containing all the library files needed by the Cocoon CLI
::
:: JAVA_HOME
::   Home of Java installation.
::
:: JAVA_OPTIONS
::   Extra options to pass to the JVM
::
:: JAVA_DEBUG_PORT
::   The location where the JVM debug server should listen to
::
:: JETTY_PORT
::   Override the default port for Jetty
:: 
:: JETTY_ADMIN_PORT
::   The port where the jetty web administration should bind
::
:: JETTY_WEBAPP
::   The directory where the webapp that jetty has to execute is located
::

:: ----- Verify and Set Required Environment Variables -------------------------

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto cleanup
:gotJavaHome

:: ----- Set Up The Classpath --------------------------------------------------

set CP=.\tools\loader

:: ----- Check System Properties -----------------------------------------------

if not "%EXEC%" == "" goto gotExec
set EXEC=start "Cocoon" /D. /MAX
:gotExec

if not "%COCOON_LIB%" == "" goto gotLib
set COCOON_LIB=build\webapp\WEB-INF\lib
:gotLib

if not "%JETTY_PORT%" == "" goto gotPort
set JETTY_PORT=8888
:gotPort

if not "%JETTY_ADMIN_PORT%" == "" goto gotPort
set JETTY_ADMIN_PORT=8889
:gotPort

if not "%JETTY_WEBAPP%" == "" goto gotWebapp
set JETTY_WEBAPP=build/webapp
:gotWebapp

if not "%JAVA_DEBUG_PORT%" == "" goto gotWebapp
set JAVA_DEBUG_PORT=8000
:gotWebapp

:: ----- Check action ----------------------------------------------------------

if ""%1"" == ""cli"" goto doCli
if ""%1"" == ""servlet"" goto doServlet
if ""%1"" == ""servlet-admin"" goto doServletAdmin
if ""%1"" == ""servlet-debug"" goto doDebug

echo Usage: cocoon (action)
echo actions:
echo   cli             Run Cocoon from command line
echo   servlet         Run Cocoon in a servlet container
echo   servlet-admin   Run Cocoon in a servlet container and turn container web administration on
echo   servlet-debug   Run Cocoon in a servlet container and turn remote debug on
goto end

:: ----- Cli -------------------------------------------------------------------

:doCli
if not "%OS%" == "Windows_NT" goto noNT
shift
%JAVA_HOME%\bin\java.exe %JAVA_OPT% -classpath %CP% -Djava.endorsed.dirs=lib\endorsed -Dloader.jar.repositories=%COCOON_LIB% -Dloader.main.class=org.apache.cocoon.Main Loader %*
goto end
:noNT
%JAVA_HOME%\bin\java.exe %JAVA_OPT% -classpath %CP% -Djava.endorsed.dirs=lib\endorsed -Dloader.jar.repositories=%COCOON_LIB% -Dloader.main.class=org.apache.cocoon.Main Loader %2 %3 %4 %5 %6 %7 %8 %9
goto end

:: ----- Servlet ---------------------------------------------------------------

:doServlet
%EXEC% %JAVA_HOME%\bin\java.exe %JAVA_OPT% -classpath %CP% -Djava.endorsed.dirs=lib\endorsed -Dwebapp=%JETTY_WEBAPP% -Dorg.xml.sax.parser=org.apache.xerces.parsers.SAXParser -Djetty.port=%JETTY_PORT% -Djetty.admin.port=%JETTY_ADMIN_PORT% -Dloader.jar.repositories=tools\jetty\lib,lib\endorsed -Dloader.main.class=org.mortbay.jetty.Server Loader tools\jetty\conf\main.xml
goto end

:: ----- Servlet with Admin ----------------------------------------------------

:doServletAdmin
%EXEC% %JAVA_HOME%\bin\java.exe %JAVA_OPT% -classpath %CP% -Djava.endorsed.dirs=lib\endorsed -Dwebapp=%JETTY_WEBAPP% -Dorg.xml.sax.parser=org.apache.xerces.parsers.SAXParser -Djetty.port=%JETTY_PORT% -Djetty.admin.port=%JETTY_ADMIN_PORT% -Dloader.jar.repositories=tools\jetty\lib,lib\endorsed -Dloader.main.class=org.mortbay.jetty.Server Loader tools\jetty\conf\main.xml tools\jetty\conf\admin.xml
goto end

:: ----- Servlet Debug ---------------------------------------------------------

:doDebug
%EXEC% %JAVA_HOME%\bin\java.exe %JAVA_OPT% -Xdebug -Xrunjdwp:transport=dt_socket,address=%JAVA_DEBUG_PORT%,server=y,suspend=n  -classpath %CP% -Djava.endorsed.dirs=lib\endorsed -Dwebapp=%JETTY_WEBAPP% -Dorg.xml.sax.parser=org.apache.xerces.parsers.SAXParser -Djetty.port=%JETTY_PORT% -Djetty.admin.port=%JETTY_ADMIN_PORT% -Dloader.jar.repositories=tools\jetty\lib,lib\endorsed -Dloader.main.class=org.mortbay.jetty.Server Loader tools\jetty\conf\main.xml

:: ----- End -------------------------------------------------------------------

:end
set CP=


