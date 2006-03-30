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
                xmlns:text="http://chaperon.sourceforge.net/schema/text/1.0"
                xmlns:st="http://chaperon.sourceforge.net/schema/syntaxtree/2.0"
                xmlns="http://chaperon.sourceforge.net/schema/grammar/1.0"
                exclude-result-prefixes="st text">

 <xsl:output indent="yes" method="xml" encoding="ASCII"/>

 <xsl:template match="st:output/st:grammar" >
  <grammar><xsl:comment>This file was generated! Don't edit!</xsl:comment>
   <priority>
    <xsl:apply-templates select="st:token_decls/st:token_decl" mode="priority"/>
   </priority>
   <xsl:apply-templates select="st:token_decls/st:token_decl" mode="associativity"/>
   <xsl:apply-templates select="st:production_decls"/>
   <xsl:apply-templates select="st:token_decls/st:start_decl"/>
  </grammar>
 </xsl:template>

 <xsl:template match="st:token_decl" mode="priority">
  <terminal symbol="{st:id}"/>
 </xsl:template>

 <xsl:template match="st:token_decl" mode="associativity">
  <xsl:if test="st:token_decl = '%left'">
   <associativity symbol="{st:id}" type="left"/>
  </xsl:if>
  <xsl:if test="st:token_decl = '%right'">
   <associativity symbol="{st:id}" type="right"/>
  </xsl:if>
 </xsl:template>

 <xsl:template match="st:production_decls" >
  <xsl:for-each select="st:production_decl/st:production_defs/st:production_def">
   <production>
    <xsl:attribute name="symbol"><xsl:value-of select="../../st:id"/></xsl:attribute>
     
    <xsl:if test="st:prec_decl">
     <xsl:attribute name="precedence"><xsl:value-of select="st:prec_decl/st:id"/></xsl:attribute>
    </xsl:if>

    <xsl:apply-templates select="st:ids/st:id"/>
   </production>
  </xsl:for-each>
 </xsl:template>

 <xsl:template match="st:id" >
  <xsl:variable name="symbol" select="text()"/>
  <xsl:choose>
   <xsl:when test="/st:output/st:grammar/st:token_decls/st:token_decl/st:id[.=$symbol]">
    <terminal symbol="{.}"/>
   </xsl:when>
   <xsl:otherwise>
    <nonterminal symbol="{.}"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <xsl:template match="st:start_decl" >
  <start symbol="{st:id}"/>
 </xsl:template>

</xsl:stylesheet>
