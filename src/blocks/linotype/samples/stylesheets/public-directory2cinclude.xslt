<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:dir="http://apache.org/cocoon/directory/2.0"
 exclude-result-prefixes="dir">
>
 
  <xsl:param name="prefix"/>
  <xsl:param name="count"/>
  
  <xsl:template match="/">
   <list>
    <xsl:for-each select="dir:directory/dir:directory[not(starts-with(@name,'template')) and not(starts-with(@name,'CVS'))]">
     <xsl:if test="position() &lt;= number($count)">
      <xsl:apply-templates select="."/>
     </xsl:if>
    </xsl:for-each>
   </list>
  </xsl:template>
    
  <xsl:template match="dir:directory">
    <element id="{@name}">
     <include:include src="cocoon:/{$prefix}/{@name}.xml" xmlns:include="http://apache.org/cocoon/include/1.0"/>
    </element>
  </xsl:template>

</xsl:stylesheet>
