<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  xmlns:block="http://apache.org/cocoon/blocks/cob/1.0" version="1.0">
  <xsl:output standalone="yes"/>
  <xsl:output indent="yes"/>
  
  <!-- paths -->
  <xsl:variable name="blockbuilder.root">${blockbuilder.root}</xsl:variable>
  <xsl:variable name="default.block.output.path">/build/public/classes</xsl:variable>
  <xsl:variable name="default.core.output.path">/build/cocoon/classes</xsl:variable>  
  <xsl:variable name="build.public.classes">${build.public.classes}</xsl:variable>
  <xsl:variable name="build.public">${build.public}</xsl:variable>    		
  <xsl:variable name="src.public">${src.public}</xsl:variable>
  <xsl:variable name="lib.core.dir">${lib.core.dir}</xsl:variable>
  <xsl:variable name="root.core">${root.core}</xsl:variable>
  <xsl:variable name="path.to.cocoon.public.jar">build/cocoon/cocoon.jar</xsl:variable>
  <xsl:variable name="build.temp.tasks">${build.root}/temp/tasks</xsl:variable>
  
  <!-- compiler -->
  <xsl:variable name="compiler.debug">${compiler.debug}</xsl:variable>
  <xsl:variable name="compiler.optimize">${compiler.optimize}</xsl:variable>
  <xsl:variable name="compiler.deprecation">${compiler.deprecation}</xsl:variable>
  <xsl:variable name="target.vm">${target.vm}</xsl:variable>
  <xsl:variable name="source.vm">${source.vm}</xsl:variable>
  <xsl:variable name="compiler.nowarn">${compiler.nowarn}</xsl:variable>
  <xsl:variable name="compiler">${compiler}</xsl:variable>    

  <!-- conditions -->
  <xsl:variable name="cond.toplevelcall">${cond.toplevelcall}</xsl:variable>
  
  <xsl:template match="block:block">

    <project default="compile" name="Building block {name}">
      
    	<target name="init">
    		<!-- read local user properties -->
    		<property file="local.build.properties"/>
    
    		<!-- read block default properties -->		
    		<property file="build.properties"/>
    		
    		<!-- read global default properties -->
    		<property file="{$blockbuilder.root}/targets/global.build.properties"/>    		
    		
    		<!-- if the value toplevelcall is set, unnecessary recursions can be avoided -->
    	  <xsl:variable name="toplevelcall">${toplevelcall}</xsl:variable>
        <condition property="cond.toplevelcall">
          <not><isset property="toplevelcall"/></not>
        </condition>
    	</target>  
      
    	<target depends="init" name="compile" 
    	  description="Compile (block + all required blocks)"  
    	  >
    	  <!-- check whether parameters are set, unless stop script execution -->
        <condition property="cond.root.core">
        	<isset property="root.core"/>
        </condition>    
        <fail unless="cond.root.core" message="Property root.core has to be set."/>
        <xsl:for-each select="block:requirements/block:requires">
          <xsl:variable name="prop">root.block.<xsl:value-of select="@name"/></xsl:variable>
          <condition property="cond.{$prop}">
          	<isset property="{$prop}"/>
          </condition>    
          <fail unless="cond.{$prop}" message="Property {$prop} has to be set."/>
        </xsl:for-each>
    	  
    	  <!-- first compile all blocks this block depends on -->
    		<antcall target="compile-required-blocks">
    		  <param name="toplevelcall" value="{$cond.toplevelcall}"/>
    		</antcall>
    		
    		<xsl:call-template name="info">
    		  <xsl:with-param name="msg">Compiling block <xsl:value-of select="block:name"/></xsl:with-param>
    		</xsl:call-template>
    		
    		<!-- setup the classpath
    		     for convenience reasons not the exact classpath is built but all 
    		     libraries are used -->
    		<path id="classpath">
    		  <xsl:variable name="lib.core.dir">${lib.core.dir}</xsl:variable>
    			<fileset dir="{$lib.core.dir}">
    				<include name="core/*.jar"/>
    				<include name="endorsed/*.jar"/>	
    				<include name="blocks/*.jar"/>
    			</fileset>
    			<xsl:variable name="output.core.classes">${root.core}<xsl:value-of select="$default.core.output.path"/></xsl:variable>
    			<dirset dir="{$output.core.classes}"/>
    			<!-- add classes of blocks this block depends on -->
    			<xsl:for-each select="block:requirements/block:requires">
    			  <xsl:variable name="output.block.classes">${root.block.<xsl:value-of select="@name"/>}<xsl:value-of select="$default.block.output.path"/></xsl:variable>
    			  <dirset dir="{$output.block.classes}"/>
    			</xsl:for-each>
    		</path>
    		
    		<!-- compile this block -->
    		<mkdir dir="{$build.public.classes}"/>
        <javac srcdir="{$src.public}"
               destdir="{$build.public.classes}"
               debug="{$compiler.debug}"
               optimize="{$compiler.optimize}"
               deprecation="{$compiler.deprecation}"
               target="{$target.vm}"
               source="{$source.vm}"
               nowarn="{$compiler.nowarn}"
               compiler="{$compiler}"
               classpathref="classpath"/>
    	</target>    
    	
      <target name="package" depends="init" description="Create JAR file (this block + required blocks))">
          <!-- here is the root of a small bug: because of recursive calls this is also called on 
               targets that block depends on what's unnecessary but doesn't really harm because 
               the javac task recognizes that nothing has changed and doesn't compile again -->
          <antcall target="compile"/>
        
      		<antcall target="package-required-blocks">
      		  <param name="toplevelcall" value="{$cond.toplevelcall}"/>
      		</antcall>     
      		
      		<xsl:call-template name="info">
      		  <xsl:with-param name="msg">Packaging block <xsl:value-of select="block:name"/></xsl:with-param>
      		</xsl:call-template>      		
      		   
          <jar jarfile="{$build.public}/cocoon-block-{/block:block/block:name/@short}-{/block:block/block:name/@version}.jar" index="true">
            <fileset dir="{$build.public.classes}"/>
          </jar>		
      </target>    	 
    	
     	<target depends="init" name="clean-all" description="Clean (this block + all required blocks)">  
     	  <!-- clean all dependand blocks -->
    		<antcall target="clean-required-blocks">
    		  <param name="toplevelcall" value="{$cond.toplevelcall}"/>
    		</antcall>
    		<antcall target="clean"/>
      </target>

     	<target depends="init" name="clean" description="Clean the build directory(this block)"> 		
     		<xsl:variable name="build.root">${build.root}</xsl:variable>   
     		<xsl:call-template name="info">
    		  <xsl:with-param name="msg">Cleaning block <xsl:value-of select="block:name"/></xsl:with-param>
    		</xsl:call-template>
    		<delete dir="{$build.root}"/>
      </target>      
    	
    	<target name="compile-eclipse-task" depends="package">
    		<mkdir dir="{$build.temp.tasks}"/>
        <javac srcdir="{$blockbuilder.root}/java"
               destdir="{$build.temp.tasks}"
               debug="{$compiler.debug}"
               optimize="{$compiler.optimize}"
               deprecation="{$compiler.deprecation}"
               target="{$target.vm}"
               source="{$source.vm}"
               nowarn="{$compiler.nowarn}"
               compiler="{$compiler}"
               classpathref="classpath"/>	    	  
    	</target>
    	
    	<target depends="compile-eclipse-task" name="eclipse-project" description="Create Eclipse project (this block)">
      		<xsl:call-template name="info">
      		  <xsl:with-param name="msg">Building Eclipse project files for block <xsl:value-of select="block:name"/></xsl:with-param>
      		</xsl:call-template>      	  
    	  <!-- include custom build task that generates the .classpath
    	       That is necessary as the library paths are decoupled from block descriptor.
    	       Using XSLT would require writing an XSLT that creates an XSLT that generates an Ant script *grrr* -->
        <path id="task.classpath">  	
          <pathelement location="{$build.temp.tasks}"/>
        </path> 
         		
        <!-- define the task -->
        <taskdef name="eclipse" 
          classname="org.apache.cocoon.blockbuilder.ant.EclipseClasspathBuilderTask"
        	classpathref="task.classpath"/>
        	
        <!-- calling the task -->
        <eclipse
        	corejars="{$lib.core.dir}/jars.xml"
        	corejardir="{$lib.core.dir}"
        	outfile=".classpath"
        	container="org.eclipse.jdt.launching.JRE_CONTAINER"
        	>
        	
          <!-- all external libraries -->
        	<xsl:for-each select="block:libraries/block:lib">
        	  <lib>
        	    <xsl:for-each select="@*">
        	      <xsl:attribute name="{name(.)}"><xsl:value-of select="."/></xsl:attribute>
        	    </xsl:for-each>
        	  </lib>
        	</xsl:for-each>
        	
        	<!-- list all blocks that this block depends on - this creates references to
        	     these blocks public jars -->
        	<xsl:for-each select="block:requirements/block:requires">
        	  <block name="{@name}" jardir="build/public">
        	    <xsl:attribute name="path">${root.block.<xsl:value-of select="@name"/>}</xsl:attribute>
        	    <xsl:attribute name="dynamicEclipseReference">${root.block.<xsl:value-of select="@name"/>.eclipse.dynamic}</xsl:attribute>
        	  </block>
        	</xsl:for-each>
        	
        	<!-- local soure files -->
        	<source dir="{$src.public}" out="{$build.public.classes}"/>
        	
          <!-- name the cocoon JARs -->
        	<cocoon jar="{$root.core}/{$path.to.cocoon.public.jar}"/>
        	
        </eclipse>        
        
        <!-- create .project file -->
        <xslt in="block.xml" out=".project" 
         style="{$blockbuilder.root}/targets/block-descriptor2eclipse-project.xsl"/>
	    </target>
    
      <target name="eclipse-project-all" description="Create Eclipse project files (this block + req. blocks)">
     	  <!-- clean all dependant blocks -->
    		<antcall target="eclipse-project-required-blocks">
    		  <param name="toplevelcall" value="{$cond.toplevelcall}"/>
    		</antcall>
    		<antcall target="eclipse-project"/>        
      </target>

    	<xsl:call-template name="multi-block-operation">
    	  <xsl:with-param name="action">compile</xsl:with-param>
    	</xsl:call-template>

    	<xsl:call-template name="multi-block-operation">
    	  <xsl:with-param name="action">package</xsl:with-param>
    	</xsl:call-template>

    	<xsl:call-template name="multi-block-operation">
    	  <xsl:with-param name="action">clean</xsl:with-param>
    	  <xsl:with-param name="nocore">true</xsl:with-param>    	  
    	</xsl:call-template>
    	
    	<xsl:call-template name="multi-block-operation">
    	  <xsl:with-param name="action">eclipse-project</xsl:with-param>
    	  <xsl:with-param name="nocore">true</xsl:with-param>
    	</xsl:call-template>
    
    </project>
    
  </xsl:template>
  
  <xsl:template name="info">
    <xsl:param name="msg"/>
    <echo>+-----------------------------------------------------------------+</echo>
    <echo>| <xsl:value-of select="$msg"/>                                    </echo>
    <echo>+-----------------------------------------------------------------+</echo>
  </xsl:template>
  
  <!-- create a target for multi-block operations - necessary for recursions -->
  <xsl:template name="multi-block-operation">
    <xsl:param name="action"/>
    <xsl:param name="nocore"/>
    <target name="{$action}-required-blocks" depends="init, {$action}-core">
    	<xsl:for-each select="block:requirements/block:requires">
    	   <xsl:variable name="root.block">${root.block.<xsl:value-of select="@name"/>}</xsl:variable>
    	   <ant antfile="{$root.block}/build.xml" target="{$action}" inheritall="false">
    	     <property name="toplevelcall" value="false"/>
    	   </ant>
    	</xsl:for-each>		    
    </target>  

    <target name="{$action}-core" depends="init" if="cond.toplevelcall">
      <xsl:call-template name="info">
        <xsl:with-param name="msg">Calling Ant target '<xsl:value-of select="$action"/>' on Cocoon core</xsl:with-param>
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="$nocore = 'true'">
          <!-- to nothing -->
          <echo message="core is not called for this task!"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="root.core">${root.core}</xsl:variable>
          <ant antfile="{$root.core}/build.xml" target="{$action}" inheritall="false"/>	          
        </xsl:otherwise>
      </xsl:choose>  
    </target>
  </xsl:template>
  
</xsl:stylesheet>