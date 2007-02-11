<?xml version="1.0"?>
<!--
  Copyright 1999-2005 The Apache Software Foundation

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
<!-- $Id$ 

-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:java="http://xml.apache.org/xalan/java">

<!-- The current picture (index) to display -->
<xsl:param name="pic"/>
<!-- Is this full screen? -->
<xsl:param name="fullscreen"/>

<xsl:template match="pictures" xmlns:cl="http://apache.org/cocoon/portal/coplet/1.0">
  <xsl:variable name="maxp" select="count(picture)"/>

  <xsl:choose>
    <xsl:when test="$fullscreen='true'">
      <!-- This is the two column version: 
      <table><tbody>
        <xsl:for-each select="picture">
          <xsl:if test="position() mod 2 = 1">
            <tr>        
              <td><img src="{.}"/></td>
              <xsl:choose>
                <xsl:when test="position() = last()">
                  <td>&#160;</td>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:variable name="p" select="position()+1"/>
                  <td><img src="{//picture[position()=$p]}"/></td>
                </xsl:otherwise>
              </xsl:choose>
            </tr>
          </xsl:if>
        </xsl:for-each>
      </tbody></table>
      -->
      <!-- And this is the simple version -->
      <table><tbody>
        <tr width="100%">
          <td>
           <xsl:for-each select="picture">
             <img src="{.}"/><xsl:text> </xsl:text>
           </xsl:for-each>
          </td>
        </tr>
      </tbody></table>
      <p>Date: <xsl:value-of select="java:java.util.Date.new()"/></p>
    </xsl:when>
    <xsl:otherwise>
      <xsl:variable name="picn">
        <xsl:choose>
          <xsl:when test="$pic=$maxp">1</xsl:when>
          <xsl:when test="$pic=''">2</xsl:when>
          <xsl:otherwise><xsl:value-of select="$pic+1"/></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="picp">
        <xsl:choose>
          <xsl:when test="$pic=1 or $pic=''"><xsl:value-of select="$maxp"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="$pic - 1"/></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="showpicindex">
        <xsl:choose>
          <xsl:when test="$pic=1 or $pic=''">1</xsl:when>
          <xsl:otherwise><xsl:value-of select="$pic"/></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <p>Picture <xsl:value-of select="$showpicindex"/> of <xsl:value-of select="$maxp"/>
        <xsl:if test="$showpicindex &gt; 1">
          - <cl:link path="attributes/picture" value="{$picp}" coplet="Gallery-Petstore">&#xAB; Previous</cl:link>
        </xsl:if>
        <xsl:if test="$showpicindex &lt; $maxp">
          - <cl:link path="attributes/picture" value="{$picn}" coplet="Gallery-Petstore">Next &#xBB;</cl:link>
        </xsl:if>
      </p>
      <p><cl:link path="attributes/picture" value="{picture[position()=$showpicindex]}" coplet="GalleryViewer-1">Push to Viewer</cl:link></p>
      <img src="{picture[position()=$showpicindex]}"/>
      <p>Date: <xsl:value-of select="java:java.util.Date.new()"/></p>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
</xsl:stylesheet>
