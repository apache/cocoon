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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- wrap2para.xsl - text line wrapping
  This stylesheet handles just <source> elements that have PCDATA.
  Any other type of source element is the reponsibility of the caller xslt.
-->

<!-- unstructured source is laid out as a sequence of paragraphs -->

<xsl:template name="format-source">
  <xsl:param name="source"/>
  <xsl:if test="normalize-space($source)">
    <xsl:choose>
      <xsl:when test="not(contains($source,'&#10;'))">
        <xsl:call-template name="format-source-line">
          <xsl:with-param name="line" select="$source"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="format-source-line">
          <xsl:with-param name="line" select="substring-before($source,'&#10;')"/>
        </xsl:call-template>
        <xsl:call-template name="format-source">
          <xsl:with-param name="source" select="substring-after($source,'&#10;')"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>
</xsl:template>

<xsl:template name="format-source-line">
  <xsl:param name="line"/>
    <xsl:variable name="text" select="normalize-space($line)"/>
    <xsl:variable name="first-non-white-space-character" select="substring($text,1,1)"/>
    <xsl:variable name="leading-spaces" select="string-length(substring-before($line,$first-non-white-space-character))"/>
    <xsl:variable name="text-with-nbsp">
      <xsl:call-template name="no-break-in-strings">
        <xsl:with-param name="line" select="$text"/>
      </xsl:call-template>
    </xsl:variable>
    <p style="margin-top:2pt;margin-bottom:2pt;padding-left:{8+$leading-spaces*4}pt;text-indent:-8pt;font-size:smaller">
      <xsl:value-of select="$text-with-nbsp"/>
    </p>
</xsl:template>

<xsl:template name="no-break-in-strings">
  <xsl:param name="line"/>
  <xsl:variable name="quote">"</xsl:variable>
  <xsl:choose>
    <xsl:when test="contains($line,$quote)">
      <xsl:value-of select="substring-before($line,$quote)"/>
      <xsl:text>"</xsl:text>
      <xsl:variable name="remainder" select="substring-after($line,$quote)"/>
      <xsl:choose>
        <xsl:when test="contains($remainder,$quote)">
          <xsl:variable name="string" select="substring-before($remainder,$quote)"/>
          <xsl:value-of select="translate($string,' ','&#160;')"/>
	  <xsl:text>"</xsl:text>
          <xsl:call-template name="no-break-in-strings">
            <xsl:with-param name="line" select="substring-after($remainder,$quote)"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>				
          <xsl:value-of select="translate($remainder,' ','&#160;')"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise><xsl:value-of select="$line"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>

