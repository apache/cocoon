<xsl:stylesheet version="1.0" 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:err="http://apache.org/cocoon/2.0/ErrorGenerator">

  <xsl:template match="/">
   <html>
   <head>
    <title><xsl:value-of select="err:notify/err:title"/></title>
    </head>
    <body bgcolor="#ffffff">
     <h1><xsl:value-of select="err:notify/err:title"/></h1>
     <table border="1">
      <tr>
       <td align="right" valign="top">Type</td>
       <td><xsl:value-of select="err:notify/@type"/></td>
      </tr>
      <tr>
       <td align="right" valign="top">Sender</td>
       <td><xsl:value-of select="err:notify/@sender"/></td>
      </tr>
      <tr>
       <td align="right" valign="top">Source</td>
       <td><xsl:value-of select="err:notify/err:source"/></td>
      </tr>
      <tr>
       <td align="right" valign="top">Message</td>
       <td><xsl:value-of select="err:notify/err:message"/></td>
      </tr>
      <tr>
       <td align="right" valign="top">Description</td>
       <td><xsl:value-of select="err:notify/err:description"/></td>
      </tr>
      <tr>
       <td align="right" valign="top"><xsl:value-of select="err:notify/err:extra/@description"/></td>
       <td><pre><xsl:apply-templates select="err:notify/err:extra"/></pre></td>
      </tr>
     </table>
    </body>
   </html>
  </xsl:template>

  <xsl:template match="err:notify/err:extra">
    <xsl:apply-templates/>
  </xsl:template>
 
  <xsl:template match="*"/>

</xsl:stylesheet>
