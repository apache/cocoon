<?xml version="1.0" encoding="UTF-8"?>
<!--
  Copyright 2002-2004 The Apache Software Foundation

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
  
  <xsl:import href="news2rss.xslt"/>

  <xsl:param name="home"/>
  
  <xsl:template match="/">
   <rss version="2.0">
    <channel>
     <title>Stefano's Linotype</title>
     <link>http://www.betaversion.org/~stefano/linotype/</link>
     <description>Stefano Mazzocchi's Weblog</description>
     <dc:creator>Stefano Mazzocchi</dc:creator>
     <dc:rights>Copyright 2003 Stefano Mazzocchi. Some rights reserved.</dc:rights>
     <lastBuildDate><xsl:value-of select="(//n:news)[1]/@creation-fulldate"/></lastBuildDate>
     <generator>Linotype 1.2</generator>
     <xsl:apply-templates select="list/element"/>
    </channel>
   </rss>
  </xsl:template>

  <xsl:template match="n:news">
   <xsl:param name="id"/>
   <item>
    <title><xsl:value-of select="n:title"/></title>
    <link><xsl:value-of select="$home"/>/<xsl:value-of select="$id"/>/</link>
    <description>
     <xsl:apply-templates select="h:body/node()" mode="encode">
      <xsl:with-param name="id" select="$id"/>
     </xsl:apply-templates>
    </description>
    <dc:date><xsl:value-of select="@creation-isodate"/></dc:date>
   </item>
  </xsl:template>
         
</xsl:stylesheet>
