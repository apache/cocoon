<?xml version="1.0"?>
<!-- $Id: authenticate.xsl,v 1.1 2003/03/09 00:02:24 pier Exp $ 

-->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<!-- Get the name from the request paramter -->
<xsl:param name="name"/>

<xsl:template match="authentication">
  <authentication>
	<xsl:apply-templates select="users"/>
  </authentication>
</xsl:template>


<xsl:template match="users">
    <xsl:apply-templates select="user"/>
</xsl:template>


<xsl:template match="user">
    <!-- Compare the name of the user -->
    <xsl:if test="normalize-space(name) = $name">
        <!-- found, so create the ID -->
        <ID><xsl:value-of select="name"/></ID>
    </xsl:if>
</xsl:template>

</xsl:stylesheet>
