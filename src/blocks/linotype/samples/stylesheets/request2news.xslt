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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:r="http://apache.org/cocoon/request/2.0"
                              xmlns:h="http://www.w3.org/1999/xhtml"
                              xmlns="http://www.w3.org/1999/xhtml"
                              exclude-result-prefixes="h r">

  <xsl:template match="/">
   <xsl:apply-templates select="//r:requestParameters"/>
  </xsl:template>

  <xsl:template match="r:requestParameters">
   <n:news online="{r:parameter[@name='online']/r:value}" xmlns:n="http://www.betaversion.org/linotype/news/1.0">
     <xsl:attribute name="author"><xsl:value-of select="r:parameter[@name='author']/r:value"/></xsl:attribute>
     <xsl:attribute name="creation-date"><xsl:value-of select="r:parameter[@name='date']/r:value"/></xsl:attribute>
     <xsl:attribute name="creation-time"><xsl:value-of select="r:parameter[@name='time']/r:value"/></xsl:attribute>
     <xsl:attribute name="creation-fulldate"><xsl:value-of select="r:parameter[@name='fulldate']/r:value"/></xsl:attribute>
     <n:title><xsl:value-of select="r:parameter[@name='title']/r:value"/></n:title>
     <xsl:apply-templates select="r:parameter[@name='xml:content']/r:value/h:html/h:body"/>
   </n:news>
  </xsl:template>

  <xsl:template match="h:b">
   <strong><xsl:apply-templates/></strong>
  </xsl:template>

  <xsl:template match="h:i">
   <em><xsl:apply-templates/></em>
  </xsl:template>

  <!-- This template filters out unnecessary declarations of the h namespace -->
  <xsl:template match="h:*">
   <xsl:element name="{name(.)}" namespace="http://www.w3.org/1999/xhtml">
    <xsl:apply-templates select="node()|@*"/>
   </xsl:element>
  </xsl:template>

  <xsl:template match="node()|@*">
   <xsl:copy>
    <xsl:apply-templates select="node()|@*"/>
   </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
