<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

 <xsl:template match="/">
  <html>
   <head>
    <title>Cocoon Live Demo</title>
   </head>
   <body bgcolor="#ffffff">
    <p align="center">The Apache Software Foundation is proud to present...</p>

    <p align="center"><img border="0" src="images/cocoon.png"/></p>

    <h3 align="center">version 2.0a2</h3>

    <p><br/></p>

    <xsl:apply-templates/>

    <p align="center">
     <font size="-1">
      Copyright &#169; 1999-2000 <a href="http://xml.apache.org">The Apache XML Project</a>.<br/>
      All rights reserved.
     </font>
    </p>
   </body>
  </html>
 </xsl:template>

 <xsl:template match="group">
  <div align="center">
  <table border="0" width="60%">
   <tr>
    <td width="100%" bgcolor="#0086b2">
     <big><xsl:value-of select="@name"/></big>
    </td>
   </tr>
   <xsl:apply-templates/>
  </table>
  </div>
 </xsl:template>

 <xsl:template match="sample">
  <tr>
   <td width="100%" bgcolor="#ffffff">
    <a href="{@url}"><xsl:value-of select="@name"/></a><xsl:text> - </xsl:text>
    <xsl:apply-templates/>
   </td>
  </tr>
 </xsl:template>
 
</xsl:stylesheet>
