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
  <xsl:import href="announcement.xsl"/>
  <xsl:output method="text" indent="no"/>

  <xsl:template match="announcement">
    <xsl:variable name="titlelen" select="string-length(project)+9"/>
    <text>
      <xsl:value-of select="project"/><xsl:text> Released
</xsl:text>
      <xsl:call-template name="line">
        <xsl:with-param name="len" select="$titlelen"/>
      </xsl:call-template>
      <xsl:text>
</xsl:text>
      <xsl:apply-templates select="abstract"/>
      <xsl:apply-templates select="body"/>
      <xsl:text>
For more information about </xsl:text>
      <xsl:value-of select="project"/>
      <xsl:text>, please go to
</xsl:text>
      <xsl:value-of select="@site"/>
      <xsl:text>

Changes with </xsl:text>
      <xsl:value-of select="project"/>
      <xsl:text>

</xsl:text>
      <xsl:apply-templates select="changes"/>
    </text>
  </xsl:template>

  <xsl:template match="project"/>
  <xsl:template match="title"/>

  <xsl:template match="subproject">
    <xsl:variable name="titlelen" select="string-length(title)"/>
    <xsl:text>
</xsl:text>
    <xsl:value-of select="title"/>
    <xsl:text>
</xsl:text>
    <xsl:call-template name="line">
      <xsl:with-param name="len" select="$titlelen"/>
    </xsl:call-template>
    <xsl:text>
</xsl:text>
    <xsl:apply-templates select="abstract"/>
    <xsl:text>
For more information about </xsl:text>
    <xsl:value-of select="title"/>
    <xsl:text>, please go to
</xsl:text>
    <xsl:value-of select="@site"/>
    <xsl:text>

Changes with </xsl:text>
    <xsl:value-of select="title"/>
    <xsl:text>

</xsl:text>
    <xsl:apply-templates select="changes"/>
  </xsl:template>

  <xsl:template match="abstract">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="p">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="link">
    <xsl:value-of select="."/>
    <xsl:text> (</xsl:text>
    <xsl:value-of select="@href"/>
    <xsl:text>)</xsl:text>
  </xsl:template>

  <xsl:template match="release">
    <xsl:for-each select="action">
      <xsl:text>*) </xsl:text>
      <xsl:value-of select="normalize-space(.)"/><xsl:text> </xsl:text>
      <xsl:if test="@dev">
        <xsl:text>[</xsl:text>
	<xsl:value-of select="@dev"/>
	<xsl:text>]</xsl:text>
      </xsl:if>
      <xsl:text>

</xsl:text>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="line">
    <xsl:param name="len"/>
    <xsl:if test="number($len) > 0">
      <xsl:text>-</xsl:text>
      <xsl:call-template name="line">
        <xsl:with-param name="len" select="number($len)-1"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
