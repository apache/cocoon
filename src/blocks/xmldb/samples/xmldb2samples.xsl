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

<!--+
    | Simple XMLDB browser
    | CVS $Id: xmldb2samples.xsl,v 1.2 2004/03/06 02:26:11 antonio Exp $
    +-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:x="http://apache.org/cocoon/xmldb/1.0">

  <xsl:import href="context://stylesheets/system/xml2html.xslt"/>

  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="x:collections">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:when test="x:results">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="resource"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="x:collections">
    <samples name="XMLDB Browser" xmlns:xlink="http://www.w3.org/1999/xlink">
      <group name="Back">
        <sample name="Back" href="..">to parent collection</sample>
      </group>
      <group name="Collection">
        <note>
          You are browsing collection <strong><xsl:value-of select="@base"/></strong>.
          <br/>
          This collection has <strong><xsl:value-of select="@collections"/></strong>
          nested collections and it stores <strong><xsl:value-of select="@resources"/></strong>
          resources.
        </note>
      </group>
      <group name="Collections">
        <xsl:if test="not(x:collection)">
          <note>Collection does not have nested collections</note>
        </xsl:if>
        <xsl:apply-templates select="x:collection"/>
      </group>
      <group name="Resources">
        <xsl:if test="not(x:resource)">
          <note>Collection does not have resources</note>
        </xsl:if>
        <xsl:apply-templates select="x:resource"/>
      </group>
      <group name="Query">
        <form method="get" action="{@name}">
          <input name="xpath"/>
          <input type="submit"/>
        </form>
      </group>
    </samples>
  </xsl:template>

  <xsl:template name="resource">
    <samples name="XMLDB Browser" xmlns:xlink="http://www.w3.org/1999/xlink">
      <group name="Back">
        <sample name="Back" href=".">to parent collection</sample>
      </group>
      <group name="Resource">
        <note>
          You are viewing resource.
        </note>
      </group>
      <group name="Resource Content">
        <xsl:call-template name="head"/>
        <xsl:apply-templates/>
      </group>
      <group name="Query">
        <form method="get" action="?">
          <input name="xpath"/>
          <input type="submit"/>
        </form>
      </group>
    </samples>
  </xsl:template>

  <xsl:template match="x:results">
    <samples name="XMLDB Browser" xmlns:xlink="http://www.w3.org/1999/xlink">
      <group name="Back">
        <sample name="Back" href="?">to collection/resource</sample>
      </group>
      <group name="Query">
        <note>
          You are viewing query results.
          <br/>
          Query was <strong><xsl:value-of select="@query"/></strong>
          and it produced <strong><xsl:value-of select="@resources"/></strong>
          results.
        </note>
      </group>
      <group name="Results">
        <xsl:call-template name="head"/>
        <xsl:if test="@resources = 0">
          <note>Query produced no results</note>
        </xsl:if>
        <xsl:apply-templates select="x:result"/>
      </group>
      <group name="Query">
        <form method="get" action="{@name}">
          <input name="xpath"/>
          <input type="submit"/>
        </form>
      </group>
    </samples>
  </xsl:template>

  <xsl:template match="x:collection">
    <sample name="{@name}" href="{@name}/">Browse Collection</sample>
  </xsl:template>

  <xsl:template match="x:resource">
    <sample name="{@name}" href="{@name}">View Resource</sample>
  </xsl:template>

  <xsl:template match="x:result">
    <p>
      Result <strong><xsl:value-of select="position()"/></strong>, from the document
      <strong><xsl:value-of select="@docid"/></strong>:
    </p>
    <xsl:apply-templates/>
  </xsl:template>
</xsl:stylesheet>
