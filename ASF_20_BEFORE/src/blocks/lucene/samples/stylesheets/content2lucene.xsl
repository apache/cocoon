<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:cinclude="http://apache.org/cocoon/include/1.0"
  xmlns:lucene="http://apache.org/cocoon/lucene/1.0" 
>
  <xsl:template match="includes">
    <lucene:index 
      analyzer="org.apache.lucene.analysis.standard.StandardAnalyzer" 
      directory="index2" 
      create="false" 
      merge-factor="10">

      <xsl:apply-templates/>

    </lucene:index>
  </xsl:template>

  <xsl:template match="file">
    <lucene:document>
      <xsl:attribute name="url"><xsl:value-of select="name"/></xsl:attribute>
      <xsl:copy-of select="include/*"/>
    </lucene:document>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-1"></xsl:template>
  <xsl:template match="text()" priority="-1"></xsl:template>
</xsl:stylesheet> 