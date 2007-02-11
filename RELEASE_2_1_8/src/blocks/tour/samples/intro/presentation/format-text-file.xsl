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

<!-- format the output of the SlopGenerator for inclusion in one of our pages -->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:slop="http://apache.org/cocoon/slop/parser/1.0"
>

    <xsl:param name="filename"/>

    <!-- format slop parsed text -->
    <xsl:template match="slop:parsed-text">
        <div class="code">
            <div class="codeFilename"><xsl:value-of select="$filename"/></div>
            <xsl:apply-templates/>
        </div>
    </xsl:template>

    <!-- number lines -->
    <xsl:template match="slop:line">
        <span class="lineNumber">
            <xsl:value-of select="concat(format-number(@line-number,'0000'),' ')"/>
        </span>
        <xsl:value-of select="."/>
        <br/>
    </xsl:template>

    <!-- each slop element takes one line -->
    <xsl:template match="slop:*">
        <xsl:value-of select="."/>
        <br/>
    </xsl:template>

</xsl:stylesheet>
