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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="title">Input Module</xsl:param>
<xsl:param name="description"></xsl:param>

<xsl:template match="/">
<page>
    <title><xsl:value-of select="$title"/></title>
    <table class="content">
        <tr>
            <td>    
                <h3><xsl:value-of select="$title"/></h3>
                <p><xsl:value-of select="$description"/></p>
                <xsl:apply-templates />
            </td>
        </tr>
    </table>
</page>
</xsl:template>

<xsl:template match="properties">
    <table class="table">
        <tr>
            <th>Accessor</th>
            <th>Value</th>            
        </tr>
        <xsl:apply-templates>
            <xsl:sort select="name" />
        </xsl:apply-templates>
    </table>
</xsl:template>

<xsl:template match="property">
    <tr>
        <td><xsl:value-of select="name"/></td>
        <td>
            <xsl:value-of select="value"/>&#160;
        </td>    
    </tr>
</xsl:template>

<xsl:template match="title"></xsl:template>

</xsl:stylesheet>
