<?xml version="1.0"?>

<!-- CVS: $Id: simple-page2html.xsl,v 1.2 2003/05/22 13:37:46 vgritsenko Exp $ -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="context://samples/common/style/xsl/html/simple-page2html.xsl"/>
 
  <xsl:template match="linkbar">
    <div>
      [
      <a href="login"> login </a>
      |
      <a href="page"> protected </a>
      |
      <a href="do-logout"> logout </a>
      ]
    </div>
  </xsl:template>

</xsl:stylesheet>
<!-- vim: set et ts=2 sw=2: -->
