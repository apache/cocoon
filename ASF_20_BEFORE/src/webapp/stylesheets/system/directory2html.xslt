<?xml version="1.0"?>

<!-- CVS $Id: directory2html.xslt,v 1.3 2003/12/02 16:52:51 vgritsenko Exp $ -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dir="http://apache.org/cocoon/directory/2.0">

  <xsl:template match="/">
    <html>
      <head>
        <title><xsl:value-of select="dir:directory/@name"/></title>
        <style>
          <xsl:comment>
            body { background-color: #ffffff }
          </xsl:comment>
        </style>
      </head>
      <body>
        <h1>Directory Listing of <xsl:value-of select="dir:directory/@name"/></h1>
        <table border="0">
          <tr>
            <td><a href="../"><i>parent directory</i></a></td>
          </tr>
          <tr>
            <td>&#160;</td>
          </tr>
          <xsl:apply-templates/>
        </table>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="dir:directory/dir:directory">
    <tr>
      <td><a href="{@name}/"><i><xsl:value-of select="@name"/></i></a></td>
      <td><xsl:value-of select="@date"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="dir:file">
    <tr>
      <td><a href="{@name}"><xsl:value-of select="@name"/></a></td>
      <td><xsl:value-of select="@date"/></td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
