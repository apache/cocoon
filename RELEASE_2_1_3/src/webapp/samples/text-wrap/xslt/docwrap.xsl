<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- docwrap.xsl - text line wrapping
  This stylesheet includes wrap2para.xsl to handle just <source> elements
  that have PCDATA.
  Any other type of source element is the reponsibility of the caller xslt.
-->

  <xsl:include href="wrap2para.xsl"/>

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
      <p>Comment from stylesheet: Only &lt;source&gt; elements that did not
        contain an xml content model, had their long
        lines handled by the included wrap2para.xsl stylesheet.
      </p>
      <xsl:apply-templates/>
    </body>
  </xsl:template>

  <xsl:template match="section">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="source">
    <pre class="code">
      <xsl:apply-templates/>
    </pre>
  </xsl:template>

  <xsl:template match="source[not(*)]">
    <div style="padding:4pt; margin-bottom:8pt; border-width:1px; border-style:solid; border-color:#0086b2;">
      <xsl:call-template name="format-source">
        <xsl:with-param name="source" select="string(.)"/>
      </xsl:call-template>
    </div>
  </xsl:template>

  <xsl:template match="title">
    <h2><xsl:apply-templates/></h2>
  </xsl:template>

  <xsl:template match="p">
    <p><xsl:apply-templates/></p>
  </xsl:template>

</xsl:stylesheet>
