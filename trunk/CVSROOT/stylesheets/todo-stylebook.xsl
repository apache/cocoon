<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

 <xsl:import href="./document-stylebook.xsl"/>

 <xsl:template match="todo">
  <s1 title="Todo List">
   <xsl:apply-templates/>
  </s1>
 </xsl:template>

 <xsl:template match="actions">
  <s2 title="{@priority}">
   <xsl:for-each select="action">
    <li>
     <em><xsl:value-of select="@context"/></em><xsl:text> - </xsl:text>
     <xsl:apply-templates/>
    </li>
   </xsl:for-each>
  </s2>
 </xsl:template>
 
</xsl:stylesheet>