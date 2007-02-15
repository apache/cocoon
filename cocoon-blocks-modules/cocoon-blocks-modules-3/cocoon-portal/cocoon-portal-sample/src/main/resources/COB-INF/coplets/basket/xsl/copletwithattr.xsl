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
<!-- SVN $Id$ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:coplet="http://apache.org/cocoon/portal/coplet/1.0">

  <xsl:param name="value"/>
  <xsl:param name="coplet"/>

  <xsl:template match="value">
    <xsl:choose>
      <xsl:when test="$value = ''">0</xsl:when>
      <xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="buttons">
    <xsl:variable name="definedvalue">
      <xsl:choose>
        <xsl:when test="$value = ''">0</xsl:when>
        <xsl:otherwise><xsl:value-of select="$value"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <table><tr>
    <td>
    <coplet:link method="POST" format="html-form" coplet="{$coplet}" path="attributes/value" value="{$definedvalue + 1}">
      <input type="submit" name="+1" value="+1"/>
    </coplet:link>
    </td>
    <td>
    <coplet:link method="POST" format="html-form" coplet="{$coplet}" path="attributes/value" value="{$definedvalue - 1}">
      <input type="submit" name="-1" value="-1"/>
    </coplet:link>
    </td>
    </tr></table>
  </xsl:template>

  <!-- Add a style attribute to the links -->
  <xsl:template match="a">
    <a style="color:blue;font-size:200%">
      <xsl:apply-templates select="@*|text()"/>
    </a>
  </xsl:template>
  
  <xsl:template match="@*|node()" ><xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy></xsl:template>
</xsl:stylesheet>
