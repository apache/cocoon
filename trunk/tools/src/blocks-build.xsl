<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

   <xsl:output method="xml" version="1.0" indent="yes" />

   <xsl:template match="/">
      <project default="compile" basedir="." name="blocks">
         <description>Autogenerated Ant build file that builds blocks.</description>

         <path id="classpath">
            <fileset dir="{string('${lib.core}')}">
              <include name="*.jar"/>
            </fileset>
            <fileset dir="{string('${lib.endorsed}')}">
              <include name="*.jar"/>
            </fileset>      
            <fileset dir="{string('${lib.core}/jvm${target.vm}')}">
              <include name="*.jar"/>
            </fileset>
            <fileset dir="{string('${lib.optional}')}">
              <include name="*.jar"/>
            </fileset>
            <fileset dir="{string('${build.blocks}')}">
              <include name="*.jar"/>
            </fileset>
            <path location="{string('${build.mocks}')}"/>
            <path location="{string('${build.dest}')}"/>
            <path location="{string('${build.deprecated.dest}')}"/>
            <path location="{string('${build.scratchpad.dest}')}"/>
         </path>

         <target name="init">
           <xsl:for-each select="module/project[contains(@name,'cocoon-block-')]">
             <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')" />
             <condition property="unless.exclude.block.{$block-name}">
               <istrue value="{string('${exclude.block.')}{$block-name}{string('}')}"/>
             </condition> 
           </xsl:for-each>
         </target>

         <xsl:apply-templates select="module" />
      </project>
   </xsl:template>

   <xsl:template match="module">
      <target name="compile">
        <xsl:attribute name="depends">init<xsl:for-each select="project[contains(@name,'cocoon-block-')]"><xsl:text>,</xsl:text><xsl:value-of select="@name"/>-compile</xsl:for-each></xsl:attribute>
      </target>

      <target name="patch">
        <xsl:attribute name="depends">init<xsl:for-each select="project[contains(@name,'cocoon-block-')]"><xsl:text>,</xsl:text><xsl:value-of select="@name"/>-patch</xsl:for-each></xsl:attribute>
      </target>

      <target name="samples">
        <xsl:attribute name="depends">init<xsl:for-each select="project[contains(@name,'cocoon-block-')]"><xsl:text>,</xsl:text><xsl:value-of select="@name"/>-samples</xsl:for-each></xsl:attribute>
      </target>

      <target name="lib">
        <xsl:attribute name="depends">init<xsl:for-each select="project[contains(@name,'cocoon-block-')]"><xsl:text>,</xsl:text><xsl:value-of select="@name"/>-lib</xsl:for-each></xsl:attribute>
      </target>

      <xsl:apply-templates select="project[contains(@name,'-block')]" />
   </xsl:template>

   <xsl:template match="project">
      <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')" />

      <target name="{@name}" unless="unless.exclude.block.{$block-name}"/>
      
      <target name="{@name}-compile" unless="unless.exclude.block.{$block-name}">
         <xsl:if test="depend">
            <xsl:attribute name="depends"><xsl:value-of select="@name"/><xsl:for-each select="depend[not(@version or contains(@project,'cocoon'))]"><xsl:text>,</xsl:text><xsl:value-of select="@project"/>-compile</xsl:for-each></xsl:attribute>
         </xsl:if>

         <!-- Test if this block has special build -->
         <available property="{$block-name}.has.build" file="{string('${blocks}')}/{$block-name}/build.xml"/>

         <!-- Test if this block has mocks -->
         <available property="{$block-name}.has.mocks" type="dir" file="{string('${blocks}')}/{$block-name}/mocks/"/>

         <xsl:if test="@status='unstable'">
           <echo message="-----------------------------------------------"/>
           <echo message="ATTENTION: {$block-name} is marked unstable."/>
           <echo message="It should be considered alpha quality"/>
           <echo message="which means that its API might change without notice."/>
           <echo message="-----------------------------------------------"/>
         </xsl:if>

         <antcall target="{$block-name}-compile"/>
      </target>

      <target name="{@name}-patch" unless="unless.exclude.block.{$block-name}">
         <xsl:if test="depend">
            <xsl:attribute name="depends"><xsl:value-of select="@name"/><xsl:for-each select="depend[not(@version or contains(@project,'cocoon'))]"><xsl:text>,</xsl:text><xsl:value-of select="@project"/>-patch</xsl:for-each></xsl:attribute>
         </xsl:if>

         <antcall target="{$block-name}-patches"/>
      </target>
      
      <target name="{@name}-samples" unless="unless.exclude.block.{$block-name}">
         <xsl:if test="depend">
            <xsl:attribute name="depends"><xsl:value-of select="@name"/><xsl:for-each select="depend[not(@version or contains(@project,'cocoon'))]"><xsl:text>,</xsl:text><xsl:value-of select="@project"/>-samples</xsl:for-each></xsl:attribute>
         </xsl:if>

         <!-- Test if this block has samples -->
         <available property="{$block-name}.has.samples" file="{string('${blocks}')}/{$block-name}/samples/sitemap.xmap"/>

         <antcall target="{$block-name}-samples"/>
      </target>
      
      <target name="{@name}-lib" unless="unless.exclude.block.{$block-name}">
         <xsl:if test="depend">
            <xsl:attribute name="depends"><xsl:value-of select="@name"/><xsl:for-each select="depend[not(@version or contains(@project,'cocoon'))]"><xsl:text>,</xsl:text><xsl:value-of select="@project"/>-lib</xsl:for-each></xsl:attribute>
         </xsl:if>

         <!-- Test if this block has libraries -->
         <available property="{$block-name}.has.lib" type="dir">
             <xsl:attribute name="file">${blocks}/<xsl:value-of select="$block-name"/>/lib/</xsl:attribute>
         </available>

         <!-- Test if this block has global WEB-INF files -->
         <available property="{$block-name}.has.webinf" type="dir">
             <xsl:attribute name="file">${blocks}/<xsl:value-of select="$block-name"/>/WEB-INF/</xsl:attribute>
         </available>

         <antcall target="{$block-name}-lib"/>
         <antcall target="{$block-name}-webinf"/>
      </target>

      <target name="{$block-name}-prepare">
         <mkdir dir="{string('${build.blocks}')}/{$block-name}/dest"/>

         <copy filtering="on" todir="{string('${build.blocks}')}/{$block-name}/conf">
            <fileset dir="{string('${blocks}')}/{$block-name}/conf">
               <include name="**/*.x*" />
            </fileset>
         </copy>

         <path id="{$block-name}.classpath">
            <path refid="classpath"/>
            <fileset dir="{string('${blocks}')}/{$block-name}/lib">
               <include name="*.jar"/>
            </fileset>
            <pathelement path="{string('${build.blocks}')}/{$block-name}/mocks"/>
         </path>
      </target>

      <target name="{$block-name}-compile" depends="{$block-name}-build,{$block-name}-mocks,{$block-name}-prepare">

         <copy filtering="on" todir="{string('${build.blocks}')}/{$block-name}/dest">
            <fileset dir="{string('${blocks}')}/{$block-name}/java">
               <include name="**/*.xsl"/>
            </fileset>
         </copy>

         <copy filtering="off" todir="{string('${build.blocks}')}/{$block-name}/dest">
            <fileset dir="{string('${blocks}')}/{$block-name}/java">
               <include name="**/Manifest.mf" />
               <include name="META-INF/**" />
            </fileset>
         </copy>

         <xpatch extension="xroles" directory="{string('${blocks}')}/{$block-name}/conf" configuration="{string('${build.dest}/org/apache/cocoon/cocoon.roles')}"/>

         <!-- This is a little bit tricky:
              As the javac task checks, if a src directory is available and
              stops if its not available, we use the following property
              to either point to a jdk dependent directory or - if not
              available - to the usual java source directory.
              If someone knows a better solution...
         -->
	     <condition property="dependend.vm" value="{string('${target.vm}')}">
	       <available file="{string('${blocks}')}/{$block-name}/java{string('${target.vm}')}"/>
	     </condition>
	     <condition property="dependend.vm" value="">
	       <not>
	         <available file="{string('${blocks}')}/{$block-name}/java{string('${target.vm}')}"/>
	       </not>
	     </condition>

         <javac
            destdir="{string('${build.blocks}')}/{$block-name}/dest"
            debug="{string('${compiler.debug}')}"
            optimize="{string('${compiler.optimize}')}"
            deprecation="{string('${compiler.deprecation}')}"
            target="{string('${target.vm}')}"
            nowarn="{string('${compiler.nowarn}')}"
            compiler="{string('${compiler}')}">
              <src path="{string('${blocks}')}/{$block-name}/java"/>
              <src path="{string('${blocks}')}/{$block-name}/java{string('${dependend.vm}')}"/>
              <classpath refid="{$block-name}.classpath" />
         </javac>
         
         <jar jarfile="{string('${build.blocks}')}/{$block-name}-block.jar">
            <fileset dir="{string('${build.blocks}')}/{$block-name}/dest">
               <include name="org/**" />
               <include name="META-INF/**" />
            </fileset>
         </jar>
      </target>

      <target name="{$block-name}-build" if="{$block-name}.has.build">
         <ant inheritAll="true"
              inheritRefs="false"
              target="main"
              antfile="{string('${blocks}')}/{$block-name}/build.xml"
              >
              <property name="block.dir" value="{string('${blocks}')}/{$block-name}"/>
         </ant>
      </target>

      <target name="{$block-name}-mocks" if="{$block-name}.has.mocks">
         <path id="{$block-name}.classpath">
            <path refid="classpath"/>
            <fileset dir="{string('${build}')}">
               <include name="*.jar"/>
            </fileset>
         </path>

         <mkdir dir="{string('${build.blocks}')}/{$block-name}/mocks"/>

         <javac
            srcdir="{string('${blocks}')}/{$block-name}/mocks"
            destdir="{string('${build.blocks}')}/{$block-name}/mocks"
            debug="{string('${compiler.debug}')}"
            optimize="{string('${compiler.optimize}')}"
            deprecation="{string('${compiler.deprecation}')}"
            target="{string('${target.vm}')}"
            nowarn="{string('${compiler.nowarn}')}"
            compiler="{string('${compiler}')}">
              <classpath refid="{$block-name}.classpath" />
         </javac>
      </target>

      <target name="{$block-name}-lib" if="{$block-name}.has.lib">
         <copy filtering="off" todir="{string('${build.webapp.lib}')}">
            <fileset dir="{string('${blocks}')}/{$block-name}/lib">
               <include name="*.jar"/>
            </fileset>
         </copy>
      </target>

      <target name="{$block-name}-webinf" if="{$block-name}.has.webinf">
         <copy filtering="on" todir="{string('${build.webapp.webinf}')}">
            <fileset dir="{string('${blocks}')}/{$block-name}/WEB-INF/">
               <include name="**"/>
            </fileset>
         </copy>
      </target>

      <target name="{$block-name}-patches" depends="{$block-name}-prepare">
         <xpatch directory="{string('${build.blocks}')}/{$block-name}/conf" extension="xmap" configuration="{string('${build.webapp}')}/sitemap.xmap"/>
         <xpatch directory="{string('${build.blocks}')}/{$block-name}/conf" extension="xpipe" configuration="{string('${build.webapp}')}/sitemap.xmap"/>
         <xpatch directory="{string('${build.blocks}')}/{$block-name}/conf" extension="xconf" configuration="{string('${build.webapp}')}/WEB-INF/cocoon.xconf"/>
      </target>
      
      <target name="{$block-name}-samples" if="{$block-name}.has.samples">
         <copy filtering="on" todir="{string('${build.webapp}')}/samples/{$block-name}">
            <fileset dir="{string('${blocks}')}/{$block-name}/samples"/>
         </copy>
         <xpatch directory="{string('${build.blocks}')}/{$block-name}/conf" extension="xsamples" configuration="{string('${build.webapp}')}/samples/block-samples.xml"/>
         <xpatch directory="{string('${build.blocks}')}/{$block-name}/conf" extension="samplesxpipe" configuration="{string('${build.webapp}')}/samples/sitemap.xmap"/>
      </target>
   </xsl:template>
</xsl:stylesheet>
