<?xml version="1.0" encoding="UTF-8"?>
<!--
  Copyright 2005 The Apache Software Foundation or its licensors,
  as applicable.

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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
<xsl:template match="body">
    <body>
        <xsl:apply-templates select="children::node()[1]">
            <xsl:with-param name="level" select="0"/>
        </xsl:apply-templates>
    </body>
</xsl:template>

<xsl:template match="h1|h2|h3">
    <xsl:param name="stop-id"/>
    <xsl:if test="generate-id() != $stop-id">
        <xsl:variable name="name" select="name(.)"/>
        <xsl:variable name="nextHeader" select="following-sibling::*[name() = $name]"/>
        
        <xsl:variable name="next-id" select="generate-id($nextHeader)"/>
        
        <xsl:element name="s{substring(name(.), 2)}">
            <xsl:attribute name="title"><xsl:value-of select="."/></xsl:attribute>
            <xsl:apply-templates select="following-sibling::node()[1]">
                <xsl:with-param name="stop-id" select="$next-id"/>
            </xsl:apply-templates>
        </xsl:element>
        
        <xsl:apply-templates select="$nextHeader"/>
    </xsl:if>
</xsl:template>

<xsl:template match="*">
    <xsl:param name="stop-id"/>
    <xsl:if test="generate-id() != $stop-id">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates>
                <xsl:with-param name="stop-id" select="$stop-id"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:if>
</xsl:template>

<!-- unknown elements: only copy the text -->
<xsl:template match="*">
    <xsl:value-of select="."/>
</xsl:template>


</xsl:stylesheet>
