@echo off
rem  Licensed to the Apache Software Foundation (ASF) under one or more
rem  contributor license agreements.  See the NOTICE file distributed with
rem  this work for additional information regarding copyright ownership.
rem  The ASF licenses this file to You under the Apache License, Version 2.0
rem  (the "License"); you may not use this file except in compliance with
rem  the License.  You may obtain a copy of the License at
rem
rem      http://www.apache.org/licenses/LICENSE-2.0
rem
rem  Unless required by applicable law or agreed to in writing, software
rem  distributed under the License is distributed on an "AS IS" BASIS,
rem  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem  See the License for the specific language governing permissions and
rem  limitations under the License.
rem
:: -----------------------------------------------------------------------------
:: Cocoon Win32 Shell Script
::
:: $Id$
:: -----------------------------------------------------------------------------

:: Configuration variables
::
:: COCOON_HOME
::   Folder that points to the root of the Cocoon distribution
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
goto end
:gotJavaHome

:: ----- Check System Properties -----------------------------------------------

if not "%EXEC%" == "" goto gotExec
if not "%OS%" == "Windows_NT" goto noExecNT
set EXEC=start "Cocoon" /D.
goto gotExec
:noExecNT
set EXEC=
:gotExec

if not "%COCOON_HOME%" == "" goto gotHome
set COCOON_HOME=.
:gotHome

if not "%COCOON_LIB%" == "" goto gotLib
set COCOON_LIB=%COCOON_HOME%\build\webapp\WEB-INF\lib
:gotLib

if not "%JETTY_PORT%" == "" goto gotJettyPort
set JETTY_PORT=8888
:gotJettyPort

if not "%JETTY_ADMIN_PORT%" == "" goto gotJettyAdminPort
set JETTY_ADMIN_PORT=8889
:gotJettyAdminPort

if not "%JETTY_WEBAPP%" == "" goto gotWebapp
set DEMO_WEBAPP=..\webapp
if not exist %DEMO_WEBAPP% goto standardWebapp
set JETTY_WEBAPP=%DEMO_WEBAPP%
goto gotWebapp
:standardWebapp
set JETTY_WEBAPP=%COCOON_HOME%\build\webapp
:gotWebapp
echo cocoon.bat: using %JETTY_WEBAPP% as the webapp directory

if not "%JAVA_DEBUG_PORT%" == "" goto gotDebugPort
set JAVA_DEBUG_PORT=8000
:gotDebugPort

:: ----- Ensure desktop.ini is activated ---------------------------------------

attrib +s %COCOON_HOME%

:: ----- Set Up The Classpath --------------------------------------------------

set CP=%COCOON_HOME%\tools\loader

:: ----- Check action ----------------------------------------------------------

if ""%1"" == """" goto doServlet
if ""%1"" == ""cli"" goto doCli
if ""%1"" == ""precompile"" goto doPrecompile
if ""%1"" == ""servlet"" goto doServlet
if ""%1"" == ""servlet-admin"" goto doAdmin
if ""%1"" == ""servlet-debug"" goto doDebug
IF ""%1"" == ""servlet-profile"" goto doProfile
IF ""%1"" == ""yourkit-profile"" goto doYourkitProfile

echo Usage: cocoon (action)
echo actions:
echo   cli             Run Cocoon from command line
echo   precompile      Crawl your webapp to compile all XSP files (requires the xsp block)
echo   servlet         Run Cocoon in a servlet container (default)
echo   servlet-admin   Run Cocoon in a servlet container and turn container web administration on
echo   servlet-debug   Run Cocoon in a servlet container and turn on remote JVM debug
echo   servlet-profile Run Cocoon in a servlet container and turn on JVM profiling
echo   yourkit-profile Run Cocoon in a servlet container and turn on Yourkit JVM profiling
goto end

:: ----- Cli -------------------------------------------------------------------

:doCli
set param=
shift
:cliLoop
if "%1"=="" goto cliLoopEnd
if not "%1"=="" set param=%param% %1
shift
goto cliLoop

:cliLoopEnd

"%JAVA_HOME%\bin\java.exe" %JAVA_OPTIONS% -classpath "%CP%" -Djava.endorsed.dirs=lib\endorsed "-Dloader.jar.repositories=%COCOON_LIB%" "-Dloader.class.path=lib\core\servlet-2.3.jar;%COCOON_HOME%\build\webapp\WEB-INF\classes" -Dloader.verbose=false -Dloader.main.class=org.apache.cocoon.Main Loader %param%
goto end

:: ----- Precompile ------------------------------------------------------------

:doPrecompile
set param=
shift
:PrecompileLoop
if "%1"=="" goto PrecompileLoopEnd
if not "%1"=="" set param=%param% %1
shift
goto PrecompileLoop

:PrecompileLoopEnd

"%JAVA_HOME%\bin\java.exe" %JAVA_OPTIONS% -classpath "%CP%" -Djava.endorsed.dirs=lib\endorsed "-Dloader.jar.repositories=%COCOON_LIB%" "-Dloader.class.path=lib\core\servlet-2.3.jar;%COCOON_HOME%\build\webapp\WEB-INF\classes" -Dloader.verbose=false -Dloader.main.class=org.apache.cocoon.bean.XSPPrecompileWrapper Loader %param%
goto end

:: ----- Servlet ---------------------------------------------------------------

:doServlet
%EXEC% "%JAVA_HOME%\bin\java.exe" %JAVA_OPTIONS% -classpath "%CP%" "-Dloader.class.path=lib\core\servlet-2.3.jar" "-Djava.endorsed.dirs=%COCOON_HOME%\lib\endorsed" "-Dwebapp=%JETTY_WEBAPP%" -Dorg.xml.sax.parser=org.apache.xerces.parsers.SAXParser -Djetty.port=%JETTY_PORT% -Djetty.admin.port=%JETTY_ADMIN_PORT% "-Dhome=%COCOON_HOME%" "-Dloader.jar.repositories=%COCOON_HOME%\tools\jetty\lib;%COCOON_HOME%\lib\endorsed" -Dloader.main.class=org.mortbay.jetty.Server Loader "%COCOON_HOME%\tools\jetty\conf\main.xml"
goto end

:: ----- Servlet with Administration Web Interface -----------------------------

:doAdmin
%EXEC% "%JAVA_HOME%\bin\java.exe" %JAVA_OPTIONS% -classpath "%CP%" "-Djava.endorsed.dirs=%COCOON_HOME%\lib\endorsed" "-Dwebapp=%JETTY_WEBAPP%" -Dorg.xml.sax.parser=org.apache.xerces.parsers.SAXParser -Djetty.port=%JETTY_PORT% -Djetty.admin.port=%JETTY_ADMIN_PORT% "-Dhome=%COCOON_HOME%" "-Dloader.jar.repositories=%COCOON_HOME%\tools\jetty\lib;%COCOON_HOME%\lib\endorsed" -Dloader.main.class=org.mortbay.jetty.Server Loader "%COCOON_HOME%\tools\jetty\conf\main.xml" "%COCOON_HOME%\tools\jetty\conf\admin.xml"
goto end

:: ----- Servlet Debug ---------------------------------------------------------

:doDebug
%EXEC% "%JAVA_HOME%\bin\java.exe" %JAVA_OPTIONS% -Xdebug -Xrunjdwp:transport=dt_socket,address=%JAVA_DEBUG_PORT%,server=y,suspend=n  -classpath "%CP%" "-Djava.endorsed.dirs=%COCOON_HOME%\lib\endorsed" "-Dwebapp=%JETTY_WEBAPP%" "-Dhome=%COCOON_HOME%" -Dorg.xml.sax.parser=org.apache.xerces.parsers.SAXParser -Djetty.port=%JETTY_PORT% -Djetty.admin.port=%JETTY_ADMIN_PORT% "-Dloader.jar.repositories=%COCOON_HOME%\tools\jetty\lib;%COCOON_HOME%\lib\endorsed" -Dloader.main.class=org.mortbay.jetty.Server Loader "%COCOON_HOME%\tools\jetty\conf\main.xml"
goto end

:: ----- Servlet Profile ---------------------------------------------------------

:doProfile
%EXEC% "%JAVA_HOME%\bin\java.exe" %JAVA_OPTIONS% -Xrunhprof:heap=all,cpu=samples,thread=y,depth=3 -classpath "%CP%" "-Djava.endorsed.dirs=%COCOON_HOME%\lib\endorsed" "-Dwebapp=%JETTY_WEBAPP%" "-Dhome=%COCOON_HOME%" -Dorg.xml.sax.parser=org.apache.xerces.parsers.SAXParser -Djetty.port=%JETTY_PORT% -Djetty.admin.port=%JETTY_ADMIN_PORT% "-Dloader.jar.repositories=%COCOON_HOME%\tools\jetty\lib;%COCOON_HOME%\lib\endorsed" -Dloader.main.class=org.mortbay.jetty.Server Loader "%COCOON_HOME%\tools\jetty\conf\main.xml"

:: ----- Yourkit Profile --------------------------------------------------------

:doYourkitProfile
echo x
%EXEC% "%JAVA_HOME%\bin\java.exe" %JAVA_OPTIONS% -Xrunyjpagent:port=10000 -classpath "%CP%" "-Djava.endorsed.dirs=%COCOON_HOME%\lib\endorsed" "-Dwebapp=%JETTY_WEBAPP%" "-Dhome=%COCOON_HOME%" -Dorg.xml.sax.parser=org.apache.xerces.parsers.SAXParser -Djetty.port=%JETTY_PORT% -Djetty.admin.port=%JETTY_ADMIN_PORT% "-Dloader.jar.repositories=%COCOON_HOME%\tools\jetty\lib;%COCOON_HOME%\lib\endorsed" -Dloader.main.class=org.mortbay.jetty.Server Loader "%COCOON_HOME%\tools\jetty\conf\main.xml"
goto end

:: ----- End -------------------------------------------------------------------

:end
set CP=
set EXEC=

