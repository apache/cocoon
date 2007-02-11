<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="file"/>

  <xsl:template match="/test">
    <test-out file="{$file}">
      <xsl:copy-of select="document($file)"/>
    </test-out>
  </xsl:template>

</xsl:stylesheet>
