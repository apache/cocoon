<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:req="http://apache.org/cocoon/request/2.0"
                xmlns:source="http://apache.org/cocoon/source/1.0">

<xsl:param name="file"></xsl:param>

<xsl:template match="/">
<page>
  <source:write create="true">
    <source:source><xsl:value-of select="$file"/></source:source>
    <!--source:path>page</source:path-->
    <source:fragment>
      <xsl:copy-of select="node()"/>
    </source:fragment>
  </source:write>
</page>
</xsl:template>

</xsl:stylesheet>
