@echo off
rem ----------------------------------------------------------------------------
rem build.bat - Win32 Build Script for Apache Cocoon
rem
rem $Id: build.bat,v 1.5 2003/10/06 16:07:22 upayavira Exp $
rem ----------------------------------------------------------------------------

rem ----- Copy Xalan and Xerces for the build system    ------------------------
if not exist "tools\lib\xerces*.jar" copy lib\local\xerces*.jar tools\lib
if not exist "tools\lib\xerces*.jar" copy lib\endorsed\xerces*.jar tools\lib
if not exist "tools\lib\xerces*.jar" copy lib\optional\xerces*.jar tools\lib
if not exist "tools\lib\xerces*.jar" copy lib\core\xerces*.jar tools\lib
if not exist "tools\lib\xalan*.jar" copy lib\local\xalan*.jar tools\lib
if not exist "tools\lib\xalan*.jar" copy lib\endorsed\xalan*.jar tools\lib
if not exist "tools\lib\xalan*.jar" copy lib\optional\xalan*.jar tools\lib
if not exist "tools\lib\xalan*.jar" copy lib\core\xalan*.jar tools\lib
if not exist "tools\lib\xml-api*.jar" copy lib\local\xml-api*.jar tools\lib
if not exist "tools\lib\xml-api*.jar" copy lib\endorsed\xml-api*.jar tools\lib
if not exist "tools\lib\xml-api*.jar" copy lib\optional\xml-api*.jar tools\lib
if not exist "tools\lib\xml-api*.jar" copy lib\core\xml-api*.jar tools\lib

rem ----- Verify and Set Required Environment Variables ------------------------

rem ----- Ignore system CLASSPATH variable
set OLD_CLASSPATH=%CLASSPATH%
set CLASSPATH=

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
