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

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="locale"/>
  <xsl:param name="page"/>

  <xsl:template match="composite">
    <page>
      <title>Internationalization (i18n) and Localization (l10n)</title>
      <content>
        <row>
          <column title="Menu">
            <xsl:apply-templates select="book"/>
          </column>
          <column title="{page/title}">
            <xsl:apply-templates select="page"/>
          </column>
        </row>
      </content>
    </page>
  </xsl:template>

  <xsl:template match="page">
    <h5><xsl:value-of select="sub-title"/></h5>
    <xsl:apply-templates select="form"/>

    <xsl:apply-templates select="content"/>

    <hr align="left" noshade="noshade" size="1"/>
    <i><xsl:apply-templates select="annotation"/></i>
  </xsl:template>

  <xsl:template match="book">
    <xsl:apply-templates select="menu"/>
  </xsl:template>

  <xsl:template match="para">
    <p>
      <b><xsl:number format="0. "/><xsl:value-of select="@name"/></b>
      <xsl:text> </xsl:text>
      <xsl:value-of select="@title"/>
      <br/>
      <xsl:apply-templates select="text() | strong | i"/>
    </p>
  </xsl:template>

  <xsl:template match="menu">
    <xsl:if test="@icon">
      <img src="{@icon}" align="middle"/>
      <xsl:text> </xsl:text>
    </xsl:if>
    <h3><xsl:value-of select="@label"/></h3>
    <ul><xsl:apply-templates/></ul>
  </xsl:template>

  <!-- Display a link to a page -->
  <xsl:template match="menu-item[substring-after(@href, 'locale=') = $locale or @href=$page or (@href='' and $locale='')]">
    <li title="{@href}">
      <xsl:if test="@icon">
        <img src="{@icon}" align="middle"/>
        <xsl:text> </xsl:text>
      </xsl:if>
      <xsl:value-of select="@label"/>
    </li>
  </xsl:template>

  <xsl:template match="menu-item">
    <li class="page">
      <xsl:if test="@icon">
        <img src="{@icon}" align="middle"/>
        <xsl:text> </xsl:text>
      </xsl:if>
      <a href="{@href}"><xsl:value-of select="@label"/></a>
    </li>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
