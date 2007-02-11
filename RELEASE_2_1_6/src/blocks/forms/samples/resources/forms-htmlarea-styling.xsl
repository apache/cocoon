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
                xmlns:fi="http://apache.org/cocoon/forms/1.0#instance"
                exclude-result-prefixes="fi">
  <!--+
      | This stylesheet is designed to be included by 'forms-advanced-styling.xsl'.
      +-->

  <!-- Location of the resources directory, where JS libs and icons are stored -->
  <xsl:param name="resources-uri">resources</xsl:param>
  <xsl:param name="htmlarea-lang">en</xsl:param>

  <xsl:template match="head" mode="forms-htmlarea">
    <script type="text/javascript">
      _editor_url = "<xsl:value-of select="concat($resources-uri, '/htmlarea/')"/>";
      _editor_lang = "<xsl:value-of select="$htmlarea-lang"/>";
    </script>
    <script type="text/javascript" src="{$resources-uri}/htmlarea/htmlarea.js"></script>
  </xsl:template>

  <xsl:template match="body" mode="forms-htmlarea"/>

  <!--+
      | fi:field with @type 'htmlarea'
      +-->
  <xsl:template match="fi:field[fi:styling[@type='htmlarea']]">
    <textarea id="{@id}" name="{@id}" title="{fi:hint}">
      <xsl:apply-templates select="." mode="styling"/>
      <!-- remove carriage-returns (occurs on certain versions of IE and doubles linebreaks at each submit) -->
      <xsl:apply-templates select="fi:value/node()" mode="htmlarea-copy"/>
    </textarea>
    <xsl:apply-templates select="." mode="common"/>
    <xsl:choose>
      <xsl:when test="fi:styling/initFunction">
        <script language="JavaScript"><xsl:value-of select="fi:styling/initFunction"/>('<xsl:value-of select="@id"/>');</script>
      </xsl:when>
      <xsl:otherwise>
        <script type="text/javascript">HTMLArea.replace('<xsl:value-of select="@id"/>');</script>        
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@*|*" mode="htmlarea-copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="htmlarea-copy"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="text()" mode="htmlarea-copy">
    <xsl:copy-of select="translate(., '&#13;', '')"/>
  </xsl:template>

</xsl:stylesheet>
