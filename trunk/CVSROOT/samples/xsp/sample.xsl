<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/XSL/Transform/1.0">

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="page">
    <html>
      <head>
        <title><xsl:value-of select="title"/></title>
      </head>
      <body>
        <h3 style="color: navy; text-align: center">
	  <xsl:value-of select="title"/>
	</h3>
	<xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="*|@*|text()">
    <xsl:copy>
      <xsl:apply-templates select="*|@*|text()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
