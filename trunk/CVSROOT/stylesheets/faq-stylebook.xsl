<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

 <xsl:import href="./document-stylebook.xsl"/>

 <xsl:template match="faqs">
  <xsl:copy>
   <xsl:apply-templates/>
  </xsl:copy>
 </xsl:template>

 <xsl:template match="faq">
  <faq title="{question}">
   <xsl:apply-templates/>
  </faq>
 </xsl:template>
 
 <xsl:template match="question">
  <q>
   <xsl:apply-templates/>
  </q>
 </xsl:template>

 <xsl:template match="answer">
  <a>
   <xsl:apply-templates/>
  </a>
 </xsl:template>
 
</xsl:stylesheet>
