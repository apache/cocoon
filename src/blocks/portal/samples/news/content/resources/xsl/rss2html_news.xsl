<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- $Id: rss2html_news.xsl,v 1.1 2003/05/07 06:22:28 cziegeler Exp $ 

-->

<!--<xsl:template match="*|/"><xsl:apply-templates/></xsl:template>
<xsl:template match="text()|@*"><xsl:value-of select="."/></xsl:template>
-->

<xsl:template match="channel">
	<table>
		<xsl:apply-templates select ="item">
		</xsl:apply-templates>
	</table>
</xsl:template>

<xsl:template match="item">
			<xsl:if test="position() &lt; 6">
				<tr bgcolor="#ffffff"><td><font face="Arial, Helvetica, sans-serif">
    			<a target="_blank"><xsl:attribute name="href"><xsl:value-of select="link"/></xsl:attribute>
				<font size="-1" color="#333333"><b><xsl:value-of select="title"/></b></font></a><br/>
	<!--			<a target="_blank"><xsl:attribute name="href"><xsl:value-of select="authorlink"/></xsl:attribute> 
    			<font size="-2" color="#46627A"><xsl:value-of select = "author"/></font></a>-->
				<font size="-2" color="#46627A">&#160;&#160;<xsl:value-of select="description"/></font>
    			</font></td></tr>
				<tr bgcolor="#ffffff"><td bgcolor="#ffffff" height="5"></td></tr>
			</xsl:if>
</xsl:template>


</xsl:stylesheet>
