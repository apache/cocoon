<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- $Id: rss2html_news.xsl,v 1.2 2003/07/12 17:02:47 cziegeler Exp $ 

-->


<xsl:template match="rss">
  <xsl:apply-templates select="channel"/>
</xsl:template>

<xsl:template match="channel">
  <xsl:if test="title">
    <b><a href="{link}"><xsl:value-of select="title"/></a></b>
    <br/>
  </xsl:if>
  <xsl:if test="description">
    <font size="-3">&#160;(<xsl:value-of select="description"/>)</font>
  </xsl:if>
  <table>
    <xsl:apply-templates select="item"/>
  </table>
</xsl:template>

<xsl:template match="item">
  <!-- Display the first 5 entries -->
  <xsl:if test="position() &lt; 6">
    <tr>
      <td>
        <a target="_blank" href="{link}">
          <font size="-1"> 
            <b><xsl:value-of select="title"/></b>
          </font>
        </a>
        <xsl:apply-templates select="description"/>
      </td>
    </tr>
    <tr><td height="5">&#160;</td></tr>
  </xsl:if>
</xsl:template>

<xsl:template match="description">
  <font size="-2">
    <br/>
    &#160;&#160;<xsl:apply-templates/>
  </font>
</xsl:template>

<xsl:template match="node()|@*" priority="-1">
  <xsl:copy>
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
