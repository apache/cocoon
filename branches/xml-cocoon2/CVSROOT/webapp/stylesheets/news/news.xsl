<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- Match The Root Node -->
	<xsl:template match="/">
		<HTML>
            <xsl:apply-templates select="//BODY"/>
		</HTML>
	</xsl:template>

	<xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
