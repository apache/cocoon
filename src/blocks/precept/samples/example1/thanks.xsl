<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:i="http://www.dff.st/ns/desire/instance/1.0">


   <xsl:template match="root">
     <html><body><xsl:apply-templates/></body></html>
   </xsl:template>
      
   <xsl:template match="/|*">
      <xsl:copy><xsl:copy-of select="@*"/><xsl:apply-templates /></xsl:copy>
   </xsl:template>

   <xsl:template match="text()">
      <xsl:value-of select="." />
   </xsl:template>
</xsl:stylesheet>

