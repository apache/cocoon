<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:st="http://chaperon.sourceforge.net/schema/syntaxtree/2.0">

 <xsl:output indent="yes" 
             method="xml"/>

 <xsl:template match="//document">
<!--  <div style="background: #b9d3ee; border: thin; border-color: black; border-style: solid; padding-left: 0.8em; 
              padding-right: 0.8em; padding-top: 0px; padding-bottom: 0px; margin: 0.5ex 0px; clear: both;">-->
   <p>
   <xsl:apply-templates select="body/p|body/section"/>
   </p>
<!--  </div>-->
 </xsl:template>

 <xsl:template match="section">
  <xsl:choose> <!-- stupid test for the hirachy deep -->
   <xsl:when test="../../../section">
    <h5><xsl:value-of select="title"/></h5>
   </xsl:when>
   <xsl:when test="../../section">
    <h4><xsl:value-of select="title"/></h4>
   </xsl:when>
   <xsl:when test="../section">
    <h3><xsl:value-of select="title"/></h3>
   </xsl:when>
  </xsl:choose>
  <p>
   <xsl:apply-templates select="*[name()!='title']"/>
  </p>
 </xsl:template>

 <xsl:template match="source">
  <div style="background: #b9d3ee; border: thin; border-color: black; border-style: solid; padding-left: 0.8em; 
              padding-right: 0.8em; padding-top: 0px; padding-bottom: 0px; margin: 0.5ex 0px; clear: both;">
  <pre>
   <xsl:value-of select="."/>
  </pre>
  </div>
 </xsl:template>

 <xsl:template match="link">
  <xsl:text> </xsl:text>
  <a href="{@href}">
   <xsl:apply-templates/>
  </a>
  <xsl:text> </xsl:text>
 </xsl:template>

 <xsl:template match="strong">
  <xsl:text> </xsl:text>
  <b>
   <xsl:apply-templates/>
  </b>
  <xsl:text> </xsl:text>
 </xsl:template>

 <xsl:template match="anchor">
  <a name="{@name}">
   <xsl:apply-templates/>
  </a>
 </xsl:template>

 <xsl:template match="table">
  <table border="1" cellspacing="3" cellpadding="3">
   <xsl:apply-templates/>
  </table>
 </xsl:template>

 <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
  <xsl:copy>
   <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:copy>
 </xsl:template>

</xsl:stylesheet>
