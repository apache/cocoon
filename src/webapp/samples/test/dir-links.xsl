<?xml version="1.0" encoding="UTF-8"?>

<!--
    Add links to subdirectories to index.xml
    CVS $Id: dir-links.xsl,v 1.1 2004/02/06 11:42:07 bdelacretaz Exp $
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
