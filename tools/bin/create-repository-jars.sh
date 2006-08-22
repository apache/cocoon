#!/bin/sh 

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# $Id$

# This script will do the following:
#   - checkout/update a cocoon-2.1 repository
#   - make a local.build.properties, that excludes all documentation stuff
#   - build all jars and the war file
#   - copy all jars and the war to the appropriate locations (repository structure)
#
# Usually one start the script in the Cocoon root directory by issuing:
#
#     ./tools/bin/create-repository-jars.sh tags/RELEASE_2_1_x
#
# The-h option will give you a short usage note and the settings of environment 
# variables the script uses
#

# The path to the local repository we maintain
if [ "$LOCAL_PATH" = "" ]; then
  LOCAL_PATH="$PWD"
fi

# The base URL to the SVN repository
if [ "$SVN_BASE_URL" = "" ]; then
  SVN_BASE_URL=https://svn.apache.org/repos/asf/cocoon
fi

# On which host should the artifacts be published
if [ "$REMOTE_HOST" = "" ]; then
  REMOTE_HOST=svn.apache.org
fi

# The path to the remote repository
if [ "$REMOTE_PATH" = "" ]; then
  REMOTE_PATH=/www/www.apache.org/dist/java-repository/cocoon
fi

# The SVN URL part indicating the HEAD of development
if [ "$HEAD_SUB_URL" = "" ]; then
  HEAD_SUB_URL=branches/BRANCH_2_1_X
fi

# The SVN URL part pointing to the revision/branch/tag to use
if [ "$REVISION_SUB_URL" = "" ]; then
  REVISION_SUB_URL=$HEAD_SUB_URL
fi

# The MD5 sum utility
if [ "$MD5SUM" = "" ]; then
  MD5SUM=/sbin/md5
fi

# ------- NO NEED TO CHANGE ANYTHING BELOW HERE ----------

if [ "$JAVA_HOME" = "" ]; then
  echo "You need to set the JAVA_HOME environment variable to the installed JDK 1.3"
  exit 1
fi

$JAVA_HOME/bin/java -version

# parse for options
NOSVN=0
NOBUILD=0
NOCLEAN=0
NOJARS=0
NOWAR=0
BUILD_SRC_DIST=0
RC=0
while getopts ":BdhJkSW" option
do
  case $option in
    B) NOBUILD=1;;
    d) BUILD_SRC_DIST=1;;
    h) echo "Usage: `basename $0` options [-S] [-B] [-h] [TAG-Path]"
       echo "       -S          don't do a svn update"
       echo "       -B          don't do a build"
       echo "       -d          build a src dist"
       echo "       -h          this usage note"
       echo "       -J          don't deploy the jar files"
       echo "       -k          keep the build tree (don't do a clean-dist)"
       echo "       -W          don't deploy the war file"
       echo "       TAG-Path    the path to do a svn Tag or Branch from (excluding the base url)"
       echo "                   if the TAG-Path equals to HEAD a snapshot version"
       echo "                   will be produced"
       echo 
       echo "   Settings:"
       echo "      LOCAL_PATH:       $LOCAL_PATH"
       echo "      SVN_BASE_URL:     $SVN_BASE_URL"
       echo "      REMOTE_HOST:      $REMOTE_HOST"
       echo "      REMOTE_PATH:      $REMOTE_PATH"
       echo "      HEAD_SUB_URL:     $HEAD_SUB_URL"
       echo "      REVISION_SUB_URL: $REVISION_SUB_URL"
       echo "      MD5SUM:           $MD5SUM"
       echo "      JAVA_HOME:        $JAVA_HOME"
       exit 0
       ;;
    J) NOJARS=1;;
    k) NOCLEAN=1;;
    S) NOSVN=1;;
    W) NOWAR=1;;
    *     ) echo "Unimplemented option $option chosen.";;
  esac
done
shift $(($OPTIND - 1))

# check if a different revision/branch/tag was specified on the command line
if [ ! -z "$1" ]; then
  REVISION_SUB_URL=$1
fi

# check if the local repository exists and do a checkout/update accordingly
if [ -d "$LOCAL_PATH" -a -d "$LOCAL_PATH/.svn" ]; then
  cd $LOCAL_PATH
  if [ $NOSVN = 0 ]; then
    echo
    echo "updating the local repository at $LOCAL_PATH with"
    echo "    svn switch ${SVN_BASE_URL}/$REVISION_SUB_URL"
    echo
    svn switch ${SVN_BASE_URL}/$REVISION_SUB_URL
  fi
elif  [ -d "$LOCAL_PATH" ]; then
  cd $LOCAL_PATH
  echo
  echo "checking out into the local repository at $LOCAL_PATH with "
  echo "    svn co ${SVN_BASE_URL}/$REVISION_SUB_URL $LOCAL_PATH"
  echo
  svn co ${SVN_BASE_URL}/$REVISION_SUB_URL $LOCAL_PATH
else
  DIRNAME=`dirname $LOCAL_PATH`
  BASENAME=`basename $LOCAL_PATH`
  if [ ! -d $DIRNAME ]; then
    mkdir -p $DIRNAME 2>/dev/null >/dev/null
  fi
  cd $DIRNAME 
  echo
  echo "checking out into the local repository at $LOCAL_PATH with "
  echo "    svn co ${SVN_BASE_URL}/$REVISION_SUB_URL $BASENAME"
  echo
  svn co ${SVN_BASE_URL}/$REVISION_SUB_URL $BASENAME
  cd $LOCAL_PATH
fi

# cleanup the repository, prepare and do a build if not suppressed by command line option
if [ "$NOBUILD" -eq 0 ]; then
  echo
  echo "clean the local repository"
  echo
  ./build.sh clean-dist 
  find . -type f -name "*~" | xargs rm -f 2>/dev/null 1>/dev/null
  find . -type f -name ".#*" | xargs rm -f 2>/dev/null 1>/dev/null
  find . -type f -name "#*#" | xargs rm -f 2>/dev/null 1>/dev/null
  find . -type f -name "*.bak" | xargs rm -f 2>/dev/null 1>/dev/null
  find . -type f -name "*.BAK" | xargs rm -f 2>/dev/null 1>/dev/null

  # build the blocks.properties file
  echo
  echo "generating local.blocks.properties file"
  echo
  cat blocks.properties \
    >local.blocks.properties

  # build the build.properties file
  echo
  echo "generating local.build.properties file"
  echo
  cat build.properties \
    | sed 's/#exclude.webapp.documentation/exclude.webapp.documentation/' \
    | sed 's/#exclude.webapp.javadocs/exclude.webapp.javadocs/' \
    | sed 's/#exclude.webapp.samples/exclude.webapp.samples/' \
    | sed 's/#exclude.webapp.test-suite/exclude.webapp.test-suite/' \
    | sed 's/#exclude.documentation/exclude.documentation/' \
    | sed 's/#exclude.javadocs/exclude.javadocs/' \
    | sed 's/#config.allow-reloads/config.allow-reloads/' \
    | sed 's/#config.enable-uploads/config.enable-uploads/' \
    | sed 's/#exclude.validate.config/exclude.validate.config/' \
    | sed 's/#exclude.validate.xdocs/exclude.validate.xdocs/' \
    | sed 's/#exclude.validate.jars/exclude.validate.jars/' \
    >local.build.properties

  # build everything
  echo
  if [ $NOCLEAN = 0 ]; then
    echo "clean the local repository, build the webapp, and the war files"
    CLEAN=clean-dist
  else
    echo "build the webapp, and the war files"
    CLEAN=""
  fi
  echo
  ./build.sh $CLEAN webapp war | tee $LOCAL_PATH/build.log
  # The build script dosn't report on failures so we have to do that by hand
  tail -n 10 $LOCAL_PATH/build.log|grep "BUILD SUCCESSFUL"
  if [ $? = 0 ]; then
    RC=0
  else
    RC=1
  fi
fi

if [ $RC -ne 0 ]; then
  echo "The build has failed"
  exit $RC
fi

# copy all the jars produced over to the web server space
VERSION=2.1.9
if [ "$REVISION_SUB_URL" == "$HEAD_SUB_URL" ]; then 
  TVERSION=`date "+%Y%m%d.%H%M%S"` 
else
  TVERSION=$VERSION
fi

if [ $NOJARS = 0 ]; then
  # create the target directory if they do not exists and make them group writable
  ssh $REMOTE_HOST "mkdir -p $REMOTE_PATH/jars 2>/dev/null >/dev/null; \
                 chmod -R g+w $REMOTE_PATH/jars"
  JARS=`find build/cocoon -name "*.jar"`
  for i in $JARS; do
    FILE=`echo $i | sed 's/.*[/]//' | sed s/[.]jar//`
    isBlock=`echo $FILE|grep block`
    if [ ! -z "$isBlock" ]; then
      BLOCKPART="-`echo $FILE | sed 's/-block//'`"
    else
      BLOCKPART=`echo $FILE | sed 's/cocoon//'`
    fi
    if [ "$REVISION_SUB_URL" == "$HEAD_SUB_URL" ]; then
      # remove all snapshots in the remote repository
      SNAPSHOT=`ssh $REMOTE_HOST "ls $REMOTE_PATH/jars/cocoon$BLOCKPART-????????.??????.jar 2>/dev/null"` 
    fi
    scp $i $REMOTE_HOST:$REMOTE_PATH/jars/cocoon$BLOCKPART-$TVERSION.jar
    if [ "$REVISION_SUB_URL" == "$HEAD_SUB_URL" ]; then
      if [ ! -z "$SNAPSHOT" ]; then
        RM="rm ${SNAPSHOT}\*;\
	    rm $REMOTE_PATH/jars/cocoon$BLOCKPART-snapshot.version;"
      else
        RM=""
      fi
      CMD="$RM \
           cd $REMOTE_PATH/jars; \
           rm cocoon$BLOCKPART-SNAPSHOT.jar; \
           ln -s cocoon$BLOCKPART-$TVERSION.jar cocoon$BLOCKPART-SNAPSHOT.jar; \
           echo $TVERSION >cocoon$BLOCKPART-snapshot.version;"
    else
      CMD=""
    fi
    ssh $REMOTE_HOST "$CMD \
                      $MD5SUM <$REMOTE_PATH/jars/cocoon$BLOCKPART-$TVERSION.jar | \
                      sed 's/ .*$//' >$REMOTE_PATH/jars/cocoon$BLOCKPART-$TVERSION.jar.md5; \
                      chmod g+w $REMOTE_PATH/jars/cocoon$BLOCKPART-$TVERSION.*"
  done
fi

# copy the war file to the web space
if [ "$NOWAR" = "0" ]; then
  # create the target directory if they do not exists and make them group writable
  ssh $REMOTE_HOST "mkdir -p $REMOTE_PATH/wars 2>/dev/null >/dev/null; \
                   chmod -R g+w $REMOTE_PATH/wars"
  WAR=build/cocoon-$VERSION/cocoon.war
  if [ "$REVISION_SUB_URL" == "$HEAD_SUB_URL" ]; then
    SNAPSHOT=`ssh $REMOTE_HOST "ls $REMOTE_PATH/wars/cocoon-war-????????.??????.war 2>/dev/null"` 
  fi
  scp $WAR $REMOTE_HOST:$REMOTE_PATH/wars/cocoon-war-$TVERSION.war
  if [ "$REVISION_SUB_URL" == "$HEAD_SUB_URL" ]; then
    if [ ! -z "$SNAPSHOT" ]; then
      RM="rm ${SNAPSHOT}\*;\
          rm $REMOTE_PATH/wars/cocoon-war-snapshot.version;"
    else
      RM=""
    fi
    CMD="$RM \
         cd $REMOTE_PATH/wars; \
         rm cocoon$BLOCKPART-SNAPSHOT.war; \
         ln -s cocoon-war-$TVERSION.war cocoon-war-SNAPSHOT.war; \
         echo $TVERSION >cocoon-war-snapshot.version;"
  else
    CMD=""
  fi
  ssh $REMOTE_HOST "$CMD \
                    $MD5SUM <$REMOTE_PATH/wars/cocoon-war-$TVERSION.war | \
                    sed 's/ .*$//' >$REMOTE_PATH/wars/cocoon-war-$TVERSION.war.md5; \
                    chmod g+w $REMOTE_PATH/wars/cocoon-war-$TVERSION.*"
fi

# create a distribution
if [ "$BUILD_SRC_DIST" = "1" ]; then
  # create the target directory if they do not exists and make them group writable
  ssh $REMOTE_HOST "mkdir -p $REMOTE_PATH/distributions 2>/dev/null >/dev/null; \
                   chmod -R g+w $REMOTE_PATH/distributions"
  ./build.sh dist
  cd dist
  if [ "$REVISION_SUB_URL" == "$HEAD_SUB_URL" ]; then
    SNAPSHOT=`ssh $REMOTE_HOST "ls $REMOTE_PATH/distributions/cocoon-src-????????.??????.zip 2>/dev/null"` 
    if [ ! -z "$SNAPSHOT" ]; then
      RM="rm ${SNAPSHOT}\*;\
	  rm $REMOTE_PATH/distributions/cocoon-src-snapshot.version;"
    else
      RM=""
    fi
    CMD="$RM \
         cd $REMOTE_PATH/distributions; \
         rm cocoon-src-SNAPSHOT.zip; \
         ln -s cocoon-src-$TVERSION.zip cocoon-src-SNAPSHOT.zip; \
         echo $TVERSION >cocoon-src-snapshot.version;"
    ln -s cocoon-$TVERSION cocoon-src-SNAPSHOT
    zip -qr $REMOTE_PATH/distributions/cocoon-src-$TVERSION.zip cocoon-src-SNAPSHOT
  else
    CMD=""
    ln -s cocoon-$TVERSION cocoon-src-$TVERSION
    zip -qr cocoon-src-$TVERSION.zip cocoon-src-$TVERSION
  fi
  scp cocoon-src-$TVERSION.zip $REMOTE_HOST:$REMOTE_PATH/distributions/cocoon-src-$TVERSION.zip
  ssh $REMOTE_HOST "$CMD \
                    $MD5SUM <$REMOTE_PATH/distributions/cocoon-src-$TVERSION.zip | \
                    sed 's/ .*$//' >$REMOTE_PATH/distributions/cocoon-src-$TVERSION.zip.md5; \
                    chmod g+w $REMOTE_PATH/distributions/cocoon-src-$TVERSION.zip*"
    
fi
