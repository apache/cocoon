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

<!--+
    | Transforms menu.xml to the navigation bar
    | CVS $Id: menu2navigation.xsl,v 1.1 2004/06/16 20:00:07 vgritsenko Exp $
    +-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="page" />

  <xsl:template match="book">
    <table class="menu">
      <tr>
        <td>
          <ul><xsl:apply-templates select="menu"/></ul>
        </td>
      </tr>
    </table>
  </xsl:template>

  <!-- Process only current book -->
  <xsl:template match="book[@current]">
    <ul><xsl:apply-templates /></ul>
  </xsl:template>

  <!-- Current (open) menu -->
  <xsl:template match="menu">
    <li>
      <xsl:if test="@icon">
        <img src="{@icon}" align="middle"/><xsl:text> </xsl:text>
      </xsl:if>
      <span class="chapter open"><xsl:value-of select="@label" /></span>
    </li>
    <ul><xsl:apply-templates /></ul>
  </xsl:template>

  <!-- Display a link to a page -->

  <xsl:template match="menu-item[@href=$page]">
    <li class="current" title="{@href}">
      <xsl:if test="@icon">
        <img src="{@icon}" align="middle"/><xsl:text> </xsl:text>
      </xsl:if>
      <xsl:value-of select="@label" />
    </li>
  </xsl:template>

  <xsl:template match="menu-item | external">
    <li class="page">
      <xsl:if test="@icon">
        <img src="{@icon}" align="middle"/><xsl:text> </xsl:text>
      </xsl:if>
      <a href="{@href}" class="page"><xsl:value-of select="@label" /></a>
    </li>
  </xsl:template>

</xsl:stylesheet>
