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

<!-- Convert RequestGenerator output to SVG -->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:h="http://apache.org/cocoon/request/2.0"
    >

    <xsl:param name="fillColor" select="'#330000'"/>

    <xsl:template id="main" match="/">
        <svg width="600" height="500">
            <defs>
                <filter id="blur1">
                    <feGaussianBlur stdDeviation="3"/>
                </filter>
                <filter id="blur2">
                    <feGaussianBlur stdDeviation="1"/>
                </filter>
            </defs>

            <g title="this is a tooltip">
                <rect
                    style="{concat('fill:',$fillColor,';stroke:#000000;stroke-width:4;filter:url(#blur1);')}"
                    x="30" y="30" rx="20" ry="20" width="500" height="400"/>
                <text style="fill:#FFFFFF;font-size:24;font-family:TrebuchetMS-Bold;filter:url(#blur2);" x="65" y="80">
                    <xsl:value-of select="concat('color:',$fillColor)"/>
                </text>
                <xsl:apply-templates select="//h:header[position() &lt; 5]"/>
            </g>
        </svg>
    </xsl:template>

    <xsl:template match="h:header">
        <text style="fill:#FFFFFF;font-size:24;font-family:TrebuchetMS-Bold;filter:url(#blur2);" x="65" y="{40 * (2 + position())}">
            <xsl:value-of select="concat(@name,':',.)"/>
        </text>
    </xsl:template>

</xsl:stylesheet>
