#!/bin/sh -x


# $Id: create-repository-jars.sh,v 1.9 2004/02/04 16:28:29 giacomo Exp $

# This script will do the following:
#   - checkout/update a cocoon-2.1 repository
#   - make a local.build.properties, that excludes all documentation stuff
#   - build all jars and the war file
#   - copy all jars and the war to the appropriate locations (repository structure)

# The cvs repository name
if [ "$REPOSITORY_NAME" = "" ]; then
  REPOSITORY_NAME=cocoon-2.1
fi

# What is the default revision/branch/tag to use
# In case of a HEAD revision a SNAPSHOT version will be created and
# any old snapshots will be removed to save some space
if [ "$REVISION" = "" ]; then
  REVISION="" # it's a HEAD
fi

# What is the default CVSROOT to be used for checkout
if [ "$CVSROOT" = "" ]; then
  CVSROOT=":pserver:anoncvs@cvs.apache.org:/home/cvspublic"
fi

# Where is the local cocoon cvs repository we use to maintain.
# If this directory doesn't exists it will do a 
#   'cvs -d $CVSROOT co -Pd $LOCAL_REPOSITORY -r $REVISION $REPOSITORY_NAME'
#   to create it.
#   
# If it exists it will do a 
#   'cvs upd -dPACr $REVISION'
#   in there to update to the requested revision (see REVISION below)
if [ "$LOCAL_REPOSITORY" = "" ]; then
  LOCAL_REPOSITORY=$HOME/cvs/cocoon-2.1
fi

# On which host should the artifacts be published
if [ "$REMOTEHOST" = "" ]; then
  REMOTEHOST=www.apache.org
fi

# Where is the path on the remote host the repository is located at
if [ "$REMOTEPATH" = "" ]; then
  REMOTEPATH=/www/www.apache.org/dist/java-repository/cocoon
fi

# Where is the md5sum command to be used
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
NOCVS=0
NOBUILD=0
NOCLEAN=0
NOJARS=0
NOWAR=0
BUILD_SRC_DIST=0
while getopts ":BCdhJkW" option
do
  case $option in
    B) NOBUILD=1;;
    C) NOCVS=1;;
    d) BUILD_SRC_DIST=1;;
    h) echo "Usage: `basename $0` [-B] [-C] [-d] [-J] [-h] [-k] [-W] [RELEASE-TAG]"
       echo "       -B          don't do a build"
       echo "       -C          don't do a cvs update"
       echo "       -d          build a src dist"
       echo "       -h          this usage note"
       echo "       -J          don't deploy the jar files"
       echo "       -k          keep the build tree (don't do a clean-dist)"
       echo "       -W          don't deploy the war file"
       echo "       RELEASE-TAG the tag to do a cvs update for"
       echo "                   if the RELEASE-TAG is HEAD a snapshot version"
       echo "                   will be produced"
       exit 0
       ;;
    J) NOJARS=1;;
    k) NOCLEAN=1;;
    W) NOWAR=1;;
    *     ) echo "Unimplemented option $option chosen.";;
  esac
done

shift $(($OPTIND - 1))
if [ "$1" != "" -a "$1" != "HEAD" ]; then
  REVISION=$1
fi

# check if the local repository exists and do a checkout/update accordingly
if [ -d "$LOCAL_REPOSITORY" ]; then
  cd $LOCAL_REPOSITORY
  if [ $NOCVS = 0 ]; then
    echo
    echo "updating the local repository at $LOCAL_REPOSITORY with"
    if [ "$REVISION" = "" ]; then
      echo "    cvs up -dPAC"
      echo
      cvs up -dPAC
    else
      echo "    cvs up -dPACr $REVISION"
      echo
      cvs up -dPACr $REVISION
    fi
  fi
else
  DIRNAME=`dirname $LOCAL_REPOSITORY`
  BASENAME=`basename $LOCAL_REPOSITORY`
  if [ ! -d $DIRNAME ]; then
    mkdir -p $DIRNAME 2>/dev/null >/dev/null
  fi
  cd $DIRNAME 
  echo
  echo "checking out into the local repository at $LOCAL_REPOSITORY with "
  if [ "$REVISION" = "" ]; then
    echo "    cvs -d $CVSROOT co -Pd $LOCAL_REPOSITORY $REPOSITORY_NAME"
    echo
    cvs -d $CVSROOT co -Pd $BASENAME $REPOSITORY_NAME
  else
    echo "    cvs -d $CVSROOT co -Pd $LOCAL_REPOSITORY -r $REVISION $REPOSITORY_NAME"
    echo
    cvs -d $CVSROOT co -Pd $BASENAME -r $REVISION $REPOSITORY_NAME
  fi
  cd $LOCAL_REPOSITORY
fi

# cleanup the repository, prepare and do a build if not suppressed by command line option
RC=0 # set in advace in case we don't do a build
if [ $NOBUILD = 0 ]; then
  # build the local.blocks.properties file
  echo
  echo "generating local.blocks.properties file"
  echo
  cat blocks.properties \
    >local.blocks.properties

  # build the local.build.properties file
  echo
  echo "generating local.build.properties file"
  echo
  cat build.properties \
    | sed 's/#exclude.webapp.documentation/exclude.webapp.documentation/' \
    | sed 's/#exclude.webapp.javadocs/exclude.webapp.javadocs/' \
    | sed 's/#exclude.webapp.samples/exclude.webapp.samples/' \
    | sed 's/#exclude.documentation/exclude.documentation/' \
    | sed 's/#exclude.javadocs/exclude.javadocs/' \
    | sed 's/#exclude.validate.xdocs/exclude.validate.xdocs/' \
    | sed 's/#config.allow-reloads/config.allow-reloads/' \
    | sed 's/#config.enable-uploads/config.enable-uploads/' >local.build.properties

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
  ./build.sh $CLEAN webapp war | tee $LOCAL_REPOSITORY/build.log
  # The build script dosn't report on failures so we have to do that by hand
  tail -10 $LOCAL_REPOSITORY/build.log|grep "BUILD SUCCESSFUL"
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

# copy all the jars produced over to the remote repository space
VERSION=`ls build | grep cocoon | sed s/cocoon-//`
if [ "$REVISION" = "" ]; then 
  TVERSION=`date "+%Y%m%d.%H%M%S"` 
else
  TVERSION=$VERSION
fi

if [ $NOJARS = 0 ]; then
  # create the target directory if they do not exists and make them group writable
  ssh $REMOTEHOST "mkdir -p $REMOTEPATH/jars 2>/dev/null >/dev/null; \
                 chmod -R g+w $REMOTEPATHi/jars"
  JARS=`find build/cocoon-$VERSION -name "*.jar"`
  for i in $JARS; do
    FILE=`echo $i | sed 's/.*[/]//' | sed s/[.]jar//`
    isBlock=`echo $FILE|grep block`
    if [ ! -z "$isBlock" ]; then
      BLOCKPART="-`echo $FILE | sed 's/-block//'`"
    else
      BLOCKPART=`echo $FILE | sed 's/cocoon//'`
    fi
    if [ "$REVISION" = "" ]; then
      # remove all snapshots in the remote repository
      SNAPSHOT=`ssh $REMOTEHOST "ls $REMOTEPATH/jars/cocoon$BLOCKPART-????????.??????.jar 2>/dev/null"` 
    fi
    scp $i $REMOTEHOST:$REMOTEPATH/jars/cocoon$BLOCKPART-$TVERSION.jar
    if [ "$REVISION" = "" ]; then
      if [ ! -z "$SNAPSHOT" ]; then
        RM="rm $SNAPSHOT;"
      else
        RM=""
      fi
      CMD="$RM \
           cd $REMOTEPATH/jars; \
           ln -fs cocoon$BLOCKPART-$TVERSION.jar cocoon$BLOCKPART-SNAPSHOT.jar; \
           echo $TVERSION >cocoon$BLOCKPART-snapshot.version;"
    else
      CMD=""
    fi
    ssh $REMOTEHOST "$CMD \
                     $MD5SUM <$REMOTEPATH/jars/cocoon$BLOCKPART-$TVERSION.jar | \
                       sed 's/ .*$//' >$REMOTEPATH/jars/cocoon$BLOCKPART-$TVERSION.jar.md5; \
                     chmod g+w $REMOTEPATH/jars/cocoon$BLOCKPART-$TVERSION.*"
  done
fi

# copy the war file to the web space
if [ "$NOWAR" = "0" ]; then
  # create the target directory if they do not exists and make them group writable
  ssh $REMOTEHOST "mkdir -p $REMOTEPATH/wars 2>/dev/null >/dev/null; \
                   chmod -R g+w $REMOTEPATH/wars"
  WAR=build/cocoon-$VERSION/cocoon.war
  if [ "$REVISION" = "" ]; then
    SNAPSHOT=`ssh $REMOTEHOST "ls $REMOTEPATH/wars/cocoon-war-????????.??????.war 2>/dev/null"` 
  fi
  scp $WAR $REMOTEHOST:$REMOTEPATH/wars/cocoon-war-$TVERSION.war
  if [ "$REVISION" = "" ]; then
    if [ ! -z "$SNAPSHOT" ]; then
      RM="rm $SNAPSHOT;"
    else
      RM=""
    fi
    CMD="$RM \
         cd $REMOTEPATH/wars; \
         ln -fs cocoon-war-$TVERSION.war cocoon-war-SNAPSHOT.war; \
         echo $TVERSION >cocoon-war-snapshot.version;"
  else
    CMD=""
  fi
  ssh $REMOTEHOST "$CMD \
                   $MD5SUM <$REMOTEPATH/wars/cocoon-war-$TVERSION.war | \
                     sed 's/ .*$//' >$REMOTEPATH/jars/cocoon-war-$TVERSION.jar.md5; \
                   chmod g+w $REMOTEPATH/jars/cocoon-war-$TVERSION.*"
fi

# create a distribution
if [ "$BUILD_SRC_DIST" = "1" ]; then
  # create the target directory if they do not exists and make them group writable
  ssh $REMOTEHOST "mkdir -p $REMOTEPATH/distributions 2>/dev/null >/dev/null; \
                   chmod -R g+w $REMOTEPATH/distributions"
  ./build.sh clean-dist
  cd ..
  if [ "$REVISION" = "" ]; then
    TVERSION=SNAPSHOT
  fi
  ln -sf $REPOSITORY_NAME cocoon-src-$TVERSION
  zip -r cocoon-src-$TVERSION.zip cocoon-src-$TVERSION
  rm cocoon-src-$TVERSION
  scp cocoon-src-$TVERSION.zip $REMOTEHOST:$REMOTEPATH/distributions/cocoon-src-$TVERSION.zip
  rm cocoon-src-$TVERSION.zip
fi
