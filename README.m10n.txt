#  Copyright 2006 The Apache Software Foundation
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

This is some quick info about the Mavenization (m10n) of Cocoon 2.2.

There are also some Daisy documentation pages about this, for more "stable" information:
http://cocoon.zones.apache.org/daisy/documentation/g2/756.html
http://cocoon.zones.apache.org/daisy/documentation/g1/798.html

HOW TO CONVERT AN EXISTING BLOCK TO MAVEN STRUCTURE

1. use archetype plugin to create template block structure in the repo root  
  $mvn archetype:create -DgroupId=org.apache.cocoon -DartifactId=cocoon-core
2. remove the src/main/java directory from the newly created structure as this will be replaced by your own java sources. If you have testsources already then remove src/main/test/java as well. If you have a pom then remove that one as well.
3. svn add cocoon-core; svn commit cocoon-core
4. go to cocoon-core and 
  $svn move location_of_sources/java src/main 
  $svn move location_of_pom.xml .
  $svn move location_of_testsources/java src/test
5. copy/move jar resources in src/main/resources and test resources in src/test/resources

HOW TO MOUNT THE PROJECTS IN ECLIPSE

from /trunk, run mvn eclipse:clean first to remove any left over eclipse
files. Then run mvn eclipse:eclipse.

Next go to eclipse, and make sure you haven't got trunk mounted as a
project already. Do File-Import->Existing projects into workspace, then
point to your trunk directory and it should detect the newly created
blocks as projects.

Note that you need to declare the M2_REPO classpath variable in your
workspace, it should point to your local m2 repository. At the moment
there are still compilation problems because of htmlunit, but this
should be enough to get you going already.

You can also get eclipse to download the sources of the dependent libraries and attach them 
to the jars in eclipse :

$ mvn -Declipse.downloadSources=true eclipse:eclipse


For further information about the maven eclipse plugin visit 

http://maven.apache.org/plugins/maven-eclipse-plugin/

HOW TO START THE COCOON WEBAPP

1. go to cocoon-webapp
  $ mvn war:inplace
  $ mvn jetty6:run
2. point your browser to http://localhost:8888/cocoon-webapp/

FIXME: The above doesn't work yet. The jetty plugin require the precence of the directory
target/classes, which isn't added by any of the above goals so it has to be added manually.
Cocoon fails during startup.

2b. Use ./cocoon.sh instead.
