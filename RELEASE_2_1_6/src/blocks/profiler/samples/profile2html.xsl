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

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:profile="http://apache.org/cocoon/profiler/1.0">

  <xsl:param name="sort"/>
  <xsl:param name="key"/>
  <xsl:param name="result"/>
  <xsl:param name="component"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>Cocoon2 profile information [<xsl:value-of select="profile:profilerinfo/@date"/>]</title>
      </head>
      <body>
        <xsl:choose>
          <xsl:when test="$component!=''">
            <xsl:apply-templates
              select="profile:profilerinfo/profile:pipeline/profile:result/profile:component[@index=$component]"
              mode="fragment"/>
          </xsl:when>
          <xsl:when test="$result!=''">
            <xsl:apply-templates select="profile:profilerinfo/profile:pipeline/profile:result" mode="result"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="profile:profilerinfo" mode="pipelines"/>
          </xsl:otherwise>
        </xsl:choose>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="profile:profilerinfo" mode="pipelines">
    Sort results by <a href="?sort=uri">uri</a>,
    <a href="?sort=count">count</a>, <a href="?sort=time">time</a>.

    <table noshade="noshade" border="0" cellspacing="1" cellpadding="0">
      <xsl:choose>
        <xsl:when test="$sort = 'uri'">
          <xsl:apply-templates select="profile:pipeline">
            <xsl:sort select="@uri"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:when test="$sort = 'time'">
          <xsl:apply-templates select="profile:pipeline">
            <xsl:sort select="@time" data-type="number"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:when test="$sort = 'count'">
          <xsl:apply-templates select="profile:pipeline">
            <xsl:sort select="@count" data-type="number"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates/>
        </xsl:otherwise>
      </xsl:choose>
    </table>
  </xsl:template>

  <xsl:template match="profile:pipeline">
    <xsl:if test="position() mod 5 = 1">
      <tr bgcolor="#FFC0C0">
       <th></th>
       <th>Component</th>
       <th>Average</th>
       <th colspan="10">Last Results</th>
      </tr>
    </xsl:if>
    <tr bgcolor="#C0C0FF">
     <td colspan="3">
       <font face="verdana"><strong><xsl:value-of select="@uri"/></strong></font>
       (<xsl:value-of select="@count"/> results,
       total time: <xsl:value-of select="@processingTime"/>,
       average time: <xsl:value-of select="profile:average/@time"/>)
     </td>
     <xsl:for-each select="profile:result">
      <td>
       <a href="?key={../@key}&amp;result={@index}">
        <xsl:value-of select="@index"/>
       </a>
      </td>
     </xsl:for-each>
    </tr>

    <xsl:for-each select="profile:average/profile:component">
      <xsl:variable name="bgcolor">
       <xsl:if test="position() mod 2 = 0">#D0D0D0</xsl:if>
       <xsl:if test="position() mod 2 = 1">#E0E0E0</xsl:if>
      </xsl:variable>
      <tr bgcolor="{$bgcolor}">

       <xsl:variable name="pos" select="position()"/>
       <td>
        <xsl:value-of select="$pos"/>
       </td>
       <td>
         <xsl:value-of select="@role"/>
         <xsl:text>&#160;</xsl:text>
         <xsl:if test="@source">
           <i>src=</i><xsl:value-of select="@source"/>
         </xsl:if>
       </td>

       <xsl:for-each select="../../profile:average/profile:component[position()=$pos]">
        <th>
         <xsl:value-of select="@time"/>
        </th>
       </xsl:for-each>

       <xsl:for-each select="../../profile:result/profile:component[position()=$pos]">
        <td>
          <xsl:value-of select="@time"/>
        </td>
       </xsl:for-each>

      </tr>
    </xsl:for-each>

       <xsl:variable name="pos" select="count(profile:average/profile:component)"/>
      <tr>
       <td>
        <xsl:value-of select="$pos+1"/>
       </td>
       <td>
        TOTAL
       </td>

        <th>
         <xsl:value-of select="profile:average/@time"/>
        </th>

       <xsl:for-each select="profile:result">
        <td>
         <xsl:value-of select="@time"/>
        </td>
       </xsl:for-each>

      </tr>
  </xsl:template>

  <xsl:template match="profile:result" mode="result">
    <h1><xsl:value-of select="profile:environmentinfo/profile:uri"/></h1>
    <table>
      <tr bgcolor="#FFC0C0">
        <th></th>
        <th>Component</th>
        <th colspan="3">Results</th>
      </tr>
      <tr bgcolor="#C0C0FF">
        <td colspan="2">
          <font face="verdana"><strong><xsl:value-of select="profile:environmentinfo/profile:uri"/></strong></font>
        </td>
        <td>Total Time</td>
        <td>Setup Time</td>
        <td>Processing Time</td>
        <td/>
      </tr>

      <xsl:for-each select="profile:component">
        <xsl:variable name="bgcolor">
          <xsl:if test="position() mod 2 = 0">#D0D0D0</xsl:if>
          <xsl:if test="position() mod 2 = 1">#E0E0E0</xsl:if>
        </xsl:variable>
        <tr bgcolor="{$bgcolor}">

          <xsl:variable name="pos" select="position()"/>
          <td>
            <xsl:value-of select="$pos"/>
          </td>
          <td>
            <xsl:value-of select="@role"/>
            <xsl:text>&#160;</xsl:text>
            <xsl:if test="@source">
              <i>src=</i><xsl:value-of select="@source"/>
            </xsl:if>
          </td>

          <td>
            <xsl:value-of select="@time"/>
          </td>

          <td>
            <xsl:value-of select="@setup"/>
          </td>

          <td>
            <xsl:value-of select="@processing"/>
          </td>

          <td>
            <xsl:if test="position() != last()">
              <a href="profile.xml?key={$key}&amp;result={$result}&amp;component={@index}&amp;fragmentonly=true">[XML]</a>
              &#160;&#160;
              <a href="profile.xml?key={$key}&amp;result={$result}&amp;component={@index}&amp;fragmentonly=true&amp;cocoon-view=pretty-content">[XML as HTML]</a>
            </xsl:if>
          </td>
        </tr>
      </xsl:for-each>
    </table>
    <xsl:apply-templates select="profile:environmentinfo"/>
  </xsl:template>

  <xsl:template match="profile:environmentinfo">
   <xsl:apply-templates select="profile:request-parameters"/>
   <xsl:apply-templates select="profile:session-attributes"/>
  </xsl:template>

  <xsl:template match="profile:request-parameters">
    <table>
      <tr bgcolor="#C0C0FF">
       <th colspan="2">
        Request parameters
       </th>
      </tr>
      <tr bgcolor="#FFC0C0">
        <th>Name</th>
        <th>Value</th>
      </tr>
      <xsl:for-each select="profile:parameter">
        <xsl:variable name="bgcolor">
          <xsl:if test="position() mod 2 = 0">#D0D0D0</xsl:if>
          <xsl:if test="position() mod 2 = 1">#E0E0E0</xsl:if>
        </xsl:variable>

        <tr bgcolor="{$bgcolor}">
          <td><xsl:value-of select="@name"/></td>
          <td><xsl:value-of select="@value"/></td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="profile:session-attributes">
    <table>
      <tr bgcolor="#C0C0FF">
       <th colspan="2">
        Session attributes
       </th>
      </tr>
      <tr bgcolor="#FFC0C0">
        <th>Name</th>
        <th>Value</th>
      </tr>
      <xsl:for-each select="profile:attribute">
        <xsl:variable name="bgcolor">
          <xsl:if test="position() mod 2 = 0">#D0D0D0</xsl:if>
          <xsl:if test="position() mod 2 = 1">#E0E0E0</xsl:if>
        </xsl:variable>

        <tr bgcolor="{$bgcolor}">
          <td><xsl:value-of select="@name"/></td>
          <td><xsl:value-of select="@value"/></td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

</xsl:stylesheet>
