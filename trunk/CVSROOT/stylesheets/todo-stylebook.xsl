<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

 <xsl:import href="./document-stylebook.xsl"/>

 <xsl:template match="todo">
  <s1 title="Todo List">
  <xsl:call-template name="section">
   <xsl:with-param name="priority">showstopper</xsl:with-param>
  </xsl:call-template>
  <xsl:call-template name="section">
   <xsl:with-param name="priority">high</xsl:with-param>
  </xsl:call-template>
  <xsl:call-template name="section">
   <xsl:with-param name="priority">medium</xsl:with-param>
  </xsl:call-template>
  <xsl:call-template name="section">
   <xsl:with-param name="priority">low</xsl:with-param>
  </xsl:call-template>
  <xsl:call-template name="section">
   <xsl:with-param name="priority">wish</xsl:with-param>
  </xsl:call-template>
  <xsl:call-template name="section">
   <xsl:with-param name="priority">dream</xsl:with-param>
  </xsl:call-template>
  </s1>
 </xsl:template>

 <xsl:template name="section">
  <xsl:param name="priority">showstopper</xsl:param>
  <xsl:if test=".//action[@priority=$priority]">
   <s2 title="{$priority}">
    <xsl:for-each select=".//action[@priority=$priority]">
     <li>
      <em><xsl:value-of select="@context"/></em><xsl:text> - </xsl:text>
      <xsl:apply-templates/>
     </li>
    </xsl:for-each>
   </s2>
  </xsl:if>
 </xsl:template>
 
</xsl:stylesheet>
