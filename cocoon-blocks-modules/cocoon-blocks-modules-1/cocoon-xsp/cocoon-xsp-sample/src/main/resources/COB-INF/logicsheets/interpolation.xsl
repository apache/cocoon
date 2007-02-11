<?xml version="1.0"?>
<!--
  Copyright 2005 The Apache Software Foundation

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
 * Logicsheet to demonstrate attribute and text interpolation for Java.
 *
 * SVN $Id$
-->

<xsl:stylesheet
  version="1.0"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:xsp-interpolation="http://apache.org/xsp/interpolation/1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="xsp-interpolation:greeting">
    <!-- Using attribute and text interpolation. -->
    <center style="color:{#color}">Hello logicsheet {#world}!</center>
  </xsl:template>
 
  <xsl:template match="@*|*|text()|processing-instruction()">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
