<?xml version="1.0" encoding="UTF-8"?>
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
xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
exclude-result-prefixes="i18n">

<xsl:output method="xml" indent="yes" />

<xsl:param name="target-locale">en</xsl:param>

<xsl:template match="/">
    <catalogue>
        <xsl:attribute name="xml:lang"><xsl:value-of select="$target-locale" /></xsl:attribute>
        <xsl:apply-templates select="//i18n:text | //@i18n:attr"/>
    </catalogue>
</xsl:template>

<!-- i18n:text element processing -->
<xsl:template match="i18n:text">
    <xsl:call-template name="create-entry">
        <xsl:with-param name="key-value">
            <xsl:choose>
                <xsl:when test="@key"><xsl:value-of select="@key"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="text()" /></xsl:otherwise>
            </xsl:choose>
        </xsl:with-param>        
    </xsl:call-template>
</xsl:template>

<!-- i18n:attr attribute processing -->
<xsl:template match="@i18n:attr">
    <xsl:call-template name="process-attributes">
        <xsl:with-param name="attr-list" select="." />
    </xsl:call-template>
</xsl:template>

<xsl:template name="process-attributes">
    <xsl:param name="attr-list" />
    <xsl:variable name="attr-nlist" select="concat(normalize-space($attr-list), ' ')" />
    <xsl:variable name="first" select="substring-before($attr-nlist, ' ')" />
    <xsl:variable name="rest" select="substring-after($attr-nlist, ' ')" />        
    <xsl:variable name="key-value" select="../@*[name()=$first][1]" />
    <xsl:call-template name="create-entry">
        <xsl:with-param name="key-value" select="$key-value" />
    </xsl:call-template>
    <xsl:if test="$rest">
        <xsl:call-template name="process-attributes">
            <xsl:with-param name="attr-list" select="$rest" />
        </xsl:call-template>
    </xsl:if>
</xsl:template>

<xsl:template name="create-entry">
    <xsl:param name="key-value" />
    <message key="{$key-value}"><xsl:value-of select="$key-value" /></message>
</xsl:template>

</xsl:stylesheet>
