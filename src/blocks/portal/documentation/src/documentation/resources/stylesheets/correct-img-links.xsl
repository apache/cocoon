<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:param name="docid"/>
  
  <xsl:template match="img|link">
    <xsl:element name="{name()}">
      <xsl:for-each select="@*">
        <xsl:choose>
          <xsl:when test="(name(.) = 'src' or name(.) = 'href') and starts-with(., 'files')">
            <xsl:attribute name="{name(.)}">
                <xsl:value-of select="$docid"/>/<xsl:value-of select="."/>
            </xsl:attribute>   
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="{name(.)}">
                <xsl:value-of select="."/>
            </xsl:attribute>       
          </xsl:otherwise>
        </xsl:choose>
     </xsl:for-each>
     <xsl:value-of select="."/>     
    </xsl:element>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-2">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>  
</xsl:stylesheet>