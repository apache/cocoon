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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="transposition" select="0"/>

  <xsl:template match="NOTE_ON">
    <xsl:variable name="offset">
      <xsl:choose>
        <xsl:when test="not(number($transposition) = number($transposition))">
          <xsl:message>The parameter $transposition must be a number!</xsl:message>
          <xsl:text>0</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$transposition"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <NOTE_ON PITCH="{@PITCH + $offset}">
      <xsl:copy-of select="@REGISTER | @VELOCITY"/>
      <xsl:apply-templates/>
    </NOTE_ON>
  </xsl:template>

  <xsl:template match="node()">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>

