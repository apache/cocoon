<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

<xsl:template match="script">
</xsl:template>

<xsl:template match="@onclick|@oncontextmenu|@style" mode="copy">
</xsl:template>

<xsl:template match="@*" mode="copy">
    <xsl:copy/>
</xsl:template>

<xsl:template match="a[img]">
    <xsl:apply-templates/>
</xsl:template>

<xsl:template match="td[a/img]">
</xsl:template>

<xsl:template match="*|text()">
    <xsl:copy>
        <xsl:apply-templates select="@*" mode="copy"/>
        <xsl:apply-templates select="*|text()"/>
    </xsl:copy>
</xsl:template>


</xsl:stylesheet>
