<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="linkalarm-report">
<html>
  <head><title>LinkAlarm summary for current Cocoon website</title></head>
  <body>
  <h1>LinkAlarm summary for current Cocoon website</h1>
  <p>The LinkAlarm service traverses the Cocoon website occasionally. The full
   set of reports are at
   <a href="http://reports.linkalarm.com/373104199608/">http://reports.linkalarm.com/373104199608/</a>
  </p>
  <p>This Cocoon Sample retrieves the summary report
   <code>links.broken.txt</code> and processes the tab-delimited file through
   a sequence of transformations to pretty-print the data.
  </p>
  <p>Please help to
   <a href="../../docs/plan/linkstatus.html">mend the broken links</a>.
  </p>

    <xsl:apply-templates/>
  </body>
</html>
</xsl:template>

<xsl:template match="item-num">
  <h3>Item number:
      <xsl:value-of select="."/>
  </h3>
</xsl:template>

<xsl:template match="broken-url">
 <p>Broken URL:
    <a href="{.}">
      <xsl:value-of select="."/>
    </a>
 </p>
</xsl:template>

<xsl:template match="referer-url">
 <p>Referer page:
    <a href="{.}">
      <xsl:value-of select="."/>
    </a>
 </p>
</xsl:template>

<xsl:template match="response-code">
  <p>Server response code:
      <xsl:value-of select="."/>
  </p>
</xsl:template>

<xsl:template match="reason-word">
  <xsl:value-of select="."/>
</xsl:template>

</xsl:stylesheet>
