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
  <xsl:param name="contextPath"/>
  <xsl:param name="servletPath" select="string('/samples')"/>
  <xsl:param name="sitemapURI"/>
  <xsl:variable name="directory" select="substring-before($servletPath,$sitemapURI)"/>
  <!-- assume that sitemapURIs don't occur in servletPath more than once -->
  <xsl:variable name="sitemap" select="concat($directory,'sitemap.xmap')"/>
  <xsl:template match="body">
    <div style="text-align:right;width:100%;">
      <a href="?cocoon-view=content">Content View</a> |
      <a href="?cocoon-view=pretty-content">Source</a> |
      <a href="{$sitemap}?cocoon-view=pretty-content">Sitemap</a>
    </div>
    <xsl:apply-templates/>
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
