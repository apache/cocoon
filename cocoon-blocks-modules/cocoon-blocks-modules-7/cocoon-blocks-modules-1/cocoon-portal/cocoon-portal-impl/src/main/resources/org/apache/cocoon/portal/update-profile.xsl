<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml" encoding="UTF-8" indent="yes"/>
<xsl:strip-space elements="*"/>


  <xsl:template match="coplet-base-data">
    <coplet-type>
      <xsl:apply-templates/>
    </coplet-type>
  </xsl:template>

  <xsl:template match="coplet-data">
    <coplet-definition>
      <xsl:apply-templates/>
    </coplet-definition>
  </xsl:template>

  <xsl:template match="coplet-instance-data">
    <coplet-instance>
      <xsl:apply-templates/>
    </coplet-instance>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
  </xsl:template>

</xsl:stylesheet>