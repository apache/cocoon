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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                              xmlns:n="http://www.betaversion.org/linotype/news/1.0"
                              xmlns:h="http://www.w3.org/1999/xhtml"
                              xmlns="http://www.w3.org/1999/xhtml">

  <xsl:param name="home"/>
  <xsl:param name="count"/>

  <xsl:template match="/">
   <xsl:for-each select="//n:news[@online = 'on']">
    <xsl:if test="position() &lt;= number($count)">
     <xsl:apply-templates select="."/>
    </xsl:if>
   </xsl:for-each>
  </xsl:template>

  <xsl:template match="n:news">
   <xsl:variable name="id" select="../@id"/>
   <div class="news">
    <h1><img src="images/hand.jpg" alt=""/><xsl:value-of select="n:title"/></h1>
    <h2><xsl:value-of select="@creation-date"/></h2>
    <xsl:apply-templates select="h:body"/>
    <div class="info">Posted at <xsl:value-of select="@creation-time"/> | <a class="permalink" href="{$home}/news/{$id}/">Permalink</a></div>
    <div class="separator"><img align="center" src="images/separator1.jpg"/></div>
   </div>
  </xsl:template>

  <xsl:template name="find-id">
   <xsl:param name="node"/>
   <xsl:choose>
    <xsl:when test="$node/@id">
     <xsl:value-of select="$node/@id"/>
    </xsl:when>
    <xsl:when test="not($node)"/>
    <xsl:otherwise>
     <xsl:call-template name="find-id">
      <xsl:with-param name="node" select="$node/.."/>
     </xsl:call-template>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:template>

  <xsl:template match="h:body">
   <div class="body">
    <xsl:apply-templates/>
   </div>
  </xsl:template>

  <xsl:template match="@src" priority="1">
   <xsl:variable name="id"><xsl:call-template name="find-id"><xsl:with-param name="node" select=".."/></xsl:call-template></xsl:variable>
   <xsl:choose>
    <xsl:when test="starts-with(.,'http://')">
     <xsl:copy>
      <xsl:apply-templates/>
     </xsl:copy>
    </xsl:when>
    <xsl:otherwise>
     <xsl:attribute name="src">news/<xsl:value-of select="$id"/>/<xsl:value-of select="."/></xsl:attribute>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:template>

  <!--xsl:template match="h:p[1]">
   <xsl:copy>
    <span class="firstletter"><xsl:value-of select="substring(text()[1],1,1)"/></span>
    <span class="first"><xsl:value-of select="substring(substring-before(text()[1],' '),2)"/></span>
    <xsl:text> </xsl:text><xsl:value-of select="substring-after(text()[1],' ')"/>
    <xsl:apply-templates select="text()[position() &gt; 1]|@*|*"/>
   </xsl:copy>
  </xsl:template-->

  <xsl:template match="h:p[1]">
   <p class="first">
    <xsl:apply-templates/>
   </p>
  </xsl:template>

  <xsl:template match="hr">
    <div class="separator"><img src="images/separator2.jpg"/></div>
  </xsl:template>

  <xsl:template match="@*|node()">
   <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
