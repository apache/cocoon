<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
 xmlns:n="http://www.betaversion.org/linotype/news/1.0"
 xmlns:h="http://www.w3.org/1999/xhtml"
>
  
  <xsl:param name="home"/>
  <xsl:param name="count"/>
  
  <xsl:template match="/">
   <rss version="2.0">
	<channel>
     <title>Stefano's Linotype</title>
     <link>http://www.betaversion.org/~stefano/linotype/</link>
     <description>Stefano Mazzocchi's weblog</description>
     <language>en</language>
     <copyright>Copyright 2003 Stefano Mazzocchi. Some rights reserved.</copyright>
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
	<description><xsl:apply-templates select="h:body"/></description>
    <pubDate><xsl:value-of select="@creation-fulldate"/></pubDate>
   </item>
  </xsl:template>

  <xsl:template match="node()|@*">
   <xsl:copy>
    <xsl:apply-templates select="node()|@*"/>
   </xsl:copy>
  </xsl:template>
         
</xsl:stylesheet>
