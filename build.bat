@echo off
rem ----------------------------------------------------------------------------
rem build.bat - Win32 Build Script for Apache Cocoon
rem
rem $Id: build.bat,v 1.6 2004/03/04 07:53:55 antonio Exp $
rem ----------------------------------------------------------------------------

rem ----- Verify and Set Required Environment Variables ------------------------

rem ----- Ignore system CLASSPATH variable
set OLD_CLASSPATH=%CLASSPATH%
set CLASSPATH=
for %%i in (.\lib\endorsed\*.jar) do call appendcp.bat %%i
echo Using classpath: "%CLASSPATH%"

rem ----- Use Java in JAVA_HOME if JAVA_HOME is set.
set OLD_PATH=%PATH%
if "%JAVA_HOME%" == "" goto noJavaHome
echo Using Java from %JAVA_HOME%
set PATH=%JAVA_HOME%\bin
:noJavaHome

rem ----- Use Ant shipped with Cocoon. Ignore installed in the system Ant
set OLD_ANT_HOME=%ANT_HOME%
set ANT_HOME=tools

call %ANT_HOME%\bin\ant -Djava.endorsed.dirs=lib\endorsed -logger org.apache.tools.ant.NoBannerLogger -emacs %1 %2 %3 %4 %5 %6 %7 %8 %9

rem ----- Restore ANT_HOME
set ANT_HOME=%OLD_ANT_HOME%
set OLD_ANT_HOME=

rem ----- Restore PATH
set PATH=%OLD_PATH%
set OLD_PATH=

rem ----- Restore CLASSPATH
set CLASSPATH=%OLD_CLASSPATH%
set OLD_CLASSPATH=
