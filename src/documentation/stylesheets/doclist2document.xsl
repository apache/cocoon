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

<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

 <xsl:template match="doclist">
  <document>
   <header>
    <title>List of all documentation</title>
   </header>
   <body>
    <s1 title="Documentation List">
     <p>
      This complete list shows, at a glance, how all of the documentation
      fits together within the hierarchy of sections.
<!--      For an overview of the documentation see the new
      &quot;Table of Contents&quot;. -->
     </p>
     <p>
      The side-panel of each actual document is used to reach other documents
      that are relevant to that section. The side-panel will change, according
      to that section's location in the documentation hierarchy.
     </p>
    </s1>
    <xsl:apply-templates/>
   </body>
  </document>
 </xsl:template>

 <xsl:template match="book">
  <s1 title="{@title}">
   <xsl:if test="position()=1">
    <p>This first section is a list of the top-level documentation
     (and is a replica of this page's side-panel).</p>
   </xsl:if>
   <xsl:apply-templates/>
  </s1>
 </xsl:template>

 <xsl:template match="menu">
  <xsl:if test="@label!='Navigation'">
  <p><strong><xsl:value-of select="@label"/></strong></p>
   <ul>
    <xsl:apply-templates>
     <xsl:with-param name="uri" select="../@uri"/>
    </xsl:apply-templates>
   </ul>
  </xsl:if>
 </xsl:template>

 <xsl:template match="menu-item">
  <xsl:param name="uri"/>
  <xsl:if test="not(@type) or @type!='hidden'">
   <xsl:if test="@label!='Main' and @label!='User Documentation'">
<!-- FIXME: ensure href is not full URL scheme:// -->
<!--
  (uri=<xsl:value-of select="$uri"/> href=<xsl:value-of select="@href"/>)
-->
    <li><link href="{$uri}{@href}"><xsl:value-of select="@label"/></link>
    </li>
   </xsl:if>
  </xsl:if>
 </xsl:template>

 <xsl:template match="external">
  <xsl:param name="uri"/>
  <xsl:if test="not(@type) or @type!='hidden'">
   <xsl:choose>
    <!-- FIXME: specially handle menu item "API (Javadoc)", it causes a bug. -->
    <xsl:when test="starts-with(@label,'API')">
     <li><link href="http://cocoon.apache.org/apidocs/"><xsl:value-of select="@label"/></link></li>
<!-- FIXME: here is the bug:
     <li><link href="{@href}"><xsl:value-of select="@label"/></link></li>
     <li><xsl:value-of select="@label"/>href=<xsl:value-of select="@href"/></li>
-->
    </xsl:when>
    <xsl:otherwise>
     <li><link href="{@href}"><xsl:value-of select="@label"/></link></li>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:if>
 </xsl:template>

</xsl:stylesheet>
