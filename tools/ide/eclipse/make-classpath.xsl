<?xml version="1.0" encoding="UTF-8"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!--
  Build the Eclipse .classpath file from a list of path items
  (see "eclipse-project" target in build.xml)

  @author Sylvain Wallez
  @version $Id$
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output indent="yes" method="xml"/>
  <xsl:param name="exportlib"/>

  <xsl:strip-space elements="*"/>

  <xsl:template match="/data">
    <classpath>

      <!-- 1. source dirs + mock classes -->
      <xsl:for-each select="src-dirs/item | mock-dirs/item">
        <!-- alphabetical sorting, complete path -->
        <xsl:sort select="."/>
        <classpathentry excluding="**/.svn/**|htmlunit/" kind="src" path="{.}"/>
      </xsl:for-each>

      <!-- 2. libraries -->
      <xsl:for-each select="libs/item">
        <!-- alphabetical sorting, only file name -->
        <!-- heavy calculation, but here's the logic:
             1. returns the string after 4 slashes (4 is the max (blocks)),
                returns empty string if string does not contain 4 slashes
             2. ... 3 slashes ...
             3. ... 2 slashes ... (the minimum) -->
        <xsl:sort select="concat(substring-after(substring-after(substring-after(substring-after(., '/'), '/'), '/'), '/'),
                                                 substring-after(substring-after(substring-after(., '/'), '/'), '/'),
                                                                 substring-after(substring-after(., '/'), '/'))"/>
            <!-- Remove duplicated library entries (diferent blocks can refer the same lib -->
            <xsl:if test="not(node()) or not(preceding-sibling::node()[.=string(current())])">
                <classpathentry exported="{$exportlib}" kind="lib" path="{.}"/>
            </xsl:if>
      </xsl:for-each>

      <!-- 3. JRE runtime -->
      <classpathentry kind="var" path="JRE_LIB" rootpath="JRE_SRCROOT" sourcepath="JRE_SRC"/>

      <!-- 4. output directory
           Build in a separate dir since Eclipse is confused
           by classes compiled externally by Sun's Javac -->
      <classpathentry kind="output" path="{output}"/>

    </classpath>
  </xsl:template>

</xsl:stylesheet>
