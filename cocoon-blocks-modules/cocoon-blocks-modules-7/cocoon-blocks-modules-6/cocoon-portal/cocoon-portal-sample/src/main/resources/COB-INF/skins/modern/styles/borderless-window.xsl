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
    <xsl:variable name="copletId"><xsl:value-of select="instance-id"/></xsl:variable>
    <div class="coplet-borderless" id="coplet-{$copletId}">
      <div class="innerbox">
        <div class="coplet-header" id="coplet-header-{$copletId}">
          <div class="coplet-title" id="coplet-title-{$copletId}">        
            <xsl:choose>
              <xsl:when test="@title">
                <xsl:value-of select="@title"/>
              </xsl:when>
              <xsl:otherwise>
                 <xsl:value-of select="title"/>
              </xsl:otherwise>
            </xsl:choose> 
          </div>
          <div class="coplet-icons" id="coplet-icons-{$copletId}">
            <xsl:variable name="copletSize"><xsl:value-of select="coplet-size"/></xsl:variable>
            <xsl:if test="edit-uri">
              <div class="coplet-icon edit-uri" id="coplet-icon-edit-uri-{$copletId}">
                <a href="{edit-uri}">
                  <img src="images/edit.gif" border="0" alt="Edit"/>
                </a>
              </div>
            </xsl:if>
            <xsl:if test="help-uri">
              <div class="coplet-icon help-uri" id="coplet-icon-help-uri-{$copletId}">
                <a href="{help-uri}">
                  <img src="images/help.gif" border="0" alt="Help"/>
                </a>
              </div>
            </xsl:if>
            <xsl:if test="view-uri">
              <div class="coplet-icon view-uri" id="coplet-icon-view-uri-{$copletId}">
                <a href="{view-uri}">
                  <img src="images/view.gif" border="0" alt="View"/>
                </a>
              </div>
            </xsl:if>
            <xsl:if test="fullscreen-uri and not(normal-uri)">
              <div class="coplet-icon fullscreen-uri" id="coplet-icon-fullscreen-uri-{$copletId}">
                <a href="{fullscreen-uri}">
                  <img src="images/customize.gif" border="0" alt="Full Screen"/>
                </a>
              </div>
            </xsl:if>
            <xsl:if test="maximize-uri and not(normal-uri)">
              <div class="coplet-icon maximize-uri" id="coplet-icon-maximize-uri-{$copletId}">
                <a href="{maximize-uri}">
                  <img src="images/show.gif" border="0" alt="Maximize"/>
                </a>
              </div>
            </xsl:if>
            <xsl:if test="normal-uri">
              <div class="coplet-icon normal-uri" id="coplet-icon-normal-uri-{$copletId}">
                <xsl:choose>
                  <xsl:when test="fullscreen-uri and maximize-uri">
                    <a href="javascript:cocoon.portal.process('{normal-uri}');">
                      <img src="images/maximize.gif" border="0" alt="Normal"/>
                    </a>
                  </xsl:when>
                  <xsl:otherwise>
                    <a href="{normal-uri}">
                      <img src="images/maximize.gif" border="0" alt="Normal"/>
                    </a>
                  </xsl:otherwise>
                </xsl:choose>
              </div>
            </xsl:if>
            <xsl:if test="minimize-uri">
              <div class="coplet-icon minimize-uri" id="coplet-icon-minimize-uri-{$copletId}">
                <xsl:choose>
                  <xsl:when test="fullscreen-uri and maximize-uri">
                    <a href="javascript:cocoon.portal.process('{minimize-uri}');">
                      <img src="images/minimize.gif" border="0" alt="Minimize"/>
                    </a>
                  </xsl:when>
                  <xsl:otherwise>
                    <a href="{minimize-uri}">
                      <img src="images/minimize.gif" border="0" alt="Minimize"/>
                    </a>
                  </xsl:otherwise>
                </xsl:choose>
              </div>
            </xsl:if>
            <xsl:if test="remove-uri">
              <div class="coplet-icon remove-uri" id="coplet-icon-remove-uri-{$copletId}">
                <a href="{remove-uri}">
                  <img src="images/delete.gif" border="0" alt="Delete"/>
                </a>
              </div>
            </xsl:if>
          </div>
        </div>
        <div class="coplet-content" id="coplet-content-{$copletId}">
          <xsl:apply-templates select="content"/>
        </div>
      </div>
    </div>
  </xsl:template>

  <!-- This is the content of the coplet. We just remove the surrounding tag. -->
  <xsl:template match="content">
    <xsl:copy-of select="*"/>
  </xsl:template>

  <!-- Copy all and apply templates -->
  <xsl:template match="@*|node()">
    <xsl:copy><xsl:apply-templates select="@*|node()" /></xsl:copy>
  </xsl:template>

</xsl:stylesheet>
