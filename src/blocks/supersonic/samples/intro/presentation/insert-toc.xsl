<?xml version="1.0" encoding="iso-8859-1"?>

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

<!-- process the insert-toc element -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:param name="basePath"/>
    <xsl:param name="currentPath"/>
    <xsl:param name="currentPage"/>
    <xsl:param name="pageExt" select="'.html'"/>

    <!-- by default copy everything -->
    <xsl:template match="*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- insert title and navigation under the page element -->
    <xsl:template match="page">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:call-template name="insert-title"/>
            <xsl:call-template name="insert-nav"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- insert table of contents where desired -->
    <xsl:template match="insert-toc">
        <xsl:apply-templates select ="//toc/toc-section" mode="toc"/>
    </xsl:template>

    <!-- toc section -->
    <xsl:template match="toc-section" mode="toc">
        <h3><xsl:value-of select="@name"/></h3>
        <ul>
            <xsl:apply-templates select ="toc-item" mode="toc"/>
        </ul>
    </xsl:template>

    <!-- toc item in toc mode -->
    <xsl:template match="toc-item" mode="toc">
        <li>
            <a href="{concat($basePath,@href,$pageExt)}">
                <xsl:value-of select="@name"/>
            </a>
        </li>
    </xsl:template>

    <!-- eat toc element -->
    <xsl:template match="toc"/>

    <!-- page title -->
    <xsl:template name="insert-title">
        <title>
            <xsl:value-of select="//toc-item[@href=$currentPage]/@name"/>
        </title>
    </xsl:template>

    <!-- navigation -->
    <xsl:template name="insert-nav">
        <navigation>
            <div id="navigation">
                <table width="100%">
                    <tr>
                        <td class="navigation">
                            <xsl:apply-templates select="//toc" mode="navigation"/>
                        </td>
                        <td>
                            <div class="copyright">
                                <xsl:apply-templates select="//toc/copyright/line" mode="copyright"/>
                            </div>
                        </td>
                    </tr>
                </table>
            </div>
        </navigation>
    </xsl:template>

    <!-- navigation: add link to main table of contents and to sibling pages -->
    <xsl:template match="toc" mode="navigation">
        <div class="currentPageHeader">
            <xsl:apply-templates select="//toc-item[@id='toc']" mode="navigation"/>
            :
            <xsl:value-of select="//toc-section[toc-item[starts-with(@href,$currentPath)]]/@name"/>
        </div>
        <xsl:for-each select="//toc-item[starts-with(@href,$currentPath) and not(@id='toc')]">
            <xsl:apply-templates select="." mode="navigation"/>
            &#160;
        </xsl:for-each>
    </xsl:template>

    <!-- toc item in navigation mode -->
    <xsl:template match="toc-item" mode="navigation">
        <xsl:choose>
            <xsl:when test="@href = $currentPage">
                <span class="currentPage">
                    <xsl:value-of select="@name"/>
                </span>
            </xsl:when>
            <xsl:otherwise>
                <a class="pageLink" href="{concat($basePath,@href,$pageExt)}">
                    <xsl:value-of select="@name"/>
                </a>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="line" mode="copyright">
        <xsl:copy-of select="."/>
        <br/>
    </xsl:template>
</xsl:stylesheet>
