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
    Generate HTML for the index of a presentation
    $Id: html-index.xsl,v 1.2 2004/03/06 02:26:04 antonio Exp $
-->
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
    <!-- heading templates -->
    <xsl:import href="heading.xsl"/>

    <xsl:variable name="pageTitle" select="/yapt-presentation/heading/presentation"/>

    <!-- output the presentation heading and the list of slides -->
    <xsl:template match="/">
        <html>
            <head>
                <title><xsl:value-of select="$pageTitle"/></title>
                <link rel="stylesheet" type="text/css" href="css/yapt-style.css"/>
            </head>
            <body>
                <div id="content">
                    <div id="presHeading">
                        <xsl:apply-templates mode="heading" select="yapt-presentation/heading/*"/>
                    </div>
                    <div id="indexContents">
                        <div id="slidesList">
                            <h2>Slides</h2>
                            <xsl:apply-templates mode="navigation" select="yapt-presentation/navigation"/>
                            <h3>All slides in a single page</h3>
                            <div class="slideListItem">
                                <div class="slideId">
                                    -
                                </div>
                                <div class="slideTitle">
                                    <a href="presentation">presentation</a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </body>
        </html>
    </xsl:template>

    <!-- navigation links -->
    <xsl:template match="slide-ref" mode="navigation">
        <div class="slideListItem">
            <div class="slideId">
                <xsl:value-of select="@slide-id"/>.
            </div>
            <div class="slideTitle">
                <a href="{concat('slide-',@slide-id)}">
                    <xsl:value-of select="@title"/>
                </a>
            </div>
        </div>
    </xsl:template>


</xsl:stylesheet>
