<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<!-- Process a tab  -->
<xsl:template match="tab-layout">
<!-- ~~~~~ Begin body table ~~~~~ -->
<table border="2" cellpadding="0" cellspacing="0" width="100%">
  <!-- ~~~~~ Begin tab row ~~~~~ -->
  <tr>
  <td>
  <table summary="tab bar" border="2" cellpadding="0" cellspacing="0" width="100%">
  <tr vAlign="top">
     <xsl:for-each select="named-item">
      <xsl:choose>
          <xsl:when test="@selected">
			<!-- ~~~~~ begin selected tab ~~~~~ -->
				<td valign="middle" bgcolor="#DDDDDD">
					<b>
						<a href="{@parameter}">
							<font color="#000000">
								<xsl:value-of select="@name"/>
							</font>
						</a>
					</b>
				</td>
			<!-- ~~~~~ end selected tab ~~~~~ -->
			</xsl:when>
			<xsl:otherwise>
			<!-- ~~~~~ begin non selected tab ~~~~~ -->
				<td valign="middle" bgcolor="#CCCCCC" >
					<div class="tab">
						<a href="{@parameter}">
								<xsl:value-of select="@name"/>
						</a>
					</div>
				</td>
			<!-- ~~~~~ end non selected tab ~~~~~ -->
          </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
	<!-- ~~~~~ last "blank" tab ~~~~~ -->
	<td width="99%" bgcolor="#CCCCCC" align="right">
	</td>
  </tr>
  </table>
  </td>
  </tr>
  <!-- ~~~~~ End tab row ~~~~~ -->

  <!-- ~~~~~ Begin content row ~~~~~ -->
  <tr>
    <td bgcolor="#FFFFFF">
      <xsl:apply-templates/>
    </td>
  </tr>
  <!-- ~~~~~ End content row ~~~~~ -->
</table>
</xsl:template>


<!-- Copy all and apply templates -->
<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
