<?xml version="1.0"?>

<!-- Written by Vjekoslav Nesek -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

 <xsl:template match="channel">
  <xsl:processing-instruction name="cocoon-format">type="text/html"</xsl:processing-instruction>
  <html>

   <head>
    <title><xsl:value-of select="title"/></title>
   </head>
    
   <body bgcolor="white">
    <h1 align="center"><xsl:value-of select="title"/></h1>
    <h3 align="center"><xsl:value-of select="description"/></h3>
    <dl>
     <xsl:apply-templates select="item"/>
    </dl>
   </body>
  </html>
 </xsl:template>
     
 <xsl:template match="item">
  <dt>
   <a>
    <xsl:attribute name="href">
     <xsl:value-of select="link"/>
    </xsl:attribute>
    <xsl:value-of select="title"/>
   </a>
  </dt>
  <dd><xsl:value-of select="description"/></dd>
 </xsl:template>

</xsl:stylesheet>
