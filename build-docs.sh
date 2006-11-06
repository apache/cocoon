#! /bin/bash
mvn install -N
cd site
mvn site-deploy -P localDocs -Ddocs.repoUrl=http://localhost:9263
