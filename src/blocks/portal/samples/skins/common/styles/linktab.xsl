<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<!-- Process a tab  -->
<xsl:template match="linktab-layout">
<!-- ~~~~~ Begin body table ~~~~~ -->
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <!-- ~~~~~ Begin tab row ~~~~~ -->
  <tr vAlign="top">
  <td width="20%" align="top">
  <br/>
  &#160;Select:<br/><br/>
     <xsl:for-each select="named-item">
     
      <xsl:choose>
        <xsl:when test="not(@selected)">
          &#160;&#160;&#160;<a href="{@parameter}"><xsl:value-of select="@name"/></a><br/><br/>
        </xsl:when>
        <xsl:otherwise>
          &#160;&#160;&#160;<b><xsl:value-of select="@name"/></b><br/><br/>
        </xsl:otherwise>
      </xsl:choose> 
      </xsl:for-each>
  </td>
				<td width="80%" align="top">
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
