<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- 
     Author: Michael Gerzabek, michael.gerzabek@at.efp.cc, EFP Consulting Österreich
     @version CVS $Id: proper.xsl,v 1.3 2003/07/10 22:12:50 reinhard Exp $
-->

<xsl:stylesheet 
    version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:rfc="http://apache.org/cocoon/Web3-Rfc/1.0">
    
	<xsl:template match="rfc:export">
		<export>
			<xsl:apply-templates/>
		</export>
	</xsl:template>
	<xsl:template match="rfc:tables">
		<tables>
			<xsl:apply-templates/>
		</tables>
	</xsl:template>
	<xsl:template match="rfc:row">
		<row>
			<xsl:attribute name="id"><xsl:number/></xsl:attribute>
			<xsl:apply-templates/>
		</row>
	</xsl:template>
	<xsl:template match="rfc:*">
		<xsl:element name="{@rfc:name}">
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	<xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
