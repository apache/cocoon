<?xml version="1.0"?>

<xsl:stylesheet version="1.0" 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:status="http://apache.org/cocoon/2.0/status">

  <xsl:template match="statusinfo">
    <html>
      <head>
        <title>Cocoon2 status [<xsl:value-of select="@host"/>]</title>
      </head>
      <body bgcolor="white">
  
      <table bgcolor="#ffffff" noshade="noshade" border="0" cellspacing="0" cellpadding="6" width="100%">
       <tr>
        <td bgcolor="#0086b2" valign="top" align="left">
         <img src="images/cocoon.gif" border="0"/>
        </td>
        <td bgcolor="#0086b2" valign="top" align="right">
         <font face="arial,helvetica,sanserif" color="#ffffff">   
	      [<xsl:value-of select="@host"/>] <xsl:value-of select="@date"/>
         </font>
        </td>
       </tr>
       <tr>
        <td bgcolor="#ffffff" valign="top" align="left" colspan="2" height="8">
        </td>
       </tr>
      </table>

      <xsl:apply-templates/>
      
      </body>
    </html>
  </xsl:template>

  <xsl:template match="group">
   <table bgcolor="#0086b2" noshade="noshade" border="0" cellspacing="2" cellpadding="6" width="100%">
    <tr>
      <td bgcolor="#0086b2" valign="top" align="left" colspan="2">
       <font color="#ffffff" face="arial,helvetica,sanserif" size="+1">
        <xsl:value-of select="@name"/>
       </font>
      </td>
    </tr>
    <tr>
      <td bgcolor="ffffff" width="100%" colspan="2">
        <table bgcolor="#0086b2" noshade="noshade" border="0" cellspacing="2" cellpadding="6" bordercolor="black" width="100%">
          <xsl:apply-templates />
        </table>
      </td>
    </tr>
   </table>
  </xsl:template>

  <xsl:template match="value">
    <tr>
      <td bgcolor="#0086b2" valign="top" align="left">
       <font face="arial,helvetica,sanserif" color="#ffffff">      
        <xsl:value-of select="@name"/>
       </font>
      </td>
      <td bgcolor="ffffff" width="100%">
       <font face="arial,helvetica,sanserif">
         <xsl:apply-templates />
       </font>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="line">
   <xsl:apply-templates/>
   <xsl:if test="../line"><br/></xsl:if>
  </xsl:template>
  
</xsl:stylesheet>