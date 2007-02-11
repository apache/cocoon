<?xml version="1.0"?>

<!-- CVS: $Id: relativize-linkmap.xsl,v 1.2 2004/02/17 00:15:21 joerg Exp $ -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="path"/>

  <xsl:include href="dotdots.xsl"/>

  <!-- Path to site root, eg '../../' -->
  <xsl:variable name="root">
    <xsl:call-template name="dotdots">
      <xsl:with-param name="path" select="$path"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:template match="@href">
    <xsl:attribute name="href">
      <xsl:value-of select="$root"/><xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
