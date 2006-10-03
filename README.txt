This is some quick info about the Mavenization (m10n) of Cocoon 2.2.

There are also some Daisy documentation pages about this, for more "stable" information:

  http://cocoon.zones.apache.org/daisy/documentation/g2/756.html
  http://cocoon.zones.apache.org/daisy/documentation/g1/798.html


PREREQUISITES
-------------
You need a JDK, 1.4.x or 1.5.x.

Maven 2 must be installed (at least 2.0.4??).


MAVEN REPOSITORY MIRRORS
------------------------
To avoid very slow builds due to slow or inaccessible Maven repositories,
it is recommended to setup your closest/fastest/preferred Maven mirror,
as explained in 

  http://maven.apache.org/guides/mini/guide-mirror-settings.html

People have been reporting good results with the dotsrc repo 
(mirrors.dotsrc.org/maven2), at least from Europe. 
A good US based repo is the one from mergere (repo.mergere.com/maven2).

Build times will vary dramatically depending on how good your connectivity
to the Maven repository/mirror is, how well it performs, and whether
you already have a loaded local Maven repository. 


HOW TO BUILD THE COCOON WEBAPP
------------------------------

Since Cocoon release 2.2, Cocoon relies on Maven 2 for its build
process. 

To build Cocoon, use the following command:

  $ mvn -Dmaven.test.skip=true -Dallblocks install

In case of any failures, repeat command as necessary till you see
the message:

  BUILD SUCCESSFUL


See also MAVEN REPOSITORY MIRRORS above.


HOW TO MOUNT THE PROJECTS IN ECLIPSE
------------------------------------

from /trunk, run mvn eclipse:clean first to remove any left over eclipse
files. Then run mvn eclipse:eclipse.

Next go to eclipse, and make sure you haven't got trunk mounted as a
project already.  Also remove .classpath and .project files possibly
remaining in /trunk from a previous project mount.
Do File-Import->Existing projects into workspace, then
point to your trunk directory and it should detect the newly created
blocks as projects.

Note that you need to declare the M2_REPO classpath variable in your
workspace, it should point to your local m2 repository.

You can also get eclipse to download the sources of the dependent libraries and attach them 
to the jars in eclipse :

  $ mvn -Declipse.downloadSources=true eclipse:eclipse

For further information about the maven eclipse plugin visit 

  http://maven.apache.org/plugins/maven-eclipse-plugin/


HOW TO START THE COCOON WEBAPP
------------------------------

Checkout complete trunk and build it. If clean rebuild is desired,
use command:

  $ mvn -Dallblocks clean install

Call this until you get "BUILD SUCCESSFUL" - sometimes downloads from maven
repositories are temporarily unaccessible and cause the build to fail.

Go to core/cocoon-webapp:
  $ mvn jetty6:run

Point your browser to http://localhost:8888/

(Don't use jetty:run-exploded as in this case the jetty6 plugin will
 alter the webapp build by the Cocoon deployer again!)

See also MAVEN REPOSITORY MIRRORS above.


HOW TO BUILD AND START THE COCOON WITH OSGI
-------------------------------------------
0. Checkout complete trunk and

  $ mvn clean install -Dmaven.test.skip=true

  Call this until you get "BUILD SUCCESSFUL" - sometimes donwloads from maven
  repositories are temporarily unaccessible and cause the build to fail.
  
  See also MAVEN REPOSITORY MIRRORS above.

1. Go to ./core/cocoon-core and call "mvn c-eclipse:eclipse -o". This makes the project
   an PDE project. This means that Eclipse offers tools to support development based on
   OSGi. 
  
2. Get the latest release of Eclipse 3.2 (I had some problems with Eclipse 3.1 but maybe
   they are resolved in the latest 3.1.x release)

3. Create a new Eclipse workspace and don't forget to set the M2_REPO classpath variable

4. Add following projects to your workspace
   - cocoon-core
   - cocoon-blocks-fw-impl
   
5. Add the Equinox target platform to your workspace:
   - [Window] - [Preferences] - [Plug-In Development] - [Target Platform] and choose
     ./cocoon/trunk/tools/equinox-target-platform as base directory for the new target
     platform
     
6. Start Cocoon:
   - [Run] - [Run ...] - [Equinox OSGI Framework] and create a new instance
   
7. Run it by hitting the "Run" button

8. Point your browser to http://localhost/sitemap-test/test
   (the first response produces an error because nothing is returned, but
    after a refresh, everything works fine.)
