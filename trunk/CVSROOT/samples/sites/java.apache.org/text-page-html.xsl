<?xml version="1.0"?>

<!-- Author: Stefano Mazzocchi "stefano@apache.org" -->
<!-- Version: $Id: text-page-html.xsl,v 1.1 1999-11-30 16:30:01 stefano Exp $ -->

<!-- This stylesheet is for text browsers -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/XSL/Transform/1.0">
 
  <xsl:template match="page">
    <xsl:pi name="cocoon-format">type="text/html"</xsl:pi>
    <html>
    <head>
     <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
     <meta name="Author" content="{author}"/>
     <meta name="Version" content="{version}"/>
     <title><xsl:value-of select="title"/></title>
    </head>
    
    <body bgcolor="#ffffff">
     <xsl:apply-templates select="newscolumn|statuscolumn"/>
    </body>
   </html>
  </xsl:template>
  
  <xsl:template match="newscolumn">
   <h2><xsl:text>News</xsl:text></h2>
   <ul>
    <xsl:apply-templates select="news"/>
   </ul>
  </xsl:template>
  
  <xsl:template match="news">
   <li>
    <a href="{link}"><strong><xsl:value-of select="title"/></strong></a>
    <xsl:text> - </xsl:text>
    <strong><xsl:value-of select="date"/></strong>
    <xsl:text> - </xsl:text>
    <xsl:value-of select="content"/>
   </li>
  </xsl:template>
  
  <xsl:template match="statuscolumn">
   <h2><xsl:text>Status</xsl:text></h2>
   <ul>
    <xsl:apply-templates select="project"/>
   </ul>
  </xsl:template>

  <xsl:template match="project">
   <li>
    <a href="{link}"><strong><xsl:value-of select="title"/></strong></a>
    <ul>
     <xsl:apply-templates select="release"/>
    </ul>
   </li>
  </xsl:template>
  
  <xsl:template match="release">
   <li>
    <strong><xsl:value-of select="version"/></strong>
    <xsl:text> - </xsl:text>
    <strong><xsl:value-of select="status"/></strong>
    <xsl:text> - </xsl:text>
    <xsl:value-of select="comment"/>
   </li>
  </xsl:template>
  
</xsl:stylesheet>