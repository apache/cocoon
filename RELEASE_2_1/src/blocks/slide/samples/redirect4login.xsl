<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- <xsl:param name="destination">content/</xsl:param>-->

 <xsl:param name="destination">bla</xsl:param>

 <xsl:template match="input[@resource]">
  <input type="hidden" name="resource" value="{$destination}"/>
 </xsl:template>

 <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
  <xsl:copy>
   <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:copy>
 </xsl:template>

</xsl:stylesheet>
