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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:fb="http://apache.org/cocoon/forms/1.0#binding"
                              xmlns:fd="http://apache.org/cocoon/forms/1.0#definition">

<xsl:template match="node()|@*">
  <xsl:copy>
    <xsl:apply-templates select="node()|@*"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="fb:repeater">
  <xsl:copy>
    <xsl:apply-templates select="@*[not(starts-with(name(), 'unique-'))]"/>
    <xsl:if test="@unique-row-id and @unique-path and
                    not(fb:unique-row | fb:identity)">
      <fb:identity>
        <fb:value id="{@unique-row-id}" path="{@unique-path}">
          <xsl:apply-templates select="fd:convertor"/>
        </fb:value>
      </fb:identity>
    </xsl:if>
    <xsl:apply-templates select="node()[not(self::fd:convertor)]"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="fb:unique-row">
  <xsl:if test="not(../fb:identity)">
    <fb:identity>
      <xsl:apply-templates select="fb:unique-field"/>
    </fb:identity>
  </xsl:if>
</xsl:template>

<xsl:template match="fb:unique-field">
  <fb:value>
    <xsl:apply-templates select="node()|@*"/>
  </fb:value>
</xsl:template>

</xsl:stylesheet>
