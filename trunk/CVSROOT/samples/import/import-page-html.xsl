<?xml version="1.0"?>

<!-- Written by Stefano Mazzocchi "stefano@apache.org" -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:import href="../hello/hello-page-html.xsl"/>
  
  <xsl:template match="paragraph">
   <p align="left">
    <b>
     <xsl:apply-templates/>
    </b>
   </p>
  </xsl:template>

</xsl:stylesheet>