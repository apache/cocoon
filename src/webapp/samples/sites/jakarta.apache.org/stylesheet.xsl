<?xml version="1.0"?>
<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!-- Author: Stefano Mazzocchi "stefano@apache.org" -->
<!-- Version: $Id: stylesheet.xsl,v 1.2 2004/03/06 02:26:16 antonio Exp $ -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="page">
   <html>
    <head>
     <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
     <title><xsl:value-of select="@title"/></title>
     <link rel="stylesheet" href="style.css"><!-- no not remove this comment --></link>
    </head>
    <body bgcolor="#FFFFFF">
     <table width="100%" border="0">
      <tr> 
       <td> 
        <p>
         <a href="welcome"><img src="{/page/logo/@href}"
           alt="The Jakarta Project" border="0"/></a> 
        </p>
       </td>
      </tr>
     </table>
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
         Copyright &#169; 1999-2000 The Apache Software Foundation<br/>
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
