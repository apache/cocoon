<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

 <xsl:import href="./document-stylebook.xsl"/>

 <xsl:template match="spec">
  <s1 title="{header/title}">
   <xsl:apply-templates/>
  </s1>
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
