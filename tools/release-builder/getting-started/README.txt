GENERAL
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

This 'Getting Started Cocoon Application' demonstrates how a Cocoon 2.2
based application that uses blocks can be organized and built.
It is not intended to be used in production but demonstrates
the necessary steps in order to build and deploy a Cocoon 2.2 application.

Compared to a Maven 2 based build systems it lacks a lot of features,
most notably it doesn't support quick development cycles like the
Cocoon Maven 2 plugin does (See http://cocoon.apache.org/2.2/1159_1_1.html and
http://cocoon.apache.org/2.2/maven-plugins/maven-plugin/1.0/1297_1_1.html).

This means that whenever you change one of the files, you have to redo the
complete build.


STRUCTURE
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The demo application comes with a pre-packaged Jetty server (./jetty). In ./app
you find a Cocoon application which consists of a block (./app/custom-block) and
a Java web application (./app/webapp).

In ./lib there are all libraries that are needed to run Cocoon Core, the Cocoon
Template block and the Cocoon Flowscript block.


HOW TO RUN
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

First make sure that you have installed Ant. Then move to the ./app directory and
run 'ant run' from there.

This will build the block (./app/custom-block), then the Java web application
(./app/webapp) and launch a Jetty server. Then you can access it via
http://localhost:8888/custom-block/.


Enjoy!