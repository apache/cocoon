<?xml version="1.0"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- $Id$ 

-->
<xsl:param name="size"/>

<xsl:template match="rss">
  <xsl:apply-templates select="channel"/>
</xsl:template>

<xsl:template match="channel">
  <xsl:if test="title">
    <b><a href="{link}"><xsl:value-of select="title"/></a></b>
    <br/>
  </xsl:if>
  <xsl:if test="description">
    <font size="-3">&#160;(<xsl:value-of select="description"/>)</font>
  </xsl:if>
  <table>
    <xsl:apply-templates select="item"/>
  </table>
</xsl:template>

<xsl:template match="item">
  <!-- Display the first 5 entries -->
  <xsl:if test="$size='3' or position() &lt; 6">
    <tr>
      <td>
        <a target="_blank" href="{link}">
          <font size="-1"> 
            <b><xsl:value-of select="title"/></b>
          </font>
        </a>
        <xsl:apply-templates select="description"/>
      </td>
    </tr>
    <tr><td height="5">&#160;</td></tr>
  </xsl:if>
</xsl:template>

<xsl:template match="description">
  <font size="-2">
    <br/>
    &#160;&#160;<xsl:apply-templates/>
  </font>
</xsl:template>

<xsl:template match="node()|@*" priority="-1">
  <xsl:copy>
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
