<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="transposition" select="0"/>

  <xsl:template match="NOTE_ON">
    <xsl:variable name="fixedTransposition">
      <xsl:choose>
        <xsl:when test="not(number($transposition) = number($transposition))">
          <xsl:message>The parameter $transposition must be a number!</xsl:message>
          <xsl:text>0</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$transposition"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="offset" select="$fixedTransposition"/>
    <NOTE_ON PITCH="{@PITCH + $offset}" REGISTER="{@REGISTER}" VELOCITY="{@VELOCITY}">
        <xsl:apply-templates/>
    </NOTE_ON>
  </xsl:template>

  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>

