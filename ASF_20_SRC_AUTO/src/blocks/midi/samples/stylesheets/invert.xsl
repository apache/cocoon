<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:variable name="notes" select="//NOTE_ON"/>
  <xsl:variable name="medianPitch" select="floor(sum($notes/@PITCH) div count($notes))"/>

  <xsl:template match="NOTE_ON">
    <NOTE_ON PITCH="{$medianPitch * 2 - @PITCH}">
      <xsl:copy-of select="@REGISTER | @VELOCITY"/>
      <xsl:apply-templates select="node()"/>
    </NOTE_ON>
  </xsl:template>

  <xsl:template match="node()">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>

