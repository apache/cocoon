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
    <xsl:attribute name="title"><xsl:value-of select="title"/></xsl:attribute>
    <p align="center">
     <small>
      <xsl:value-of select="description"/>
     </small>
    </p>

    <p>
     <xsl:for-each select="item">
      <xsl:call-template name="news-item"/> <br/>
     </xsl:for-each>
    </p>
 
   <do label="About" type="accept">
     <go href="#about"/>
    </do>
   </card>
 
   <xsl:apply-templates select="item"/>
  
   <card id="about" title="About">
    <onevent type="ontimer">
      <prev/>
    </onevent>
    <timer value="25"/>
  
    <p align="center">
      <br/>
      <br/>
      <small>
         Copyright &#xA9; 2000
        <br/>
         TIS d.o.o.
        <br/>
         All rights reserved. 
      </small>
    </p>
   </card>
  </wml>
</xsl:template>

 <xsl:template match="*" name="news-item">
    <a>
     <xsl:attribute name="href">#_<xsl:number/></xsl:attribute>
     <xsl:value-of select="title"/>
    </a>
 </xsl:template>

 <xsl:template match="item">
  <card>
   <xsl:attribute name="id">
    _<xsl:number/>
   </xsl:attribute>
   <p>
    <small>
     <xsl:value-of select="description"/>
    </small>
   </p>

   <do type="prev">
    <prev/>
   </do>

   <do label="View URL" type="accept">
    <go>
     <xsl:attribute name="href"><xsl:value-of select="link"/></xsl:attribute>
    </go>
   </do>
  </card>
 </xsl:template>

</xsl:stylesheet>