<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pl="http://xml.apache.org/cocoon/PrincipalListGenerator">

 <xsl:param name="name"/>
 <xsl:param name="password"/>

 <xsl:template match="pl:list">
  <authentication>
	 <xsl:apply-templates select="pl:principal"/>
  </authentication>
 </xsl:template>

 <xsl:template match="pl:principal">
  <xsl:if test="normalize-space(@name) = $name and normalize-space(@password) = $password">
	 <ID><xsl:value-of select="@name"/></ID>
	 <role><xsl:value-of select="@role"/></role>
  </xsl:if>
 </xsl:template>

</xsl:stylesheet>
