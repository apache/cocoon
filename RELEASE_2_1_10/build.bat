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
rem ----------------------------------------------------------------------------
rem build.bat - Win32 Build Script for Apache Cocoon
rem
rem $Id$
rem ----------------------------------------------------------------------------

rem ----- Ignore system CLASSPATH variable
set OLD_CLASSPATH=%CLASSPATH%
set CLASSPATH=
for %%i in (lib\endorsed\*.jar) do call tools\bin\appendcp.bat %%i

rem ----- Use Ant shipped with Cocoon. Ignore installed in the system Ant
set OLD_ANT_HOME=%ANT_HOME%
set ANT_HOME=tools

call %ANT_HOME%\bin\ant -Djava.endorsed.dirs=lib\endorsed -logger org.apache.tools.ant.NoBannerLogger -emacs %1 %2 %3 %4 %5 %6 %7 %8 %9

rem ----- Restore ANT_HOME and ANT_OPTS
set ANT_HOME=%OLD_ANT_HOME%
set OLD_ANT_HOME=

rem ----- Restore CLASSPATH
set CLASSPATH=%OLD_CLASSPATH%
set OLD_CLASSPATH=
