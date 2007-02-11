<?xml version="1.0"?>
<!--
  Transforms a form definition file into a binding file. This is an attempt at merging
  these two files in only one.
  To use it, simply load the form bindings from a cocoon: pipeline that applies this stylesheet
  on a form definition. Next step will by to rewrite this directly into Woody's binding system.
  
  The binding is created according to the following rules :
  - wb:* attributes on widget definitions lead to the creation of
    - <wb:context> if a @wb:context attribute is found
    - <wb:simple-repeater> if a @wb:parent-path is found on a <wd:repeater>
    - <wb:value> if a @wb:path is found on any wd:* element    
  - if a <wd:binding> is present, its content is copied as is with the @id of the enclosing widget
  
  @author Sylvain Wallez
  @version CVS $Id: def2binding.xsl,v 1.1 2003/09/08 20:41:12 sylvain Exp $
-->

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:wd="http://apache.org/cocoon/woody/definition/1.0"
  xmlns:wb="http://apache.org/cocoon/woody/binding/1.0">
  
<xsl:template match="wd:*[@wb:context]">
  <wb:context path="{@wb:context}">
    <xsl:for-each select="@*[(local-name(.) != 'context') and (namespace-uri() = 'http://apache.org/cocoon/woody/binding/1.0')]">
      <xsl:attribute name="{local-name(.)}"><xsl:value-of select="."/></xsl:attribute>
    </xsl:for-each>
    <xsl:apply-templates/>
  </wb:context>
</xsl:template>

<xsl:template match="wd:*[@wb:path]">
  <wb:value id="{@id}">
    <xsl:for-each select="@*[namespace-uri() = 'http://apache.org/cocoon/woody/binding/1.0']">
      <xsl:attribute name="{local-name(.)}"><xsl:value-of select="."/></xsl:attribute>
    </xsl:for-each>
    <xsl:apply-templates/>
  </wb:value>
</xsl:template>

<xsl:template match="wd:repeater[@wb:parent-path]">
  <wb:simple-repeater id="{@id}">
    <xsl:for-each select="@*[namespace-uri() = 'http://apache.org/cocoon/woody/binding/1.0']">
      <xsl:attribute name="{local-name(.)}"><xsl:value-of select="."/></xsl:attribute>
    </xsl:for-each>
    <xsl:apply-templates/>
  </wb:simple-repeater>
</xsl:template>

<xsl:template match="wd:*[wd:binding]">
  <!-- copy the binding element -->
  <xsl:variable name="binding" select="wd:binding/wb:*[1]"/>
  <xsl:element name="{local-name($binding)}" namespace="{namespace-uri($binding)}">
    <xsl:copy-of select="$binding/@*"/>
    <xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute>
    <xsl:copy-of select="$binding/node()"/>
    <!-- and recurse in the widgets while in the binding element -->
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<!-- avoid copying text -->
<xsl:template match="text()|@*"/>

</xsl:stylesheet>
