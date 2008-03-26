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
  - Convert samples file to the HTML page. Uses styles/main.css stylesheet.
  -
  - $Id$
  -->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="version">2</xsl:param>
  <xsl:param name="year">????</xsl:param>

  <xsl:param name="contextPath">servlet:</xsl:param>

  <xsl:variable name="stdLinks" select="samples/links/link[not(@role)]"/>
  <xsl:variable name="seeAlsoLinks" select="samples/links/link[@role='see-also']"/>


  <xsl:template match="/">
    <html>
      <head>
        <title>
          <xsl:text>Apache Cocoon </xsl:text><xsl:value-of select="$version"/>
          <xsl:if test="samples/@name">
            <xsl:text> | </xsl:text>
            <xsl:value-of select="samples/@name"/>
          </xsl:if>
        </title>
        <link rel="SHORTCUT ICON" href="{$contextPath}/icons/cocoon.ico"/>
        <link href="{$contextPath}/styles/main.css" type="text/css" rel="stylesheet"/>
      </head>
      <body>
        <div id="top">
          <div id="header">
            <div class="projectlogo">
              <a href="http://cocoon.apache.org/"><img class="logoImage" src="{$contextPath}/images/cocoon-logo.jpg" alt="Apache Cocoon" border="0"/></a>
            </div>
            <div class="grouplogo">
              <p class="grouptitle">
                <a href="http://cocoon.apache.org/">The Apache Cocoon Project</a>
                <img src="{$contextPath}/images/apache-logo.jpg" alt="Cocoon Project Logo"/>
              </p>
            </div>
          </div>
          <div id="samplesBar">
            <h1 class="samplesTitle"><xsl:value-of select="samples/@name"/></h1>
            <ul id="links">
              <xsl:if test="$seeAlsoLinks">
                <li class="sep">See also:</li>
                <xsl:apply-templates select="$seeAlsoLinks"/>
              </xsl:if>
              <xsl:if test="$stdLinks">
                <li class="sep"/>
                <xsl:apply-templates select="$stdLinks"/>
              </xsl:if>
              <xsl:if test="not(samples/@add-view-links='false')">
                <li class="sep">Views:</li>
                <li><a href="?cocoon-view=content">Content</a></li>
                <li><a href="?cocoon-view=pretty-content">Pretty content</a></li>
                <li><a href="?cocoon-view=links">Links</a></li>
              </xsl:if>
            </ul>
          </div>
          <div class="samplesBarClear"/>
        </div>

        <xsl:apply-templates select="samples"/>
        <xsl:apply-templates select="samples/additional-info"/>
       
        <p class="copyright">
          <xsl:text>Copyright &#169; </xsl:text><xsl:value-of select="$year"/><xsl:text> </xsl:text>
          <a href="http://www.apache.org/">The Apache Software Foundation</a>. All rights reserved.
        </p>
      </body>
    </html>
  </xsl:template>


  <xsl:template match="samples">
    <xsl:variable name="gc" select="4"/><!-- group correction -->
    <xsl:variable name="all-groups" select="$gc * count(group)"/>
    <xsl:variable name="all-samples" select="count(group/sample)+count(group/note)+$all-groups"/>
    <xsl:variable name="half-samples" select="round($all-samples div 2)"/>
    <xsl:variable name="half-possibilities">
      <xsl:choose>
        <xsl:when test="count(group) = 1">1 </xsl:when><!-- single group sample.xml -->
        <xsl:otherwise>
          <xsl:for-each select="group">
            <xsl:if test="position() &lt; last() and position() &gt;= 1">
              <xsl:variable name="group-position" select="position()"/>
              <xsl:variable name="prev-sample" select="count(../group[position() &lt;= $group-position - 1]/sample) + count(../group[position() &lt;= $group-position - 1]/note) + position() * $gc - $gc"/>
              <xsl:variable name="curr-sample" select="count(../group[position() &lt;= $group-position]/sample) + count(../group[position() &lt;= $group-position]/note) + position() * $gc"/>
              <xsl:variable name="next-sample" select="count(../group[position() &lt;= $group-position + 1]/sample) + count(../group[position() &lt;= $group-position + 1]/note) + position() * $gc + $gc"/>
              <xsl:variable name="prev-deviation">
                <xsl:choose>
                  <xsl:when test="$prev-sample &gt; $half-samples">
                    <xsl:value-of select="$prev-sample - $half-samples"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$half-samples - $prev-sample"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:variable name="curr-deviation">
                <xsl:choose>
                  <xsl:when test="$curr-sample &gt; $half-samples">
                    <xsl:value-of select="$curr-sample - $half-samples"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$half-samples - $curr-sample"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:variable name="next-deviation">
                <xsl:choose>
                  <xsl:when test="$next-sample &gt; $half-samples">
                    <xsl:value-of select="$next-sample - $half-samples"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$half-samples - $next-sample"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:if test="$prev-deviation &gt;= $curr-deviation and $curr-deviation &lt;= $next-deviation">
                <xsl:value-of select="$group-position"/><xsl:text> </xsl:text>
              </xsl:if>
            </xsl:if>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="half">
      <xsl:value-of select="substring-before($half-possibilities, ' ')"/>
    </xsl:variable>

    <table width="100%" cellspacing="5">
      <tr>
        <td width="50%" valign="top">
          <xsl:for-each select="group">
            <xsl:variable name="group-position" select="position()"/>
            <xsl:choose>
              <xsl:when test="$group-position &lt;= $half">
                <h4 class="samplesGroup"><xsl:value-of select="@name"/></h4>
                <p class="samplesText"><xsl:apply-templates/></p>
              </xsl:when>
              <xsl:otherwise/>
            </xsl:choose>
          </xsl:for-each>
        </td>
        <td valign="top">
          <xsl:for-each select="group">  <!-- [position()<=$half] -->
            <xsl:variable name="group-position" select="position()"/>
            <xsl:choose>
              <xsl:when test="$group-position &gt; $half">
                <h4 class="samplesGroup"><xsl:value-of select="@name"/></h4>
                <p class="samplesText"><xsl:apply-templates/></p>
              </xsl:when>
              <xsl:otherwise/>
            </xsl:choose>
          </xsl:for-each>
        </td>
      </tr>
    </table>
  </xsl:template>


  <xsl:template match="sample">
    <xsl:choose>
      <xsl:when test="string-length(@href) &gt; 0">
        <a href="{@href}"><xsl:value-of select="@name"/></a>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@name"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text> - </xsl:text>
    <xsl:copy-of select="*|text()"/>
    <br/>
  </xsl:template>


  <xsl:template match="note">
    <p class="samplesNote">
      <xsl:apply-templates/>
    </p>
  </xsl:template>


  <xsl:template match="links/link">
    <li><a href="{@href}"><xsl:value-of select="."/></a></li>
  </xsl:template>

  <xsl:template match="additional-info">
    <h4><xsl:value-of select="@title"/></h4>
    <xsl:copy-of select="node()"/>
  </xsl:template>


  <xsl:template match="@*|node()" priority="-2">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="text()" priority="-1">
    <xsl:value-of select="."/>
  </xsl:template>
  
</xsl:stylesheet>
