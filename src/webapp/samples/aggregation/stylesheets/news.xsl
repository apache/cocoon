<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- Match The Root Node -->
	<xsl:template match="page">
		<html>
            <body>
                <xsl:apply-templates/>
            </body>
		</html>
	</xsl:template>

	<xsl:template match="slashdot | moreover | xmlhack">
	    <xsl:variable name="ABC" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'"/>
	    <xsl:variable name="abc" select="'abcdefghijklmnopqrstuvwxyz'"/>
        <xsl:apply-templates select="*[translate(local-name(), $ABC, $abc) = 'html']/
                                     *[translate(local-name(), $ABC, $abc) = 'body']/*"/>
    </xsl:template>

	<xsl:template match="@*|*|text()" priority="-1">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|text()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
