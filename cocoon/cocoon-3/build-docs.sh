#! /bin/bash
mvn install -N
cd site
MAVEN_OPTS=-Xmx512M
export MAVEN_OPTS
mvn site-deploy -P localDocs -Ddocs.repoUrl=http://localhost:9263
mvn site-deploy -P localDocs-all-reports,all-reports,simian-report -Ddocs.repoUrl=http://localhost:9263
