#!/bin/sh
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#      http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Description: This script automates updating artifact versions in POMs
# Usage: update-artifact-version.sh -g <groupid> -a <artifactid> -v <version> <paths...>
# Requirements: bash, xsltproc, find, diff, rm, mv
# Sample execution:
# ./update-artifact-version.sh -g org.apache.cocoon -a cocoon-forms -v 3-SNAPSHOT cocoon-trunk/blocks/cocoon-forms
# ./update-artifact-version.sh -g org.apache.cocoon -a cocoon-forms-impl -v 1.0.0-M3-SNAPSHOT cocoon-trunk/blocks/cocoon-forms
# ./update-artifact-version.sh -g org.apache.cocoon -a cocoon-forms-sample -v 1.0.0-M1-SNAPSHOT cocoon-trunk/blocks/cocoon-forms

basedir=`dirname $0`
xslfile="$basedir/update-artifact-version.xsl"
groupid="org.apache.cocoon"
artifactid=""
version=""
paths=""

while getopts  ":a:g:v:" OPTION $@
do
  case $OPTION in
    a) artifactid="$OPTARG" ;;
    g) groupid="$OPTARG" ;;
    v) version="$OPTARG" ;;
    *) echo "Usage: $0 [-g groupId] -a artifactId -v version [paths ...]"
  esac
done

shift $(($OPTIND-1))
paths="$@"
#echo "groupid:$groupid, artifactid:$artifactid, version:$version, paths:$paths"

# Find all pom.xml files within $paths and correct the artifact versions:
for f in `find $paths -type f -name pom.xml`; do
  xsltproc --param file "'$f'" --param groupId "'$groupid'" --param artifactId "'$artifactid'" --param version "'$version'" $xslfile $f > $f.new
  if diff -qw $f $f.new > /dev/null 2>&1; then
    # Remove identical generated version:
    #echo "Removing $f.new ..."
    rm -f $f.new
  else
    # Replace original POM with modified version:
    #echo "Replacing $f with $f.new ..."
    rm -f $f
    mv $f.new $f
  fi
done

# Fatal errors can lead to empty POM files, thus this warning:
echo "ATTENTION: It is recommended to review the modified POMs!"
