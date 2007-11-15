#!/bin/sh

export MAVEN_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=5005,server=y,suspend=n"
cd core/cocoon-webapp && echo "Starting in `pwd`" && mvn -Djava.awt.headless=true -Dorg.apache.cocoon.mode=dev jetty:run

