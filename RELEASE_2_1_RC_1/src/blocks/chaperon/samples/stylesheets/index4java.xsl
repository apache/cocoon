<?xml version="1.0"?>

<xsl:stylesheet version="1.0" 
                xmlns:dir="http://apache.org/cocoon/directory/2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

 <xsl:template match="/dir:directory">
  <packages>
  <xsl:apply-templates select="dir:directory"/>

  <classes>
   <xsl:apply-templates select="//dir:file"/>
  </classes>
  </packages>
 </xsl:template>

 <xsl:template match="dir:directory">
  <package name="{@name}">
   <xsl:attribute name="full">
    <xsl:for-each select="ancestor::dir:directory[parent::dir:directory]"><xsl:value-of select="@name"/>.</xsl:for-each>
    <xsl:value-of select="@name"/>
   </xsl:attribute>

   <xsl:apply-templates select="dir:file"/>
  </package>

  <xsl:apply-templates select="dir:directory"/>
 </xsl:template>

 <xsl:template match="dir:file">
  <xsl:if test="substring-after(@name, '.')='java'">
   <class name="{substring-before(@name, '.')}">
    <xsl:attribute name="full">
     <xsl:for-each select="ancestor::dir:directory[parent::dir:directory]"><xsl:value-of select="@name"/>.</xsl:for-each>
     <xsl:value-of select="substring-before(@name, '.')"/>
    </xsl:attribute>
   </class>
  </xsl:if>
 </xsl:template>

</xsl:stylesheet>
