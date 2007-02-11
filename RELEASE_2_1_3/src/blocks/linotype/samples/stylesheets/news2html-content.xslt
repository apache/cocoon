<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:n="http://www.betaversion.org/linotype/news/1.0"
                              xmlns:h="http://www.w3.org/1999/xhtml"
                              xmlns="http://www.w3.org/1999/xhtml">

  <xsl:param name="home"/>

  <xsl:template match="/">
   <html>
    <head>
     <link rel="stylesheet" type="text/css" href="{$home}/styles/main.css"/>
     <link rel="stylesheet" type="text/css" href="{$home}/styles/editor_page.css"/>
    </head>
    <body class="body">
     <xsl:apply-templates select="n:news/h:body"/>
    </body>
   </html>
  </xsl:template>

  <xsl:template match="h:strong">
   <b><xsl:apply-templates/></b>
  </xsl:template>

  <xsl:template match="h:em">
   <i><xsl:apply-templates/></i>
  </xsl:template>

  <xsl:template match="@*|node()">
   <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
