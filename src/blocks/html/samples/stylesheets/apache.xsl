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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:xhtml="http://www.w3.org/1999/xhtml">

<xsl:template match="/xhtml:html">
  <html>
    <head>
      <title><xsl:value-of select="xhtml:head/xhtml:title"/></title>
    </head>
    <body>
      <h2><xsl:value-of select="xhtml:head/xhtml:title"/></h2>
      <ul>
        <xsl:apply-templates select="//xhtml:div[@class='content']/xhtml:ul"/>
      </ul>
    </body>
  </html>
</xsl:template>

<xsl:template match="xhtml:ul">
    <ul>
        <xsl:apply-templates select="xhtml:li"/>
    </ul>
</xsl:template>

<xsl:template match="xhtml:li">
    <li><xsl:apply-templates/></li>
</xsl:template>

<xsl:template match="xhtml:a">
    <a href="http://cocoon.apache.org/news/{@href}" title="{@title}">
      <xsl:value-of select="text()"/>
    </a>
</xsl:template>

</xsl:stylesheet>