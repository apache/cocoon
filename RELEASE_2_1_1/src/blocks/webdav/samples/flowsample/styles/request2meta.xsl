<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:req="http://apache.org/cocoon/request/2.0">
<xsl:template match="/req:request">
  <metapage>
    <xsl:apply-templates select="req:requestParameters"/>
  </metapage>
</xsl:template>

<xsl:template match="req:requestParameters">
    <xsl:apply-templates select="req:parameter[@name='author']"/>
    <xsl:apply-templates select="req:parameter[@name='state']"/>
    <xsl:apply-templates select="req:parameter[@name='category']"/>
</xsl:template>

<xsl:template match="req:parameter[@name='title']">
  <title>
    <xsl:value-of select="req:value"/>
  </title>
</xsl:template>

<xsl:template match="req:parameter[@name='author']">
  <author>
    <xsl:value-of select="req:value"/>
  </author>
</xsl:template>
<xsl:template match="req:parameter[@name='state']">
  <state>
    <xsl:value-of select="req:value"/>
  </state>
</xsl:template>
<xsl:template match="req:parameter[@name='category']">
  <category>
    <xsl:value-of select="req:value"/>
  </category>
</xsl:template>

</xsl:stylesheet>
