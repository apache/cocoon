<?xml version="1.0" encoding="utf-8"?>

<!-- Author: Ovidiu Predescu "ovidiu@cup.hp.com" -->
<!-- Date: July 27, 2001 -->
<!-- Implement the xscript:value-of support -->

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:saxon="http://icl.com/saxon"
  exclude-result-prefixes="xalan saxon">

  <xsl:param name="xpath" select="'/'"/>

  <xsl:template match="/">
    <root>
      <xsl:choose>
        <xsl:when test="contains(system-property('xsl:vendor-url'), 'xalan')">
          <xsl:value-of select="xalan:evaluate($xpath)"/>
        </xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor-url'), 'saxon')">
          <xsl:value-of select="saxon:evaluate($xpath)"/>
        </xsl:when>
      </xsl:choose>
    </root>
  </xsl:template>

</xsl:stylesheet>
