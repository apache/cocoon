<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:st="http://chaperon.sourceforge.net/schema/syntaxtree/2.0"
                exclude-result-prefixes="st">

 <xsl:template match="st:output">
  <document>
   <header>
    <title>Chaperon examples</title>
    <tab title="back" href="."/>
   </header>
   <body>
    <section>
     <title>Stock quote</title>

     <p>
      The example Comma-Separated Values (CSV) file is three months of end-of-day
      data for one particular stock symbol (data such as that obtained from
      <a href="http://finance.yahoo.com/">Yahoo Finance</a>).
     </p>
     <p>
      Here are the first 5 lines (total 64) of the input file ...
     </p>
     <pre>--------------------------------------------
20021101,0.11,0.11,0.11,0.11,74000
20021104,0.11,0.11,0.1,0.105,1166900
20021105,0.1,0.105,0.1,0.105,759670
20021106,0.1,0.105,0.1,0.105,101000
20021107,0.105,0.105,0.097,0.097,808230
...
--------------------------------------------</pre>
     <p>
      After processing with the Lexer Transformer and the Parser Transformer,
      here is the result ...
     </p>
     <table cellpadding="3" border="1">
      <tr>
       <th>Line #</th>
       <th>Date</th>
       <th>Open</th>
       <th>High</th>
       <th>Low</th>
       <th>Close</th>
       <th>Volume</th>
      </tr>
      <xsl:apply-templates select="st:document/st:rows"/>
     </table>
    </section>
   </body>
  </document>
 </xsl:template>

 <xsl:template match="st:rows">
  <xsl:apply-templates select="st:row"/>
 </xsl:template>

 <xsl:template match="st:row">
  <tr>
   <td><xsl:number/></td>
   <xsl:apply-templates select="st:Value"/>
  </tr>
 </xsl:template>

 <xsl:template match="st:Value">
  <td><xsl:value-of select="."/></td>
 </xsl:template>

 <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
  <xsl:copy>
   <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:copy>
 </xsl:template>

</xsl:stylesheet>
