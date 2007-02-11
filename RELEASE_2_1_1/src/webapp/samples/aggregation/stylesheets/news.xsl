<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ns0="http://foo.bar.com/myspace"
                xmlns:ns1="http://foo.bar.com/slashdot"
                xmlns:ns2="http://foo.bar.com/moreover"
                xmlns:ns3="http://foo.bar.com/xmlhack">

	<!-- Match The Root Node -->
	<xsl:template match="ns0:page">
		<html>
            <body>
                <xsl:apply-templates/>
            </body>
		</html>
	</xsl:template>

	<xsl:template match="ns1:news">
        <xsl:apply-templates select="ns1:HTML/ns1:BODY/*"/>
    </xsl:template>

	<xsl:template match="ns2:news">
        <xsl:apply-templates select="ns2:HTML/ns2:BODY/*"/>
    </xsl:template>

	<xsl:template match="ns3:news">
        <xsl:apply-templates select="ns3:html/ns3:body/*"/>
    </xsl:template>

	<xsl:template match="@*|*|text()" priority="-1">
		<xsl:copy>
			<xsl:apply-templates select="@*|*|text()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
