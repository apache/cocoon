<?xml version="1.0" encoding="UTF-8"?>

<!-- CVS $Id: style.xsl,v 1.2 2003/05/07 04:57:14 vgritsenko Exp $ -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="page">
    <html>
        <head>
            <title><xsl:value-of select="title" /></title>
        </head>
        <body>
            <xsl:apply-templates />
        </body>
    </html>
</xsl:template>

<xsl:template match="title">
    <h1><xsl:value-of select="." /></h1>
</xsl:template>

</xsl:stylesheet>
