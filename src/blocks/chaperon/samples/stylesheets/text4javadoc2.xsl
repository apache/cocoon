<?xml version="1.0"?>

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:st="http://chaperon.sourceforge.net/schema/syntaxtree/2.0"
                xmlns:text="http://chaperon.sourceforge.net/schema/text/1.0">

 <xsl:template match="st:JAVADOC">
  <xsl:variable name="text"><xsl:value-of select="."/></xsl:variable>
  <text:text>
   <xsl:if test="//st:output/@source">
    <xsl:attribute name="source"><xsl:value-of select="//st:output/@source"/></xsl:attribute>
   </xsl:if>
   <xsl:if test="@line">
    <xsl:attribute name="line"><xsl:value-of select="@line"/></xsl:attribute>
   </xsl:if>
   <xsl:if test="@column">
    <xsl:attribute name="column"><xsl:value-of select="number(@column) + 2"/></xsl:attribute>
   </xsl:if>
   <xsl:value-of select="substring($text,3,string-length($text)-3)"/>
  </text:text>
 </xsl:template>

 <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
  <xsl:copy>
   <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:copy>
 </xsl:template>

</xsl:stylesheet>
