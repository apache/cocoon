<?xml version="1.0"?>
<!-- $Id: authenticate.xsl,v 1.2 2003/12/17 15:03:28 cziegeler Exp $ 

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="password"/>
<xsl:param name="name"/>

<xsl:template match="authentication">
	<authentication>
		<xsl:apply-templates select="users"/>
	</authentication>
</xsl:template>

<xsl:template match="users">
	<xsl:apply-templates select="user"/>
</xsl:template>

<xsl:template match="user">
	<xsl:if test="normalize-space(name) = $name and normalize-space(password) = $password">
		<ID><xsl:value-of select="name"/></ID>
		<role><xsl:value-of select="role"/></role>
		<data>
			<user><xsl:value-of select="name"/></user>
		</data>
	</xsl:if>
</xsl:template>

</xsl:stylesheet>
