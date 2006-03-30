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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:dir="http://apache.org/cocoon/directory/2.0">

<xsl:template match="dir:directory[.//dir:file]">
    <xsl:apply-templates>
            <xsl:sort select="@name"/>
    </xsl:apply-templates>
</xsl:template>

<xsl:template match="dir:directory">
</xsl:template>

<xsl:template match="dir:file">
    <file>
        <xsl:apply-templates select="." mode="print"/>
    </file>
</xsl:template>


<xsl:template match="*" mode="print">
    <xsl:if test="count(ancestor::*)>1">
        <xsl:apply-templates select=".." mode="print"/>
        <xsl:text>/</xsl:text>
    </xsl:if>
    <xsl:value-of select="@name"/>
</xsl:template>

<xsl:template match="/">
    <files>
        <xsl:apply-templates>
            <xsl:sort select="@name"/>
        </xsl:apply-templates>
    </files>
</xsl:template>

</xsl:stylesheet>
