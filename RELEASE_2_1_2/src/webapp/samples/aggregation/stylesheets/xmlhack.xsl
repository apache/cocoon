<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- Match The Root Node -->
  <xsl:template match="/">
    <html>
      <body>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="CHANNEL">
    <table border="0" width="100%">
      <tr>
        <td colspan="3" style="background-color: #B0E0E6; font: x-small Arial, Helvetica, sans-serif;">
          <center><b>Current News from xmlhack.com</b></center>
        </td>
      </tr>
      <xsl:apply-templates select="ITEM"/>
    </table>
  </xsl:template>

  <xsl:template match="ITEM">
    <tr>
      <xsl:if test="(position() mod 2) = 1">
        <xsl:attribute name="style">background-color: lightgrey; font: x-small Arial, Helvetica, sans-serif;</xsl:attribute>
      </xsl:if>
      <xsl:if test="(position() mod 2) = 0">
        <xsl:attribute name="style">background-color: #C0C0C0; font: x-small Arial, Helvetica, sans-serif;</xsl:attribute>
      </xsl:if>
      <td STYLE="font: bold;" width="75%">
        <a target="_blank" style="text-decoration: none;" href="{@href}">
          <xsl:value-of select="TITLE"/>
        </a>
      </td>
      <td>
        <xsl:value-of select="@LASTMOD"/>
      </td>
    </tr>
    <tr>
      <td colspan="3">
        <xsl:value-of select="ABSTRACT"/>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
