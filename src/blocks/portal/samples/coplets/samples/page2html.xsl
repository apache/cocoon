<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="page">
   <html>
     <head>
       <title><xsl:value-of select="title"/></title>
     </head>
     <body>
       <xsl:apply-templates/>
     </body>
   </html>
  </xsl:template>

  <xsl:template match="title">
   <h2>
     <xsl:apply-templates/>
   </h2>
  </xsl:template>
  
  <xsl:template match="content">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="para">
   <p>
     <xsl:apply-templates/>
   </p>
  </xsl:template>

  <xsl:template match="link">
   <a href="{@href}">
     <xsl:apply-templates/>
   </a>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-2"><xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy></xsl:template>
  <xsl:template match="text()" priority="-1"><xsl:value-of select="."/></xsl:template>
</xsl:stylesheet>
