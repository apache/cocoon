<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="NOTE_ON">
    <xsl:variable name="notes" select="//NOTE_ON"/>
    <xsl:variable name="medianPitch" select="floor(sum($notes/@PITCH) div count($notes))"/>
    <NOTE_ON PITCH="{$medianPitch - (@PITCH - $medianPitch)}" REGISTER="{@REGISTER}" VELOCITY="{@VELOCITY}">
      <xsl:apply-templates />
    </NOTE_ON>
  </xsl:template>

  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>

