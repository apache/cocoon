<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- $Id: sunLet_sundnNews.xsl,v 1.1 2003/03/09 00:05:33 pier Exp $ 

-->

<xsl:template match="*|/"><xsl:apply-templates/></xsl:template>

<xsl:template match="text()|@*"><xsl:value-of select="."/></xsl:template>

<xsl:template match="news"><!-- Stylesheet to be used inside a coplet. Generates HTML from an XML-feed from http://w.moreover.com/ -->

	<table>
		<xsl:apply-templates select ="article">
		</xsl:apply-templates>
	</table>
</xsl:template>

<xsl:template match="article">
			<xsl:if test="position() &lt; 6">
				<tr bgcolor="#ffffff"><td><font face="Arial, Helvetica, sans-serif">
    			<a target="_blank"><xsl:attribute name="href"><xsl:value-of select="link"/></xsl:attribute>
				<font size="-1" color="#333333"><b><xsl:value-of select="title"/></b></font></a><br/>
				<a target="_blank"><xsl:attribute name="href"><xsl:value-of select="authorlink"/></xsl:attribute> 
    			<font size="-2" color="#46627A"><xsl:value-of select = "author"/></font></a>
				<font size="-2" color="#46627A">&#160;&#160;<xsl:value-of select="date"/></font>
    			</font></td></tr>
				<tr bgcolor="#ffffff"><td bgcolor="#ffffff" height="5"></td></tr>
			</xsl:if>
</xsl:template>

</xsl:stylesheet>
