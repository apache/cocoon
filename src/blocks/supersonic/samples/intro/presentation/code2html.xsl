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

<!-- convert code elements to HTML -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <!-- by default copy everything -->
    <xsl:template match="*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- process xml-code elements -->
    <xsl:template match="xml-code">
        <div class="code">
            <xsl:apply-templates mode="code"/>
        </div>
    </xsl:template>

    <!-- in code mode, dump all elements -->
    <xsl:template match="*" mode="code">
        <div class="codeIndent">
            <xsl:choose>
                <xsl:when test="*|.//text()|.//comment">
                    &lt;<xsl:value-of select="name()"/><xsl:apply-templates select="@*" mode="code"/>&gt;
                    <xsl:apply-templates mode="code"/>
                    &lt;/<xsl:value-of select="name()"/>&gt;
                </xsl:when>

                <xsl:otherwise>
                    &lt;<xsl:value-of select="name()"/><xsl:apply-templates select="@*" mode="code"/>/&gt;
                </xsl:otherwise>
            </xsl:choose>
        </div>

    </xsl:template>

    <!-- in code mode, dump all attributes -->
    <xsl:template match="@*" mode="code">
        <xsl:value-of select="concat(' ',name(),'=&quot;',.,'&quot;')"/>
    </xsl:template>

</xsl:stylesheet>
