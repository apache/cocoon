@echo off
rem CVS $Id: runclient.bat,v 1.1 2003/03/09 00:11:34 pier Exp $
rem
rem Script to run instrumentation client (run from the bin directory).

setlocal

set FRAMEWORK=..\lib\avalon-framework-4.1.3.jar
set CLIENT=..\lib\excalibur-instrument-client-20030116.jar;..\lib\excalibur-instrument-manager-interfaces-20030116.jar
set ALTRMI=..\lib\excalibur-altrmi-client-impl-20030116.jar;..\lib\excalibur-altrmi-client-interfaces-20030116.jar;..\lib\excalibur-altrmi-common-20030116.jar

set CLASSPATH=%FRAMEWORK%;%CLIENT%;%ALTRMI%

rem ----- Verify and Set Required Environment Variables ------------------------

rem ----- Use Java in JAVA_HOME if JAVA_HOME is set.
set OLD_PATH=%PATH%
if "%JAVA_HOME%" == "" goto noJavaHome
echo Using Java from %JAVA_HOME%
set PATH=%JAVA_HOME%\bin
:noJavaHome

java -classpath %CLASSPATH% org.apache.excalibur.instrument.client.Main

endlocal
