<?xml version="1.0" encoding="UTF-8"?>
<!--
  Copyright 2002-2004 The Apache Software Foundation

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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:variable name="content" select="/aggregate/content/document/body"/>
  <xsl:variable name="content-header" select="/aggregate/content/document/header"/>
  <xsl:variable name="comments" select="/aggregate/comments"/>
  <xsl:variable name="generated-before" select="/aggregate/generated-before"/>
  <xsl:variable name="generated-after" select="/aggregate/generated-after"/>  
  <xsl:template match="/">
    <document>
      <header>
        <title>
          <xsl:value-of select="$content-header/title"/>
        </title>
      </header>
      <body>
        <xsl:copy-of select="$generated-before/html/body/*"/>
        <xsl:copy-of select="$content/*"/>
        <xsl:copy-of select="$generated-after/html/body/*"/>
        <xsl:apply-templates select="$comments"/>
      </body>
    </document>
  </xsl:template>
  <xsl:template match="comments">
    <section id="Comments">
      <title>Comments</title>
      <div class="commentsarea">
        <div class="commenttaskbar">
          add your comments
        </div>
        <xsl:apply-templates select="comment"/>
      </div>
    </section>
  </xsl:template>
  <xsl:template match="comment">
    <div class="comment">
      <div class="commentheader">
        <xsl:value-of select="@subject"/> by <xsl:value-of select="@name"/>
      </div>
      <div class="commentbody">
        <xsl:copy-of select="*"/>
      </div>
    </div>
  </xsl:template>
</xsl:stylesheet>
