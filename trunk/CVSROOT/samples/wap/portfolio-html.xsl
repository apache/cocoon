<?xml version="1.0"?>

<!-- Written by Stefano Mazzocchi "stefano@apache.org" -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="portfolio">
   <xsl:processing-instruction name="cocoon-format">type="text/html"</xsl:processing-instruction>
   <html>

   <head>
    <meta HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=iso-8859-1"/>
    <meta NAME="Author" CONTENT="Cocoon"/>
    <title>Portfolio</title>
   </head>

   <body BGCOLOR="#FFFFFF">
    <table border="0" bgcolor="#000000" cellspacing="0" cellpadding="0">
     <tr>
      <td>
       <table border="0" width="100%" cellspacing="2" cellpadding="5">
        <tr>
         <td bgcolor="#F0F0F0">
          <table border="0" width="100%" cellspacing="0" cellpadding="3">
           <tr>
            <td width="100%" align="center">
             <table border="0" width="100%" cellspacing="10">
              <tr>
               <td valign="top" width="40%">
                <table border="0" width="100%" bgcolor="#000000" cellspacing="0" cellpadding="0">
                 <tr>
                  <td width="100%">
                   <table border="0" cellpadding="4" width="933">
                    <tr>
                     <td bgcolor="#C0C0C0" align="right" colspan="2">
                      <strong><big><big>Portfolio</big></big></strong>
                     </td>
                    </tr>
                    <tr>
                     <td bgcolor="#FFFFFF" align="center">
                      <strong><big><big>
                       <xsl:value-of select="total"/>
                       <xsl:text>$ (</xsl:text>
                       <xsl:value-of select="variations/day/@rate"/>
                       <xsl:value-of select="variations/day"/>
                       <xsl:text>%)</xsl:text>
                      </big></big></strong>
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
            </td>
           </tr>
          </table>
         </td>
        </tr>
       </table>
      </td>
     </tr>
    </table>
    <xsl:apply-templates select="note"/>    
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
          <td bgcolor="#C0C0C0" align="left" colspan="3">
            <a href="{@url}">
             <strong><big>
              <xsl:value-of select="@company"/>
             </big></strong>
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
  
  <xsl:template match="note">
   <p>
    <xsl:apply-templates/>
   </p>
  </xsl:template>
  
</xsl:stylesheet>