<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

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
  <s1 title="{@priority}">
   <xsl:for-each select="action">
    <li>
     <em><xsl:value-of select="@context"/></em><xsl:text> - </xsl:text>
     <xsl:apply-templates/>
    </li>
   </xsl:for-each>
  </s1>
 </xsl:template>
 
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template> 
 
</xsl:stylesheet>