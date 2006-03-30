<?xml version="1.0" encoding="iso-8859-1"?>

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

<!-- simple transformation of RequestGenerator output to HTML -->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:rss="http://purl.org/rss/1.0/"
>

    <xsl:template id="main" match="/">
        <html>
            <body>
                <h1>
                    RSS news in HTML
                </h1>
                <ul>
                    <xsl:apply-templates select="//rss:item"/>
                </ul>
            </body>
        </html>
    </xsl:template>

    <xsl:template id="rssItem" match="rss:item">
        <li>
            <a href="{@rdf:about}"><xsl:value-of select="rss:title"/></a>
        </li>
    </xsl:template>

</xsl:stylesheet>
