<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- 
     Author: Michael Gerzabek, michael.gerzabek@at.efp.cc, EFP Consulting Österreich
     @version CVS $Id: pics2view.xsl,v 1.3 2003/05/06 14:13:01 vgritsenko Exp $
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:rfc="http://efp.cc/Web3-Rfc/1.0">
	<xsl:template match="@src">
		<xsl:attribute name="src">../../docs/<xsl:value-of select="."/></xsl:attribute>
	</xsl:template>
	<xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
