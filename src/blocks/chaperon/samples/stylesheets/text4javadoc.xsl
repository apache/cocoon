<?xml version="1.0"?>

<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:lex="http://chaperon.sourceforge.net/schema/lexer/2.0"
    xmlns:text="http://chaperon.sourceforge.net/schema/text/1.0">

 <xsl:template match="lex:lexeme[@symbol='JAVADOC']">
  <lex:lexeme symbol="JAVADOC">
   <text:text><xsl:value-of select="substring(@text,3,string-length(@text)-3)"/></text:text>
  </lex:lexeme>
 </xsl:template>

<!-- <xsl:template match="lex:lexeme[@symbol='MULTILINECOMMENT']">
  <lex:lexeme symbol="MULTILINECOMMENT">
   <text:text><xsl:value-of select="substring(@text,2,string-length(@text)-2)"/></text:text>
  </lex:lexeme>
 </xsl:template>-->

  <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
