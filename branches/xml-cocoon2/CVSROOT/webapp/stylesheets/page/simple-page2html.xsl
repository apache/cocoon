<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="page">
   <html>
    <head>
     <title>
      <xsl:value-of select="title"/>
     </title>
    </head>
    <body bgcolor="white" alink="red" link="blue" vlink="blue">
     <xsl:apply-templates/>
    </body>
   </html>
  </xsl:template>

  <xsl:template match="title">
   <h2 style="color: navy; text-align: center">
    <xsl:apply-templates/>
   </h2>
  </xsl:template>

  <xsl:template match="para">
   <p align="center">
    <i><xsl:apply-templates/></i>
   </p>
  </xsl:template>

</xsl:stylesheet>