<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:text="http://chaperon.sourceforge.net/schema/text/1.0"
                version="1.0">

 <xsl:param name="text">3*a-5*6/7 + ( b-c)*5- b</xsl:param>

 <xsl:template match="input[@name='text']">
  <input name="text" type="text" size="80" maxlength="110" value="{$text}"/>
 </xsl:template>

 <xsl:template match="text:text">
  <text:text><xsl:value-of select="$text"/></text:text>
 </xsl:template>

 <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
  <xsl:copy>
   <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:copy>
 </xsl:template>

</xsl:stylesheet>
