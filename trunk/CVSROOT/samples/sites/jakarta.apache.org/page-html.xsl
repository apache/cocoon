<?xml version="1.0"?>

<!-- Author: Stefano Mazzocchi "stefano@apache.org" -->
<!-- Version: $Id: page-html.xsl,v 1.1 1999-12-03 23:50:26 stefano Exp $ -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="page">
   <xsl:processing-instruction name="cocoon-format">type="text/html"</xsl:processing-instruction>
   <html>
    <head>
     <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
     <title><xsl:value-of select="@title"/></title>
     <link rel="stylesheet" href="style.css"><!-- no not remove this comment --></link>
    </head>
    <body bgcolor="#FFFFFF">
     <table width="100%" border="0" cellpadding="10" cellspacing="0">
      <tr valign="top">          
       <xsl:apply-templates/>
      </tr>
     </table>
     
     <br/>
     
     <table width="100%" border="0" cellpadding="10" cellspacing="0">
      <tr>
       <td>
        <p class="fineprint">
         Copyright &#169; 1999 The Apache Software Foundation<br/>
         <a href="legal.html">Legal Stuff They Make Us Say</a><br/>
         <a href="contact.html">Contact Information</a>
        </p>
       </td>
      </tr>
     </table>
    </body>
   </html>
  </xsl:template>
  
  <xsl:template match="sidebar">
   <td width="120">
    <xsl:apply-templates/>
   </td>
  </xsl:template>

  <xsl:template match="body">
   <td>
    <xsl:apply-templates/>
   </td>
  </xsl:template>
  
  <xsl:template match="group">
   <p>
    <span class="navheading"><xsl:value-of select="@name"/><xsl:text>:</xsl:text></span><br/>
    <span class="navitem">
     <xsl:apply-templates/>
    </span>
   </p> 
  </xsl:template>
  
  <xsl:template match="section">
   <h3><xsl:value-of select="@title"/></h3>
   <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="p|a">
   <xsl:copy>
    <xsl:apply-templates/>
   </xsl:copy>
  </xsl:template>

  <xsl:template match="link">
   <a href="{@href}"><xsl:apply-templates/></a><br/>
  </xsl:template>
  
</xsl:stylesheet>