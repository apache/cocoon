<?xml version="1.0"?>

<!-- CVS $Id: page2html.xsl,v 1.3 2003/05/07 19:15:51 vgritsenko Exp $ -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="page">
   <html>
    <head>
     <title>
      <xsl:value-of select="title"/>
     </title>
     <link href="/styles/main.css" type="text/css" rel="stylesheet"/>
    </head>
    <body>
     <xsl:apply-templates/>
    </body>
   </html>
  </xsl:template>

  <xsl:template match="title">
   <h1><xsl:apply-templates/></h1>
  </xsl:template>

  <xsl:template match="content">
   <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="para">
   <p>
    <xsl:apply-templates/>
   </p>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-2"><xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy></xsl:template>
  <xsl:template match="text()" priority="-1"><xsl:value-of select="."/></xsl:template>

</xsl:stylesheet>
