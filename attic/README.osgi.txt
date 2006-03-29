#  Copyright 1999-2005 The Apache Software Foundation
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License

Cocoon can be run based on OSGi. You have to build Cocoon, start the OSGI 
framework and finally you should be able to access Cocoon from within your 
favorite webbrowser.

*****************************************************************************
This is experimental, see http://wiki.apache.org/cocoon/osgi for more info.
*****************************************************************************

To build and start under OSGI do:

  build osgi
  cocoon osgi or ./cocoon.sh osgi
  http://localhost:8888 and http://localhost:8888/samples/

Note that once the (Knopflerfish) OSGI framework starts, you can press
enter on the console to access the framework command line:

  ./cocoon.sh osgi
  ...
  Knopflerfish OSGi framework, version 3.3.6
  ...
  Started: file:build/osgi/org.apache.cocoon_servlet_1.0.0.jar (id#10)

now type enter to get the prompt:

  > help
  Available command groups (type 'enter' to enter a group):
  session - Session commands built into the console
  logconfig - Configuration commands for the log.
  ...

To stop the framework use the "shutdown" command.

