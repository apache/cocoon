<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:st="http://chaperon.sourceforge.net/schema/syntaxtree/1.0"
                version="1.0">

 <xsl:template match="st:output">
  <p>
   <!--<xsl:apply-templates select="*[name()!='error']"/>-->
   <xsl:for-each select="*[name()!='error']">
    <xsl:apply-templates select="."/><br/>
   </xsl:for-each>
   <xsl:if test="st:error">
    <ul>
     <xsl:for-each select="st:error">
      <li><font color="#ff0000"><xsl:value-of select="."/></font></li>
     </xsl:for-each>
    </ul>
   </xsl:if>
  </p>
 </xsl:template>

 <xsl:template match="st:E">
  <xsl:choose>
   <xsl:when test="count(*)">
    <table bgcolor="#a0ffff" cellspacing="1">
     <tr>
      <xsl:for-each select="child::node()">
       <td>
        <xsl:apply-templates select="."/>
       </td>
      </xsl:for-each>
     </tr>
    </table>
   </xsl:when>
   <xsl:otherwise>
    <xsl:apply-templates/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <xsl:template match="st:F">
  <xsl:choose>
   <xsl:when test="count(*)">
    <table bgcolor="#ffffa0" cellspacing="1">
     <tr>
      <xsl:for-each select="child::node()">
       <td>
        <xsl:apply-templates select="."/>
       </td>
      </xsl:for-each>
     </tr>
    </table>
   </xsl:when>
   <xsl:otherwise>
    <xsl:apply-templates/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <xsl:template match="st:T">
  <xsl:choose>
   <xsl:when test="count(*)">
    <table bgcolor="#ffa0ff" cellspacing="1">
     <tr>
      <xsl:for-each select="child::node()">
       <td>
        <xsl:apply-templates select="."/>
       </td>
      </xsl:for-each>
     </tr>
    </table>
   </xsl:when>
   <xsl:otherwise>
    <xsl:apply-templates/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
  <xsl:copy>
   <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:copy>
 </xsl:template>

</xsl:stylesheet>
