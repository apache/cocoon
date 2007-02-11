<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/links">
	<xsl:for-each select="link">
		<xsl:text> &gt; </xsl:text>
		<a>
			<xsl:attribute name="href">
				<xsl:value-of select="concat('bookmark?history=', number)"/>
			</xsl:attribute>
			<xsl:value-of select="title"/>
		</a>
	</xsl:for-each>
</xsl:template>

</xsl:stylesheet>
