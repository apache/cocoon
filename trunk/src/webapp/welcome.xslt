<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="welcome">
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
      <head>
        <title>Welcome to Cocoon!</title>
        <link href="styles/cocoon.css" type="text/css" rel="stylesheet"/>
        <link href="favicon.ico" rel="SHORTCUT ICON" />
      </head>
      <body>
        <h1>Welcome to Cocoon!</h1>
        <xsl:apply-templates/>
        <p class="copyright">
         Copyright © @year@ <a href="http://www.apache.org/">The Apache Software Foundation</a>. All rights reserved.
        </p>
        <p class="block">
          <a href="http://cocoon.apache.org/"><img src="images/powered.gif" alt="Powered by Cocoon"/></a>
        </p>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="message">
    <p class="block"><xsl:apply-templates/></p>
  </xsl:template>
  
  <xsl:template match="link">
    <a href="{@href}"><xsl:apply-templates/></a>
  </xsl:template>

</xsl:stylesheet>
