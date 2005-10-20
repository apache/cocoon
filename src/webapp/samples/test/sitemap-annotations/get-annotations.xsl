<?xml version="1.0" encoding="UTF-8"?>
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
  Extract annotations from sitemap
  $Id: dir-links.xsl 36225 2004-08-11 14:36:46Z vgritsenko $
 -->

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fyi="http://apache.org/cocoon/sitemap/annotations/1.0"
  exclude-result-prefixes="fyi"
  >

  <xsl:template match="/">
    <sitemap-annotations>
      <xsl:apply-templates/>
    </sitemap-annotations>
  </xsl:template>

  <!-- copy fyi elements, without namespace -->
  <xsl:template match="fyi:*">
    <xsl:element name="{local-name()}">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
