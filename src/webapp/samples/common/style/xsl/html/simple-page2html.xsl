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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="contextPath"/>
  <xsl:param name="servletPath" select="string('/samples')"/>
  <xsl:param name="sitemapURI"/>

  <xsl:variable name="directory" select="substring-before($servletPath,$sitemapURI)"/>
  <!-- assume that sitemapURIs don't occur in servletPath more than once -->
  <xsl:variable name="sitemap" select="concat($directory,'sitemap.xmap')"/>

  <xsl:template match="page">
   <html>
     <head>
       <title><xsl:value-of select="title"/></title>
       <link rel="stylesheet" href="{$contextPath}/styles/main.css" title="Default Style"/>
       <!-- copy local CSS, if any -->
       <xsl:copy-of select="*[not(name() = 'content')]"/>
     </head>
     <body>
       <xsl:call-template name="resources"/>
       <xsl:apply-templates select="content"/>
     </body>
   </html>
  </xsl:template>

  <xsl:template name="resources">
    <div class="resources">
      <a href="?cocoon-view=content">Content View</a>
      <a href="?cocoon-view=pretty-content">Source</a>
      <a href="{$sitemap}?cocoon-view=pretty-content">Sitemap</a>
      <xsl:for-each select="resources/resource">
        <xsl:variable name="href">
          <xsl:choose>
            <xsl:when test="@type='file'">
              <!-- we need an explicite match in the sitemap showing
                   the source of these resources -->
              <xsl:value-of select="@href"/>
            </xsl:when>
            <xsl:when test="@type='doc'">
              <xsl:value-of select="concat($contextPath, '/docs/', @href)"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="concat($contextPath, '/', @href)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <a class="{@type}" href="{$href}">
          <xsl:apply-templates/>
        </a>
      </xsl:for-each>
    </div>
  </xsl:template>

  <xsl:template match="resources"/>

  <xsl:template match="title">
   <h2>
     <xsl:apply-templates/>
   </h2>
  </xsl:template>

  <xsl:template match="content">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="para">
   <p>
     <xsl:apply-templates/>
   </p>
  </xsl:template>

  <xsl:template match="link">
   <a href="{@href}">
     <xsl:apply-templates/>
   </a>
  </xsl:template>

  <xsl:template match="error">
    <span class="error">
      <xsl:apply-templates/>
    </span>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-2"><xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy></xsl:template>
  <xsl:template match="text()" priority="-1"><xsl:value-of select="."/></xsl:template>
</xsl:stylesheet>
