<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:text="http://chaperon.sourceforge.net/schema/text/1.0"
                xmlns:st="http://chaperon.sourceforge.net/schema/syntaxtree/2.0"
                xmlns="http://chaperon.sourceforge.net/schema/grammar/2.0"
                exclude-result-prefixes="st text">

 <xsl:output indent="yes" method="xml" encoding="ASCII"/>

 <xsl:template match="st:output/st:grammar">
  <grammar>

   <xsl:if test="not (st:start)">
    bla
    <xsl:message terminate="yes">
     Start element is not defined!
    </xsl:message>
   </xsl:if>

   <xsl:attribute name="start"><xsl:value-of select="st:start/st:name"/></xsl:attribute>

   <xsl:comment>This file was generated! Don't edit!</xsl:comment>

   <xsl:apply-templates select="st:definition"/>
  </grammar>
 </xsl:template>

 <xsl:template match="st:definition">
  <definition>
   <xsl:attribute name="name"><xsl:value-of select="st:name"/></xsl:attribute>
     
   <xsl:apply-templates select="st:regex"/>
  </definition>
 </xsl:template>

 <xsl:template match="st:regex">
  <xsl:apply-templates select="st:choice|st:sequence|st:quantifier"/>
 </xsl:template>

 <xsl:template match="st:element" >
  <xsl:variable name="name" select="."/>
  <xsl:choose>
   <xsl:when test="/st:output/st:grammar/st:abbreviation[st:name=$name]">
    <xsl:apply-templates select="/st:output/st:grammar/st:abbreviation[st:name=$name]/st:regex"/>
   </xsl:when>
   <xsl:otherwise>
    <element name="{$name}"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <xsl:template match="st:choice" >
  <choice>
   <xsl:apply-templates select="st:sequence|st:quantifier"/>
  </choice>
 </xsl:template>

 <xsl:template match="st:sequence">
  <sequence>
   <xsl:apply-templates select="st:quantifier"/>
  </sequence>
 </xsl:template>

 <xsl:template match="st:quantifier" >
  <xsl:apply-templates select="st:optional|st:one-or-more|st:zero-or-more|st:term"/>
 </xsl:template>

 <xsl:template match="st:optional" >
  <optional>
   <xsl:apply-templates select="st:term"/>
  </optional>
 </xsl:template>

 <xsl:template match="st:one-or-more">
  <one-or-more>
   <xsl:apply-templates select="st:term"/>
  </one-or-more>
 </xsl:template>

 <xsl:template match="st:zero-or-more">
  <zero-or-more>
   <xsl:apply-templates select="st:term"/>
  </zero-or-more>
 </xsl:template>

<!-- <xsl:template match="st:regexvar">
  <xsl:choose>
   <xsl:when test="count(st:regexmultiplicator/st:string)=2">
    <concat>
     <xsl:attribute name="minOccurs"><xsl:value-of select="normalize-space(st:regexmultiplicator/st:string[1])"/></xsl:attribute>
     <xsl:attribute name="maxOccurs"><xsl:value-of select="normalize-space(st:regexmultiplicator/st:string[2])"/></xsl:attribute>
     <xsl:apply-templates select="st:regexterm"/>
    </concat>
   </xsl:when>
   <xsl:otherwise>
    <concat>
     <xsl:attribute name="minOccurs"><xsl:value-of select="normalize-space(st:regexmultiplicator/st:string)"/></xsl:attribute>
     <xsl:attribute name="maxOccurs"><xsl:value-of select="normalize-space(st:regexmultiplicator/st:string)"/></xsl:attribute>
     <xsl:apply-templates select="st:regexterm"/>
    </concat>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>-->

 <xsl:template match="st:term">
  <xsl:apply-templates select="st:class|st:nested|st:string|st:element|st:dot"/>
 </xsl:template>

 <xsl:template match="st:nested">
  <xsl:apply-templates select="st:choice|st:sequence|st:quantifier"/>
 </xsl:template>

 <xsl:template match="st:dot">
  <class exclusive="true">
   <char value="#10"/>
   <char value="#13"/>
  </class>
 </xsl:template>

<!-- <xsl:template match="st:regexabref">
  <xsl:variable name="ref" select="translate(normalize-space(st:string), ' ', '')"/>
  <xsl:apply-templates select="/st:output/st:grammar/st:token_decls/st:ab_decl[st:id=$ref]/st:output/st:regexexpression"/>
 </xsl:template>-->

 <xsl:template match="st:string">
  <xsl:choose>
   <xsl:when test="count(st:char|st:masked-char)>1">
  <sequence>
   <xsl:apply-templates select="st:char|st:masked-char"/>
  </sequence>
   </xsl:when>
   <xsl:otherwise>
    <xsl:apply-templates select="st:char|st:masked-char"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <xsl:template match="st:class">
  <class>
   <xsl:if test="st:exclusive">
    <xsl:attribute name="exclusive">true</xsl:attribute>
   </xsl:if>
   <xsl:apply-templates select="st:interval|st:class-char|st:masked-char"/>
  </class>
 </xsl:template>

 <xsl:template match="st:class-char">
  <char value="{.}"/>
 </xsl:template>

 <xsl:template match="st:masked-char">
  <char>
   <xsl:choose>
    <xsl:when test="substring(translate(normalize-space(.), ' ', ''), 2,1) = 'n'">
<!--     <xsl:attribute name="content"><xsl:text disable-output-escaping="yes">&#10;</xsl:text></xsl:attribute>-->
     <xsl:attribute name="value">#10</xsl:attribute>
    </xsl:when>
    <xsl:when test="substring(translate(normalize-space(.), ' ', ''), 2,1) = 'r'">
<!--     <xsl:attribute name="content"><xsl:text disable-output-escaping="yes">&#13;</xsl:text></xsl:attribute>-->
     <xsl:attribute name="value">#13</xsl:attribute>
    </xsl:when>
    <xsl:when test="substring(translate(normalize-space(.), ' ', ''), 2,1) = 't'">
<!--     <xsl:attribute name="content"><xsl:text disable-output-escaping="yes">&#9;</xsl:text></xsl:attribute>-->
     <xsl:attribute name="value">#9</xsl:attribute>
    </xsl:when>
    <xsl:when test="substring(translate(normalize-space(.), ' ', ''), 2,1) = 'u'">
     <xsl:attribute name="value">#<xsl:value-of select="substring(translate(normalize-space(.), ' ', ''),3,6)"/></xsl:attribute>
    </xsl:when>
    <xsl:when test="contains(.,'\ ')">
     <xsl:attribute name="value"><xsl:text disable-output-escaping="yes">&#32;</xsl:text></xsl:attribute>
    </xsl:when>
    <xsl:otherwise>
     <xsl:attribute name="value"><xsl:value-of select="substring(translate(normalize-space(.), ' ', ''), 2,1)"/></xsl:attribute>
    </xsl:otherwise>
   </xsl:choose>
  </char>
 </xsl:template>

 <xsl:template match="st:char">
  <char value="{.}"/>
 </xsl:template>
 
 <xsl:template match="st:interval">
  <interval>
   <xsl:apply-templates select="st:class-char|st:masked-char"/>
  </interval>
 </xsl:template>

</xsl:stylesheet>
