<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Process a Column  -->
<xsl:template match="column-layout">

  <xsl:variable name="border">
    <xsl:choose>
      <xsl:when test="@border">
        <xsl:value-of select="@border" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>0</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <table border="{$border}" cellSpacing="0" cellpadding="0" width="100%">
    <xsl:if test="@bgcolor">
      <xsl:attribute name="bgcolor">
        <xsl:value-of select="@bgcolor" /> 
      </xsl:attribute>
    </xsl:if>
    <tr vAlign="top">
      <xsl:for-each select="item">
        <td>
          <xsl:if test="@bgcolor">
            <xsl:attribute name="bgcolor">
              <xsl:value-of select="@bgcolor" /> 
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@width">
            <xsl:attribute name="width">
              <xsl:value-of select="@width" /> 
            </xsl:attribute>
          </xsl:if>
          <xsl:apply-templates />
        </td>
      </xsl:for-each>
    </tr>
  </table>
</xsl:template>


<!-- Copy all and apply templates -->
<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
