<?xml version="1.0" encoding="ISO-8859-1"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY copy   "&#169;">
<!ENTITY nbsp   "&#160;">
]>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/TR/xhtml1/strict">
  <xsl:output method="html" indent="yes" doctype-public="-//W3C//DTD HTML 4.0 Transitional//EN"/>
  <!-- PAGE ========================================================-->
  <xsl:template match="page">
    <xsl:processing-instruction name="cocoon-format">type="text/html"</xsl:processing-instruction>
    <xsl:comment>
      XSL <xsl:value-of select="system-property('xsl:version')"/> /
      <xsl:value-of select="system-property('xsl:vendor')"/> 
      (<xsl:value-of select="system-property('xsl:vendor-url')"/>)
    </xsl:comment>
    <html>
      <head>
        <link rel="stylesheet" type="text/css" href="./stylesheets/javadoc.html.css"/>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
        <meta name="Author" content="{authors/author/@name}"/>
        <!-- meta name="Version" content="{version}"/ -->
        <title>
          <xsl:value-of select="@title"/>
        </title>
      </head>
      <body>
        <h1>
          <a name="top">
            <xsl:value-of select="@title"/>
          </a>
        </h1>
        <xsl:apply-templates select="authors"/>
        <xsl:apply-templates select="section"/>
      </body>
    </html>
  </xsl:template>
  <!-- AUTHORS =====================================================-->
  <xsl:template match="authors">
    <div>
        <xsl:apply-templates select="author"/>
    </div>
  </xsl:template>
  <!-- AUTHOR ======================================================-->
  <xsl:template match="author">
    <xsl:choose>
      <xsl:when test="@email[. != '']">
        <a href="mailto:{@email}">
          <em>
            <xsl:value-of select="@name"/>
          </em>
        </a>
      </xsl:when>
      <xsl:otherwise>
        <em>
          <xsl:value-of select="@name"/>
        </em>
      </xsl:otherwise>
    </xsl:choose>
    <br/>
  </xsl:template>
  <!-- SECTION =====================================================-->
  <xsl:template match="section">
    <h2><xsl:value-of select="@title"/></h2>
    <xsl:apply-templates select="p|source"/>
  </xsl:template>
  <!-- P ===========================================================-->
  <xsl:template match="p">
    <div class="p">
      <xsl:apply-templates/>
    </div>
  </xsl:template>
  <!-- SOURCE ======================================================-->
  <xsl:template match="source">
    <div class="source" style="background-color:#EEEEEE">
      <pre>
        <xsl:value-of select="."/>
      </pre>
    </div>
  </xsl:template>
  <!-- LINK ========================================================-->
  <xsl:template match="link">
    <a href="{@href}">
      <xsl:apply-templates/>
    </a>
  </xsl:template>
</xsl:stylesheet>
