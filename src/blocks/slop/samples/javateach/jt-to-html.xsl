<?xml version="1.0"?>

<!--
    Convert slop output to HTML for the javateach sample
    $Id: jt-to-html.xsl,v 1.1 2003/10/14 12:00:05 bdelacretaz Exp $
-->
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:slop="http://apache.org/cocoon/slop/parser/1.0"
>

    <xsl:param name="pageTitle" select="'Javateach sample - using the Cocoon SLOP parser'"/>

    <!-- keys based on last preceding lpstart or lpend, used to split between code and teaching comments -->
    <xsl:key
        name="lastMarkKey"
        match="slop:*" use="generate-id(preceding::slop:*[self::slop:__lpstart|self::slop:__lpend][1])"
    />

    <xsl:template match="/">
        <html>
            <head>
                <title><xsl:value-of select="$pageTitle"/></title>
                <link rel="stylesheet" type="text/css" href="css/javateach.css"/>
            </head>
            <body>
                <div id="content">
                    <h1 class="pageTitle"><xsl:value-of select="$pageTitle"/></h1>
                    <div class="code">
                        <pre>
                            <xsl:apply-templates
                                select="slop:parsed-text/slop:*[not(preceding::slop:__lpstart) and not(self::slop:__lpstart)]"
                                mode="code"
                            />
                        </pre>
                    </div>
                    <xsl:apply-templates select="slop:parsed-text/slop:__lpstart|slop:parsed-text/slop:__lpend"/>
                </div>
            </body>
        </html>

    </xsl:template>

    <xsl:template match="slop:__lpstart">
        <div class="teachingComments">
            <xsl:apply-templates select="key('lastMarkKey',generate-id(.))" mode="teachingComments"/>
        </div>
    </xsl:template>

    <xsl:template match="slop:__lpend">
        <div class="code">
            <pre>
                <xsl:apply-templates select="key('lastMarkKey',generate-id(.))" mode="code"/>
            </pre>
        </div>
    </xsl:template>

    <xsl:template match="slop:*" mode="teachingComments">
        <xsl:value-of select="concat(substring-after(.,'//'),'&#xD;')" disable-output-escaping="yes"/>
    </xsl:template>

    <xsl:template match="slop:*" mode="code">
        <span class="lineNumber">
            <xsl:value-of select="concat(@line-number,'  ')"/>
        </span>
        <span class="codeLine">
            <xsl:value-of select="concat(.,'&#xD;')"/>
        </span>
    </xsl:template>

</xsl:stylesheet>
