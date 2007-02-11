<?xml version="1.0"?>
<!-- $Id: basket.admin.xsl,v 1.1 2004/02/23 14:52:49 cziegeler Exp $ 

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="basket-content">
<h1>Basket Content</h1>
<p>There are <xsl:value-of select="item-count"/> items in the basket.</p>
<xsl:apply-templates select="items"/>
</xsl:template>
<xsl:template match="items">
<table>
<xsl:for-each select="item">
<tr>
<td>
<a href="{show-url}"><xsl:value-of select="id"/></a>
</td>
<td>
<xsl:value-of select="size"/>
</td>
<td>
<a href="{remove-url}">Remove Item</a>
</td>
</tr>
</xsl:for-each>
</table>
</xsl:template>

<xsl:template match="basket-admin">
<h1>Basket Administration</h1>
<xsl:apply-templates select="baskets"/>
<p><a href="{refresh-url}">Refresh list</a> - <a href="{clean-url}">Clean all baskets</a></p>
</xsl:template>

<xsl:template match="baskets">
<table>
<xsl:for-each select="basket">
<tr>
<td>
<a href="{show-url}"><xsl:value-of select="id"/></a>
</td>
<td>
<xsl:value-of select="size"/>
</td>
<td>
<a href="{remove-url}">Clean Basket</a>
</td>
</tr>
</xsl:for-each>
</table>
</xsl:template>

</xsl:stylesheet>
