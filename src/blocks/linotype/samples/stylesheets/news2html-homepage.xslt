<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:include="http://apache.org/cocoon/include/1.0"
  xmlns:n="http://www.betaversion.org/linotype/news/1.0"
  xmlns:h="http://www.w3.org/1999/xhtml"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#default include n h"
>

  <xsl:param name="home"/>

  <xsl:template match="/">
   <xsl:apply-templates select="//n:news"/>
  </xsl:template>
  
  <xsl:template match="n:news">
   <xsl:variable name="id" select="../@id"/>
   <div class="news">
    <h1><img src="images/hand.jpg" alt=""/><xsl:value-of select="n:title"/></h1>
    <h2><xsl:value-of select="@creation-date"/></h2>
    <xsl:apply-templates select="h:body"/>
    <div class="info">Posted at <xsl:value-of select="@creation-time"/> | <a class="permalink" href="{$home}/news/{$id}/">Permalink</a></div>
   </div>
   <xsl:if test="not(position() = last())">
    <div class="separator"><img src="images/separator1.jpg"/></div>
   </xsl:if>
  </xsl:template>

  <xsl:template name="find-id">
   <xsl:param name="node"/>
   <xsl:choose>
    <xsl:when test="$node/@id">
     <xsl:value-of select="$node/@id"/>
    </xsl:when>
    <xsl:when test="not($node)"/>
    <xsl:otherwise>
     <xsl:call-template name="find-id">
      <xsl:with-param name="node" select="$node/.."/>
     </xsl:call-template>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:template>

  <xsl:template match="h:body">
   <div class="body">
    <xsl:apply-templates/>
   </div>
  </xsl:template>

  <xsl:template match="@src" priority="1">
   <xsl:variable name="id"><xsl:call-template name="find-id"><xsl:with-param name="node" select=".."/></xsl:call-template></xsl:variable>
   <xsl:choose>
    <xsl:when test="starts-with(.,'http://')">
     <xsl:attribute name="src">
      <xsl:apply-templates/>
     </xsl:attribute>
    </xsl:when>
    <xsl:otherwise>
     <xsl:attribute name="src">news/<xsl:value-of select="$id"/>/<xsl:value-of select="."/></xsl:attribute>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:template>

  <xsl:template match="h:p[1]">
   <p>
    <span class="firstletter"><xsl:value-of select="substring(text()[1],1,1)"/></span>
    <xsl:value-of select="substring(text()[1],2)"/>
    <xsl:apply-templates select="text()[position() &gt; 1]|@*|*"/>
   </p>
  </xsl:template>

  <xsl:template match="hr">
    <div class="separator"><img src="images/separator2.jpg"/></div>
  </xsl:template>

  <xsl:template match="@*">
   <xsl:copy>
    <xsl:apply-templates/>
   </xsl:copy>
  </xsl:template>

  <xsl:template match="*">
   <xsl:element name="{name()}">
    <xsl:apply-templates select="@*|node()"/>
   </xsl:element>
  </xsl:template>
    
  <xsl:template match="text()">
   <xsl:copy/>
  </xsl:template>

</xsl:stylesheet>
