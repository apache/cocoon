<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.0'>

<xsl:template match="body">
<body>
  <xsl:apply-templates/>
</body>
</xsl:template>

<xsl:template match="a">
<!-- massage link for 301 redirect, it messes up the url detection later -->
  <xsl:value-of select="substring-before(.,'/')"/>
  <xsl:text> </xsl:text>
  <xsl:value-of select="substring-after(.,':')"/>
</xsl:template>

</xsl:stylesheet>
