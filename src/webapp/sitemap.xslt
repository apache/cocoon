<?xml version="1.0" encoding="UTF-8"?>
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

<!-- CVS $Id$ -->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:map="http://apache.org/cocoon/sitemap/1.0">
  
  <xsl:template match="/">
class Sitemap extends Pipeline {
  
    boolean setup(environment) {
        uri = environment.uri
        <xsl:apply-templates/>
    }
}    
  </xsl:template>
  
  <xsl:template match="map:match[position() = 1]">
    if (m = (uri =~ "<xsl:value-of select="@pattern"/>")) {
      <xsl:apply-templates select="map:generate|map:transform|map:serialize|map:read"/>
    }
  </xsl:template>
  
  <xsl:template match="map:match">
    else if (m = (uri =~ "<xsl:value-of select="@pattern"/>")) {
      <xsl:apply-templates select="map:generate|map:transform|map:serialize|map:read"/>
    }
  </xsl:template>

  <xsl:template match="map:generate">
    generate "<xsl:value-of select="@type"/>", <xsl:call-template name="backref">
      <xsl:with-param name="src" select="@src"/></xsl:call-template>,
    [ <xsl:apply-templates select="map:parameter"/> ];
  </xsl:template>

  <xsl:template match="map:transform">
    transform "<xsl:value-of select="@type"/>", <xsl:call-template name="backref">
      <xsl:with-param name="src" select="@src"/></xsl:call-template>,
    [ <xsl:apply-templates select="map:parameter"/> ];
  </xsl:template>

  <xsl:template match="map:serialize">
    serialize "<xsl:value-of select="@type"/>",
    [ <xsl:apply-templates select="map:parameter"/> ];
  </xsl:template>

  <xsl:template match="map:read">
    <xsl:variable name="type">
      <xsl:choose>
        <xsl:when test="@type"><xsl:value-of select="@type"/></xsl:when>
        <xsl:otherwise>resource</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    read "<xsl:value-of select="$type"/>", <xsl:call-template name="backref">
      <xsl:with-param name="src" select="@src"/></xsl:call-template>,
    [ "mime-type": "<xsl:value-of select="@mime-type"/>", <xsl:apply-templates select="map:parameter"/> ];
  </xsl:template>
  
  <xsl:template match="map:parameter">
    "<xsl:value-of select="@name"/>": "<xsl:value-of select="@value"/>",
  </xsl:template>
  
  <xsl:template name="backref">
    <xsl:param name="src"/>
    <xsl:choose>
      <xsl:when test="contains($src, '{')">
        <xsl:variable name="before" select="substring-before($src, '{')"/>
        <xsl:variable name="after1" select="substring-after($src, concat($before, '{'))"/>
        <xsl:variable name="backref" select="substring-before($after1, '}')"/>
        <xsl:variable name="after" select="substring-after($src, concat($backref, '}'))"/>
        "<xsl:value-of select="$before"/>" + m.group(<xsl:value-of select="$backref"/>) + 
        <xsl:call-template name="backref">
          <xsl:with-param name="src" select="$after"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>"<xsl:value-of select="$src"/>"</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
