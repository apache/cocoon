<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:fb="http://apache.org/cocoon/forms/1.0#binding">

<xsl:template match="node()|@*">
  <xsl:copy>
    <xsl:apply-templates select="node()|@*"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="comment()">
  <!-- circumvent a Xalan bug while handling empty comments -->
  <xsl:if test="normalize-space()">
    <xsl:copy/>
  </xsl:if>
</xsl:template>

<xsl:template match="fb:repeater">
  <xsl:copy>
    <xsl:apply-templates select="@*[not(starts-with(name(), 'unique-'))]"/>
    <xsl:if test="@unique-row-id and @unique-path and
                    not(fb:unique-row | fb:identity)">
      <fb:identity>
        <fb:value id="{@unique-row-id}" path="{@unique-path}"/>
      </fb:identity>
    </xsl:if>
    <xsl:apply-templates select="node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="fb:unique-row">
  <xsl:if test="not(../fb:identity)">
    <fb:identity>
      <xsl:apply-templates select="fb:unique-field"/>
    </fb:identity>
  </xsl:if>
</xsl:template>

<xsl:template match="fb:unique-field">
  <fb:value>
    <xsl:apply-templates select="node()|@*"/>
  </fb:value>
</xsl:template>

</xsl:stylesheet>
