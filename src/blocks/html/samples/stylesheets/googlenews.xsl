<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:xhtml="http://www.w3.org/1999/xhtml">

<xsl:template match="/xhtml:html">
  <html>
    <head>
      <title><xsl:value-of select="xhtml:head/xhtml:title"/></title>
    </head>
    <body>
      <h2><xsl:value-of select="xhtml:head/xhtml:title"/></h2>
      <ul>
        <xsl:apply-templates select="xhtml:body/xhtml:table[position() > 3]"/>
      </ul>
    </body>
  </html>
</xsl:template>

<xsl:template match="xhtml:table">
  <li>
    <xsl:apply-templates select="xhtml:tr/xhtml:td[last()]"/>
  </li>
</xsl:template>

<xsl:template match="xhtml:td">
  <xsl:apply-templates select="xhtml:a"/>
  <br/>
  <xsl:apply-templates select="xhtml:font/text()[normalize-space()][1]"/>
</xsl:template>

<xsl:template match="xhtml:a">
    <a href="http://news.google.com{@href}" title="{@title}">
      <xsl:value-of select="text()"/>
    </a>
</xsl:template>

</xsl:stylesheet>