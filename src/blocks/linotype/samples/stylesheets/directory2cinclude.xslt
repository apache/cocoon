<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:dir="http://apache.org/cocoon/directory/2.0"
 xmlns:include="http://apache.org/cocoon/include/1.0"
>
 
  <xsl:param name="prefix"/>
    
  <xsl:template match="/">
   <list>
    <xsl:apply-templates/>
   </list>
  </xsl:template>

  <xsl:template match="dir:directory/dir:directory[not(starts-with(@name,'template')) and not(starts-with(@name,'CVS'))]">
    <element id="{@name}">
     <include:include src="cocoon:/{$prefix}/{@name}.xml"/>
    </element>
  </xsl:template>

</xsl:stylesheet>
