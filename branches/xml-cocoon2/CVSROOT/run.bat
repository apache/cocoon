@echo off
:: -----------------------------------------------------------------------------
:: run.bat - Win32 Run Script for Apache Cocoon
::
:: $Id: run.bat,v 1.1.2.2 2000-12-16 14:21:50 giacomo Exp $
:: -----------------------------------------------------------------------------

:: ----- Verify and Set Required Environment Variables -------------------------

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto cleanup
:gotJavaHome

:: ----- Verify and Set Required Environment Variables -------------------------

if not "%COCOON_LIB%" == "" goto gotCocoonLib
set COCOON_LIB=.\lib
:gotCocoonLib

if not "%COCOON_WORK%" == "" goto gotCocoonWork
set COCOON_WORK=.\work
:gotCocoonWork

:: ----- Set Up The Runtime Classpath ------------------------------------------

set CP=%JAVA_HOME%\lib\tools.jar;%COCOON_WORK%
for %%i in (%COCOON_LIB%\*.jar) do call appendcp.bat %%i

:: ----- Run Cocoon ------------------------------------------------------------

%JAVA_HOME%\bin\java.exe %COCOON_OPTS% -classpath %CP% org.apache.cocoon.Main %1 %2 %3 %4 %5 %6 %7 %8 %9

:: ----- Cleanup the environment -----------------------------------------------

:cleanup
set CP=


