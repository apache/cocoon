<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:source="http://apache.org/cocoon/source/1.0" 
	xmlns:session="http://apache.org/cocoon/session/1.0"
	>

	<xsl:param name="profiles"/>

  <xsl:template match="source:source">
  	<xsl:copy><xsl:value-of select="$profiles"/><xsl:apply-templates/></xsl:copy>
  </xsl:template>
  
  <xsl:template match="node()|@*" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
