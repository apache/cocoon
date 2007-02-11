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
    Simple listing of available yapt presentations
    $Id: dir-to-html.xsl,v 1.2 2004/03/06 02:26:04 antonio Exp $
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
