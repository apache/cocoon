<?xml version="1.0"?>
<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!--
    Convert slop output to HTML for the javateach sample
    $Id: jt-to-html.xsl,v 1.2 2004/03/06 02:25:42 antonio Exp $
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
