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

<!-- CVS $Id: error2page.xsl,v 1.2 2004/03/06 02:25:33 antonio Exp $ -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:lex="http://chaperon.sourceforge.net/schema/lexer/2.0"
                xmlns:st="http://chaperon.sourceforge.net/schema/syntaxtree/2.0">

 <xsl:param name="contextPath"/>

 <!-- let sitemap override default page title -->
 <xsl:param name="pageTitle" select="//parse-exception/message"/>

 <xsl:template match="header">
  <xsl:copy>
   <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
   <style href="error.css"/>
  </xsl:copy>
 </xsl:template>

 <xsl:template match="st:output[st:error]">
  <row>
   <column title="{st:error/@message}">

    <p class="extra"><span class="description">column&#160;</span><xsl:value-of select="st:error/@column"/></p>
    <p class="extra"><span class="description">line&#160;</span><xsl:value-of select="st:error/@line"/></p>
    <xsl:if test="source">
     <p class="extra"><span class="description">source&#160;</span><xsl:value-of select="@source"/></p>
    </xsl:if>

    <xsl:apply-templates select="st:error"/>

   </column>
  </row>
 </xsl:template>

 <xsl:template match="st:error">
  <xsl:variable name="line" select="number(@line)"/>

  <p class="topped">
   <pre class="error">
    <xsl:apply-templates select="lex:output/lex:lexeme[(number(@line) &lt; $line) and (number(@line) &gt; number($line - 10))]" mode="lt"/>
    <xsl:apply-templates select="lex:output/lex:lexeme[number(@line) = $line]" mode="eq"/>
    <xsl:apply-templates select="lex:output/lex:lexeme[(number(@line) &gt; $line) and (number(@line) &lt; number($line + 10))]" mode="gt"/>
   </pre>
  </p>
 </xsl:template>

 <xsl:template match="lex:lexeme" mode="lt">
  <xsl:value-of select="@line"/>&#160;:&#160;<span class="lt"><xsl:value-of select="@text"/></span>
 </xsl:template>

 <xsl:template match="lex:lexeme" mode="eq">
  <xsl:variable name="column" select="number(../../@column)"/>
  <xsl:value-of select="@line"/>&#160;:&#160;<span class="lt"><xsl:value-of select="substring(@text, 1, $column - 1)"/></span>
  <span class="eq"><xsl:value-of select="substring(@text, $column, 1)"/></span>
  <span class="gt"><xsl:value-of select="substring(@text, $column + 1, string-length(@text) - $column)"/></span>
 </xsl:template>

 <xsl:template match="lex:lexeme" mode="gt">
  <xsl:value-of select="@line"/>&#160;:&#160;<span class="gt"><xsl:value-of select="@text"/></span>
 </xsl:template>

 <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
  <xsl:copy>
   <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:copy>
 </xsl:template>

</xsl:stylesheet>
