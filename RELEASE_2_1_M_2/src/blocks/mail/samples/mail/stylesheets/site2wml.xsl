<?xml version="1.0" encoding="ISO-8859-1"?>

<!--
  Version <![CDATA[ $Id: site2wml.xsl,v 1.1 2003/03/09 00:04:48 pier Exp $ ]]>
  
  Transformation of an aggregated site document to wml
-->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:page="http://apache.org/cocoon/paginate/1.0"
  
  exclude-result-prefixes="page"
>

<xsl:template match="site">
  <!--xsl:copy-of select="."/-->
<wml>
  <template>
    <do type="prev" name="prev" label="Zur�ck">
      <prev/>
    </do>
  </template>
  
  <card id="Main">
    <xsl:attribute name="title"><xsl:value-of select="/site/mid-col-2/header/title"/></xsl:attribute>

    <xsl:apply-templates select="/site/mid-col-2/body/*"/>
    <!--xsl:apply-templates select="/site/bottom-col-1/body/*"/-->
    
    <p>
      <xsl:apply-templates select="/site/page:page"/>
    </p>
    
    <p> <a href="#Links">Links</a>
    </p>
  </card>
  
  <card id="Links" title="Links">
    <xsl:apply-templates select="/site/top-col-1"/>

    <xsl:apply-templates select="/site/mid-col-1/body/*"/>
    <xsl:apply-templates select="/site/mid-col-3/body/*"/>
    
  </card>
  
</wml>
</xsl:template>

<xsl:template match="top-col-1">
  <p>
    <em>
      <xsl:value-of select="body/p"/>
    </em>
  </p>
</xsl:template>

<xsl:template match="ul|ol">
  <p>
    <xsl:apply-templates/>
  </p>
</xsl:template>

<xsl:template match="li">
  <xsl:text>&#187;</xsl:text>
  <xsl:apply-templates/>
  <br/>
</xsl:template>

<xsl:template match="s1">
  <p>
    <strong><xsl:value-of select="@title"/></strong>
  </p>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="s2">
  <p>
    <em><xsl:value-of select="@title"/></em>
  </p>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="s3">
  <p>
    <u><xsl:value-of select="@title"/></u>
  </p>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="p">
  <xsl:variable name="pcontent" select="."/>
  <xsl:choose>
    <xsl:when test="string-length($pcontent) &gt; 120">
      <p>
        <xsl:value-of select="substring($pcontent, 1, 120 )"/>
        <xsl:text>...</xsl:text>
      </p>
    </xsl:when>
    <xsl:otherwise>
      <p>
        <xsl:apply-templates/>
      </p>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="p/br">
  <br/>
</xsl:template>

<xsl:template match="blockquote">
  <p> <xsl:text> + </xsl:text>
    <xsl:apply-templates/>
  </p>
</xsl:template>

<xsl:template match="pre|source">
  <p>
    <xsl:text> ... </xsl:text>
  </p>
</xsl:template>

<xsl:template match="link">
  <xsl:variable name="href" select="@href"/>
  <xsl:choose>
    <xsl:when test="not(contains( $href, '.wml' ))">
      <xsl:value-of select="."/> 
      <xsl:text>: </xsl:text> 
      <u><xsl:value-of select="@href"/></u>
    </xsl:when>
    <xsl:otherwise>
      <a><xsl:attribute 
        name="href"><xsl:value-of select="@href"/></xsl:attribute><xsl:value-of 
        select="."/></a>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="jump">
  <a href="{@href}#{@anchor}">
    <xsl:apply-templates/>
  </a>
</xsl:template>

<xsl:template match="page:page">
  <xsl:if test="page:link[@type='prev']">
    [<a href="{page:link[@type = 'prev']/@uri}">Back</a>]
  </xsl:if>
  &#160;
  <xsl:if test="page:link[@type='next']">
    [<a href="{page:link[@type = 'next']/@uri}">Next</a>]
  </xsl:if>
</xsl:template>

</xsl:stylesheet>

