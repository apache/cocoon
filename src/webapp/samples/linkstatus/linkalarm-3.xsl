<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.0'>
<!-- FIXME: Under development.
* Not sure how it will cope with a big links.broken.txt list (currently small).
* Should be able to produce more structure with <item> so that we can display
as table. Seems to be a tricky process.
-->

<xsl:template match="linkalarm-report">
<linkalarm-report><xsl:text>&#xA;</xsl:text>
  <xsl:apply-templates/>
</linkalarm-report>
</xsl:template>

<xsl:template match="url">
<!-- process each url and decide which link type and whether new item -->
  <xsl:variable name="url-num"><xsl:number/></xsl:variable>
  <xsl:variable name="item-num">
    <xsl:value-of select="round($url-num div 2)"/>
  </xsl:variable>
<!-- DEBUG: URL-NUM=<xsl:value-of select="$url-num"/> -->
  <xsl:choose>
    <!-- odd-number <url> is the beginning of <item> and is the broken url -->
    <xsl:when test="$url-num mod 2 != 0">
        <item-num><xsl:value-of select="$item-num"/></item-num>
        <broken-url><xsl:value-of select="."/></broken-url>
    </xsl:when>
    <!-- even-number <url> is the referer url -->
    <xsl:otherwise>
      <referer-url><xsl:value-of select="."/></referer-url>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="response-code">
  <response-code><xsl:value-of select="."/></response-code>
</xsl:template>

<xsl:template match="reason-word">
  <reason-word><xsl:value-of select="."/></reason-word>
</xsl:template>

</xsl:stylesheet>
