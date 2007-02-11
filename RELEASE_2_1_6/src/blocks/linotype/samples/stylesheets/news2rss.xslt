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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                              xmlns:n="http://www.betaversion.org/linotype/news/1.0"
                              xmlns:h="http://www.w3.org/1999/xhtml"
                              xmlns:dc="http://purl.org/dc/elements/1.1/">
  
  <xsl:template match="element">
   <xsl:apply-templates select="n:news">
  	<xsl:with-param name="id"><xsl:value-of select="@id"/></xsl:with-param>
   </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="h:br" priority="1" mode="encode">
   <xsl:text>&lt;br/&gt;</xsl:text>
  </xsl:template>

  <xsl:template match="h:img" priority="1" mode="encode">
   <xsl:param name="id"/>
   <xsl:text>&lt;img src="</xsl:text><xsl:value-of select="$home"/>/<xsl:value-of select="$id"/>/<xsl:value-of select="@src"/><xsl:text>" width="</xsl:text><xsl:value-of select="@width"/><xsl:text>" height="</xsl:text><xsl:value-of select="@height"/><xsl:text>" vspace="4" hspace="4"/&gt;</xsl:text>
  </xsl:template>
    
  <xsl:template match="*" mode="encode">
    <xsl:param name="id"/>
    <xsl:text>&lt;</xsl:text><xsl:value-of select="name()"/><xsl:apply-templates select="@*" mode="encode"/><xsl:text>&gt;</xsl:text>
    <xsl:apply-templates mode="encode">
      <xsl:with-param name="id" select="$id"/>
    </xsl:apply-templates>
    <xsl:text>&lt;/</xsl:text><xsl:value-of select="name()"/><xsl:text>&gt;</xsl:text>
  </xsl:template>

  <xsl:template match="@*" mode="encode">
   <xsl:text> </xsl:text><xsl:value-of select="name()"/><xsl:text>="</xsl:text><xsl:value-of select="."/><xsl:text>"</xsl:text>
  </xsl:template>

  <xsl:template match="text()" mode="encode">
   <xsl:value-of select="."/>
  </xsl:template>
         
</xsl:stylesheet>
