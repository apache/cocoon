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

<!-- $Id: allpackages2overviewhtml.xsl,v 1.1 2004/05/25 12:53:43 gcasper Exp $ -->

<!DOCTYPE xsl:stylesheet [
  <!ENTITY nbsp "&#160;">
  <!ENTITY lt   "&#60;">
  <!ENTITY gt   "&#62;">
  ]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:jd="http://apache.org/cocoon/javadoc/1.0">
  
  
  <xsl:output method="html" />
  
  
  <xsl:template match="/jd:packages">
    <html>
      <head>
        <title>Packages</title>
      </head>

      <body bcolor="white">
        <xsl:comment> ======== START OF CLASS DATA ======== </xsl:comment>
        <table border="1" width="100%">
          <tr bgcolor="#CCCCFF" class="TableHeadingColor">
          <td colspan="2"><font size="+2">
          <b>Packages</b></font></td>
          </tr>
          <xsl:apply-templates/>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="jd:package">
    <tr bgcolor="white" class="TableRowColor">
      <td width="80%"><b><a href="{@packagepath}/package-frame.html"><xsl:value-of select="@packagename"/></a></b></td>
      <td>&nbsp;</td>
    </tr>
  </xsl:template>

</xsl:stylesheet>