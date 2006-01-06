
HOW TO CONVERT AN EXISTING BLOCK TO MAVEN STRUCTURE

1. use archetype plugin to create template block structure in the repo root  
  $mvn archetype:create -DgroupId=org.apache.cocoon -DartifactId=cocoon-core
2. remove the src/main/java directory from the newly created structure as this will be replaced by your own java sources. If you have testsources already then remove src/main/test/java as well. If you have a pom then remove that one as well.
3. svn add cocoon-core; svn commit cocoon-core
4. go to cocoon-core and 
  $svn move location_of_sources/java src/main 
  $svn move location_of_pom.xml .
  $svn move location_of_testsources/java src/test
5. copy/move jar resources in src/main/resources and test resources in src/test/resources
