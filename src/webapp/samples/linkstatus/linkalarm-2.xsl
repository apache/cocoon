<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.0'>

<xsl:template match="body">
<linkalarm-report><xsl:text>&#xA;</xsl:text>
  <xsl:call-template name="split-it">
    <xsl:with-param name="list" select="."/>
  </xsl:call-template>
</linkalarm-report>
</xsl:template>

<xsl:template name="split-it">
<!-- split the list based on whitespace -->
  <xsl:param name="list"/>
  <xsl:variable name="nlist"
      select="concat(normalize-space($list),' ')"/>
  <xsl:variable name="token" select="substring-before($nlist, ' ')"/>
  <xsl:variable name="rest" select="substring-after($nlist, ' ')"/>
  <xsl:choose>
    <!-- server response code -->
    <xsl:when test="string(number($token))!='NaN'">
      <response-code>
        <xsl:value-of select="$token"/>
      </response-code><xsl:text>&#xA;</xsl:text>
    </xsl:when>
    <!-- broken-url or referer-url -->
    <xsl:when test="contains($token,'://')">
      <url>
        <xsl:value-of select="$token"/>
      </url><xsl:text>&#xA;</xsl:text>
    </xsl:when>
    <!-- text reason-->
    <xsl:otherwise>
      <reason-word>
        <xsl:value-of select="$token"/>
      </reason-word><xsl:text>&#xA;</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
  <!-- if any text remains then split it too -->
  <xsl:if test="$rest">
    <xsl:call-template name="split-it">
      <xsl:with-param name="list" select="$rest"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>
