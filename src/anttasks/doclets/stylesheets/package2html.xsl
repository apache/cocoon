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

<!-- $Id: package2html.xsl,v 1.1 2004/05/25 12:53:43 gcasper Exp $ -->

<!DOCTYPE xsl:stylesheet [
  <!ENTITY nbsp "&#160;">
  <!ENTITY lt   "&#60;">
  <!ENTITY gt   "&#62;">
  ]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:jd="http://apache.org/cocoon/javadoc/1.0">
  
  
  <xsl:output method="html" />
  
  
  <xsl:template match="/jd:package">
    <html>
      <head>
        <title>Package <xsl:value-of select="@packagename"/></title>
      </head>
      <body bcolor="white">
        <xsl:comment> ======== START OF CLASS DATA ======== </xsl:comment>
        <font size="+1" class="FrameTitleFont"><xsl:value-of select="@packagename"/></font>
        <table border="0" width="100%" summary="">
          <tr>
            <td nowrap="nowrap">
              <font class="FrameHeadingFont">Interfaces</font>
              <font class="FrameItemFont">
                <xsl:apply-templates select="jd:interfaces"/>
              </font>
            </td>
          </tr>
        </table>
        <table border="0" width="100%" summary="">
          <tr>
            <td nowrap="nowrap">
              <font class="FrameHeadingFont">Classes</font>
              <font class="FrameItemFont">
                <xsl:apply-templates select="jd:classes"/>
              </font>
            </td>
          </tr>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="jd:interfaces">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="jd:interface">
    <br/>
    <a href="{@name}.html" title="interface in {../../package/@packagename}" target="classFrame"><i><xsl:value-of select="@name"/></i></a>
  </xsl:template>

  <xsl:template match="jd:classes">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="jd:class">
    <br/>
    <a href="{@name}.html" title="class in {../../package/@packagename}" target="classFrame"><xsl:value-of select="@name"/></a>
  </xsl:template>

</xsl:stylesheet>