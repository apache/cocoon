<?xml version="1.0"?>

<xsl:stylesheet version="1.0" 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:err="http://apache.org/cocoon/2.0/error">

<xsl:template match="notify">
<html>
 <head>
  <title>
   <xsl:value-of select="@type"/>:<xsl:value-of select="title"/></title>
 </head>
 <body bgcolor="#ffffff">
 <TABLE border="0" bgcolor="#000000" cellpadding="2" cellspacing="2">
  <TBODY>
 <TR>
      <TD bgcolor="#0086b2" colspan="2">
       <FONT color="#ffffff" face="arial,helvetica,sanserif" size="+2">
       <xsl:value-of select="title"/>
       </FONT>
     </TD>
    </TR>

    <TR>
      <TD bgcolor="#0086b2" valign="top">
       <FONT color="#ffffff" face="arial,helvetica,sanserif" size="+1">
         <xsl:value-of select="@type"/>
       </FONT>
     </TD>
      <TD bgcolor="#ffffff" >
        <xsl:apply-templates select="message"/>
      </TD>
    </TR>

   <TR>
      <TD bgcolor="#0086b2" valign="top" colspan="2">
       <FONT color="#ffffff" face="arial,helvetica,sanserif" size="+1">
       details
       </FONT>
     </TD>
    </TR>

      <TR>
      <TD bgcolor="#0086b2" valign="top">
       <FONT face="arial,helvetica,sanserif" color="#ffffff">
       from
       </FONT>
      </TD>
      <TD bgcolor="#ffffff">
       <FONT face="arial,helvetica,sanserif">
        <xsl:value-of select="@sender"/>
       </FONT>
     </TD>
    </TR>

      <TR>
      <TD bgcolor="#0086b2" valign="top">
       <FONT face="arial,helvetica,sanserif" color="#ffffff">
       source
       </FONT>
      </TD>
      <TD bgcolor="#ffffff">
       <FONT face="arial,helvetica,sanserif">
        <xsl:value-of select="source"/>
       </FONT>
     </TD>
    </TR>
   <xsl:apply-templates select="description"/>

 <TR>
      <TD bgcolor="#0086b2" valign="top" colspan="2">
       <FONT color="#ffffff" face="arial,helvetica,sanserif" size="+1">
       extra info
       </FONT>
     </TD>
    </TR>

   <xsl:apply-templates select="extra"/>

   </TBODY>
  </TABLE> 
 </body>
</html>

  </xsl:template>

  <xsl:template match="description">
    <TR>
      <TD bgcolor="#0086b2" valign="top">
        <FONT color="#ffffff" face="arial,helvetica,sanserif">
	description
        </FONT></TD>
      <TD bgcolor="#ffffff">
      <FONT face="arial,helvetica,sanserif">
         <xsl:value-of select="."/>
      </FONT>
     </TD>
    </TR>
  </xsl:template>

  <xsl:template match="message">
      <FONT face="arial,helvetica,sanserif">
         <xsl:value-of select="."/>
      </FONT>
  </xsl:template>

  <xsl:template match="extra">
    <TR>
      <TD bgcolor="#0086b2" valign="top">
        <FONT color="#ffffff" face="arial,helvetica,sanserif">
          <xsl:value-of select="@description"/>
        </FONT></TD>
      <TD bgcolor="#ffffff">
        <PRE>
         <xsl:value-of select="."/>
       </PRE>
     </TD>
    </TR>
  </xsl:template>
 
  <xsl:template match="*"/>

</xsl:stylesheet>
