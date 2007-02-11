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

<!-- Process a Column  -->
<xsl:template match="column-layout">

  <xsl:variable name="border">
    <xsl:choose>
      <xsl:when test="@border">
        <xsl:value-of select="@border" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>0</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <table border="{$border}" cellSpacing="0" cellpadding="0" width="100%">
    <xsl:if test="@bgcolor">
      <xsl:attribute name="bgcolor">
        <xsl:value-of select="@bgcolor" /> 
      </xsl:attribute>
    </xsl:if>
    <tr vAlign="top">
      <xsl:for-each select="item">
        <td>
          <xsl:if test="@bgcolor">
            <xsl:attribute name="bgcolor">
              <xsl:value-of select="@bgcolor" /> 
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@width">
            <xsl:attribute name="width">
              <xsl:value-of select="@width" /> 
            </xsl:attribute>
          </xsl:if>
          <xsl:apply-templates />
        </td>
      </xsl:for-each>
    </tr>
  </table>
</xsl:template>


<!-- Copy all and apply templates -->
<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
