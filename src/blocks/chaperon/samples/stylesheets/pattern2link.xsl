<?xml version="1.0"?>

<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:lex="http://chaperon.sourceforge.net/schema/lexemes/2.0">

 <xsl:template match="lex:lexeme[@symbol='link']">
  <a href="{@text}">
   <xsl:value-of select="@text"/>
  </a>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='email']">
  <a href="mailto:{@text}">
   <xsl:value-of select="lex:group[2]"/> at
   <xsl:value-of select="lex:group[3]"/>
  </a>
 </xsl:template>

 <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
  <xsl:copy>
   <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:copy>
 </xsl:template>

</xsl:stylesheet>
