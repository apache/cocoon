<?xml version="1.0"?>

<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:status="http://apache.org/cocoon/status"
>

  <xsl:template match="status:statusinfo">
    <html>
      <head>
        <title>Cocoon Status [<xsl:value-of select="@host"/>]</title>
      </head>
      <body bgcolor="white">
        <table noshade="noshade" border="1" cellspacing="0" bordercolor="black">
          <xsl:apply-templates />
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="status:group">
    <tr>
      <td bgcolor="lightgrey" valign="top" align="right">
        <b><xsl:value-of select="@name"/>:</b>
      </td>
      <td width="100%">
        <table noshade="noshade" border="1" cellspacing="0" bordercolor="black" width="100%">
          <xsl:apply-templates />
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="status:value">
    <tr>
      <td bgcolor="silver" valign="top" align="right">
        <xsl:value-of select="@name"/>:
      </td>
      <td width="100%">
        <xsl:value-of select="." />
      </td>
    </tr>
  </xsl:template>
</xsl:stylesheet>
