<?xml version="1.0"?>

<!-- Written by Stefano Mazzocchi "stefano@apache.org" -->

<xsl:stylesheet xsl:version="1.0"  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

 <xsl:template match="portfolio">
  <xsl:processing-instruction name="cocoon-format">type="text/wml"</xsl:processing-instruction>

  <wml>
   <card id="index" title="Your Portfolio">
    <p align="center">
     <a href="#value">Value</a><br/>
     <a href="#stocks">Stocks</a><br/>
    </p>
    <do type="accept" label="About">
     <go href="#About"/>
    </do>
   </card>

   <card id="about" title="About">
    <onevent type="ontimer">
     <prev/>
    </onevent>
    <timer value="25"/>
    <p align="center">
     <br/>
     <br/>
     <small>
      Copyright &#xA9; 1999<br/>
      Apache Software Foundation.<br/>
      All rights reserved.
     </small>
    </p>
   </card>

   <card id="value" title="Portfolio Value">
    <p>
     Total value: <b>$$<xsl:value-of select="total"/></b><br/>
     <small>(<b><xsl:value-of select="variations/day/@rate"/><xsl:value-of select="variations/day"/>&#x25;</b> from yesterday)</small><br/>
     <small>(<b><xsl:value-of select="variations/week/@rate"/><xsl:value-of select="variations/week"/>&#x25;</b> from last week)</small><br/>
     <small>(<b><xsl:value-of select="variations/month/@rate"/><xsl:value-of select="variations/month"/>&#x25;</b> from last month)</small><br/>
     <small>(<b><xsl:value-of select="variations/ever/@rate"/><xsl:value-of select="variations/ever"/>&#x25;</b> ever)</small><br/>
    </p>
	<do type="prev" label="Back">
	 <prev/>
	</do>
   </card>

   <card id="stocks" title="Your Current Stocks">
    <p align="center">
     <xsl:for-each select="stocks">
      <a href="#{@company}"><xsl:value-of select="@company"/></a><br/>
     </xsl:for-each>
    </p>
	<do type="prev" label="Back">
	 <prev/>
	</do>
   </card>

   <xsl:apply-templates select="stocks"/>
  </wml>
 </xsl:template>

 <xsl:template match="stocks">
  <card id="{@company}" title="{@company}">
   <p>
    Quotes: <xsl:value-of select="quotes"/>
    <br/>
    Value: <xsl:value-of select="value"/>$$<br/>
   	<small>(<b><xsl:value-of select="variations/day/@rate"/><xsl:value-of select="variations/day"/>&#x25;</b> from yesterday)</small><br/>
   	<small>(<b><xsl:value-of select="variations/week/@rate"/><xsl:value-of select="variations/week"/>&#x25;</b> from last week)</small><br/>
   	<small>(<b><xsl:value-of select="variations/month/@rate"/><xsl:value-of select="variations/month"/>&#x25;</b> from last month)</small><br/>
   	<small>(<b><xsl:value-of select="variations/ever/@rate"/><xsl:value-of select="variations/ever"/>&#x25;</b> ever)</small><br/>
   </p>
   <do type="prev" label="Back">
	<prev/>
   </do>
  </card>
 </xsl:template>
</xsl:stylesheet>