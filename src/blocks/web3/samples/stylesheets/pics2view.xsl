<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- 
     Author: Michael Gerzabek, michael.gerzabek@at.efp.cc, EFP Consulting Österreich
     @version CVS $Revision: 1.1 $ $Date: 2003/03/09 00:06:43 $
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:rfc="http://efp.cc/Web3-Rfc/1.0">
	<xsl:template match="@src">
		<xsl:attribute name="src">../../documents/<xsl:value-of select="."/></xsl:attribute>
	</xsl:template>
	<xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
