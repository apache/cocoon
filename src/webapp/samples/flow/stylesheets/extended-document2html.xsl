<?xml version="1.0" encoding="utf-8"?>

<!-- An extension to the document DTD, which allows for HTML forms to
be embedded within the document.
 -->

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="../../../docs/stylesheets/document2html.xsl"/>

  <xsl:template match="form | input | select | option | textarea | keygen | isindex">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>

