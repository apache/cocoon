<?xml version="1.0"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<!--
  this is a modified copy of the stylesheet "forms-samples-styling.xsl" that comes with CForms
-->
<!--+ Include styling stylesheets, one for the widgets, the other one for the
    | page. As 'forms-advanced-field-styling.xsl' is a specialization of
    | 'forms-field-styling.xsl' the latter one is imported there. If you don't
    | want advanced styling of widgets, change it here!
    | See xsl:include as composition and xsl:import as extension/inheritance.
    +-->
  <xsl:include href="resource://org/apache/cocoon/forms/resources/forms-page-styling.xsl"/>
  <xsl:include href="resource://org/apache/cocoon/forms/resources/forms-advanced-field-styling.xsl"/>
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
  <xsl:template match="forms-help-image">
    <img src="{$resources-uri}/forms/img/help.gif" alt="helppopup"/>
  </xsl:template>
  <xsl:template match="fi:output" mode="label">
    <xsl:param name="id" select="@id"/>
    <label for="{$id}" title="{fi:hint}">
      <xsl:copy-of select="fi:label/node()"/>
    </label>
  </xsl:template>
</xsl:stylesheet>
