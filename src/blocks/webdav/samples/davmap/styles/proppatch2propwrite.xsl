<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:collection="http://apache.org/cocoon/collection/1.0" 
  xmlns:source="http://apache.org/cocoon/propwrite/1.0" 
  xmlns:D="DAV:">
  
  <xsl:param name="location" />
  
  <xsl:template match="/D:propertyupdate">
    <proppatch>
      <source:patch>
        <source:source><xsl:value-of select="$location" /></source:source>
        <xsl:apply-templates />
      </source:patch>
    </proppatch>
  </xsl:template>
  
  <xsl:template match="D:set/D:prop">
    <source:set>
      <xsl:copy-of select="child::node()" />
    </source:set>
  </xsl:template>
  
  <xsl:template match="D:remove/D:prop">
    <source:remove>
      <xsl:copy-of select="child::node()" />
    </source:remove>
  </xsl:template>  

</xsl:stylesheet>
