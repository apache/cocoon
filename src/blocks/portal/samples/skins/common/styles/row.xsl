<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Process a row  -->
<xsl:template match="row-layout">

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

  <table border="{$border}" cellSpacing="10" width="100%">
    <xsl:if test="@bgcolor">
      <xsl:attribute name="bgcolor">
        <xsl:value-of select="@bgcolor" /> 
      </xsl:attribute>
    </xsl:if>
    <xsl:for-each select="item">
      <tr vAlign="top">
        <xsl:if test="@bgcolor">
          <xsl:attribute name="bgcolor">
            <xsl:value-of select="@bgcolor" /> 
          </xsl:attribute>
        </xsl:if>
        <td>
          <xsl:apply-templates />
        </td>
      </tr>
    </xsl:for-each>
  </table>
</xsl:template>


<!-- Copy all and apply templates -->
<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
