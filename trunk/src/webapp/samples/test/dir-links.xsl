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

<!--
    Add links to subdirectories to index.xml
    CVS $Id: dir-links.xsl,v 1.1 2004/03/10 10:32:56 cziegeler Exp $
 -->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dir="http://apache.org/cocoon/directory/2.0"
    xmlns="http://www.w3.org/1999/xhtml"
>

    <!-- by default copy everything -->
    <xsl:template match="*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="combo">
        <xsl:apply-templates select="page"/>
    </xsl:template>

    <!-- insert links to subdirectories -->
    <xsl:template match="insert-directories">
        <ul>
            <xsl:apply-templates select="//dir:directory/dir:directory" mode="links"/>
        </ul>
    </xsl:template>

    <!-- ignore directory listing -->
    <xsl:template match="dir:directory"/>

    <!-- generate link to subdirectory -->
    <xsl:template match="dir:directory" mode="links">
        <li>
            <a href="{concat(@name,'/')}"><xsl:value-of select="@name"/></a>
        </li>
    </xsl:template>

</xsl:stylesheet>
