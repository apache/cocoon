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

<!-- convert toc.xml to cinclude instructions to generate a printable version -->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:cinclude="http://apache.org/cocoon/include/1.0"
>
    <xsl:template match="/">
        <page>
            <title>Supersonic Tour of Apache Cocoon</title>
            <content>
                <xsl:apply-templates match="toc-section"/>
            </content>
        </page>
    </xsl:template>

    <xsl:template match="toc-section">
        <h1><xsl:value-of select="@name"/></h1>
        <xsl:apply-templates match="toc-item"/>
    </xsl:template>

    <xsl:template match="toc-item">
        <h2><xsl:value-of select="@name"/></h2>
        <cinclude:include src="{concat('cocoon:/page-content/',@href,'.xml')}"/>
    </xsl:template>
</xsl:stylesheet>
