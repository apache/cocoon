<xsl:stylesheet version="1.0" 
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:dir="http://xml.apache.org/cocoon/2.0/DirectoryGenerator">

  <xsl:template match="/">
   <html>
    <head>
     <title><xsl:value-of select="@name"/></title>
    </head>
    <body bgcolor="#ffffff">
     <h1>Directory Listing <xsl:value-of select="@name"/></h1>
     <table border="0">
      <tr>
       <td>
        <a href="../"><i>parent directory</i></a>
       </td>
      </tr>
      <xsl:apply-templates/>
     </table>
    </body>
   </html>
  </xsl:template>

  <xsl:template match="dir:directory">
   <tr>
    <td>
     <a href="{@name}"><i><xsl:apply-templates/></i></a>
    </td>
   </tr>
  </xsl:template>

  <xsl:template match="dir:file">
   <tr>
    <td>
     <a href="{@name}"><xsl:apply-templates/></a>
    </td>
   </tr>
  </xsl:template>

</xsl:stylesheet>