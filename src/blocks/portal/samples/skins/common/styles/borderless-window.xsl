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
<!-- SVN $Id$ -->
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="window">

<xsl:variable name="bgColor">
  <xsl:choose>
    <xsl:when test="@bgColor">
        <xsl:value-of select="@bgColor" />
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>#294563</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
</xsl:variable>

  <table cellSpacing="0" cellpadding="0" width="100%" border="0"><tbody>
    <tr vAlign="top" height="20">
      <td width="5" style="background-color:#294563;" valign="top">
        <img src="images/window-top-left.gif" border="0" width="5" height="5" alt="top-left"/>
      </td>
      <td bgColor="{$bgColor}" align="left" valign="middle">
        <font color="#ffffff" face="Arial" size="2"><b>
          <xsl:choose>
            <xsl:when test="@title">
              <xsl:value-of select="@title"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="title"/>
            </xsl:otherwise>
          </xsl:choose>	
        </b></font>
      </td>
      <td align="right" valign="middle" bgColor="{$bgColor}">
        <table cellSpacing="0" cellpadding="0" border="0"><tbody><tr>
          <xsl:if test="basket-add-link">
            <td width="15">
              <a href="{basket-add-link}">
                <img src="images/basket.gif" border="0" alt="Add Link"/>
              </a>
            </td>
          </xsl:if>
          <xsl:if test="basket-add-content">
            <td width="15">
              <a href="{basket-add-content}">
                <img src="images/basket.gif" border="0" alt="Add Content"/>
              </a>
            </td>
          </xsl:if>
          <xsl:if test="edit-uri">
            <td width="15">
              <a href="{edit-uri}">
                <img src="images/edit.gif" border="0" alt="Edit"/>
              </a>
            </td>
          </xsl:if>
          <xsl:if test="help-uri">
            <td width="15">
              <a href="{help-uri}">
                <img src="images/help.gif" border="0" alt="Help"/>
              </a>
            </td>
          </xsl:if>
          <xsl:if test="view-uri">
            <td width="15">
              <a href="{view-uri}">
                <img src="images/view.gif" border="0" alt="View"/>
              </a>
            </td>
          </xsl:if>
          <xsl:if test="fullscreen-uri and not(maximize-uri)">
            <td width="15">
              <a href="{fullscreen-uri}">
                <img src="images/customize.gif" border="0" alt="Full Screen"/>
              </a>
            </td>
          </xsl:if>
          <xsl:if test="maxpage-uri">
            <td width="15">
              <a href="{maxpage-uri}">
                <img src="images/show.gif" border="0" alt="Max Page"/>
              </a>
            </td>
          </xsl:if>
          <xsl:if test="minpage-uri">
            <td width="15">
              <a href="{minpage-uri}">
                <img src="images/show.gif" border="0" alt="Min Page"/>
              </a>
            </td>
          </xsl:if>
          <xsl:if test="maximize-uri">
            <td width="15">
              <a href="{maximize-uri}">
                <img src="images/maximize.gif" border="0" alt="Maximize"/>
              </a>
            </td>
          </xsl:if>
          <xsl:if test="minimize-uri">
            <td width="15">
              <a href="{minimize-uri}">
                <img src="images/minimize.gif" border="0" alt="Minimize"/>
              </a>
            </td>
          </xsl:if>
          <xsl:if test="remove-uri">
            <td width="15">
              <a href="{remove-uri}">
                <img src="images/delete.gif" border="0" alt="Delete"/>
              </a>
            </td>
          </xsl:if>
        </tr></tbody></table>
      </td>
      <td width="5" style="background-color:#294563;" valign="top">
        <img src="images/window-top-right.gif" border="0" width="5" height="5" alt="top-right"/>
      </td>
      <td width="1"/>
    </tr>
    <tr>
      <td width="5"></td>
      <td colSpan="2">
        <xsl:apply-templates select="content"/>
      </td>
      <td width="6" colSpan="2"></td>
    </tr>
  </tbody></table>
</xsl:template>

<xsl:template match="content">
    <xsl:apply-templates/>
</xsl:template>

<!-- Copy all and apply templates -->
<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
