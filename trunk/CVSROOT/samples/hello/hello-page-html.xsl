<?xml version="1.0"?>

<!-- Written by Stefano Mazzocchi "stefano@apache.org" -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/XSL/Transform/1.0">

  <xsl:template match="page">
   <xsl:pi name="cocoon-format">type="text/html"</xsl:pi>
   <html>
    <head>
     <title>
      <xsl:value-of select="title"/>
     </title>
    </head>
    <body bgcolor="#ffffff">
     <xsl:apply-templates/>
    </body>
   </html>
  </xsl:template>

  <xsl:template match="title">
   <h1 align="center">
    <xsl:apply-templates/>
   </h1>
  </xsl:template>

  <xsl:template match="paragraph">
   <p align="center">
    <i>
     <xsl:apply-templates/>
    </i>
   </p>
  </xsl:template>

</xsl:stylesheet>