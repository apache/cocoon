<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

 <xsl:output indent="yes"/>
  
 <xsl:template match="/">
  <html>
   <head>
    <title>Apache Cocoon 2.1-dev</title>
    <link rel="SHORTCUT ICON" href="favicon.ico"/>

    <xsl:apply-templates select="page/style"/>
    <xsl:apply-templates select="page/script"/>
   </head>
   <body bgcolor="#ffffff" link="#0086b2" vlink="#00698c" alink="#743e75">
    <table border="0" cellspacing="2" cellpadding="2" align="center" width="100%">
     <tr>
      <td width="*"><font face="arial,helvetica,sanserif" color="#000000">The Apache Software Foundation is proud to
present...</font></td>
      <td width="40%" align="center"><img border="0" src="/cocoon/samples/images/cocoon.gif"/></td>
      <td width="30%" align="center"><font face="arial,helvetica,sanserif" color="#000000"><b>version
2.1-dev</b></font></td>
     </tr>
     <tr>
      <table bgcolor="#000000" border="0" cellspacing="2" cellpadding="2" align="center" width="100%">
       <tr>
        <td width="90%" align="left" bgcolor="#0086b2"><font size="+1" face="arial,helvetica,sanserif"
            color="#ffffff"><xsl:value-of select="page/@title"/></font></td>

        <xsl:apply-templates select="page/tab"/>
       </tr>
      </table>
     </tr>
    </table>

    <table width="100%">   
     <xsl:apply-templates select="page/row"/>
    </table>
   
    <p align="center">
     <font size="-1">
      Copyright &#169; 1999-2002 <a href="http://www.apache.org/">The Apache Software Foundation</a>.<br/>
      All rights reserved.
     </font>
    </p>
   </body>
  </html>
 </xsl:template>

 <xsl:template match="style">
  <link type="text/css" rel="stylesheet" href="{@href}"/>
 </xsl:template>

 <xsl:template match="script">
  <script type="text/javascript" src="{@href}"/>
 </xsl:template>

 <xsl:template match="tab">
  <td nowrap="nowrap" bgcolor="#ffffff"><a href="{@href}"><i><xsl:value-of select="@title"/></i></a></td>
 </xsl:template>

 <xsl:template match="row">
  <tr>
   <xsl:apply-templates select="column"/>
  </tr>
 </xsl:template>

 <xsl:template match="column">
  <td valign="top">
   <table border="0" bgcolor="#000000" cellpadding="0" cellspacing="0" width="97%">
    <tbody>
     <tr>
      <td>

       <table bgcolor="#000000" border="0" cellspacing="2" cellpadding="2" align="center" width="100%">
        <tr>
         <td bgcolor="#0086b2" width="100%" align="left">
          <font size="+1" face="arial,helvetica,sanserif" color="#ffffff"><xsl:value-of select="@title"/></font>
         </td>
        </tr>
        <tr>
         <td width="100%" bgcolor="#ffffff" align="left">

          <xsl:apply-templates/>

         </td>
        </tr>
       </table>
      
      </td>
     </tr> 
    </tbody>
   </table>
  </td> 
 </xsl:template>

 <xsl:template match="para">
  <p align="left">
   <i><xsl:apply-templates/></i>
  </p>
 </xsl:template>

 <xsl:template match="@*|node()" priority="-2"><xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy></xsl:template>
 <xsl:template match="text()" priority="-1"><xsl:value-of select="."/></xsl:template>

</xsl:stylesheet>
