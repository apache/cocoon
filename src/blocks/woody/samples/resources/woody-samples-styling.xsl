<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!--+ Include styling stylesheets, one for the widgets, the other one for the
      | page. As 'woody-advanced-field-styling.xsl' is a specialization of
      | 'woody-field-styling.xsl' the latter one is imported there. If you don't
      | want advanced styling of widgets, change it here!
      | See xsl:include as composition and xsl:import as extension/inheritance.
      +-->
  <xsl:include href="woody-page-styling.xsl"/>
  <xsl:include href="woody-advanced-field-styling.xsl"/>

  <xsl:template match="head">
    <head>
      <xsl:apply-templates/>
      <xsl:apply-templates select="." mode="woody-page"/>
      <xsl:apply-templates select="." mode="woody-field"/>
    </head>
  </xsl:template>

  <xsl:template match="body">
    <body>
      <xsl:apply-templates/>
      <xsl:apply-templates select="." mode="woody-page"/>
      <xsl:apply-templates select="." mode="woody-field"/>
    </body>
  </xsl:template>

</xsl:stylesheet>
