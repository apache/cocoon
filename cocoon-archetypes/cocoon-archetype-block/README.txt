
This archetype requires Maven 2.0 release, grab it from 
http://www.apache.org/dist/maven/binaries/ or a local mirror.

As long as the archetype is not installed on ibiblio you will have to install 
it locally first. You can do this by running "mvn install" from this directory.

To try it out, go to your favourite temp directory and do :

mvn archetype:create -DarchetypeGroupId=org.apache.cocoon \
-DarchetypeArtifactId=cocoon-archetype-block -DgroupId=mynewblock \
-DartifactId=theBlock -DarchetypeVersion=1.0-SNAPSHOT

Note: You can replace -DartifactId=theBlock with anything you like, same goes for 
-DgroupId=mynewblock as they are effectively the groupId and artifactId for 
your new project.


Upon successful completion, you will have a new directory c:\temp\theBlock that 
contains the project structure. This is a ready-to-go maven2 project, meaning that executing 
for example "mvn war:exploded" will create a webapp directory in target\ ready to be mounted.
Similarly, "mvn war:war" creates a deployable war file. 
