<?xml version="1.0" encoding="ISO-8859-1"?>
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

<!--
  Version <![CDATA[ $Id: site2html.xsl,v 1.2 2004/03/06 02:26:00 antonio Exp $ ]]>
  
  Transformation of an aggregated site document to html
-->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:page="http://apache.org/cocoon/paginate/1.0"

  exclude-result-prefixes="page"
>

<xsl:template match="site">
  <!--xsl:copy-of select="."/-->

<html>
  <head>
    <title><xsl:value-of select="/site/mid-col-2/header/title"/></title>
    <meta name="keywords" content="Cocoon, Java, IMAP, SMTP, XML, XSL, XSLT, MultiChannel, publishing"/>
    <meta name="description" content="A site mail access via Cocoon."
    />
    <link rel="stylesheet" type="text/css" media="screen" title="" href="main.css"/>
    <link rel="icon" href="favicon.ico"/>
  </head>
  <body>
    <div id="top-section">
      <div id="top-col-1">
        <xsl:apply-templates select="/site/top-col-1/*"/>
      </div>
    </div>

    <div id="head-sectin">
      <div id="head-col-1">
        <xsl:apply-templates select="/site/head-col-1/*"/>
      </div>
    </div>
    
    <div id="mid-section">
      <div id="mid-col-1">
        <xsl:apply-templates select="/site/mid-col-1/*"/>
      </div>
      <div id="mid-col-2">
        <xsl:apply-templates select="/site/mid-col-2/*"/>

        <br/>
        <xsl:apply-templates select="/site/page:page"/>
        
        <br/>
        <xsl:apply-templates select="/site/bottom-col-1/*"/>

      </div>
      <div id="mid-col-3">
        <xsl:apply-templates select="/site/mid-col-3/*"/>
      </div>
    </div>
  </body>
</html>
</xsl:template>

<xsl:template match="header">
</xsl:template>

<xsl:template match="body">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="s1">
  <h1><xsl:value-of select="@title"/></h1>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="s2">
  <h2><xsl:value-of select="@title"/></h2>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="s3">
  <h3><xsl:value-of select="@title"/></h3>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="source">
  <pre><code><xsl:value-of select="."/></code></pre>
</xsl:template>

<xsl:template match="figure">
  <p align="center">
    <xsl:choose>
      <xsl:when test="string(@width) and string(@height)">
        <img src="{@src}" alt="{@alt}" width="{@width}" height="{@height}" border="0" vspace="4" hspace="4"/>
      </xsl:when>
      <xsl:otherwise>
        <img src="{@src}" alt="{@alt}" border="0" vspace="4" hspace="4"/>
      </xsl:otherwise>
    </xsl:choose>
  </p>
</xsl:template>
<xsl:template match="img">
  <img src="{@src}" alt="{@alt}" border="0" vspace="4" hspace="4" align="right"/>
</xsl:template>
<xsl:template match="icon">
  <img src="{@src}" alt="{@alt}" border="0" align="absmiddle"/>
</xsl:template>

<xsl:template match="link">
  <a href="{@href}">
    <xsl:apply-templates/>
  </a>
</xsl:template>

<xsl:template match="jump">
  <a href="{@href}#{@anchor}">
    <xsl:apply-templates/>
  </a>
</xsl:template>

<xsl:template match="anchor">
  <a name="{@id}"/>
</xsl:template>

<xsl:template match="page:page">

  <div class="row">
    <span class="left">
      <xsl:for-each select="page:range-link[@type='prev']">
        [<a href="{@uri}"> <xsl:value-of select="@page"/> </a>]
      </xsl:for-each>
      
      <xsl:for-each select="page:link[@type='prev']">
        [<a href="{@uri}"> <xsl:value-of select="@page"/> </a>]
      </xsl:for-each>
    </span>
    <span class ="right">
      <xsl:for-each select="page:link[@type='next']">
        [<a href="{@uri}"> <xsl:value-of select="@page"/> </a>]
      </xsl:for-each>
      <xsl:for-each select="page:range-link[@type='next']">
        [<a href="{@uri}"><xsl:value-of select="@page"/></a>]
      </xsl:for-each>
    </span>
  </div>
  <div class="spacer"/>
  <div class="row" align="center">
    <xsl:value-of select="@current"/> / <xsl:value-of select="@total"/> 
  </div>
</xsl:template>


<xsl:template match="@*|*|text()" priority="-1">
  <xsl:copy>
    <xsl:apply-templates select="@*|*|text()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
