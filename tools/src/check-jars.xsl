<?xml version="1.0" encoding="utf-8"?>
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

<!--+
    | CVS $Id: check-jars.xsl,v 1.4 2004/03/08 06:07:15 antonio Exp $
    |
    | Simple stylesheet to verify that files defined in lib/jars.xml
    | actually appear in the lib/ directory, and vice-versa, that files
    | that appear in the lib/ directory have an entry with a
    | description in the lib/jars.xml file.
    |
    | Author: Ovidiu Predescu "ovidiu@cup.hp.com"
    | Date: May 22, 2002
    |
    +-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output indent="yes" method="xml" doctype-public="-//APACHE//DTD Documentation V1.0//EN" doctype-system="../dtd/document-v10.dtd" />

  <xsl:strip-space elements="*" />

  <xsl:param name="stylesheet-path" select="''"/>
  <xsl:param name="current-jars-path" select="''"/>
  <xsl:param name="current-jars-file" select="''"/>

  <xsl:variable name="current-jars">
    <xsl:choose>
      <xsl:when test="starts-with($current-jars-path, '/')">
        <!-- absolute current-jars-path => simply use it -->
        <xsl:value-of select="concat($current-jars-path, '/', $current-jars-file)"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- relative current-jars-path -->
        <xsl:choose>
          <xsl:when test="starts-with($stylesheet-path, '/')">
            <!-- absolute stylesheet path outside of COCOON_HOME, no way until now to find the path back -->
            <xsl:message terminate="yes">
              <xsl:text>Absolute stylesheet path makes it impossible to find the current-jars.xml. </xsl:text>
              <xsl:text>If you need this feature, you have to pass the COCOON_HOME directory as </xsl:text>
              <xsl:text>param to the stylesheet.</xsl:text>
            </xsl:message>
          </xsl:when>
          <xsl:otherwise>
            <!-- relative stylesheet-path => build the path to current-jars-path -->
            <xsl:call-template name="relativize-path">
              <xsl:with-param name="current-directory" select="$stylesheet-path"/>
            </xsl:call-template>
            <xsl:value-of select="concat($current-jars-path, '/', $current-jars-file)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="current-jars.xml" select="document($current-jars)"/>

  <xsl:variable name="jars.xml" select="/"/>

  <xsl:template match="/">
 	<!-- Validate lib/jars.xml prior to checking files -->

 	<!-- Verify that all the entries in lib/jars.xml have appropriate tags -->
    <xsl:apply-templates select="//file" mode="no-tag"/>

    <!-- Verify that all the file entries are unique -->
    <xsl:for-each select="$jars.xml/jars/file/lib">
      <xsl:variable name="this" select="normalize-space(text())"/>
      <xsl:if test="count($jars.xml/jars/file/lib[normalize-space(text()) = $this]) > 1">
        <xsl:message terminate="yes">
  Duplicate entry for file <xsl:value-of select="$this"/>
        </xsl:message>
      </xsl:if>
      <!-- ensure a decent filename -->
      <xsl:if test="not(contains(translate($this, '0123456789', '9999999999'), '9'))">
        <xsl:message terminate="no">
  [WARN] Poor filename for file <xsl:value-of select="$this"/>
  Please add version number or datestamp.
        </xsl:message>
      </xsl:if>
    </xsl:for-each>

    <!-- Verify if files declared in jars.xml appear in the lib/ directory -->
    <xsl:apply-templates select="jars/file/lib" mode="declared-but-doesnt-appear"/>

    <!-- Verify that files that appear in lib/ are declared in jars.xml -->
    <xsl:apply-templates select="$current-jars.xml/jars" mode="appears-but-not-declared"/>
    
	<!-- create the documentation -->
	<document>
	 <header>
	  <title>Cocoon JARs</title>
	  <authors>
	   <person name="Cocoon Developers" email="dev@cocoon.apache.org"/>
	  </authors>
	 </header>
	 <body>
	 <s1 title="What, why and when...">
	  <p>This is a list of the available jars, what they are, where they come from,
	   and what they do.</p>
	  <table>
	   <tr>
	     <th>Title</th>
	     <th>Jar (type/name)</th>
	     <th>Description</th>
	     <th>Used by</th>
	   </tr>
	   <xsl:apply-templates select="jars/file" mode="documentation"/>
	  </table>
     </s1>
    </body>
   </document>
  </xsl:template>

  <!-- Template to verify if files declared in jars.xml appear in the
       lib/ directory
  -->
  <xsl:template match="lib" mode="declared-but-doesnt-appear">
    <xsl:variable name="this" select="normalize-space(text())"/>
    <xsl:if test="count($current-jars.xml/jars/jar[normalize-space(text()) = $this]) = 0">
      <xsl:message terminate="yes">
  File <xsl:value-of select="$this"/> is declared in lib/jars.xml, but doesn't appear in the lib/ directory.

  If this file was removed, please update the lib/jars.xml file to remove this file entry.</xsl:message>
    </xsl:if>
  </xsl:template>

  <!-- Template to verify that files that appear in lib/ are declared
       in jars.xml
  -->
  <xsl:template match="jar" mode="appears-but-not-declared">
    <xsl:variable name="this" select="normalize-space(text())"/>
    <xsl:if test="count($jars.xml/jars/file/lib[normalize-space(text()) = $this]) = 0">
      <xsl:choose>
        <xsl:when test="starts-with($this, 'local/')">
          <xsl:message>
  [WARN] Using local library <xsl:value-of select="$this"/> : ensure it doesn't conflict with any other library.
          </xsl:message>
        </xsl:when>
        <xsl:otherwise>
          <xsl:message terminate="yes">
  File <xsl:value-of select="$this"/> appears in the lib/ directory, but is not declared in lib/jars.xml.

  Please update the lib/jars.xml file to include the <xsl:value-of select="$this"/> file together with a description.</xsl:message>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!-- Verify if a file element has a "title" tag -->
  <xsl:template match="file[not(title)]" mode="no-tag">
    <xsl:message terminate="yes">
  Entry for file <xsl:value-of select="normalize-space(lib/text())"/> in the lib/jars.xml does not have a "title" tag.

  Please add a title tag before continuing.</xsl:message>
  </xsl:template>

  <!-- Verify if a file element has a "description" tag -->
  <xsl:template match="file[not(description)]" mode="no-tag">
    <xsl:message terminate="yes">
  Entry for file <xsl:value-of select="normalize-space(lib/text())"/> in the lib/jars.xml does not have a "description" tag.

  Please add a description tag before continuing.</xsl:message>
  </xsl:template>

  <!-- Verify if a file element has an "used-by" tag -->
  <xsl:template match="file[not(used-by)]" mode="no-tag">
    <xsl:message terminate="yes">
  Entry for file <xsl:value-of select="normalize-space(lib/text())"/> in the lib/jars.xml does not have a "used-by" tag.

  Please add a used-by tag before continuing.</xsl:message>
  </xsl:template>

  <!-- Verify if a file element has an "lib" tag -->
  <xsl:template match="file[not(lib)]" mode="no-tag">
    <xsl:message terminate="yes">
  Entry for file <xsl:value-of select="normalize-space(title/text())"/> in the lib/jars.xml does not have a "lib" tag.

  Please add a lib tag before continuing.</xsl:message>
  </xsl:template>

  <!-- Verify if a file element has an "url" tag -->
  <xsl:template match="file[not(homepage)]" mode="no-tag">
    <xsl:message terminate="yes">
  Entry for file <xsl:value-of select="normalize-space(lib/text())"/> in the lib/jars.xml does not have a "homepage" tag.

  Please add a homepage tag before continuing.</xsl:message>
  </xsl:template>

  <!-- Format for documentation -->
  <xsl:template match="file" mode="documentation">
	<tr>
		<td><link href="{homepage}"><xsl:value-of select="title"/></link></td>
		<td><xsl:value-of select="lib"/></td>
		<td><xsl:value-of select="description"/></td>
		<td><xsl:value-of select="used-by"/></td>
	</tr>
  </xsl:template>

  <xsl:template name="relativize-path">
    <xsl:param name="current-directory" select="''"/>
    <xsl:if test="string($current-directory)">
      <xsl:text>../</xsl:text>
      <xsl:call-template name="relativize-path">
        <xsl:with-param name="current-directory" select="substring-after($current-directory, '/')"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template match="file" mode="no-tag" priority="-1"/>
  <xsl:template match="file" mode="declared-but-doesnt-appear" priority="-1"/>
  <xsl:template match="file" mode="appears-but-not-declared" priority="-1"/>    

</xsl:stylesheet>
