<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template name="abs">
  <xsl:param name="node"/>
  <xsl:if test="$node/..">
    <xsl:call-template name="abs">
      <xsl:with-param name="node" select="$node/.."/>
    </xsl:call-template>
  </xsl:if>
  <xsl:value-of select="$node/@href"/>

</xsl:template>

<xsl:template match="@href">
  <xsl:attribute name="href">
  <xsl:call-template name="abs">
    <xsl:with-param name="node" select=".."/>
  </xsl:call-template>
  </xsl:attribute>
</xsl:template>

<xsl:template match="@*|node()" priority="-1">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>
</xsl:stylesheet>
