<?xml version="1.0" encoding="utf-8"?>
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

<!-- Author: Ovidiu Predescu "ovidiu@cup.hp.com" -->
<!-- Date: July 27, 2001 -->
<!-- Implement the xscript:value-of support -->

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:saxon="http://icl.com/saxon"
  exclude-result-prefixes="xalan saxon">

  <xsl:param name="xpath" select="'/'"/>

  <xsl:template match="/">
    <root>
      <xsl:choose>
        <xsl:when test="contains(system-property('xsl:vendor-url'), 'xalan')">
          <xsl:value-of select="xalan:evaluate($xpath)"/>
        </xsl:when>
        <xsl:when test="contains(system-property('xsl:vendor-url'), 'saxon')">
          <xsl:value-of select="saxon:evaluate($xpath)"/>
        </xsl:when>
      </xsl:choose>
    </root>
  </xsl:template>

</xsl:stylesheet>
