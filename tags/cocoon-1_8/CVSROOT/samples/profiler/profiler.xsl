<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="profile">
    <html>
      <head>
        <title>Profiler Table</title>
      </head>
      <body>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="profile-table">
    <table border="1">
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="headings|row">
    <tr>
      <xsl:apply-templates/>
    </tr>
  </xsl:template>

  <xsl:template match="heading">
    <th><xsl:value-of select="."/></th>
  </xsl:template>

  <xsl:template match="*" priority="-1">
    <td><xsl:value-of select="."/></td>
  </xsl:template>
</xsl:stylesheet>