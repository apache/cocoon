<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:include href="woody-page-styling.xsl"/>
  <xsl:include href="woody-field-styling.xsl"/>
  
  <!-- head and body stuff required to use the calendar popup -->
  <xsl:template match="head">
    <xsl:copy>
      <xsl:apply-templates/>
      <!-- insert the head snippets required by the styling stylesheets -->
      <xsl:call-template name="woody-page-head"/>
      <xsl:call-template name="woody-field-head"/>
      <link rel="stylesheet" type="text/css" href="resources/woody.css"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="body">
    <xsl:copy>
      <!-- insert the body  snippets required by the styling stylesheets -->
      <xsl:call-template name="woody-page-body"/>
      <xsl:call-template name="woody-field-body"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
