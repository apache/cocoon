<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:dir="http://apache.org/cocoon/directory/2.0">

<xsl:template match="dir:directory[.//dir:file[@name='sitemap.xmap']]">
    <xsl:apply-templates>
            <xsl:sort select="@name"/>
    </xsl:apply-templates>
</xsl:template>

<xsl:template match="dir:directory">
</xsl:template>

<xsl:template match="dir:file[@name='sitemap.xmap']">
    <file>
    <xsl:apply-templates select="." mode="print"/>
    </file>
    <dir>
        <xsl:apply-templates select=".." mode="print"/>
    </dir>
</xsl:template>


<xsl:template match="*" mode="print">
    <xsl:if test="count(ancestor::*)>1">
        <xsl:apply-templates select=".." mode="print"/>
        <xsl:text>/</xsl:text>
    </xsl:if>
    <xsl:value-of select="@name"/>
</xsl:template>

<xsl:template match="/">
    <lines>
        <xsl:apply-templates>
            <xsl:sort select="@name"/>
        </xsl:apply-templates>
    </lines>
</xsl:template>

</xsl:stylesheet>
