<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xi="http://www.w3.org/2001/XInclude"
                xmlns:text="http://chaperon.sourceforge.net/schema/text/1.0"
                xmlns:st="http://chaperon.sourceforge.net/schema/syntaxtree/2.0">

 <xsl:template match="st:output/st:error">
  <xsl:copy>
   <xsl:copy-of select="@*"/>
   <text:text source="{../@source}" line="1" column="1"><xsl:value-of select="."/></text:text>
  </xsl:copy>
 </xsl:template>

 <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
  <xsl:copy>
   <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:copy>
 </xsl:template>

</xsl:stylesheet>
