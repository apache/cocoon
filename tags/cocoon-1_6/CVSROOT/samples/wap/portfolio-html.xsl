<?xml version="1.0"?>

<!-- Written by Stefano Mazzocchi "stefano@apache.org" -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="portfolio">
   <xsl:processing-instruction name="cocoon-format">type="text/html"</xsl:processing-instruction>
   <html>

    <head>
     <title>Your Portfolio</title>
    </head>
    
    <body BGCOLOR="#FFFFFF">
     <center>
      <table border="0" cellspacing="0" cellpadding="3">
       <tr>
        <td width="100%" align="center">
         <table border="0" width="100%" bgcolor="#000000" cellspacing="0" cellpadding="0">
          <tr>
           <td width="100%">
            <table border="0" cellpadding="4">
             <tr>
              <td bgcolor="#C0C0C0" align="right" colspan="2">
               <big><big>Your Portfolio</big></big>
              </td>
             </tr>
             <tr>
              <td bgcolor="#FFFFFF" align="center">
               <big>
                <xsl:value-of select="total"/>
                <xsl:text>$ (</xsl:text>
                <xsl:value-of select="variations/day/@rate"/>
                <xsl:value-of select="variations/day"/>
                <xsl:text>%)</xsl:text>
               </big>
              </td>
              <td bgcolor="#FFFFFF" align="center">
               <table border="0" width="100%" cellspacing="10">
                <xsl:apply-templates select="stocks"/>
               </table>
              </td>
             </tr>
            </table>
           </td>
          </tr>
         </table>
        </td>
       </tr>
      </table>
     </center> 
    </body>
   </html>
  </xsl:template>
  
  <xsl:template match="stocks">
   <tr>
    <td valign="top">
     <table border="0" width="100%" bgcolor="#000000" cellspacing="0" cellpadding="0">
      <tr>
       <td width="100%">
        <table border="0" cellpadding="4" width="100%">
         <tr>
          <td bgcolor="#C0C0C0" align="center" colspan="3">
            <a href="{@url}">
             <big>
              <xsl:value-of select="@company"/>
             </big>
            </a>
          </td>
         </tr>
         <tr>
          <td bgcolor="#FFFFFF" align="center">
           <xsl:value-of select="quotes"/>
           <xsl:text> quotes</xsl:text>
          </td>
          <td bgcolor="#FFFFFF" align="center">
           <font color="#0000FF"><big>
            <xsl:value-of select="value"/>
            <xsl:text>$</xsl:text>
           </big></font>
          </td>
          <td bgcolor="#FFFFFF" align="center">
           <big>
            <xsl:value-of select="variations/day/@rate"/>
            <xsl:value-of select="variations/day"/>
            <xsl:text>%</xsl:text>
           </big>
          </td>
         </tr>
        </table>
       </td>
      </tr>
     </table>
    </td>
   </tr>
  </xsl:template>
</xsl:stylesheet>