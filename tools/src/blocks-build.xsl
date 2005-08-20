<?xml version="1.0"?>

<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="/">
    <project default="compile" basedir="." name="blocks">
      <description>Ant build file that builds blocks, autogenerated from blocks-build.xsl.</description>

      <path id="classpath">
        <fileset dir="${{lib.core}}">
          <include name="*.jar"/>
        </fileset>
        <fileset dir="${{lib.endorsed}}">
          <include name="*.jar"/>
        </fileset>
        <!-- Currently, we have no JVM dependent libraries
          <fileset dir="${{lib.core}}/jvm${{target.vm}}">
             <include name="*.jar"/>
          </fileset>
        -->
        <fileset dir="${{build.blocks}}">
          <include name="*.jar"/>
        </fileset>
        <path location="${{build.mocks}}"/>
        <path location="${{build.dest}}"/>
        <pathelement path="${{build.webapp.webinf}}/classes/"/>
      </path>

      <path id="test.classpath">
        <fileset dir="${{tools.lib}}">
          <include name="*.jar"/>
        </fileset>
      </path>

      <path id="htmlunit.classpath">
        <fileset dir="${{htmlunit.home}}/lib">
          <include name="*.jar"/>
        </fileset>
        <fileset dir="${{tools.lib}}">
          <include name="*.jar"/>
        </fileset>
        <fileset dir="${{lib}}">
          <include name="core/avalon-framework-*.jar"/>
        </fileset>
      </path>

      <!-- Files, which should no compiled or otherwise processed -->
      <patternset id="unprocessed.sources">
        <exclude name="**/*.java"/>
        <exclude name="**/*.xconf"/>
        <exclude name="**/*.xroles"/>
        <exclude name="**/*.xmap"/>
        <exclude name="**/*.xpipe"/>
        <exclude name="**/*.xlog"/>
        <exclude name="**/*.xweb"/>
        <exclude name="**/package.html"/>
      </patternset>

      <macrodef name="test-include-block">
        <attribute name="name"/>
        <sequential>
          <condition property="include.block.@{{name}}">
            <not>
              <istrue value="${{exclude.block.@{{name}}}}"/>
            </not>
          </condition>
          <condition property="internal.exclude.block.@{{name}}">
            <and>
              <isfalse value="${{include.all.blocks}}"/>
              <or>
                <istrue value="${{exclude.all.blocks}}"/>
                <isfalse value="${{include.block.@{{name}}}}"/>
              </or>
            </and>
          </condition>
        </sequential>
      </macrodef>

      <macrodef name="print-excluded-block">
        <attribute name="name"/>
        <sequential>
          <if>
            <istrue value="${{internal.exclude.block.@{{name}}}}"/>
            <then>
              <echo message=" Block '@{{name}}' is excluded from the build."/>
            </then>
          </if>
        </sequential>
      </macrodef>

      <macrodef name="block-compile">
        <attribute name="name"/>
        <attribute name="package"/>
        <attribute name="dir"/>
        <sequential>
        <!-- Test if this block has special build -->
          <if>
            <available file="@{{dir}}/build.xml"/>
            <then>
              <ant inheritAll="true"
                   inheritRefs="false"
                   target="main"
                   antfile="@{{dir}}/build.xml">
                <property name="block.dir" value="@{{dir}}"/>
              </ant>
            </then>
          </if>
          <!-- Test if this block has mocks -->
          <if>
            <available type="dir" file="@{{dir}}/mocks/"/>
            <then>
              <mkdir dir="${{build.blocks}}/@{{name}}/mocks"/>
              <javac srcdir="@{{dir}}/mocks"
                     destdir="${{build.blocks}}/@{{name}}/mocks"
                     debug="${{compiler.debug}}"
                     optimize="${{compiler.optimize}}"
                     deprecation="${{compiler.deprecation}}"
                     target="${{target.vm}}"
                     nowarn="${{compiler.nowarn}}"
                     compiler="${{compiler}}">
                <classpath refid="@{{name}}.classpath"/>
              </javac>
            </then>
          </if>
          <!-- This is a little bit tricky:
           As the javac task checks, if a src directory is available and
           stops if its not available, we use the following property
           to either point to a jdk dependent directory or - if not
           available - to the usual java source directory.
           If someone knows a better solution...
      -->
      <!-- Currently, we have no JVM dependent sources
      <condition property="dependend.vm" value="${{target.vm}}">
        <available file="@{{dir}}/java${{target.vm}}"/>
      </condition>
      <condition property="dependend.vm" value="">
        <not>
          <available file="@{{dir}}/java${{target.vm}}"/>
        </not>
      </condition>
      -->
      <javac destdir="${{build.blocks}}/@{{name}}/dest"
             debug="${{compiler.debug}}"
             optimize="${{compiler.optimize}}"
             deprecation="${{compiler.deprecation}}"
             target="${{target.vm}}"
             nowarn="${{compiler.nowarn}}"
             compiler="${{compiler}}">
        <src path="@{{dir}}/java"/>
        <!-- Currently, we have no JVM dependent sources
        <src path="@{{dir}}/java${{dependend.vm}}"/>
        -->
        <classpath refid="@{{name}}.classpath"/>
        <exclude name="**/samples/**/*.java"/>
      </javac>

      <copy filtering="on" todir="${{build.blocks}}/@{{name}}/dest">
        <fileset dir="@{{dir}}/java">
          <patternset refid="unprocessed.sources"/>
        </fileset>
      </copy>

      <copy filtering="off" todir="${{build.blocks}}/@{{name}}/dest">
        <fileset dir="@{{dir}}/java">
          <include name="**/Manifest.mf"/>
          <include name="META-INF/**"/>
        </fileset>
      </copy>
      <jar jarfile="${{build.blocks}}/@{{name}}-block.jar" index="true">
        <fileset dir="${{build.blocks}}/@{{name}}/dest">
          <include name="@{{package}}/**"/>
          <include name="META-INF/**"/>
        </fileset>
      </jar>
      <if>
        <istrue value="${{include.sources-in-jars}}"/>
        <then>
          <jar jarfile="${{build.blocks}}/@{{name}}-block.jar" update="true">
            <fileset dir="@{{dir}}/java">
              <include name="**/*.java"/>
            </fileset>
          </jar>
        </then>
      </if>

      <if>
        <istrue value="${{include.sources-jars}}"/>
        <then>
          <jar jarfile="${{build.blocks}}/@{{name}}-block.src.jar">
            <fileset dir="@{{dir}}/java">
              <include name="**/*.java"/>
            </fileset>
          </jar>
        </then>
      </if>

      <!-- exclude sample classes from the block package -->
      <if>
        <isfalse value="${{internal.exclude.webapp.samples}}"/>
        <then>
          <mkdir dir="${{build.blocks}}/@{{name}}/samples"/>
          <javac destdir="${{build.blocks}}/@{{name}}/samples"
             debug="${{compiler.debug}}"
             optimize="${{compiler.optimize}}"
             deprecation="${{compiler.deprecation}}"
             target="${{target.vm}}"
             nowarn="${{compiler.nowarn}}"
             compiler="${{compiler}}">
            <src path="@{{dir}}/java"/>
            <!-- Currently, we have no JVM dependent sources
            <src path="@{{dir}}/java${{dependend.vm}}"/>
            -->
            <classpath refid="@{{name}}.classpath"/>
            <include name="**/samples/**/*.java"/>
          </javac>
        </then>
      </if>
       </sequential>
      </macrodef>

      <macrodef name="block-patch">
        <attribute name="name"/>
        <attribute name="dir"/>
        <sequential>
          <xpatch file="${{build.webapp}}/sitemap.xmap" srcdir="@{{dir}}">
            <include name="conf/*.xmap"/>
          </xpatch>
          <xpatch file="${{build.webapp}}/WEB-INF/cocoon.xconf" srcdir="@{{dir}}" addcomments="true">
            <include name="conf/*.xconf"/>
          </xpatch>
          <xpatch file="${{build.webapp}}/WEB-INF/logkit.xconf" srcdir="@{{dir}}">
            <include name="conf/*.xlog"/>
          </xpatch>
          <xpatch file="${{build.webapp}}/WEB-INF/web.xml" srcdir="@{{dir}}">
            <include name="conf/*.xweb"/>
          </xpatch>

          <!-- generate sitemap entries
          <sitemap-components sitemap="${{build.webapp}}/sitemap.xmap"
                              source="@{{dir}}/java"
                              block="@{{name}}">
            <xsl:if test="@status='unstable'">
              <xsl:attribute name="stable">false</xsl:attribute>
            </xsl:if>
            <xsl:if test="@status='deprecated'">
              <xsl:attribute name="deprecated">true</xsl:attribute>
            </xsl:if>
          </sitemap-components>
          -->

          <!-- generate sitemap components docs -->
          <!-- TODO - this is the wrong place for documentation, but currently blocks
               don't have own docs!
            <mkdir dir="${{build.context}}/xdocs/userdocs"/>
          <sitemap-components docDir="${{build.context}}/xdocs/userdocs"
                              source="@{{dir}}/java"
                              block="@{{name}}">
            <xsl:if test="@status='unstable'">
              <xsl:attribute name="stable">false</xsl:attribute>
            </xsl:if>
            <xsl:if test="@status='deprecated'">
              <xsl:attribute name="deprecated">true</xsl:attribute>
            </xsl:if>
          </sitemap-components>
          -->
        </sequential>
      </macrodef>

      <macrodef name="block-roles">
        <attribute name="name"/>
        <attribute name="dir"/>
        <sequential>
            <xpatch file="${{build.dest}}/org/apache/cocoon/cocoon.roles" srcdir="@{{dir}}">
                <include name="conf/*.xroles"/>
            </xpatch>
        </sequential>
      </macrodef>

      <macrodef name="block-patch-samples">
        <attribute name="name"/>
        <attribute name="dir"/>
        <sequential>
            <xpatch file="${{build.webapp}}/samples/sitemap.xmap" srcdir="@{{dir}}">
                <include name="conf/*.samplesxpipe"/>
            </xpatch>
            <xpatch file="${{build.webapp}}/WEB-INF/cocoon.xconf" srcdir="@{{dir}}">
                <include name="conf/*.samplesxconf"/>
            </xpatch>
        </sequential>
      </macrodef>

      <macrodef name="block-samples">
        <attribute name="name"/>
        <attribute name="dir"/>
        <sequential>
          <!-- Test if this block has samples -->
          <if>
            <available file="@{{dir}}/samples/sitemap.xmap"/>
            <then>
              <!-- Important to use here encoding="iso-8859-1" to avoid
                   mutilating LATIN-1 characters in the copy operation.
                   If these were read assuming UTF-8 encoding, umlauts
                   are invalid byte sequences and get replaced by '?'.
              
                   On the other hand, reading UTF-8 files using LATIN-1
                   encoding is not a problem in this context since every
                   UTF-8 byte sequence maps to one or more LATIN-1 characters.
                   We only need to assume that the tokens to be replaced
                   by the filtering option are written in ASCII only.
               -->
              <copy filtering="on" todir="${{build.webapp}}/samples/blocks/@{{name}}" encoding="iso-8859-1">
                <fileset dir="@{{dir}}/samples">
                  <exclude name="**/*.gif"/>
                  <exclude name="**/*.jpg"/>
                </fileset>
                <fileset dir="@{{dir}}/conf" includes="*.xsamples"/>
              </copy>
              <copy filtering="off" todir="${{build.webapp}}/samples/blocks/@{{name}}">
                <fileset dir="@{{dir}}/samples">
                  <include name="**/*.gif"/>
                  <include name="**/*.jpg"/>
                </fileset>
              </copy>
              <!-- copy sample classes -->
              <copy todir="${{build.webapp.classes}}" filtering="off">
                <fileset dir="${{build.blocks}}/@{{name}}/samples"/>
              </copy>
            </then>
          </if>
        </sequential>
      </macrodef>

      <macrodef name="block-lib">
        <attribute name="name"/>
        <attribute name="dir"/>
        <sequential>
          <!-- if this block has a lib directory copy those too (deprecated) -->
          <if>
            <available type="dir" file="@{{dir}}/lib"/>
            <then>
              <echo>
              NOTICE: the preferred method of including library dependencies in your block
              is by putting them in lib/optional and then declaring them in gump.xml.
              </echo>
              <copy filtering="off" todir="${{build.webapp.lib}}">
                <fileset dir="@{{dir}}/lib">
                  <include name="*.jar"/>
                  <exclude name="servlet*.jar"/>
                </fileset>
              </copy>
            </then>
          </if>
          <!-- Test if this block has global WEB-INF files -->
          <if>
            <available type="dir" file="@{{dir}}/WEB-INF/"/>
            <then>
              <copy filtering="on" todir="${{build.webapp.webinf}}">
                <fileset dir="@{{dir}}/WEB-INF/">
                  <include name="**"/>
                </fileset>
              </copy>
            </then>
          </if>
        </sequential>
      </macrodef>

      <macrodef name="block-tests">
        <attribute name="name"/>
        <attribute name="dir"/>
        <sequential>
          <!-- Test if this block has tests -->
          <if>
            <available file="@{{dir}}/test/org/apache"/>
            <then>
              <mkdir dir="${{build.blocks}}/@{{name}}/test"/>

              <copy todir="${{build.blocks}}/@{{name}}/test" filtering="on">
                <fileset dir="@{{dir}}/test" excludes="**/*.java"/>
              </copy>

              <javac destdir="${{build.blocks}}/@{{name}}/test"
                     debug="${{compiler.debug}}"
                     optimize="${{compiler.optimize}}"
                     deprecation="${{compiler.deprecation}}"
                     target="${{target.vm}}"
                     nowarn="${{compiler.nowarn}}"
                     compiler="${{compiler}}">
                <src path="@{{dir}}/test/org/apache"/>
                <classpath>
                  <path refid="@{{name}}.classpath"/>
                  <path refid="test.classpath"/>
                  <pathelement location="${{build.test}}"/>
                </classpath>
              </javac>

              <junit printsummary="yes" fork="yes" failureproperty="junit.test.failed">
                <jvmarg value="-Djava.endorsed.dirs=lib/endorsed"/>
                <jvmarg value="-Djunit.test.loglevel=${{junit.test.loglevel}}"/>
                <classpath>
                  <path refid="@{{name}}.classpath"/>
                  <path refid="test.classpath"/>
                  <pathelement location="${{build.test}}"/>
                  <pathelement location="${{build.blocks}}/@{{name}}/test"/>
                </classpath>
                <formatter type="plain" usefile="no"/>
                <formatter type="xml"/>
                <batchtest todir="${{build.test.output}}">
                  <fileset dir="${{build.blocks}}/@{{name}}/test">
                    <include name="**/*TestCase.class"/>
                    <include name="**/*Test.class"/>
                    <exclude name="**/AllTest.class"/>
                    <exclude name="**/*$$*Test.class"/>
                    <exclude name="**/Abstract*.class"/>
                    <exclude name="htmlunit/**"/>
                  </fileset>
                </batchtest>
              </junit>
            </then>
          </if>
        </sequential>
      </macrodef>

      <macrodef name="block-prepare-htmlunit-tests">
        <attribute name="name"/>
        <attribute name="dir"/>
        <sequential>
          <!-- Test if this block has tests -->
          <if>
            <and>
              <available file="@{{dir}}/test/htmlunit"/>
              <available file="${{htmlunit.home}}"/>
            </and>
            <then>
              <mkdir dir="${{build.blocks}}/@{{name}}/test/htmlunit"/>

              <copy todir="${{build.blocks}}/@{{name}}/test/htmlunit" filtering="on">
                <fileset dir="@{{dir}}/test/htmlunit" excludes="**/*.java"/>
              </copy>

              <javac destdir="${{build.blocks}}/@{{name}}/test/htmlunit"
                     debug="${{compiler.debug}}"
                     optimize="${{compiler.optimize}}"
                     deprecation="${{compiler.deprecation}}"
                     target="${{target.vm}}"
                     nowarn="${{compiler.nowarn}}"
                     compiler="${{compiler}}">
                <src path="@{{dir}}/test/htmlunit"/>
                <classpath>
                  <path refid="htmlunit.classpath"/>
                  <pathelement location="${{build.test.htmlunit}}"/>
                </classpath>
              </javac>

              <junit printsummary="yes" fork="yes" failureproperty="junit.test.failed">
                <jvmarg value="-Djava.endorsed.dirs=lib/endorsed"/>
                <jvmarg value="-Djunit.test.loglevel=${{junit.test.loglevel}}"/>
                <jvmarg value="-Dhtmlunit.test.baseurl=${{htmlunit.test.baseurl}}"/>
                <classpath>
                  <path refid="htmlunit.classpath"/>
                  <pathelement location="${{build.test.htmlunit}}"/>
                  <pathelement location="${{build.blocks}}/@{{name}}/test/htmlunit"/>
                </classpath>
                <formatter type="plain" usefile="no"/>
                <formatter type="xml"/>
                <batchtest todir="${{build.test.htmlunit.output}}">
                  <fileset dir="${{build.blocks}}/@{{name}}/test/htmlunit">
                    <include name="${htmlunit.test.include}"/>
                    <!-- BD: there was also <include name="**/*Test.class"/> but let's use the
                    same configurable include clause as in test-build.xml -->
                    <exclude name="**/AllTest.class"/>
                    <exclude name="**/*$$*Test.class"/>
                    <exclude name="**/Abstract*.class"/>
                  </fileset>
                </batchtest>
              </junit>
            </then>
          </if>
        </sequential>
      </macrodef>

      <macrodef name="block-prepare-anteater-tests">
        <attribute name="name"/>
        <attribute name="dir"/>
        <sequential>
          <!-- Test if this block has Anteater tests -->
          <if>
            <available file="@{{dir}}/test/anteater"/>
            <then>
              <copy todir="${{build.test}}/anteater">
                <fileset dir="@{{dir}}/test/anteater"/>
                <mapper type="glob" from="*.xml" to="@{{name}}-*.xml"/>
              </copy>
            </then>
          </if>
        </sequential>
      </macrodef>
      <xsl:apply-templates select="module"/>
    </project>
  </xsl:template>

  <xsl:template match="module">
    <xsl:variable name="cocoon-blocks" select="project[starts-with(@name, 'cocoon-block-')]"/>

    <target name="init">
      <xsl:for-each select="$cocoon-blocks">
        <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')"/>
        <test-include-block name="{$block-name}"/>
      </xsl:for-each>
    </target>

    <target name="unstable" depends="init">
      <condition property="unstable.blocks.present">
        <or>
          <xsl:for-each select="$cocoon-blocks[@status='unstable']">
            <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')"/>
            <isfalse value="${{internal.exclude.block.{$block-name}}}"/>
          </xsl:for-each>
        </or>
      </condition>
      <if>
        <istrue value="${{unstable.blocks.present}}"/>
        <then>
          <echo message="==================== WARNING ======================="/>
          <xsl:for-each select="$cocoon-blocks[@status='unstable']">
            <xsl:sort select="@name"/>
            <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')"/>
            <echo message=" Block '{$block-name}' should be considered unstable."/>
          </xsl:for-each>
          <echo message="----------------------------------------------------"/>
          <echo message="         This means that its API, schemas "/>
          <echo message="  and other contracts might change without notice."/>
          <echo message="===================================================="/>
        </then>
      </if>
    </target>

    <target name="excluded" depends="init">
      <condition property="excluded.blocks.present">
        <or>
          <xsl:for-each select="$cocoon-blocks">
            <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')"/>
              <istrue value="${{internal.exclude.block.{$block-name}}}"/>
          </xsl:for-each>
        </or>
      </condition>
      <if>
        <istrue value="${{excluded.blocks.present}}"/>
        <then>
          <echo message="==================== NOTICE ========================"/>
          <xsl:for-each select="$cocoon-blocks">
            <xsl:sort select="@name"/>
            <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')"/>
                <print-excluded-block name="{$block-name}"/>
          </xsl:for-each>
          <echo message="===================================================="/>
        </then>
      </if>
    </target>

    <target name="compile">
      <xsl:attribute name="depends">
        <xsl:text>unstable,excluded</xsl:text>
        <xsl:for-each select="$cocoon-blocks">
          <xsl:text>,</xsl:text>
          <xsl:value-of select="concat(@name, '-compile')"/>
        </xsl:for-each>
      </xsl:attribute>
    </target>

    <target name="patch">
      <xsl:attribute name="depends">
        <xsl:text>init</xsl:text>
        <xsl:for-each select="$cocoon-blocks">
          <xsl:text>,</xsl:text>
          <xsl:value-of select="concat(@name, '-patch')"/>
        </xsl:for-each>
      </xsl:attribute>
    </target>

    <target name="roles">
      <xsl:attribute name="depends">
        <xsl:text>init</xsl:text>
        <xsl:for-each select="$cocoon-blocks">
          <xsl:text>,</xsl:text>
          <xsl:value-of select="concat(@name, '-roles')"/>
        </xsl:for-each>
      </xsl:attribute>
    </target>

    <target name="patch-samples">
      <xsl:attribute name="depends">
        <xsl:text>init</xsl:text>
        <xsl:for-each select="$cocoon-blocks">
          <xsl:text>,</xsl:text>
          <xsl:value-of select="concat(@name, '-patch-samples')"/>
        </xsl:for-each>
      </xsl:attribute>
    </target>

    <target name="samples">
      <xsl:attribute name="depends">
        <xsl:text>init,patch-samples</xsl:text>
        <xsl:for-each select="$cocoon-blocks">
          <xsl:text>,</xsl:text>
          <xsl:value-of select="concat(@name, '-samples')"/>
        </xsl:for-each>
      </xsl:attribute>
    </target>

    <target name="lib">
      <xsl:attribute name="depends">
        <xsl:text>init</xsl:text>
        <xsl:for-each select="$cocoon-blocks">
          <xsl:text>,</xsl:text>
          <xsl:value-of select="concat(@name, '-lib')"/>
        </xsl:for-each>
      </xsl:attribute>
    </target>

    <target name="tests">
      <xsl:attribute name="depends">
        <xsl:text>init</xsl:text>
        <xsl:for-each select="$cocoon-blocks">
          <xsl:text>,</xsl:text>
          <xsl:value-of select="concat(@name, '-tests')"/>
        </xsl:for-each>
      </xsl:attribute>
    </target>

    <target name="prepare-htmlunit-tests">
      <xsl:attribute name="depends">
        <xsl:text>init</xsl:text>
        <xsl:for-each select="$cocoon-blocks">
          <xsl:text>,</xsl:text>
          <xsl:value-of select="concat(@name, '-prepare-htmlunit-tests')"/>
        </xsl:for-each>
      </xsl:attribute>
    </target>

    <target name="prepare-anteater-tests">
      <xsl:attribute name="depends">
        <xsl:text>init</xsl:text>
        <xsl:for-each select="$cocoon-blocks">
          <xsl:text>,</xsl:text>
          <xsl:value-of select="concat(@name, '-prepare-anteater-tests')"/>
        </xsl:for-each>
      </xsl:attribute>
    </target>

    <!-- Check if javadocs have to be generated -->
    <target name="javadocs-check">
      <mkdir dir="${{build.javadocs}}"/>
      <condition property="javadocs.notrequired" value="true">
        <or>
          <uptodate targetfile="${{build.javadocs}}/packages.html">
            <srcfiles dir="${{java}}" includes="**/*.java,**/package.html"/>
            <srcfiles dir="${{deprecated.src}}" includes="**/*.java,**/package.html"/>
            <xsl:for-each select="$cocoon-blocks">
              <srcfiles dir="{@dir}/java" includes="**/*.java,**/package.html"/>
            </xsl:for-each>
          </uptodate>
          <istrue value="${{internal.exclude.javadocs}}"/>
        </or>
      </condition>
    </target>

    <!-- Creates Javadocs -->
    <target name="javadocs" unless="javadocs.notrequired">
      <xsl:attribute name="depends">
        <xsl:text>init, javadocs-check</xsl:text>
        <xsl:for-each select="$cocoon-blocks">
          <xsl:text>,</xsl:text>
          <xsl:value-of select="concat(@name, '-prepare')"/>
        </xsl:for-each>
      </xsl:attribute>

      <condition property="javadoc.additionalparam" value="-breakiterator -tag todo:all:Todo:">
        <equals arg1="1.4" arg2="${{ant.java.version}}"/>
      </condition>
      <condition property="javadoc.additionalparam" value="">
        <not>
          <equals arg1="1.4" arg2="${{ant.java.version}}"/>
        </not>
      </condition>

      <javadoc destdir="${{build.javadocs}}"
               author="true"
               version="true"
               use="true"
               noindex="false"
               splitindex="true"
               windowtitle="${{Name}} API ${{version}} [${{TODAY}}]"
               doctitle="${{Name}} API ${{version}}"
               bottom="Copyright &#169; ${{year}} The Apache Software Foundation. All Rights Reserved."
               stylesheetfile="${{resources.javadoc}}/javadoc.css"
               useexternalfile="yes"
               additionalparam="${{javadoc.additionalparam}}"
               maxmemory="192m">

        <link packagelistloc="${{resources.javadoc}}/avalon-excalibur"
              offline="true" href="http://excalibur.apache.org/apidocs"/>
        <link packagelistloc="${{resources.javadoc}}/avalon-framework"
              offline="true" href="http://excalibur.apache.org/apidocs"/>
        <link packagelistloc="${{resources.javadoc}}/j2ee"
              offline="true" href="http://java.sun.com/j2ee/sdk_1.3/techdocs/api"/>
        <link packagelistloc="${{resources.javadoc}}/j2se"
              offline="true" href="http://java.sun.com/j2se/1.4.2/docs/api"/>
        <link packagelistloc="${{resources.javadoc}}/jstl"
              offline="true" href="http://java.sun.com/products/jsp/jstl/1.1/docs/api"/>
        <link packagelistloc="${{resources.javadoc}}/xalan"
              offline="true" href="http://xml.apache.org/xalan-j/apidocs"/>
        <link packagelistloc="${{resources.javadoc}}/xerces"
              offline="true" href="http://xml.apache.org/xerces2-j/javadocs/api"/>
        <link packagelistloc="${{resources.javadoc}}/log4j"
              offline="true" href="http://logging.apache.org/log4j/docs/api"/>
        <link packagelistloc="${{resources.javadoc}}/logkit"
              offline="true" href="http://avalon.apache.org/avalon/runtime/3.3.0/impl"/>

        <tag name="avalon.component"   scope="types"   description="Avalon component" />
        <tag name="avalon.service"     scope="types"   description="Implements service:" />
        <!-- FIXME: javadoc or ant seems to not understand these
        <tag name="x-avalon.info"      scope="types"   description="Shorthand:" />
        <tag name="x-avalon.lifestyle" scope="types"   description="Lifestyle:" />
        -->
        <tag name="avalon.context"     scope="methods" description="Requires entry:" />
        <tag name="avalon.dependency"  scope="methods" description="Requires component:" />
        <tag name="cocoon.sitemap.component.configuration" enabled="false"/>
        <tag name="cocoon.sitemap.component.documentation" enabled="false"/>
        <tag name="cocoon.sitemap.component.documentation.caching" enabled="false"/>
        <tag name="cocoon.sitemap.component.label" enabled="false"/>
        <tag name="cocoon.sitemap.component.logger" enabled="false"/>
        <tag name="cocoon.sitemap.component.mimetype" enabled="false"/>
        <tag name="cocoon.sitemap.component.name" enabled="false"/>
        <tag name="cocoon.sitemap.component.parameter" enabled="false"/>
        <tag name="cocoon.sitemap.component.pooling.grow" enabled="false"/>
        <tag name="cocoon.sitemap.component.pooling.max" enabled="false"/>
        <tag name="cocoon.sitemap.component.pooling.min" enabled="false"/>

        <packageset dir="${{java}}">
          <include name="**"/>
        </packageset>
        <packageset dir="${{deprecated.src}}">
          <include name="**"/>
        </packageset>
        <xsl:for-each select="$cocoon-blocks">
          <packageset dir="{@dir}/java">
            <include name="**"/>
          </packageset>
        </xsl:for-each>
        <classpath>
          <fileset dir="${{tools.lib}}">
            <include name="*.jar"/>
          </fileset>
          <path refid="classpath" />
          <xsl:for-each select="$cocoon-blocks">
            <path refid="{substring-after(@name,'cocoon-block-')}.classpath"/>
          </xsl:for-each>
        </classpath>
      </javadoc>
    </target>

    <xsl:apply-templates select="$cocoon-blocks"/>
  </xsl:template>


  <xsl:template match="project">
    <xsl:variable name="block-name" select="substring-after(@name,'cocoon-block-')"/>
    <xsl:variable name="cocoon-block-dependencies" select="depend[starts-with(@project,'cocoon-block-')]"/>

    <target name="{@name}" unless="internal.exclude.block.{$block-name}"/>

    <target name="{@name}-compile" unless="internal.exclude.block.{$block-name}">
      <xsl:attribute name="depends">
        <xsl:if test="depend">
          <xsl:value-of select="concat(@name, '-prepare,')"/>
          <xsl:value-of select="@name"/>
          <xsl:for-each select="$cocoon-block-dependencies">
            <xsl:text>,</xsl:text>
            <xsl:value-of select="concat(@project, '-compile')"/>
          </xsl:for-each>
        </xsl:if>
      </xsl:attribute>
      <block-compile name="{$block-name}" 
                     package="{translate(package/text(), '.', '/')}"
                     dir="{@dir}"/>
    </target>

    <target name="{@name}-patch" unless="internal.exclude.block.{$block-name}">
      <xsl:attribute name="depends">
        <xsl:value-of select="concat(@name, '-prepare')"/>
        <xsl:if test="depend">
          <xsl:text>,</xsl:text>
          <xsl:value-of select="@name"/>
          <xsl:for-each select="depend[contains(@project,'cocoon-block-')]">
            <xsl:text>,</xsl:text>
            <xsl:value-of select="@project"/><xsl:text>-patch</xsl:text>
          </xsl:for-each>
        </xsl:if>
      </xsl:attribute>
      <block-patch name="{$block-name}" dir="{@dir}"/>
    </target>

    <target name="{@name}-roles" unless="internal.exclude.block.{$block-name}">
      <xsl:if test="depend">
        <xsl:attribute name="depends">
          <xsl:value-of select="@name"/>
          <xsl:for-each select="depend[contains(@project,'cocoon-block-')]">
            <xsl:text>,</xsl:text>
            <xsl:value-of select="concat(@project, '-roles')"/>
          </xsl:for-each>
        </xsl:attribute>
      </xsl:if>
      <block-roles name="{$block-name}" dir="{@dir}"/>
    </target>

    <target name="{@name}-patch-samples" unless="internal.exclude.block.{$block-name}">
        <block-patch-samples name="{$block-name}" dir="{@dir}"/>
    </target>

    <target name="{@name}-samples" unless="internal.exclude.block.{$block-name}">
      <xsl:if test="depend">
        <xsl:attribute name="depends">
          <xsl:value-of select="@name"/>
          <xsl:for-each select="$cocoon-block-dependencies">
            <xsl:text>,</xsl:text>
            <xsl:value-of select="concat(@project, '-samples')"/>
          </xsl:for-each>
        </xsl:attribute>
      </xsl:if>
      <block-samples name="{$block-name}" dir="{@dir}"/>
    </target>

    <target name="{@name}-lib" unless="internal.exclude.block.{$block-name}">
      <xsl:if test="depend">
        <xsl:attribute name="depends">
          <xsl:value-of select="@name"/>
          <xsl:for-each select="$cocoon-block-dependencies">
            <xsl:text>,</xsl:text>
            <xsl:value-of select="concat(@project, '-lib')"/>
          </xsl:for-each>
        </xsl:attribute>
      </xsl:if>

      <!-- Copy the library depencies -->
      <xsl:if test="library[not(@bundle='false')]">
        <copy filtering="off" todir="${{build.webapp.lib}}">
          <fileset dir="${{lib.optional}}">
            <xsl:for-each select="library[not(@bundle='false')]">
              <include name="{@name}*.jar"/>
            </xsl:for-each>
          </fileset>
        </copy>
      </xsl:if>
      <block-lib name="{$block-name}" dir="{@dir}"/>
    </target>

    <target name="{@name}-prepare" unless="internal.exclude.block.{$block-name}">
      <xsl:if test="depend">
        <xsl:attribute name="depends">
          <xsl:value-of select="@name"/>
          <xsl:for-each select="$cocoon-block-dependencies">
            <xsl:text>,</xsl:text>
            <xsl:value-of select="concat(@project, '-prepare')"/>
          </xsl:for-each>
        </xsl:attribute>
      </xsl:if>

      <mkdir dir="${{build.blocks}}/{$block-name}/dest"/>

      <path id="{$block-name}.classpath">
        <path refid="classpath"/>
        <xsl:if test="library">
          <fileset dir="${{lib.optional}}">
            <xsl:for-each select="library">
              <include name="{@name}*.jar"/>
            </xsl:for-each>
          </fileset>
        </xsl:if>
        <!-- include the block/lib directory (deprecated) -->
        <fileset dir="{@dir}">
          <include name="lib/*.jar"/>
        </fileset>
        <pathelement location="${{build.blocks}}/{$block-name}/mocks"/>
        <pathelement location="${{build.blocks}}/{$block-name}/dest"/>
        <pathelement location="${{build.blocks}}/{$block-name}/samples"/>
        <xsl:for-each select="$cocoon-block-dependencies">
          <path refid="{substring-after(@project,'cocoon-block-')}.classpath"/>
        </xsl:for-each>
      </path>
    </target>

    <target name="{@name}-tests" unless="internal.exclude.block.{$block-name}">
      <xsl:attribute name="depends">
        <xsl:value-of select="@name"/><xsl:text>-compile</xsl:text>
        <xsl:if test="depend">
          <xsl:text>,</xsl:text>
          <xsl:value-of select="@name"/>
          <xsl:for-each select="$cocoon-block-dependencies">
            <xsl:text>,</xsl:text>
            <xsl:value-of select="concat(@project, '-compile')"/>
          </xsl:for-each>
        </xsl:if>
      </xsl:attribute>
      <block-tests name="{$block-name}" dir="{@dir}"/>
    </target>

    <target name="{@name}-prepare-htmlunit-tests" unless="internal.exclude.block.{$block-name}">
        <block-prepare-htmlunit-tests name="{$block-name}" dir="{@dir}"/>
    </target>

    <target name="{@name}-prepare-anteater-tests" unless="internal.exclude.block.{$block-name}">
        <block-prepare-anteater-tests name="{$block-name}" dir="{@dir}"/>
    </target>
  </xsl:template>
</xsl:stylesheet>
