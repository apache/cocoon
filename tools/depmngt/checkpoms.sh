#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


HERE=`dirname $0`
cd "$HERE"
HERE=$PWD
mkdir target >/dev/null 2>&1
cd ../..
echo "Start searching from $PWD"
echo '<root>' >"$HERE/target/allPom.xml"
for i in `find . -maxdepth 5 -name pom.xml`; do 
  echo "<pom file=\"$i\">" >>"$HERE/target/allPom.xml"
  cat $i | sed -r 's/^ *<[?]xml[^>]*>(.*)/\1/' >>"$HERE/target/allPom.xml"
  echo "</pom>" >>"$HERE/target/allPom.xml"
done
echo "</root>" >>"$HERE/target/allPom.xml"

lastgroup=""
lastid=""
lastver=""
lastpom=""
lastmatched=0

#XALAN=`find $HOME/.m2/repository -name "xalan*.jar"|tail -n 1`
#XERCES=`find $HOME/.m2/repository -name "xercesImpl*.jar"|tail -n 1`
#XMLAPIS=`find $HOME/.m2/repository -name "xml-apis*.jar"|tail -n 1`
#$JAVA_HOME/bin/java -classpath $XALAN:$XERCES:$XMLAPIS.jar org.apache.xalan.xslt.Process $* \
$JAVA_HOME/bin/java org.apache.xalan.xslt.Process $* \
  -xsl "$HERE/extractDeps.xsl" -in "$HERE/target/allPom.xml" \
| grep -vE '^[<]' \
| while read group id ver pom; do
  if [ "$lastid" = "$id" ]; then
    if [ "$lastver" != "$ver" -o "$lastgroup" != "$group" -o "$lastmatched" = "1" ]; then
      if [ $lastmatched = "0" ]; then
        lastmatched=1
        echo "POM: $lastpom"
        echo -e "\tgroup=$lastgroup\tartifact=$lastid\tversion=$lastver"
      fi
      echo "POM: $pom"
      echo -e "\tgroup=$group\tartifact=$id\tversion=$ver"
    fi
  else
    lastmatched=0
  fi
  lastgroup=$group
  lastid=$id
  lastver=$ver
  lastpom=$pom
done
