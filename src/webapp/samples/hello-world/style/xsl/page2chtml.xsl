<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="page">
   <html version="C-HTML 1.0">
    <head>
     <meta name="CHTML" content="yes"/>
     <title>
      <xsl:value-of select="title"/>
     </title>
    </head>
    <body>
     <xsl:apply-templates/>
    </body>
   </html>
  </xsl:template>

  <xsl:template match="title">
   <center>
    <font color="#0000ff">
     <xsl:apply-templates/>
    </font>
   </center>
   <hr/>
  </xsl:template>

  <xsl:template match="para">
   <p>
    <xsl:apply-templates/>
   </p>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-2">
   <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
