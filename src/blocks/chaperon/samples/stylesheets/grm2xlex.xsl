<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:text="http://chaperon.sourceforge.net/schema/text/1.0"
                xmlns:st="http://chaperon.sourceforge.net/schema/syntaxtree/2.0"
                xmlns="http://chaperon.sourceforge.net/schema/lexicon/1.0"
                exclude-result-prefixes="st text">

 <xsl:output indent="yes" method="xml" encoding="ASCII"/>

 <xsl:template match="st:output/st:grammar" >
  <lexicon><xsl:comment>This file was generated! Don't edit!</xsl:comment>
   <xsl:apply-templates select="st:token_decls/st:token_decl | st:token_decls/st:ignorabletoken_decl"/>
  </lexicon>
 </xsl:template>

 <xsl:template match="st:token_decl" >
  <lexeme symbol="{st:id}">
   <xsl:if test="st:token_decl = '%left'">
    <xsl:attribute name="assoc">left</xsl:attribute>
   </xsl:if>
   <xsl:if test="st:token_decl = '%right'">
    <xsl:attribute name="assoc">right</xsl:attribute>
   </xsl:if>
   <xsl:apply-templates select="st:output/st:regexexpression"/>
  </lexeme>
 </xsl:template>

 <xsl:template match="st:ignorabletoken_decl" >
  <lexeme>
   <xsl:apply-templates select="st:output/st:regexexpression"/>
  </lexeme>
 </xsl:template>

 <xsl:template match="st:regexexpression" >
  <xsl:apply-templates select="st:regexalternation"/>
 </xsl:template>

 <xsl:template match="st:regexalternation" >
  <xsl:choose>
   <xsl:when test="count(st:regexconcatenation)>1">
    <alt>
     <xsl:apply-templates select="st:regexconcatenation"/>
    </alt>
   </xsl:when>
   <xsl:otherwise>
    <xsl:apply-templates select="st:regexconcatenation"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <xsl:template match="st:regexconcatenation" >
  <xsl:choose>
   <xsl:when test="count(st:regexquantifier)>1">
    <concat>
     <xsl:apply-templates select="st:regexquantifier"/>
    </concat>
   </xsl:when>
   <xsl:otherwise>
    <xsl:apply-templates select="st:regexquantifier"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <xsl:template match="st:regexquantifier" >
  <xsl:apply-templates select="st:regexoptional|st:regexstar|st:regexplus|st:regexterm|st:regexvar"/>
 </xsl:template>

 <xsl:template match="st:regexoptional" >
  <concat minOccurs="0" maxOccurs="1">
   <xsl:apply-templates select="st:regexterm"/>
  </concat>
 </xsl:template>

 <xsl:template match="st:regexstar" >
  <concat minOccurs="0" maxOccurs="*">
   <xsl:apply-templates select="st:regexterm"/>
  </concat>
 </xsl:template>

 <xsl:template match="st:regexplus" >
  <concat minOccurs="1" maxOccurs="*">
   <xsl:apply-templates select="st:regexterm"/>
  </concat>
 </xsl:template>

 <xsl:template match="st:regexvar">
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
 </xsl:template>

 <xsl:template match="st:regexterm">
  <xsl:apply-templates select="st:characterclass|st:exclusivecharacterclass|st:regexklammer|st:string|st:maskedcharacter|st:regexdot|st:regexbol|st:regexabref"/>
 </xsl:template>

 <xsl:template match="st:regexklammer">
  <xsl:apply-templates select="st:regexalternation"/>
 </xsl:template>

 <xsl:template match="st:regexdot">
  <cclass exclusive="true">
   <!--<cset content="&#10;&#13;"/>-->
   <cset code="10"/>
   <cset code="13"/>
  </cclass>
 </xsl:template>

 <xsl:template match="st:regexbol">
  <bol/>
 </xsl:template>

 <xsl:template match="st:regexeol">
  <eol/>
 </xsl:template>

 <xsl:template match="st:regexabref">
  <xsl:variable name="ref" select="translate(normalize-space(st:string), ' ', '')"/>
  <xsl:apply-templates select="/st:output/st:grammar/st:token_decls/st:ab_decl[st:id=$ref]/st:output/st:regexexpression"/>
 </xsl:template>

 <xsl:template match="st:string" mode="name">
  <xsl:for-each select="st:character"><xsl:value-of select="normalize-space(.)"/></xsl:for-each>
 </xsl:template>

 <xsl:template match="st:string">
  <cstring>
   <xsl:attribute name="content"><xsl:apply-templates select="st:character" mode="string"/></xsl:attribute>
  </cstring>
 </xsl:template>

 <xsl:template match="st:characterclass">
  <cclass>
   <xsl:apply-templates select="st:sequence"/>
  </cclass>
 </xsl:template>

 <xsl:template match="st:exclusivecharacterclass" >
  <cclass exclusive="true">
   <xsl:apply-templates select="st:sequence"/>
  </cclass>
 </xsl:template>

 <xsl:template match="st:sequence">
  <xsl:apply-templates select="st:character|st:maskedcharacter|st:intervall" mode="cclass"/>
 </xsl:template>

 <xsl:template match="st:character" mode="cclass">
  <cset>
   <xsl:attribute name="content"><xsl:value-of select="translate(normalize-space(.), ' ', '')"/></xsl:attribute>
  </cset>
 </xsl:template>

 <xsl:template match="st:maskedcharacter" mode="cclass">
  <cset>
   <xsl:choose>
    <xsl:when test="substring(translate(normalize-space(.), ' ', ''), 2,1) = 'n'">
<!--     <xsl:attribute name="content"><xsl:text disable-output-escaping="yes">&#10;</xsl:text></xsl:attribute>-->
     <xsl:attribute name="code">10</xsl:attribute>
    </xsl:when>
    <xsl:when test="substring(translate(normalize-space(.), ' ', ''), 2,1) = 'r'">
<!--     <xsl:attribute name="content"><xsl:text disable-output-escaping="yes">&#13;</xsl:text></xsl:attribute>-->
     <xsl:attribute name="code">13</xsl:attribute>
    </xsl:when>
    <xsl:when test="substring(translate(normalize-space(.), ' ', ''), 2,1) = 't'">
<!--     <xsl:attribute name="content"><xsl:text disable-output-escaping="yes">&#9;</xsl:text></xsl:attribute>-->
     <xsl:attribute name="code">9</xsl:attribute>
    </xsl:when>
    <xsl:when test="substring(translate(normalize-space(.), ' ', ''), 2,1) = 'u'">
     <xsl:attribute name="code">#<xsl:value-of select="substring(translate(normalize-space(.), ' ', ''),2,6)"/></xsl:attribute>
    </xsl:when>
    <xsl:when test="contains(.,'\ ')">
     <xsl:attribute name="content"><xsl:text disable-output-escaping="yes">&#32;</xsl:text></xsl:attribute>
    </xsl:when>
    <xsl:otherwise>
     <xsl:attribute name="content"><xsl:value-of select="substring(translate(normalize-space(.), ' ', ''), 2,1)"/></xsl:attribute>
    </xsl:otherwise>
   </xsl:choose>
  </cset>
 </xsl:template>

 <xsl:template match="st:character" mode="string">
  <xsl:value-of select="translate(normalize-space(.), ' ', '')"/>
 </xsl:template>
 
 <xsl:template match="st:maskedcharacter">
  <xsl:choose>
    <xsl:when test="substring(translate(normalize-space(.), ' ', ''), 2,1) = 'n'">
     <!--<xsl:text disable-output-escaping="yes">&#10;</xsl:text>-->
     <cstring code="10"/>
    </xsl:when>
    <xsl:when test="substring(translate(normalize-space(.), ' ', ''), 2,1) = 'r'">
     <!--<xsl:text disable-output-escaping="yes">&#13;</xsl:text>-->
     <cstring code="13"/>
    </xsl:when>
    <xsl:when test="substring(translate(normalize-space(.), ' ', ''), 2,1) = 't'">
     <!--<xsl:text disable-output-escaping="yes">&#9;</xsl:text>-->
     <cstring code="9"/>
    </xsl:when>
    <xsl:when test="substring(translate(normalize-space(.), ' ', ''), 2,1) = 'u'">
     <cstring code="#{substring(translate(normalize-space(.), ' ', ''), 2,6)}"/>
    </xsl:when>
    <xsl:when test="contains(.,'\ ')">
     <!--<xsl:text disable-output-escaping="yes">&#32;</xsl:text>-->
     <cstring content=" "/>
    </xsl:when>
    <xsl:otherwise>
     <cstring content="{substring(translate(normalize-space(.), ' ', ''),2,1)}"/>
    </xsl:otherwise>
   </xsl:choose>
 </xsl:template>

 <xsl:template match="st:intervall" mode="cclass">
  <cinterval>
   <xsl:attribute name="min"><xsl:value-of select="st:character[1]"/></xsl:attribute>
   <xsl:attribute name="max"><xsl:value-of select="st:character[2]"/></xsl:attribute>
  </cinterval>
 </xsl:template>

</xsl:stylesheet>
