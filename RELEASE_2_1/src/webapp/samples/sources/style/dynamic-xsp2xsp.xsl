<?xml version="1.0"?>

<!-- CVS $Id: dynamic-xsp2xsp.xsl,v 1.1 2003/05/07 04:57:15 vgritsenko Exp $ -->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsp="http://apache.org/xsp"
                xmlns:xsp-request="http://apache.org/xsp/request/2.0">

  <xsl:template match="*[namespace-uri() = 'urn:xsp']">
    <xsl:element name="xsp:{local-name()}">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name(.)}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>

      <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="*[namespace-uri() = 'urn:xsp-request']">
    <xsl:element name="xsp-request:{local-name()}" namespace="http://apache.org/xsp/request/2.0">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name(.)}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>

      <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
