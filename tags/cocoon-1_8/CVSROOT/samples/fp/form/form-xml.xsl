<?xml version="1.0"?>
<!-- Written by Jeremy Quinn "sharkbait@mac.com" -->

<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:fp="http://apache.org/cocoon/XSP/FP/1.0"
	version="1.0">

	<xsl:template match="/">
		<xsl:processing-instruction name="cocoon-format">type="text/xml"</xsl:processing-instruction>
		
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="*|@*|text()">
		<xsl:copy>
			<xsl:apply-templates select="*|@*|text()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
