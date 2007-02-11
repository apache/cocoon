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
     <title>CSV Example</title>

     <table cellpadding="3" border="1">
      <xsl:apply-templates select="st:document/st:rows/st:row[1]" mode="title"/>
      <xsl:apply-templates select="st:document/st:rows/st:row[position()>1]"/>
     </table>
    </section>
   </body>
  </document>
 </xsl:template>

 <xsl:template match="st:row" mode="title">
  <tr>
   <th>Line #</th>
   <xsl:apply-templates select="st:column" mode="title"/>
  </tr>
 </xsl:template>

 <xsl:template match="st:row">
  <tr>
   <td><xsl:value-of select="position()"/></td>
   <xsl:apply-templates select="st:column"/>
  </tr>
 </xsl:template>

 <xsl:template match="st:column" mode="title">
  <th><xsl:value-of select="st:Value"/></th>
 </xsl:template>

 <xsl:template match="st:column">
  <td><xsl:value-of select="st:Value"/></td>
 </xsl:template>

 <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
  <xsl:copy>
   <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:copy>
 </xsl:template>

</xsl:stylesheet>
