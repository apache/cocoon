<?xml version="1.0"?>

<!DOCTYPE xsl:stylesheet [
 <!ENTITY message SYSTEM "message.txt">
]>

<!-- Written by Stefano Mazzocchi "stefano@apache.org" -->

<xsl:stylesheet xsl:version="1.0"  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="page">
   <xsl:processing-instruction name="cocoon-format">type="text/html"</xsl:processing-instruction>
   <html>
    <head>
     <title>
      <xsl:value-of select="title"/><xsl:text> - &message;</xsl:text>
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