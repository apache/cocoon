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
                              xmlns:dc="http://purl.org/dc/elements/1.1/">
  
  <xsl:param name="home"/>
  <xsl:param name="count"/>
  
  <xsl:template match="/">
   <rss version="2.0">
    <channel>
     <title>Stefano's Linotype</title>
     <link>http://www.betaversion.org/~stefano/linotype/</link>
     <description>Stefano Mazzocchi's weblog</description>
     <dc:creator>Stefano Mazzocchi</dc:creator>
     <dc:rights>Copyright 2003 Stefano Mazzocchi. Some rights reserved.</dc:rights>
     <lastBuildDate><xsl:value-of select="(//n:news[@online = 'on'])[1]/@creation-fulldate"/></lastBuildDate>
     <generator>Linotype 1.1</generator>
     <xsl:apply-templates select="(//n:news[@online = 'on'])[position() &lt; number($count)]"/>
    </channel>
   </rss>
  </xsl:template>
  
  <xsl:template match="n:news">
   <item>
    <title><xsl:value-of select="n:title"/></title>
    <link><xsl:value-of select="$home"/>/<xsl:value-of select="../@id"/>/</link>
    <description><xsl:apply-templates select="h:body/node()" mode="encode"/></description>
    <xsl:apply-templates select="h:body"/>
    <dc:date><xsl:value-of select="@creation-fulldate"/></dc:date>
   </item>
  </xsl:template>

 <xsl:template match="*" mode="encode">
    <xsl:text>&lt;</xsl:text><xsl:value-of select="name()"/><xsl:apply-templates select="@*" mode="encode"/><xsl:text>&gt;</xsl:text>
    <xsl:apply-templates select="node()" mode="encode"/>
    <xsl:text>&lt;/</xsl:text><xsl:value-of select="name()"/><xsl:text>&gt;</xsl:text>
 </xsl:template>

<xsl:template match="@*" mode="encode"><xsl:text> </xsl:text><xsl:value-of select="name()"/><xsl:text>="</xsl:text><xsl:value-of select="."/><xsl:text>"</xsl:text></xsl:template>

 <xsl:template match="text()" mode="encode"><xsl:value-of select="."/></xsl:template>

  <xsl:template match="node()|@*">
   <xsl:copy>
    <xsl:apply-templates select="node()|@*"/>
   </xsl:copy>
  </xsl:template>
         
</xsl:stylesheet>
