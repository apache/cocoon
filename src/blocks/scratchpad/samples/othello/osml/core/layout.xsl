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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:osm="http://osmosis.gr/osml/1.0">
  <xsl:output method="xml"/>
  <xsl:template match="/site">
    <osm:site>
      <xsl:apply-templates select="layout"/>
    </osm:site>
  </xsl:template>
  <xsl:template match="layout">
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="osm:page">
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="osm:content-copy">
    <xsl:call-template name="getContent">
      <xsl:with-param name="select" select="@select"/>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="osm:block-copy">
    <xsl:call-template name="getBlock">
      <xsl:with-param name="select" select="@select"/>
    </xsl:call-template>
  </xsl:template>
  <xsl:template name="getContent">
    <xsl:param name="select"/>
    <xsl:apply-templates select="//*[@contentID=$select]"/>
  </xsl:template>
  <xsl:template name="getBlock">
    <xsl:param name="select"/>
    <xsl:apply-templates select="//osm:blocks/osm:block[@blockID=$select]"/>
  </xsl:template>
  <!-- just copy all other elements -->
  <xsl:template match="node()|@*" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
