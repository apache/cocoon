<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:status="http://apache.org/cocoon/2.0/status">

  <xsl:template match="status:statusinfo">
    <html>
      <head>
        <title>Cocoon2 status [<xsl:value-of select="@status:host"/>]</title>
      </head>
      <body bgcolor="white">

  <table bgcolor="#000000" cellspacing="0" cellpadding="2" width="100%">
    <tr>
      <td>

  <table bgcolor="#ffffff" noshade="noshade" cellspacing="0" cellpadding="6" width="100%">
    <tr>
      <td bgcolor="#0086b2" valign="top" align="left">
        <img src="images/cocoon.gif" border="0"/>
      </td>
      <td bgcolor="#0086b2" valign="top" align="right">
       <FONT face="arial,helvetica,sanserif" color="#ffffff">
	[<xsl:value-of select="@status:host"/>] <xsl:value-of select="@status:date"/>
       </FONT>
      </td>
    </tr>
        </table>

      </td>
    </tr>
        </table>

  <table bgcolor="#000000" cellspacing="0" cellpadding="2" width="100%">
    <tr>
      <td bgcolor="#ffffff" valign="top" align="left" colspan="2" height="12">
      </td>
    </tr>
  </table>

          <xsl:apply-templates />
      </body>
    </html>
  </xsl:template>

  <xsl:template match="status:group">
   <table bgcolor="#000000" noshade="noshade" border="0" cellspacing="2" cellpadding="6" width="100%">
    <tr>
      <td bgcolor="#0086b2" valign="top" align="left" colspan="2">
       <FONT color="#ffffff" face="arial,helvetica,sanserif" size="+1">
        -<xsl:value-of select="@status:name"/>
       </FONT>
      </td>
    </tr>
    <tr>
      <td bgcolor="ffffff" width="100%" colspan="2">
        <table bgcolor="#000000" noshade="noshade" border="0" cellspacing="2" cellpadding="6" bordercolor="black" width="100%">
          <xsl:apply-templates />
        </table>
      </td>
    </tr>
   </table>

  <table bgcolor="#000000" cellspacing="0" cellpadding="2" width="100%">
    <tr>
      <td bgcolor="#ffffff" valign="top" align="left" colspan="2" height="8">
      </td>
    </tr>
  </table>

  </xsl:template>

  <xsl:template match="status:value">
    <tr>
      <td bgcolor="#0086b2" valign="top" align="left">
       <FONT face="arial,helvetica,sanserif" color="#ffffff">
        <xsl:value-of select="@status:name"/>
       </FONT>
      </td>
      <td bgcolor="ffffff" width="100%">
       <FONT face="arial,helvetica,sanserif">
        <xsl:value-of select="." />
       </FONT>
      </td>
    </tr>

  </xsl:template>
</xsl:stylesheet>
