<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:param name="locale"/>
  <xsl:param name="page"/>
  <xsl:template match="site">
    <html>
      <head>
        <title>
          <xsl:value-of select="page/title"/>
        </title>
        <link rel="stylesheet" href="page.css" type="text/css"/>
      </head>
      <body>
        <table class="path">
          <tr>
            <td>
              <a href="../../">Apache Cocoon Main</a>&#xA0;&gt;&#xA0;
              <a href="../">Samples</a>&#xA0;&gt;&#xA0;
              <span class="current">Internationalization (i18n) and Localization (l10n)</span>
            </td>
          </tr>
        </table>
        <table class="topline">
          <tr>
            <td>&#xA0;</td>
          </tr>
        </table>
        <table cellspacing="0" cellpadding="0" summary="content pane">
          <tr>
            <td width="5" class="navbar">&#xA0;</td>
            <td rowspan="2" valign="top" nowrap="nowrap" width="300">
              <xsl:apply-templates select="table[@class='menu']"/>
            </td>
            <td valign="top" class="navbar" align="left">
              Language: <xsl:value-of select="page/@language"/> (<xsl:value-of select="$locale"/>)
            </td>
            <td width="*" valign="top" class="navbar" align="right">
              Page: <xsl:value-of select="$page"/>
            </td>
          </tr>
          <tr>
            <td>&#xA0;</td>
            <td class="content" valign="top" colspan="2">
              <xsl:apply-templates select="page"/>
            </td>
          </tr>
        </table>
        <table>
          <tr>
            <td class="copyright">
              Copyright (c) 1999-2002 <a href="http://www.apache.org/">Apache Software Foundation</a>. All Rights Reserved.
            </td>
          </tr>
        </table>
      </body>
    </html>
  </xsl:template>
  <xsl:template match="node()|@*" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
