#! /bin/bash
mvn install -N
cd site
mvn site-deploy -P localDocs
