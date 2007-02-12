<?xml version="1.0"?>
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

<!--+
    | Simple repository browser
    | CVS $Id$
    +-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:c="http://apache.org/cocoon/collection/1.0">

  <xsl:import href="context://stylesheets/system/xml2html.xslt"/>

  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="c:collection">
        <xsl:apply-templates mode="root"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="resource"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="c:collection" mode="root">
    <samples name="JCR Browser">
      <group name="Back">
        <sample name="Back" href="..">to parent collection</sample>
      </group>
      <group name="Collection">
        <note>
          You are browsing collection <strong><xsl:value-of select="@name"/></strong>.
          <br/>
          This collection has <strong><xsl:value-of select="count(c:collection)"/></strong>
          nested collections and it stores <strong><xsl:value-of select="count(c:resource)"/></strong>
          resources.
        </note>
      </group>
      <group name="Collections">
        <xsl:if test="not(c:collection)">
          <note>Collection does not have nested collections</note>
        </xsl:if>
        <xsl:apply-templates select="c:collection"/>
      </group>
      <group name="Resources">
        <xsl:if test="not(c:resource)">
          <note>Collection does not have resources</note>
        </xsl:if>
        <xsl:apply-templates select="c:resource"/>
      </group>
    </samples>
  </xsl:template>

  <xsl:template name="resource">
    <samples name="JCR Browser">
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
    </samples>
  </xsl:template>

  <xsl:template match="c:collection">
    <sample name="{@name}" href="./{@name}/">Browse Collection</sample>
  </xsl:template>

  <xsl:template match="c:resource">
    <sample name="{@name}" href="./{@name}">View Resource</sample>
  </xsl:template>

  <xsl:template name="head">
    <link href="/styles/prettycontent.css" type="text/css" rel="stylesheet"/>
    <script src="/scripts/prettycontent.js" type="text/javascript"/>
  </xsl:template>
</xsl:stylesheet>
