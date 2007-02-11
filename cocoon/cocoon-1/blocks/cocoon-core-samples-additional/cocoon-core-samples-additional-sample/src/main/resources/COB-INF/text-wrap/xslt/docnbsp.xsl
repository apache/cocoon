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
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:str="http://exslt.org/strings"
  extension-element-prefixes="str">

<!-- doc2nbsp.xsl - text line wrapping
  This stylesheet uses exslt extension for strings to add non-breaking spaces.
-->

  <xsl:template match="document">
    <html>
      <head>
        <title><xsl:value-of select="header/title"/></title>
      </head>
      <xsl:apply-templates/>
    </html>
  </xsl:template>

  <xsl:template match="body">
    <body>
      <h1><xsl:value-of select="header/title"/></h1>
      <p>Comment from stylesheet: sequences of two consecutive spaces are replaced by a non-breaking space and a space.
      This keeps indentation while allowing word wrapping.
      </p>
      <xsl:apply-templates/>
    </body>
  </xsl:template>

  <xsl:template match="section">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="source">
    <div style="padding:4pt; margin-bottom:8pt; border-width:1px; border-style:solid; border-color:#0086b2;">
      <!-- iterate over each line -->
      <xsl:for-each select="str:split(string(.), '&#10;')">
        <code>
          <!-- replace each group of two spaces by a &nbsp; and a space to allow line wrap while still
               keeping indentation -->
          <xsl:for-each select="str:split(string(.), '  ')">
            <xsl:value-of select="."/><xsl:text>&#160; </xsl:text>
          </xsl:for-each>
        </code>
        <br/>
      </xsl:for-each>
    </div>
  </xsl:template>

  <xsl:template match="title">
    <h2><xsl:apply-templates/></h2>
  </xsl:template>

  <xsl:template match="p">
    <p><xsl:apply-templates/></p>
  </xsl:template>

</xsl:stylesheet>
