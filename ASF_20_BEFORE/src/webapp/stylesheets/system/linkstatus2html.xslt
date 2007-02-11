<?xml version="1.0"?>

<!-- CVS $Id: linkstatus2html.xslt,v 1.2 2003/12/02 16:52:51 vgritsenko Exp $ -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:linkstatus="http://apache.org/cocoon/linkstatus/2.0">

  <xsl:template match="linkstatus:linkstatus">
    <html>
      <body>
        <table border="1">
          <tr><th>URL</th><th>referrer</th><th>content-type</th><th>status</th><th>message</th></tr>
          <xsl:apply-templates/>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="linkstatus:link">
    <tr>
      <xsl:attribute name = "bgcolor">
        <xsl:choose>
          <xsl:when test="normalize-space(@status)='200'">#00ff00</xsl:when>
          <xsl:when test="normalize-space(@status)='404'">#ffff00</xsl:when>     	
          <xsl:otherwise>#ff0000</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <td><a href="{@href}"><xsl:value-of select="@href"/></a></td>
      <td><a href="{@referrer}">referrer</a></td>
      <td><xsl:value-of select="@content"/></td> 
      <td><xsl:value-of select="@status"/></td> 
      <td><xsl:value-of select="@message"/></td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
