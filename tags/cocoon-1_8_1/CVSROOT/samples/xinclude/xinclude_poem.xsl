<?xml version="1.0"?>

<!--
     This file is the stylesheet for the xinclude_poem.xml example
     so that something browser-renderable comes out.
  -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="poem">
   <html>
    <head>
     <title>
      <xsl:value-of select="@title"/>
     </title>
    </head>
    <body bgcolor="#ffffff">
     <h1><xsl:value-of select="@title"/></h1>
     <xsl:apply-templates select="stanza"/>
    </body>
   </html>
  </xsl:template>

  <xsl:template match="stanza">
    <p>
      <xsl:apply-templates />
    </p>
  </xsl:template>

  <xsl:template match="line">
    <xsl:value-of select="." /><br></br>
  </xsl:template>

</xsl:stylesheet>
