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

 <xsl:import href="copyover.xsl"/>

 <xsl:param name="name"/>

 <xsl:variable name="bugzilla" select="'http://issues.apache.org/bugzilla/'"/>
 <xsl:variable name="singleBug" select="concat($bugzilla, 'show_bug.cgi?id=')"/>
 <xsl:variable name="buglist" select="concat($bugzilla, 'buglist.cgi?bug_id=')"/>

 <xsl:template match="/">
  <xsl:apply-templates select="//changes"/>
 </xsl:template>

 <xsl:template match="changes">
  <document>
   <header>
    <title>History of Changes</title>
   </header>
   <body>
    <xsl:apply-templates/>
   </body>
  </document>
 </xsl:template>

 <xsl:template match="release">
  <s2 title="{$name} {@version} ({@date})">
   <sl>
    <xsl:apply-templates/>
   </sl>
  </s2>
 </xsl:template>

 <xsl:template match="action">
  <li>
   <icon src="images/{@type}.jpg" alt="{@type}"/>
   <xsl:apply-templates/>
   <xsl:text>(</xsl:text><xsl:value-of select="@dev"/><xsl:text>)</xsl:text>

   <xsl:if test="@due-to and @due-to!=''">
    <xsl:text> Thanks to </xsl:text>
    <xsl:choose>
     <xsl:when test="@due-to-email and @due-to-email!=''">
      <link href="mailto:{@due-to-email}">
       <xsl:value-of select="@due-to"/>
      </link>
     </xsl:when>
     <xsl:otherwise>
      <xsl:value-of select="@due-to"/>
     </xsl:otherwise>
    </xsl:choose>
    <xsl:text>.</xsl:text>
   </xsl:if>

   <xsl:if test="@fixes-bug">
    <xsl:text> Fixes </xsl:text>
    <xsl:choose>
     <xsl:when test="contains(@fixes-bug, ',')">
      <link href="{$buglist}{@fixes-bug}">
       <xsl:text>bugs </xsl:text><xsl:value-of select="@fixes-bug"/>
      </link>
     </xsl:when>
     <xsl:otherwise>
      <link href="{$singleBug}{@fixes-bug}">
       <xsl:text>bug </xsl:text><xsl:value-of select="@fixes-bug"/>
      </link>
     </xsl:otherwise>
    </xsl:choose>
    <xsl:text>.</xsl:text>
   </xsl:if>
  </li>
 </xsl:template>

 <xsl:template match="developers|todo">
  <!-- ignore -->
 </xsl:template>

</xsl:stylesheet>
