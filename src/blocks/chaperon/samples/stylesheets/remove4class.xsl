<?xml version="1.0"?>

<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:lex="http://chaperon.sourceforge.net/schema/lexer/2.0">

<!-- <xsl:template match="lex:lexeme[@symbol='JAVADOCCOMMENT']">
 </xsl:template>-->

 <xsl:template match="lex:lexeme[@symbol='MULTILINECOMMENT']">
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='SINGLELINECOMMENT']">
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='WHITESPACE']">
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='EOL']">
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='SPACES']">
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='TAB']">
 </xsl:template>

 <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
  <xsl:copy>
   <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:copy>
 </xsl:template>

</xsl:stylesheet>
