<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">

 <xsl:template match="/">
  <html>
   <head>
    <title>Apache Cocoon @version@</title>
   </head>
   <body bgcolor="#ffffff" link="#0086b2" vlink="#00698c" alink="#743e75">
    <p align="center"><font size="+0" face="arial,helvetica,sanserif" color="#000000">The Apache Software Foundation is proud to present...</font></p>

    <p align="center"><img border="0" src="images/cocoon.gif"/></p>

    <p align="center"><font size="+0" face="arial,helvetica,sanserif" color="#000000"><b>version @version@</b></font></p>

    <xsl:apply-templates/>

    <p align="center">
     <font size="-1">
      Copyright &#169; @year@ <a href="http://www.apache.org">The Apache Software Foundation</a>.<br/>
      All rights reserved.
     </font>
    </p>
   </body>
  </html>
 </xsl:template>

 <xsl:template match="samples">
  <xsl:variable name="t-size" select="count(group)"/>
  <xsl:variable name="half" select="round($t-size div 2)"/>
    
  <table width="100%">
   <tr>
    <td valign="top">
     <xsl:for-each select="group">  
      <xsl:variable name="here" select="position()"/>
      <xsl:choose>
       <xsl:when test="../group[$here+$half]">
        <table bgcolor="#000000" border="0" cellspacing="2" cellpadding="2" width="97%" align="center">
         <tr>
          <td bgcolor="#0086b2" width="100%" align="left">
           <font size="+1" face="arial,helvetica,sanserif" color="#ffffff"><xsl:value-of select="@name"/></font>
          </td>
         </tr>
         <tr>
          <td width="100%" bgcolor="#ffffff" align="left">
           <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2"  width="100%" align="center">
            <xsl:apply-templates/>
           </table>
          </td>
         </tr>
        </table>
        <br/>
       </xsl:when>
       <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
     </xsl:for-each>
    </td>
    <td valign="top">
     <xsl:for-each select="group">  <!-- [position()<=$half] -->
      <xsl:variable name="here" select="position()"/>
      <xsl:choose>
       <xsl:when test="../group[$here>$half]">
        <table bgcolor="#000000" border="0" cellspacing="2" cellpadding="2" width="97%" align="center">
         <tr>
          <td bgcolor="#0086b2" width="100%" align="left">
           <font size="+1" face="arial,helvetica,sanserif" color="#ffffff"><xsl:value-of select="@name"/></font>
          </td>
         </tr>
         <tr>
          <td width="100%" bgcolor="#ffffff" align="left">
           <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2"  width="100%" align="center">
            <xsl:apply-templates/>
           </table>
          </td>
         </tr>
        </table>
        <br/>
       </xsl:when>
       <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
     </xsl:for-each>
    </td>
   </tr>
  </table>
 </xsl:template>
 
 <xsl:template match="sample">
  <tr>
   <td width="100%" bgcolor="#ffffff" align="left">
    <font size="+0" face="arial,helvetica,sanserif" color="#000000">    
      <a href="{@href}"><xsl:value-of select="@name"/></a><xsl:text> - </xsl:text>
      <xsl:value-of select="."/>
    </font>
   </td>
  </tr>
 </xsl:template>
 
</xsl:stylesheet>
