<?xml version="1.0"?>
<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!DOCTYPE html [
<!ENTITY nbsp "&#160;">
<!ENTITY copy "&#0169;">
<!ENTITY laquo "&#0171;">
<!ENTITY raquo "&#0187;">
]>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="page" />

  <xsl:template match="site">
    <html>
      <head>
        <title>
          <xsl:value-of select="page/title" />
        </title>
        <link rel="stylesheet" href="page.css" type="text/css"/>
      </head>
      <body>
        <table class="path">
          <tr>
            <td>
              <a href="../../">Apache Cocoon Main</a>&#160;&gt;&#160;<a href="../">Samples</a>&#160;&gt;&#160;<a href="./">Modules</a>&#160;&gt;&#160;<span class="current"><xsl:value-of select="page/title"/></span>                        
            </td>
          </tr>
        </table>            
        <table class="topline"><tr><td>&#160;</td></tr></table>
        <table cellspacing="0" cellpadding="0" summary="content pane">
          <tr>
            <td width="5" class="navbar">&#160;</td>
            <td rowspan="2" valign="top" nowrap="nowrap" width="200">
              <xsl:apply-templates select="table[@class='menu']"/>
            </td>
            <td valign="top" class="navbar" align="left">
              &#160;
            </td>
            <td width="*" valign="top" class="navbar" align="right">
              Page: <xsl:value-of select="$page" />
            </td>
          </tr>
          <tr>
            <td>&#160;</td>
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
