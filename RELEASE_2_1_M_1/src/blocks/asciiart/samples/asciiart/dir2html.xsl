<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:dir="http://apache.org/cocoon/directory/2.0"
>

<xsl:template match="dir:directory">
  <html>
    <head>
      <title>AsciiArt Samples</title>
    </head>
    <body>
      <table width="90%">
        <tr><td>JPEG</td><td>PNG</td></tr>
        <tr>
          <td>Available ascii art as <em>TXT</em>
          <ul>
            <xsl:apply-templates select="dir:file" mode="txt"/>
          </ul>
          </td>
          <td>Available ascii art as <em>JPEG</em>
          <ul>
            <xsl:apply-templates select="dir:file" mode="jpg"/>
          </ul>
          </td>
          <td>Available ascii art as <em>PNG</em>
          <ul>
            <xsl:apply-templates select="dir:file" mode="png"/>
          </ul>
          </td>
        </tr>
      </table>
    </body>
  </html>
</xsl:template>

<xsl:template match="dir:file" mode="txt">
   <li> 
     <a>
       <xsl:attribute name="href"><xsl:value-of select="@name"/></xsl:attribute>
       <xsl:value-of select="@name"/>
     </a>
   </li>
</xsl:template>

<xsl:template match="dir:file" mode="jpg">
   <li> 
     <a>
       <xsl:attribute name="href"><xsl:value-of select="@name"/>.jpg</xsl:attribute>
       <xsl:value-of select="@name"/>
     </a>
   </li>
</xsl:template>

<xsl:template match="dir:file" mode="png">
   <li> 
     <a>
       <xsl:attribute name="href"><xsl:value-of select="@name"/>.png</xsl:attribute>
       <xsl:value-of select="@name"/>
     </a>
   </li>
</xsl:template>
</xsl:stylesheet>
