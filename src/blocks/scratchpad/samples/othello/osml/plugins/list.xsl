<?xml version="1.0" encoding="UTF-8"?>
<!-- edited with XMLSPY v5 U (http://www.xmlspy.com) by Stavros Kounis (private) -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:osm="http://osmosis.gr/osml/1.0" xmlns:fo="http://www.w3.org/1999/XSL/Format">

	<xsl:template match="osm:list">
		<ul>
			<xsl:apply-templates/>
		</ul>
	</xsl:template>
	<xsl:template match="osm:listItem">
	
		<xsl:choose>
			<xsl:when test="@href">
		<li>
			<xsl:attribute name="class"><xsl:value-of select="@class"/></xsl:attribute>
			<a>
				<xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute>
			<xsl:value-of select="." disable-output-escaping="yes"/>				
			</a>

		</li>					
			
			</xsl:when>
			<xsl:otherwise>
		<li>
			<xsl:attribute name="class"><xsl:value-of select="@class"/></xsl:attribute>
			<xsl:value-of select="." disable-output-escaping="yes"/>
			<!--
			<xsl:apply-templates/>
			-->
		</li>			
			
			</xsl:otherwise>
		</xsl:choose>

	</xsl:template>
	
	
	
</xsl:stylesheet>
