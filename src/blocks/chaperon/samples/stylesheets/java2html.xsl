<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:lex="http://chaperon.sourceforge.net/schema/lexer/2.0">

 <xsl:output indent="no"/>

 <xsl:param name="package"/>
 <xsl:param name="class"/>

 <xsl:param name="prefix">cocoon/samples/chaperon/</xsl:param>

 <xsl:template match="/lex:output">
  <document>
   <header>
    <title>Java2HTML example</title>
    <style href="java.css"/>
   </header>
   <body>
    <row>
     <column title="Source file: {$class}">
      <pre class="srcLine"><xsl:apply-templates select="lex:lexeme"/></pre>
     </column>
    </row>
   </body>
  </document>
 </xsl:template>

 <xsl:template match="package">
  <a href="/{$prefix}{translate(@full,'.','/')}/index.html"><xsl:value-of select="@full"/></a><br/>
 </xsl:template>

 <xsl:template match="class">
  <a href="/{$prefix}{translate(@full,'.','/')}.java.html"><xsl:value-of select="@name"/></a><br/>
 </xsl:template>

 <xsl:template name="linenumber">
  <xsl:param name="nr">1</xsl:param>
  <xsl:param name="max">1</xsl:param>&#160;<xsl:value-of select="$nr"/><xsl:text>
</xsl:text><xsl:if test="$nr &lt;= $max">
   <xsl:call-template name="linenumber">
     <xsl:with-param name="nr"  select="$nr + 1"/>
     <xsl:with-param name="max" select="$max"/>
   </xsl:call-template>
  </xsl:if>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='ABSTRACT']">
  <font class="ABSTRACT"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='BOOLEAN']">
  <font class="BOOLEAN"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='BREAK']">
  <font class="BREAK"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='BYTE']">
  <font class="BYTE"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='CASE']">
  <font class="CASE"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='CATCH']">
  <font class="CATCH"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='CHAR']">
  <font class="CHAR"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='CLASS']">
  <font class="CLASS"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='CONST']">
  <font class="CONST"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='CONTINUE']">
  <font class="CONTINUE"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='DEFAULT']">
  <font class="DEFAULT"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='DO']">
  <font class="DO"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='DOUBLE']">
  <font class="DOUBLE"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='ELSE']">
  <font class="ELSE"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='EXTENDS']">
  <font class="EXTENDS"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='FALSE']">
  <font class="FALSE"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='FINAL']">
  <font class="FINAL"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='FINALLY']">
  <font class="FINALLY"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='FLOAT']">
  <font class="FLOAT"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='FOR']">
  <font class="FOR"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='GOTO']">
  <font class="GOTO"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='IF']">
  <font class="IF"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='IMPLEMENTS']">
  <font class="IMPLEMENTS"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='IMPORT']">
  <font class="IMPORT"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='INSTANCEOF']">
  <font class="INSTANCEOF"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='INT']">
  <font class="INT"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='INTERFACE']">
  <font class="INTERFACE"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='LONG']">
  <font class="LONG"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='NATIVE']">
  <font class="NATIVE"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='NEW']">
  <font class="NEW"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='PACKAGE']">
  <font class="PACKAGE"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='PRIVATE']">
  <font class="PRIVATE"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='PROTECTED']">
  <font class="PROTECTED"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='PUBLIC']">
  <font class="PUBLIC"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='RETURN']">
  <font class="RETURN"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='SHORT']">
  <font class="SHORT"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='STATIC']">
  <font class="STATIC"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='SUPER']">
  <font class="SUPER"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='SWITCH']">
  <font class="SWITCH"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='SYCHRONIZED']">
  <font class="SYCHRONIZED"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='THIS']">
  <font class="THIS"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='THROW']">
  <font class="THROW"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='THROWS']">
  <font class="THROWS"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='TRANSIENT']">
  <font class="TRANSIENT"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='TRUE']">
  <font class="TRUE"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='TRY']">
  <font class="TRY"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='VOID']">
  <font class="VOID"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='VOLATILE']">
  <font class="VOLATILE"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='WHILE']">
  <font class="WHILE"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='LITERAL']">
  <font class="LITERAL"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='IDENTIFIER']">
  <font class="IDENTIFIER"><xsl:value-of select="@text"/></font> 
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='DOPEN']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='DCLOSE']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='COPEN']">
  <xsl:value-of select="@text"/>
 </xsl:template>
  
 <xsl:template match="lex:lexeme[@symbol='CCLOSE']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='BOPEN']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='BCLOSE']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='SEMICOLON']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='COMMA']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='DOT']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='OP_EQ']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='OP_LE']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='OP_GE']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='OP_NE']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='OP_LOR']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='OP_LAND']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='OP_INC']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='OP_DEC']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='OP_SHR']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='OP_SHL']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='OP_SHRR']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='ASS_OP']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='EQ']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='GT']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='LT']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='NOT']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='TILDE']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='QM']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='COLON']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='PLUS']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='MINUS']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='MULT']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='DIV']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='AND']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='OR']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='XOR']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='MOD']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='BOOLLIT']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='EOL']">
  <xsl:text>
&#160;</xsl:text>
<!--  <br/>-->
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='SPACES']"><xsl:value-of select="@text"/></xsl:template>

 <xsl:template match="lex:lexeme[@symbol='TAB']">
  <xsl:text>  </xsl:text>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='JAVADOC']">
  <font class="MultiLineComment">/*<xsl:apply-templates select="lex:output/*"/>/</font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='MULTILINECOMMENT']">
  <font class="MultiLineComment"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='SINGLEMULTILINECOMMENT']">
  <font class="MultiLineComment"><xsl:value-of select="@text"/></font>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='ASTERISK']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='TEXT']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='TAG']">
  <xsl:value-of select="@text"/>
 </xsl:template>

 <xsl:template match="lex:lexeme[@symbol='PROPERTYNAME']">
  <xsl:value-of select="@text"/>
 </xsl:template>

</xsl:stylesheet>
