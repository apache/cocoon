 This directory implements a Cocoon Ant Task.
 
 The basic attributes of this ant task are taken from org.apache.cocoon.Main
 Beside implementing launching Cocoon via ant the number of Cocoon
 invocation should be reduced compared to the Main implementation.
 
 Another trigger for implementing an Ant task:
 I wanted to exploit a bit launching cocoon from a different environment
 than servlet, and main-commandline environment.
 
 Side Note:
   Main invokes Cocoon for getting the links for a URI, it
   invokes Cocoon for getting the content type of a URI, and
   finally it invokes Cocoon for getting the page content itself.
 
 This implementation skips getting the content type of a URI,
 by implementing a DelayedFileSavingEnvironment. This class
 write the content of a page into a ByteArrayBuffer till the content type
 of page has been settled. This should and will save time generating
 off-line pages.
 Moreover getting links of an URI is reduced to URIs having content type text/*.
 This should speed up the page generation a bit more. This feature should
 be made configurable... 

 Classloader Note:
   I struggled a bit making Cocoon running as an Ant task, the 
   the classpath for loading the cocoon task should include all 
   classes need for running cocoon too.
   In the CocoonTask java code itself the Thread.currentThred().setContextClassLoader()
   is absolutly important, otherwise Cocoon will fail configuring using
   it starup manager.
   Of course you can define all Cocoon classes in your java classpath commandline
   environment, but i wanted to avoid this. Defining all needed classes only
   in the ant script.
 
 Cocoon Ant task usage:
 <project basedir="." default="cocoon-docs" name="cocoon-docs">
   <property name="cocoon.dir" value="your-cocoon-directory"/>
 
   <path id="cocoon.classpath">
     <fileset dir="${cocoon.dir}/lib">
     <include name="core/jvm1.4/*.jar"/>
     <include name="core/*.jar"/>
     <include name="optional/*.jar"/>
     </fileset>
     <fileset dir="${cocoon.dir}/build/cocoon">
     <include name="*.jar"/>
     </fileset>
   </path>

   <target 
     name="prepare-cocoon-docs"
     description="[internal] define the task for launching Cocoon">
     
     <taskdef name="cocoon" classname="org.apache.cocoon.ant.CocoonTask" 
       classpathref="cocoon.classpath"/>
   </target>

   <target name="cocoon-docs" depends="prepare-cocoon-docs"
     description="* build cocoon documentation">
     
     <property name="contextDir" value="${cocoon.dir}/build/cocoon/documentation"/>
     <property name="workDir" value="${cocoon.dir}/build/cocoon-ant-work"/>
     <property name="destDir" value="${cocoon.dir}/build/cocoon-ant-docs"/>
     
     <cocoon
       contextDir="${contextDir}"
       workDir="${workDir}"
       destDir="${destDir}"
       targets="/index.html"
       logLevel="WARN"
     />
   </target>
 </project>

 CocoonTask Attributes

 Cocoon Creation
 ---------------

 contextDir - Cocoon's context directory, mandatory
 configFile - specifies cocoon xconf file, by default try using ${contextDir}/WEB-INF/cocoon.xconf,
 ${contextDir}/cocoon.xconf

 workDir - Cocoon's work directory, by default sub directory work of directory specified
 by system property java.io.tmpdir

 Cocoon Logging
 --------------
 logLevel - log level option DEBUG, INFO, WARN, ERROR, by default INFO
 logger - logger category, by default "cocoon"
 logkitXconf - logkit xconf file, by default try using ${contextDir}/WEB-INF/logkit.xconf,
 ${contextDir}/cocoon.xconf

 Processing Cocoon options
 -------------------------
 targets - comma, space, or semicolon seperated list of target URIs, eg. /index.html,
 mandatory

 acceptHeader - accept header, by default text/html, * / *
 agentHeader - agent header, by default Apache Cocoon 2.1-dev

 Processing Cocoon File Generation
 ---------------------------------
 destDir - destination directory of generated targets, mandatory

 Processing Cocoon Modes
 -----------------------
 followLinks - boolean value, if following links should be processed, by default true
 precompileOnly - boolean value, if xsp precompile should be performed, by default false,
 not evaluted - yet

 Processing Cocoon Link Generation
 ---------------------------------
 to-do

 Processing Cocoon Index Generation
 ----------------------------------
 to-do


Future Plans
 I think about creating some ant inner classes for specifying some more subtask
 Subtask ideas:
   Offline index generation
   Offline xsp compilation
   Offline link generation

 Ideas are welcome
 Have fun.
 
