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
                xmlns:st="http://chaperon.sourceforge.net/schema/syntaxtree/2.0"
                xmlns:text="http://chaperon.sourceforge.net/schema/text/1.0">

 <xsl:template match="st:JAVADOC">
  <xsl:variable name="text"><xsl:value-of select="."/></xsl:variable>
  <text:text>
   <xsl:if test="//st:output/@source">
    <xsl:attribute name="source"><xsl:value-of select="//st:output/@source"/></xsl:attribute>
   </xsl:if>
   <xsl:if test="@line">
    <xsl:attribute name="line"><xsl:value-of select="@line"/></xsl:attribute>
   </xsl:if>
   <xsl:if test="@column">
    <xsl:attribute name="column"><xsl:value-of select="number(@column) + 2"/></xsl:attribute>
   </xsl:if>
   <xsl:value-of select="substring($text,3,string-length($text)-3)"/>
  </text:text>
 </xsl:template>

 <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
  <xsl:copy>
   <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:copy>
 </xsl:template>

</xsl:stylesheet>
