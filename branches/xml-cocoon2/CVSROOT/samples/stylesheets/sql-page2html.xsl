<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:sql="http://xml.apache.org/cocoon/SQL">

  <xsl:template match="page">
   <html>
    <head>
     <title>
      <xsl:value-of select="title"/>
     </title>
    </head>
    <body bgcolor="#ffffff">
     <xsl:apply-templates/>
    </body>
   </html>
  </xsl:template>

  <xsl:template match="title">
   <h1 align="center">
    <xsl:apply-templates/>
   </h1>
  </xsl:template>

  <xsl:template match="paragraph">
   <p align="center">
    <i><xsl:apply-templates/></i>
   </p>
  </xsl:template>

  <xsl:template match="sql:ROWSET">
   <hr/>
    <xsl:apply-templates/>
   <hr/>
  </xsl:template>

  <xsl:template match="sql:ROW">
   <dl compact="compact">
    <xsl:for-each select="./*">
     <dt><xsl:value-of select="name(.)"/></dt>  
     <dd><xsl:value-of select="text()"/></dd>
    </xsl:for-each>
   </dl>
  </xsl:template>

</xsl:stylesheet>
