<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="title">Input Module</xsl:param>
<xsl:param name="description"></xsl:param>

<xsl:template match="/">
<page>
    <title><xsl:value-of select="$title"/></title>
    <table class="content">
        <tr>
            <td>    
                <h3><xsl:value-of select="$title"/></h3>
                <p><xsl:value-of select="$description"/></p>
                <xsl:apply-templates />
            </td>
        </tr>
    </table>
</page>
</xsl:template>

<xsl:template match="properties">
    <table class="table">
        <tr>
            <th>Accessor</th>
            <th>Value</th>            
        </tr>
        <xsl:apply-templates>
            <xsl:sort select="name" />
        </xsl:apply-templates>
    </table>
</xsl:template>

<xsl:template match="property">
    <tr>
        <td><xsl:value-of select="name"/></td>
        <td>
            <xsl:value-of select="value"/>&#160;
        </td>    
    </tr>
</xsl:template>

<xsl:template match="title"></xsl:template>

</xsl:stylesheet>
