<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:req="http://apache.org/cocoon/request/2.0">
<xsl:template match="/req:request">
  <request>
    <xsl:apply-templates select="req:requestParameters"/>
  </request>
</xsl:template>

<xsl:template match="req:requestParameters">
  <parameters>
    <xsl:apply-templates select="req:parameter[@name='title']"/>
    <xsl:apply-templates select="req:parameter[@name='para']"/>
  </parameters>
</xsl:template>

<xsl:template match="req:parameter[@name='title']">
  <title>
    <xsl:value-of select="req:value"/>
  </title>
</xsl:template>

<xsl:template match="req:parameter[@name='para']">
  <content>
    <xsl:for-each select="req:value">
      <para><xsl:value-of select="normalize-space(.)"/></para>
    </xsl:for-each>
  </content>
</xsl:template>

</xsl:stylesheet>
