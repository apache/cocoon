<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Process a tab  -->
<xsl:template match="tab-layout">
<table border="2" cellSpacing="0" width="100%">
  <tr vAlign="top">
    <xsl:for-each select="named-item">
      <xsl:choose>
          <xsl:when test="@selected">
              <td bgColor="#46627A"><p align="center" style="color:white;font-size:18pt"><b><xsl:value-of select="@name"/></b></p></td>
          </xsl:when>
          <xsl:otherwise>
              <td bgColor="#666666"><p align="center"><a style="text-decoration:none;color:white;text-decoration:underline;font-size:16pt" href="{@parameter}"><xsl:value-of select="@name"/></a></p></td>
          </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </tr>
  <tr>
    <td colSpan="{count(named-item)}">
      <xsl:apply-templates select="named-item"/>
    </td>
  </tr>
</table>
</xsl:template>

<xsl:template match="named-item">
  <xsl:apply-templates />
</xsl:template>

<!-- Copy all and apply templates -->

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
