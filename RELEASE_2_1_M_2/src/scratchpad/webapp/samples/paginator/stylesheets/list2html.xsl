<?xml version="1.0"?>

<!-- CVS: $Id: list2html.xsl,v 1.1 2003/03/09 00:10:33 pier Exp $ -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/">
   <html>
    <head>
     <title>Complete List</title>
    </head>
    <body bgcolor="white" alink="red" link="blue" vlink="blue">
     <h3>Complete List</h3>
     <xsl:apply-templates/>
    </body>
   </html>
  </xsl:template>

  <xsl:template match="list">
   <ul>
    <xsl:apply-templates/>
   </ul>
  </xsl:template>

  <xsl:template match="item">
   <li><xsl:apply-templates/></li>
  </xsl:template>

</xsl:stylesheet>
