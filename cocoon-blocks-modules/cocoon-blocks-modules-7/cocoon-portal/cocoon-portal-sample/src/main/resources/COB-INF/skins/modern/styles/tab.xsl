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

  <!-- Process a tab  -->
  <xsl:template match="tab-layout">
    <div class="tab-layout">
      <ul>
        <xsl:for-each select="named-item">
          <xsl:choose>
            <xsl:when test="@selected">
              <li class="tab-selected"><a href="#"><xsl:value-of select="@name"/></a></li>
            </xsl:when>
            <xsl:otherwise>
              <li class="tab"><a href="{@parameter}"><xsl:value-of select="@name"/></a></li>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </ul>
    </div>
    <div class="tab-layout-clear">&#160;</div>
    <xsl:apply-templates select="named-item"/>
  </xsl:template>

  <xsl:template match="named-item">
    <xsl:copy-of select="*"/>
  </xsl:template>

  <!-- Copy all and apply templates -->
  <xsl:template match="@*|node()">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
  </xsl:template>

</xsl:stylesheet>
