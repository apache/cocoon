<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="file"></xsl:param>

<xsl:template match="/page">
  <html>
    <body>
      <p>Title:<br />
        <xsl:value-of select="title"/>
      </p>
      <xsl:apply-templates select="content/para"/>
    </body>
  </html>
</xsl:template>

<xsl:template match="page/content/para">
  <p>para:<br />
    <xsl:value-of select="normalize-space(.)"/>
  </p>
</xsl:template>

</xsl:stylesheet>
