@echo off
setlocal

rem (%~dp0 is expanded pathname of the current script under NT/XP).

set dist=%~dp0

set jars=%dist%\lib\altrmi-client-impl-0.9.2.jar;%dist%\lib\altrmi-client-interfaces-0.9.2.jar;%dist%\lib\excalibur-instrument-client-2003-03-31.jar

set core=%dist%\..\..\lib\core

set jars=%jars%;%core%\logkit-1.2.jar;%core%\avalon-framework-4.1.4.jar;%core%\excalibur-instrument-manager-interfaces-1.0.jar;%core%\..\optional\altrmi-common-0.9.2.jar

%JAVA_HOME%\bin\java -Dsun.java2d.noddraw=true -cp %jars% -Dsun.java2d.noddraw=true org.apache.excalibur.instrument.client.Main %*

endlocal
