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

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:search="http://apache.org/cocoon/search/1.0"
>

  <xsl:template match="search:results">
    <html>
      <head>
        <title>Index2 Search</title>
        <link title="Default Style" href="/styles/main.css" rel="stylesheet"/>
      </head>
      <body bgcolor="white" alink="red" link="blue" vlink="blue">
        <a href="http://jakarta.apache.org/lucene/">
          <img border="0" alt="Lucene Logo" src="images/lucene_green_300.gif"/>
        </a>
        <h1>Index2Search</h1>
        
        <p>
          <small>
            <a href="welcome">Welcome</a> |
            <a href="statistic?indexName=index2">Index2 Statistic</a>
          </small>
        </p>
        
        <form action="">
          <input type="text" name="queryString" size="60" value="{@query-string}"/>
          &#160;
          <input type="submit" name="Search" value="Search"/>
        </form>
        Search Help
        <table cellspacing="2" cellpadding="2">
          <tr bgcolor="#dddedd" valign="top">
          <td width="50%"><font size="-2" >
            <ul>
              <li>free AND "text search"
                Search for documents containing "free" and the 
                phrase "text search"
              </li>
              <li>+text search
                Search for documents containing "text" and 
                preferentially containing "search".
              </li>
              <li>giants -football
                Search for "giants" but omit documents containing "football"
              </li>
            </ul>
          </font></td>
          
          <td><font size="-2">
            <ul>
              <li>body:john
                Search for documents containing "john" in the  body field.
                The field "body" is used by default.
                Thus query "body:john" is equivalent to query "john".
              </li>
              <li>s1@title:cocoon
                Search for documents containing "cocoon" in the
                using field s1@title, ie searching in
                title attribute of s1 element of xml document.
              </li>
            </ul>
          </font></td>
          </tr>
        </table>
        
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="search:hits">
    <p>
      Total Hits <xsl:value-of select="@total-count"/>
      Pages <xsl:value-of select="@count-of-pages"/>
    </p>
    <p>
      Page:
      <xsl:for-each select="/search:results/search:navigation/search:navigation-page">
        <xsl:call-template name="navigation-link"> 
          <xsl:with-param name="query-string" select="/search:results/@query-string"/>
          <xsl:with-param name="page-length" select="/search:results/@page-length"/>
          <xsl:with-param name="start-index" select="@start-index"/>
          <xsl:with-param name="link-text" select="position()"/>
        </xsl:call-template>
      </xsl:for-each>
    </p>
    
    <p>
    <xsl:call-template name="navigation-paging-link">
      <xsl:with-param name="query-string" select="/search:results/@query-string"/>
      <xsl:with-param name="page-length" select="/search:results/@page-length"/>
      <xsl:with-param name="has-previous" select="/search:results/search:navigation/@has-previous"/>
      <xsl:with-param name="has-next" select="/search:results/search:navigation/@has-next"/>
      <xsl:with-param name="previous-index" select="/search:results/search:navigation/@previous-index"/>
      <xsl:with-param name="next-index" select="/search:results/search:navigation/@next-index"/>
    </xsl:call-template>
    </p>
    <hr/>
    
    <table border="1" width="90%" cellpadding="4">
      <tr>
        <td>Score</td><td>Rank</td><td>URI</td>
      </tr>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="search:navigation">
    <p>
    <xsl:call-template name="navigation-paging-form">
      <xsl:with-param name="query-string"><xsl:value-of select="/search:results/@query-string"/></xsl:with-param>
      <xsl:with-param name="page-length"><xsl:value-of select="/search:results/@page-length"/></xsl:with-param>
      <xsl:with-param name="has-previous"><xsl:value-of select="@has-previous"/></xsl:with-param>
      <xsl:with-param name="has-next"><xsl:value-of select="@has-next"/></xsl:with-param>
      <xsl:with-param name="previous-index"><xsl:value-of select="@previous-index"/></xsl:with-param>
      <xsl:with-param name="next-index"><xsl:value-of select="@next-index"/></xsl:with-param>
    </xsl:call-template>
    </p>
  </xsl:template>
  
  <xsl:template match="search:hit">
    <tr>
      <td>
        <xsl:value-of select="format-number( @score, '### %' )"/>
      </td>
      <td>
        <xsl:value-of select="@rank"/>
      </td>
      <td>
        <xsl:variable name="pure-name" select="substring-before(@uri,'.xml')"/>
        <a target="_blank">
          <xsl:attribute name="href">/docs/<xsl:value-of select="$pure-name"/>.html</xsl:attribute>
          <xsl:value-of select="@uri"/>
        </a>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="navigation-paging-form">
    <xsl:param name="query-string"/>
    <xsl:param name="page-length"/>
    <xsl:param name="has-previous"/>
    <xsl:param name="has-next"/>
    <xsl:param name="previous-index"/>
    <xsl:param name="next-index"/>

    <xsl:if test="$has-previous = 'true'">
      <form action="findIt2">
        <input type="hidden" name="startIndex" value="{$previous-index}"/>
        <input type="hidden" name="queryString" value="{$query-string}"/>
        <input type="hidden" name="pageLength" value="{$page-length}"/>
        <input type="submit" name="previous" value="previous"/>
      </form>
    </xsl:if>
    
    <xsl:if test="$has-next = 'true'">
      <form action="findIt2">
        <input type="hidden" name="startIndex" value="{$next-index}"/>
        <input type="hidden" name="queryString" value="{$query-string}"/>
        <input type="hidden" name="pageLength" value="{$page-length}"/>
        <input type="submit" name="next" value="next"/>
      </form>
    </xsl:if>
    
  </xsl:template>

  <xsl:template name="navigation-paging-link">
    <xsl:param name="query-string"/>
    <xsl:param name="page-length"/>
    <xsl:param name="has-previous"/>
    <xsl:param name="has-next"/>
    <xsl:param name="previous-index"/>
    <xsl:param name="next-index"/>

    <xsl:if test="$has-previous = 'true'">
      
      <xsl:call-template name="navigation-link">
        <xsl:with-param name="query-string"><xsl:value-of select="$query-string"/></xsl:with-param>
        <xsl:with-param name="page-length"><xsl:value-of select="$page-length"/></xsl:with-param>
        <xsl:with-param name="start-index"><xsl:value-of select="$previous-index"/></xsl:with-param>
        <xsl:with-param name="link-text">Previous Page Of Hits</xsl:with-param>
      </xsl:call-template>
    </xsl:if>
    &#160;
    <xsl:if test="$has-next = 'true'">
      <a href="findIt2?startIndex={$next-index}&amp;queryString={$query-string}&amp;pageLength={$page-length}">
        Next Page Of Hits
      </a>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="navigation-link">
    <xsl:param name="query-string"/>
    <xsl:param name="page-length"/>
    <xsl:param name="start-index"/>
    <xsl:param name="link-text"/>

    <a href="findIt2?startIndex={$start-index}&amp;queryString={$query-string}&amp;pageLength={$page-length}">
      <xsl:value-of select="$link-text"/>
    </a>
    &#160;
  </xsl:template>

  <xsl:template match="@*|node()" priority="-2"><xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy></xsl:template>
  <xsl:template match="text()" priority="-1"><xsl:value-of select="."/></xsl:template>

</xsl:stylesheet>

