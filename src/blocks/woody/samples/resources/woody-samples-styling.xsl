<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- Location of the resources directory, where JS libs and icons are stored -->
  <xsl:param name="resources-uri">resources</xsl:param>

  <!-- Include styling stylesheets -->
  <!-- FIXME: xalan silently ignores non-existing stylesheets (such as when there's a typo in the name)
       ==> Is it in the XSLT spec? -->
  <xsl:include href="woody-field-styling.xsl"/>
  <xsl:include href="woody-page-styling.xsl"/>
  <xsl:include href="woody-calendar-styling.xsl"/>
  <xsl:include href="woody-advanced-field-styling.xsl"/>

  <xsl:template match="head">
    <xsl:copy>
      <xsl:apply-templates/>
      <!-- insert the head snippets required by the styling stylesheets -->
      <xsl:call-template name="woody-field-head"/>
      <xsl:call-template name="woody-page-head"/>
      <xsl:call-template name="woody-calendar-head"/>
      <xsl:call-template name="woody-advanced-field-head"/>

      <link rel="stylesheet" type="text/css" href="{$resources-uri}/woody.css"/>

      <xsl:call-template name="woody-calendar-css"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="body">
    <xsl:copy>
      <!--xsl:copy-of select="@*"/-->
      
      <!-- insert the body  snippets required by the styling stylesheets -->
      <xsl:call-template name="woody-field-body"/>
      <xsl:call-template name="woody-page-body"/>
      <xsl:call-template name="woody-calendar-body"/>
      <xsl:call-template name="woody-advanced-field-body"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
