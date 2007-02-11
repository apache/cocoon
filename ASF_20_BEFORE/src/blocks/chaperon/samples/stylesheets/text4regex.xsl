<?xml version="1.0"?>

<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:st="http://chaperon.sourceforge.net/schema/syntaxtree/2.0"
    xmlns:text="http://chaperon.sourceforge.net/schema/text/1.0"
    exclude-result-prefixes="st">

 <xsl:output encoding="ASCII"/>

 <xsl:template match="st:string">
  <text:text><xsl:value-of select="substring(.,2,string-length(.)-2)"/></text:text>
 </xsl:template>

  <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
