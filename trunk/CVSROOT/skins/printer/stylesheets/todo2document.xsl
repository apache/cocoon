<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

 <xsl:import href="copyover.xsl"/>
 
 <xsl:template match="todo">
  <document>
   <header>
    <title><xsl:value-of select="@title"/></title>
   </header>
   <body>
    <xsl:apply-templates/>
   </body>
  </document>
 </xsl:template>

 <xsl:template match="actions">
  <s2 title="{@priority}">
   <sl>
    <xsl:for-each select="action">
     <li>
      <strong>[<xsl:value-of select="@context"/>]</strong><xsl:text> </xsl:text>
      <xsl:apply-templates/>
      <xsl:if test="@assigned-to">
       <em>(assigned to <xsl:value-of select="@assigned-to"/>)</em>
      </xsl:if>
     </li>
    </xsl:for-each>
   </sl>
  </s2>
 </xsl:template>
 
</xsl:stylesheet>