<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:st="http://chaperon.sourceforge.net/schema/syntaxtree/1.0"
                version="1.0">

 <xsl:template match="st:E">
  <table bgcolor="#a0ffff" cellspacing="1">
   <tr>
    <xsl:for-each select="child::node()">
     <td>
      <xsl:apply-templates select="."/>
     </td>
    </xsl:for-each>
   </tr>
  </table>
 </xsl:template>

 <xsl:template match="st:F">
  <table bgcolor="#ffffa0" cellspacing="1">
   <tr>
    <xsl:for-each select="child::node()">
     <td>
      <xsl:apply-templates select="."/>
     </td>
    </xsl:for-each>
   </tr>
  </table>
 </xsl:template>

 <xsl:template match="st:T">
  <table bgcolor="#ffa0ff" cellspacing="1">
   <tr>
    <xsl:for-each select="child::node()">
     <td>
      <xsl:apply-templates select="."/>
     </td>
    </xsl:for-each>
   </tr>
  </table>
 </xsl:template>

 <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
  <xsl:copy>
   <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:copy>
 </xsl:template>

</xsl:stylesheet>
