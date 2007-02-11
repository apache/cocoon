<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:ch="http://cocoonhive.org/portal/schema/2002"
  >
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	
	<xsl:template match="ch:menu">
	  <xsl:copy>
          <ch:item>
            <ch:label>Book List</ch:label>
            <ch:command>page-amazon-listMania</ch:command>
          </ch:item>
	    <xsl:apply-templates select="./*"/>
	  </xsl:copy>
	</xsl:template>
	
	<xsl:template match="@*|node()">
	  <xsl:copy>
	    <xsl:apply-templates select="@*|node()"/>
	  </xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
