<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="./document-stylebook.xsl"/>

<xsl:template match="spec">
  <s1 title="{header/title}">
   <xsl:apply-templates/>
  </s1>
 </xsl:template>

<xsl:template match="header">
  <s2 title="{subtitle}">
   <table>
    <tr><th>Authors</th></tr>
    <xsl:for-each select="authors/person">
     <tr><td><strong><xsl:value-of select="@name"/></strong> - <xsl:value-of select="@email"/></td></tr>
    </xsl:for-each>
    <tr><th>Status</th></tr>
    <tr><td><strong><xsl:value-of select="type"/> - <xsl:value-of select="version"/></strong></td></tr>
    <tr><th>Notice</th></tr>
    <tr><td><xsl:value-of select="notice"/></td></tr>
    <tr><th>Abstract</th></tr>
    <tr><td><xsl:value-of select="abstract"/></td></tr>
   </table>
  </s2>
 </xsl:template>

<xsl:template match="appendices">
  <s2 title="{@title}">
   <xsl:apply-templates/>
  </s2>
 </xsl:template>

<xsl:template match="bl">
  <ul>
   <xsl:apply-templates/>
  </ul>
 </xsl:template>

<xsl:template match="bi">
  <li>
   <em>
    <xsl:text>[</xsl:text>
     <jump href="{@href}"><xsl:value-of select="@name"/></jump>
    <xsl:text>]</xsl:text>
   </em>
   <xsl:text> &quot;</xsl:text>
   <xsl:value-of select="@title"/>
   <xsl:text>&quot;, </xsl:text>
   <xsl:value-of select="@authors"/>
   <xsl:text>, </xsl:text>
   <xsl:value-of select="@date"/>
  </li>
 </xsl:template>

</xsl:stylesheet>
