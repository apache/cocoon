OSGi Blocks Framework
=====================

This far this project only contain some preliminary OSGi experimentation.
Specifically I experimenting with how to use the declarative services for
wiring and mounting some servlets.

The project does not use Maven yet, I don't know how to build it with Maven
in a convenient way, suggestions are welcome.

I run it within Eclipse, and use 3.2M4 as it has better support for developing
and executing OSGi bundles than 3.1. Getting some knowledge about Eclipse RCP 
or at least about plugin development doesn't hurt.

Create a platform
-----------------

First one need to create a "platform" consisting of the basic OSGi framework
and service bundles. Get the latest Eclipse-Equinox-SDK and launcher (for your OS),
from the stream integration builds at http://download.eclipse.org/eclipse/equinox/.
Unpack the SDK at some appropriate place and then unpack the launcher in the top
directory of the SDK.

Choose the platform as target platform:
Window -> Preferences ... -> Plug-in development -> Target platform

Plugin development
------------------

Import the this project into Eclipse and make it a plugin:
Right click the project -> PDE Tools -> Convert project to plug-in projects ...

Running the platform
--------------------

Start the framework:
Run -> Run ... -> Equinox OSGi Framework -> New
Make shure that this project and all the bundles in the target platform are chosen
in the Plug-ins tab and that -console is given as program argument.

When the platform is started you get a OSGi console in the console view in Eclipse.
Type "help" to see what commands are available. Take a look in META-INF/components.xml
and the use "services" to see that the services are started and connected. Type
"bundles" to see what bundles there are, "log" for the log messages.

Test to stop, update, refresh and start this bundle and take a look at the log between
the steps to see how the services are connected.

Open a browser and test http://localhost/test1 and http://localhost/test2.

At last
-------

I have probably missed some information. Ask, update this document or better
make it work with Maven.
