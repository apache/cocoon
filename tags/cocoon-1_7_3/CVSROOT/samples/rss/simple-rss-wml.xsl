<?xml version="1.0"?>

<!-- Written by Vjekoslav Nesek -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

 <xsl:template match="rss">
  <xsl:apply-templates select="channel"/>
 </xsl:template>

 <xsl:template match="channel">
  <xsl:processing-instruction name="cocoon-format">type="text/wml"</xsl:processing-instruction>
  <wml>

   <card id="news">
    <xsl:apply-templates select="item"/>
   </card>
  </wml>
 </xsl:template>
     
 <xsl:template match="item">
  <p>
   <a>
    <xsl:attribute name="href">
     <xsl:value-of select="link"/>
    </xsl:attribute>
    <xsl:value-of select="title"/>
   </a>
   <br/>
   <xsl:value-of select="description"/>
  </p>
 </xsl:template>

</xsl:stylesheet>