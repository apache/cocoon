@echo off
:: -----------------------------------------------------------------------------
:: build.bat - Win32 Build Script for Apache Cocoon
::
:: $Id: build.bat,v 1.10.2.18 2000-11-16 17:22:45 dims Exp $
:: -----------------------------------------------------------------------------

:: ----- Verify and Set Required Environment Variables -------------------------

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto cleanup
:gotJavaHome

if not "%ANT_HOME%" == "" goto gotAntHome
set ANT_HOME=.
:gotAntHome

:: ----- Set Up The Runtime Classpath ------------------------------------------

set CP=%JAVA_HOME%\lib\tools.jar;%ANT_HOME%\lib\ant_1_2.jar;.\lib\xerces_1_2_1.jar

:: ----- Execute The Requested Build -------------------------------------------

%JAVA_HOME%\bin\java.exe %ANT_OPTS% -classpath %CP% org.apache.tools.ant.Main -Dant.home=%ANT_HOME% %1 %2 %3 %4 %5 %6 %7 %8 %9

:: ----- Cleanup the environment -----------------------------------------------

:cleanup
set CP=


