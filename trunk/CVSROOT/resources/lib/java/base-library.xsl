<?xml version="1.0"?>

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
  xmlns:xsp="http://apache.org/DTD/XSP/Layer1"
>
  <!-- Default copy-over's -->
  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
  </xsl:template>

  <!-- *** Utility Templates *** -->
  <!-- Retrieve "name" parameter as either attribute or element -->
  <xsl:template name="value-for-name">
    <xsl:choose>
      <!-- As attribute (String constant) -->
      <xsl:when test="@name">"<xsl:value-of select="@name"/>"</xsl:when>
      <!-- As nested (presumably dynamic) element -->
      <xsl:when test="name">
        <!-- Recursively evaluate nested expression -->
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="name"/>
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- Return nested element content as expression or constant -->
  <xsl:template name="get-nested-content">
    <xsl:choose>
      <!-- Nested element -->
      <xsl:when test="$content/*">
        <xsl:apply-templates select="$content/*"/>
      </xsl:when>
      <!-- Plain Text -->
      <xsl:otherwise>"<xsl:value-of select="normalize($content)"/>"</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Ensure attribute "as" has a value -->
  <xsl:template name="value-for-as">
    <xsl:choose>
      <xsl:when test="@as"><xsl:value-of select="@as"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$default"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Default xsp:page preprocessing -->
  <!--
  <xsl:template match="xsp:page">
    <xsp:page>
      <xsl:copy>
        <xsl:apply-templates select="@*"/>
      </xsl:copy>

      <xsl:apply-templates/>

    </xsp:page>
  </xsl:template>
  -->

</xsl:stylesheet>
