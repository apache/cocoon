<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">

<html>
<body style="background-color: #FFFFFF; margin: 0px 0px 0px 0px; font-family: Verdana, Helvetica, sans-serif;	font-size : 100%;">
  <xsl:apply-templates/>
</body>
</html>

</xsl:template>

<!-- Copy all and apply templates -->

<xsl:template match="@*|node()">
   <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
   </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
