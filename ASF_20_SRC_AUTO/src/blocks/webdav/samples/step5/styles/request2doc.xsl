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
    <xsl:apply-templates select="req:parameter[@name='author']"/>
    <xsl:apply-templates select="req:parameter[@name='state']"/>
    <xsl:apply-templates select="req:parameter[@name='category']"/>
    <xsl:apply-templates select="req:parameter[@name='filename']"/>
    <xsl:apply-templates select="req:parameter[@name='para']"/>
  </parameters>
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
<xsl:template match="req:parameter[@name='filename']">
  <filename>
    <xsl:value-of select="req:value"/>
  </filename>
</xsl:template>

<xsl:template match="req:parameter[@name='para']">
  <content>
    <xsl:for-each select="req:value">
      <para><xsl:value-of select="normalize-space(.)"/></para>
    </xsl:for-each>
  </content>
</xsl:template>

</xsl:stylesheet>
