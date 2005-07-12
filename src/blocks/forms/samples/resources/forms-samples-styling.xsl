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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!--+ Include styling stylesheets, one for the widgets, the other one for the
      | page. As 'forms-advanced-field-styling.xsl' is a specialization of
      | 'forms-field-styling.xsl' the latter one is imported there. If you don't
      | want advanced styling of widgets, change it here!
      | See xsl:include as composition and xsl:import as extension/inheritance.
      +-->
  <xsl:include href="resource://org/apache/cocoon/forms/resources/forms-page-styling.xsl"/>
  <xsl:include href="resource://org/apache/cocoon/forms/resources/forms-advanced-field-styling.xsl"/>

  <!-- Location of the resources directory, where JS libs and icons are stored -->
  <xsl:param name="resources-uri">resources</xsl:param>
  
  <xsl:template match="head">
    <head>
      <xsl:apply-templates/>
      <xsl:apply-templates select="." mode="forms-page"/>
      <xsl:apply-templates select="." mode="forms-field"/>
    </head>
  </xsl:template>

  <xsl:template match="body">
    <body>
      <!--+ !!! If template with mode 'forms-page' adds text or elements
          |        template with mode 'forms-field' can no longer add attributes!!!
          +-->
      <xsl:apply-templates select="." mode="forms-page"/>
      <xsl:apply-templates select="." mode="forms-field"/>
      <xsl:apply-templates/>
    </body>
  </xsl:template>

</xsl:stylesheet>
