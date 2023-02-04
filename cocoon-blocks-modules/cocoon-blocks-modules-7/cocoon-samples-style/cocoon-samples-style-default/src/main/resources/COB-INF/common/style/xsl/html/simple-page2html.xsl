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
  - $Id$
  -->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="version">2</xsl:param>
  <xsl:param name="year">????</xsl:param>

  <xsl:param name="contextPath">servlet:/</xsl:param>

  <xsl:template match="page">
   <html>
   <head>
     <title>
       <xsl:text>Apache Cocoon </xsl:text><xsl:value-of select="$version"/>
       <xsl:if test="title">
         <xsl:text> | </xsl:text>
         <xsl:value-of select="title"/>
       </xsl:if>
     </title>
     <link rel="SHORTCUT ICON" href="{$contextPath}/icons/cocoon.ico"/>
     <link href="{$contextPath}/styles/main.css" type="text/css" rel="stylesheet" title="Default Style"/>
     <!-- copy local CSS, if any -->
     <xsl:copy-of select="style"/>
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
         <h1 class="samplesTitle"><xsl:value-of select="title"/></h1>
         <xsl:call-template name="resources"/>
       </div>
       <div class="samplesBarClear"/>
     </div>

     <xsl:apply-templates/>

     <p class="copyright">
       <xsl:text>Copyright &#169; </xsl:text><xsl:value-of select="$year"/><xsl:text> </xsl:text>
       <a href="http://www.apache.org/">The Apache Software Foundation</a>. All rights reserved.
     </p>
   </body>
   </html>
  </xsl:template>

  <xsl:template name="resources">
    <ul id="links">
      <xsl:for-each select="resources/resource">
        <xsl:variable name="href">
          <xsl:choose>
            <xsl:when test="@type='file'">
              <!-- we need an explicite match in the sitemap showing
                   the source of these resources -->
              <xsl:value-of select="@href"/>
            </xsl:when>
            <xsl:when test="@type='doc'">
              <xsl:value-of select="concat('docs/', @href)"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="@href"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <li><a class="{@type}" href="{$href}"><xsl:apply-templates/></a></li>
      </xsl:for-each>
      <li class="sep">See also:</li>
      <li><a href="sitemap.xmap">Sitemap</a></li>
      <li class="sep">Views:</li>
      <li><a href="?cocoon-view=content">Content</a></li>
      <li><a href="?cocoon-view=pretty-content">Pretty content</a></li>
      <li><a href="?cocoon-view=links">Links</a></li>
    </ul>
  </xsl:template>


  <xsl:template match="resources"/>
  <xsl:template match="title"/>
  <xsl:template match="style"/>


  <xsl:template match="content">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="content[row]">
    <table width="100%">
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="row">
    <tr>
      <xsl:apply-templates select="column"/>
    </tr>
  </xsl:template>

  <xsl:template match="column">
    <td valign="top">
      <h4 class="samplesGroup"><xsl:value-of select="@title"/></h4>
      <p class="samplesText"><xsl:apply-templates/></p>
    </td>
  </xsl:template>
  
  <xsl:template match="para">
    <p><xsl:apply-templates/></p>
  </xsl:template>


  <xsl:template match="link">
    <a href="{@href}"><xsl:apply-templates/></a>
  </xsl:template>

  <xsl:template match="anchor">
    <a name="{@name}"><xsl:apply-templates/></a>
  </xsl:template>

  <xsl:template match="error">
    <span class="error"><xsl:apply-templates/></span>
  </xsl:template>


  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
