<?xml version="1.0" encoding="UTF-8"?>
<!-- edited with XMLSPY v5 U (http://www.xmlspy.com) by Stavros Kounis (private) -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:osm="http://osmosis.gr/osml/1.0" xmlns:fo="http://www.w3.org/1999/XSL/Format">

	<xsl:template match="osm:custombutton">
		<input type="button">
		<xsl:attribute name="value"><xsl:value-of select="@caption"/></xsl:attribute>
		<xsl:attribute name="onclick">alert('<xsl:value-of select="@msg"/>')</xsl:attribute>
		</input>
	</xsl:template>
	
	
	
</xsl:stylesheet>
