<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
  <xsl:template match="page">
   <xsl:processing-instruction name="cocoon-format">type="text/html"</xsl:processing-instruction>
   <html>
    <head>
     <title><xsl:value-of select="title"/></title>
    </head>
    <body>
    <p><br/></p>
    <center>
     <table border="0" width="60%" bgcolor="#000000" cellspacing="0" cellpadding="0">
      <tr>
       <td width="100%">
        <table border="0" width="100%" cellpadding="4">
         <tr>
          <td width="100%" bgcolor="#c0c0c0" align="right" valign="middle">
           <big><big><xsl:value-of select="title"/></big></big>
          </td>
         </tr>
         <tr>
          <td width="100%" bgcolor="#ffffff">
           <xsl:apply-templates/>
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

  <xsl:template match="title">
   <!-- ignore -->
  </xsl:template>

  <xsl:template match="p">
   <xsl:copy>
    <xsl:apply-templates/>
   </xsl:copy>
  </xsl:template>

  <xsl:template match="em">
    <strong><xsl:apply-templates/></strong>
  </xsl:template>

  <xsl:template match="parameters">
   <p>The following is the list of parameters for this request:</p>

   <center>
    <table border="0" width="90%" bgcolor="#000000" cellspacing="0" cellpadding="0">
     <tr>
      <td width="100%">
       <table border="0" width="100%" cellpadding="4">
        <tr>
         <th bgcolor="#e0e0e0" align="left">Name</th>             
         <th bgcolor="#e0e0e0" align="left">Value(s)</th>             
        </tr>
        <xsl:apply-templates/>
       </table>
      </td>
     </tr>
    </table>
   </center>
  </xsl:template>

  <xsl:template match="parameter">
   <tr>
    <td width="100%">
     <xsl:choose>
      <xsl:when test="position() mod 2 = 0">
       <xsl:attribute name="bgcolor">#f0f0f0</xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
       <xsl:attribute name="bgcolor">#ffffff</xsl:attribute>
      </xsl:otherwise>
     </xsl:choose>
     <xsl:value-of select="@name"/>
    </td>
    <td width="100%">
     <xsl:choose>
      <xsl:when test="position() mod 2 = 0">
       <xsl:attribute name="bgcolor">#f0f0f0</xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
       <xsl:attribute name="bgcolor">#ffffff</xsl:attribute>
      </xsl:otherwise>
     </xsl:choose>
     <table border="0">
      <xsl:apply-templates/>
     </table>
    </td>
   </tr>
  </xsl:template>
 
  <xsl:template match="parameter-value">
   <tr>
    <td>
     <xsl:apply-templates/>
    </td>
   </tr>
  </xsl:template>
  
</xsl:stylesheet>