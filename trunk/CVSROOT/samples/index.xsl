<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="samples">
<xsl:processing-instruction name="cocoon-format">type="text/html"</xsl:processing-instruction>
<html>
 <head>
  <title>Cocoon Live Show</title>
 </head>
 <body>
 
 <p><br /></p>
 
 <center>
  <table border="0" width="60%" bgcolor="#000000" cellspacing="0" cellpadding="0">
   <tr>
    <td width="100%">
     <table border="0" width="100%" cellpadding="4">
      <tr>
       <td width="100%" bgcolor="#c0c0c0" align="right">
        <big><big>Cocoon Live Show</big></big>
       </td>
      </tr>
      <tr>
       <td width="100%" bgcolor="#ffffff" align="center">
        <xsl:apply-templates/>
        <p><br/></p>
       </td>
      </tr>
     </table>
    </td>
   </tr>
  </table>
 </center>
 
 <p align="center">
  <font size="-1">
   Copyright &#169; 1999 <a href="http://xml.apache.org">The Apache XML Project</a>.<br/>
   All rights reserved.
  </font>
 </p>
 </body>
</html>
</xsl:template>

 <xsl:template match="group">
  <p><br/></p>
  <table border="0" width="90%" bgcolor="#000000" cellspacing="0" cellpadding="0">
   <tr>
    <td width="100%">
     <table border="0" width="100%" cellpadding="4">
      <tr>
       <td width="100%" bgcolor="#e0e0e0">
        <big><xsl:value-of select="@name"/></big>
       </td>
      </tr>
      <xsl:apply-templates/>
     </table>
    </td>
   </tr>
  </table>
 </xsl:template>

<xsl:template match="sample">
 <tr>
  <td width="100%" bgcolor="#ffffff">
   <a href="{@url}"><xsl:value-of select="@name"/></a><xsl:text> - </xsl:text>
   <xsl:apply-templates/>
  </td>
 </tr>
</xsl:template>

<xsl:template match="a">
 <a href="{@href}">
  <xsl:apply-templates/>
 </a>
</xsl:template>

</xsl:stylesheet>