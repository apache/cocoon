<xsl:stylesheet version="1.0" 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:err="http://apache.org/cocoon/2.0/ErrorGenerator">

<xsl:template match="/">
<html>
 <head>
  <title>
   <xsl:value-of select="err:notify/@type"/>:<xsl:value-of select="err:notify/err:title"/></title>
 </head>
 <body bgcolor="#ffffff">
 <TABLE border="0" bgcolor="#0086b2" cellpadding="2" cellspacing="2">
  <TBODY>
 <TR>
      <TD colspan="2">
       <FONT color="#ffffff" face="arial,helvetica,sanserif" size="+2">
       <xsl:value-of select="err:notify/err:title"/>
       </FONT>
     </TD>
    </TR>

    <TR>
      <TD valign="top">
       <FONT color="#ffffff" face="arial,helvetica,sanserif" size="+1">
         <xsl:value-of select="err:notify/@type"/>
       </FONT>
     </TD>
      <TD bgcolor="#ffffff" >
        <xsl:apply-templates select="err:notify/err:message"/>
      </TD>
    </TR>

   <TR>
      <TD valign="top" colspan="2">
       <FONT color="#ffffff" face="arial,helvetica,sanserif" size="+1">
       details
       </FONT>
     </TD>
    </TR>

      <TR>
      <TD valign="top">
       <FONT face="arial,helvetica,sanserif" color="#ffffff">
       from
       </FONT>
      </TD>
      <TD bgcolor="#ffffff">
       <FONT face="arial,helvetica,sanserif">
        <xsl:value-of select="err:notify/@sender"/>
       </FONT>
     </TD>
    </TR>

   <xsl:apply-templates select="err:notify/err:source"/>
   <xsl:apply-templates select="err:notify/err:description"/>

 <TR>
      <TD valign="top" colspan="2">
       <FONT color="#ffffff" face="arial,helvetica,sanserif" size="+1">
       extra info
       </FONT>
     </TD>
    </TR>

   <xsl:apply-templates select="err:notify/err:extra"/>

   </TBODY>
  </TABLE> 
 </body>
</html>

  </xsl:template>

  <xsl:template match="err:notify/err:description">
    <TR>
      <TD valign="top">
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

  <xsl:template match="err:notify/err:message">
      <FONT face="arial,helvetica,sanserif">
         <xsl:value-of select="."/>
      </FONT>
  </xsl:template>

  <xsl:template match="err:notify/err:extra">
    <TR>
      <TD valign="top">
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