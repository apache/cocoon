<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ns1="http://foo.bar.com/slashdot"
                xmlns:ns2="http://foo.bar.com/moreover"
                xmlns:ns3="http://foo.bar.com/isyndicate"
>

	<!-- Match The Root Node -->
	<xsl:template match="/">
		<HTML>
            <xsl:apply-templates select="//ns1:BODY"/>
            <xsl:apply-templates select="//ns2:BODY"/>
            <xsl:apply-templates select="//ns3:BODY"/>
		</HTML>
	</xsl:template>

	<xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
