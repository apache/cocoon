<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:str="http://exslt.org/strings"
                extension-element-prefixes="str">

<!-- doc.xsl - text line wrapping
  This stylesheet does not do any line wrapping.
-->

  <xsl:template match="document">
    <html>
      <head>
        <title><xsl:value-of select="header/title"/></title>
      </head>
      <xsl:apply-templates/>
      </html>
  </xsl:template>

  <xsl:template match="body">
    <body>
      <h1><xsl:value-of select="header/title"/></h1>
      <p>Comment from stylesheet: No special handling was done for the
        &lt;source&gt; elements. Note the long right-left scrollbar.
      </p>
      <xsl:apply-templates/>
    </body>
  </xsl:template>

  <xsl:template match="section">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="source">
    <div style="padding:4pt; margin-bottom:8pt; border-width:1px; border-style:solid; border-color:#0086b2;">
      <!-- iterate over each line -->
      <xsl:for-each select="str:split(string(.), '&#10;')">
        <code>
          <!-- replace each group of two spaces by a &nbsp; and a space to allow line wrap while still
               keeping indentation -->
          <xsl:for-each select="str:split(string(.), '  ')">
            <xsl:value-of select="."/><xsl:text>&#160; </xsl:text>
          </xsl:for-each>
        </code>
        <br/>
      </xsl:for-each>
    </div>
  </xsl:template>

  <xsl:template match="title">
    <h2><xsl:apply-templates/></h2>
  </xsl:template>

  <xsl:template match="p">
    <p><xsl:apply-templates/></p>
  </xsl:template>

</xsl:stylesheet>
