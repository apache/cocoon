<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:err="http://apache.org/cocoon/2.0/error">

<xsl:template match="notify">
 <html>
  <head>
   <title>
    <xsl:value-of select="@type"/>:<xsl:value-of select="title"/>
   </title>
  </head>
  <body bgcolor="#ffffff">
   <table border="0" bgcolor="#000000" cellpadding="2" cellspacing="2">
    <tbody>
     <tr>
      <td bgcolor="#0086b2" colspan="2">
       <font color="#ffffff" face="arial,helvetica,sanserif" size="+2">
        <xsl:value-of select="title"/>
       </font>
      </td>
     </tr>

     <tr>
      <td bgcolor="#0086b2" valign="top">
       <font color="#ffffff" face="arial,helvetica,sanserif" size="+1">
        <xsl:value-of select="@type"/>
       </font>
      </td>
      <td bgcolor="#ffffff" >
       <xsl:apply-templates select="message"/>
      </td>
     </tr>

     <tr>
      <td bgcolor="#0086b2" valign="top" colspan="2">
       <font color="#ffffff" face="arial,helvetica,sanserif" size="+1">details</font>
      </td>
     </tr>

     <tr>
      <td bgcolor="#0086b2" valign="top">
       <font face="arial,helvetica,sanserif" color="#ffffff">from</font>
      </td>
      <td bgcolor="#ffffff">
       <font face="arial,helvetica,sanserif">
        <xsl:value-of select="@sender"/>
       </font>
      </td>
     </tr>

     <tr>
      <td bgcolor="#0086b2" valign="top">
       <font face="arial,helvetica,sanserif" color="#ffffff">source</font>
      </td>
      <td bgcolor="#ffffff">
       <font face="arial,helvetica,sanserif">
        <xsl:value-of select="source"/>
       </font>
      </td>
     </tr>

     <xsl:apply-templates select="description"/>

     <tr>
      <td bgcolor="#0086b2" valign="top" colspan="2">
       <font color="#ffffff" face="arial,helvetica,sanserif" size="+1">extra info</font>
      </td>
     </tr>

     <xsl:apply-templates select="extra"/>

    </tbody>
   </table>
  </body>
 </html>
</xsl:template>

 <xsl:template match="description">
  <tr>
   <td bgcolor="#0086b2" valign="top">
    <font color="#ffffff" face="arial,helvetica,sanserif">description</font>
   </td>
   <td bgcolor="#ffffff">
    <font face="arial,helvetica,sanserif">
     <xsl:value-of select="."/>
    </font>
   </td>
  </tr>
 </xsl:template>

 <xsl:template match="message">
  <font face="arial,helvetica,sanserif">
   <xsl:value-of select="."/>
  </font>
 </xsl:template>

 <xsl:template match="extra">
  <tr>
   <td bgcolor="#0086b2" valign="top">
    <font color="#ffffff" face="arial,helvetica,sanserif">
     <xsl:value-of select="@description"/>
    </font>
   </td>
   <td bgcolor="#ffffff">
    <pre>
     <xsl:value-of select="."/>
    </pre>
   </td>
  </tr>
 </xsl:template>

 <xsl:template match="*"/>

</xsl:stylesheet>
