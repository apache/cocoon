<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="view-source"/>

  <xsl:template match="page">
   <itext>
     <paragraph size="18" align="Center">
        <xsl:value-of select="title"/>
     </paragraph>

     <paragraph leading="11" align="Center">
       <xsl:value-of select="content/para"/>
     </paragraph>
   </itext>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-2"><xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy></xsl:template>
  <xsl:template match="text()" priority="-1"><xsl:value-of select="."/></xsl:template>

</xsl:stylesheet>
