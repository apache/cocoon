<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- 
This stylesheet simply removes the surrounding html and body tag

$Id: cleanhtml.xsl,v 1.1 2004/02/12 09:32:00 cziegeler Exp $ 

-->
<xsl:template match="/" xmlns:xhtml="http://www.w3.org/1999/xhtml">
  <xsl:apply-templates select="xhtml:html/xhtml:body"/>
  <xsl:apply-templates select="html/body"/>
</xsl:template>

<xsl:template match="xhtml:body" xmlns:xhtml="http://www.w3.org/1999/xhtml">
  <div>
    <xsl:copy-of select="*"/>
  </div>
</xsl:template>

<xsl:template match="body">
  <div>
    <xsl:copy-of select="*"/>
  </div>
</xsl:template>

</xsl:stylesheet>
