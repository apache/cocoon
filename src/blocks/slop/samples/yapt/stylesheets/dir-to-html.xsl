<?xml version="1.0"?>

<!--
    Simple listing of available yapt presentations
    $Id: dir-to-html.xsl,v 1.1 2003/09/26 14:42:36 bdelacretaz Exp $
-->
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dir="http://apache.org/cocoon/directory/2.0"
>

    <xsl:param name="pageTitle" select="'Available YAPT presentations'"/>
    <xsl:param name="baseDir"/>

    <xsl:template match="/">
        <html>
            <head>
                <title><xsl:value-of select="$pageTitle"/></title>
                <link rel="stylesheet" type="text/css" href="css/yapt-style.css"/>
            </head>
            <body>
                <div id="content">
                    <h1><xsl:value-of select="$pageTitle"/></h1>
                    <ul>
                        <xsl:apply-templates select="dir:directory/dir:file"/>
                    </ul>
                </div>
            </body>
        </html>

    </xsl:template>

    <!-- link to presentation index and its source code -->
    <xsl:template match="dir:file">
        <xsl:variable name="presName" select="substring-before(@name,'.txt')"/>
        <xsl:if test="$presName">
            <li>
                <a href="{concat($baseDir,'/',$presName,'/html/index')}">
                    <xsl:value-of select="$presName"/>
                </a>
                (
                <a href="{concat($baseDir,'/',$presName,'/txt/presentation')}">
                    .txt source file
                </a>
                )
            </li>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>
