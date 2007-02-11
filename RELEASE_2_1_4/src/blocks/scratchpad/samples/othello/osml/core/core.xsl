<?xml version="1.0" encoding="utf-8"?>
<!-- edited with XMLSPY v5 U (http://www.xmlspy.com) by Stavros Kounis (private) -->
<?xml-stylesheet type="text/css" href="main.css"?>
<!-- edited with XML Spy v4.1 U (http://www.xmlspy.com) by . (.) -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:osm="http://osmosis.gr/osml/1.0">
	<xsl:output method="html" version="1.0" encoding="utf-8" indent="yes" omit-xml-declaration="no"/>

	<!-- if you create your custom xml - xsl element add here refernce to your xsl -->

	<xsl:include href="../plugins/list.xsl"/>	
	<xsl:include href="../plugins/custombutton.xsl"/>		
	<xsl:include href="../plugins/custom.xsl"/>	
	
<!--	 i think that we dont need (for the momenf special tag to include xhtml code 
       everything else, that dont belong in any specific name space is xhtml
       -->
       
       
       <xsl:template match="osm:site">
		<xsl:apply-templates/>       
       </xsl:template>
       
	<xsl:template match="osm:xhtml">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="osm:block">
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template match="osm:site">
	<xsl:apply-templates/>
	</xsl:template>
	
	
	<!-- in case a osm:pageTitle element exist don't try to transform it -->
	<xsl:template match="osm:pageTitle">
	
	</xsl:template>
	

	<xsl:template match="node()|@*" priority="-1">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
