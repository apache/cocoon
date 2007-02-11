<?xml version="1.0"?>
<!-- $Id: sunletconf.xsl,v 1.1 2003/03/09 00:05:33 pier Exp $ 

 Description: The configuration page of a coplet

-->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="page">
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="picture">
	<img>
		<xsl:attribute name="src"><xsl:value-of select="normalize-space(url)"/></xsl:attribute>
	</img>
</xsl:template>


<xsl:template match="form">
	<form>
		<xsl:attribute name="method"><xsl:value-of select="@method"/></xsl:attribute>
		<xsl:attribute name="action"><xsl:value-of select="@action"/></xsl:attribute>
		<table><tbody>
			<xsl:apply-templates select="inputxml"/>
			<tr><td colspan="2" align="middle">
			<xsl:apply-templates select="input"/>
			</td></tr>
		</tbody></table>
	</form>
</xsl:template>

<xsl:template match="inputxml">
	<tr>
		<td><xsl:value-of select="@name"/>:&#160;</td>
		<td>
			<xsl:choose>
				<xsl:when test="@name='Newsfeed'">
					<select name="Newsfeed">
						<option value="usa">
							<xsl:if test="normalize-space(.) = 'usa'">
								<xsl:attribute name="selected">true</xsl:attribute>
							</xsl:if>
							USA
						</option>
						<option value="entertainmentgeneral">
							<xsl:if test="normalize-space(.) = 'entertainmentgeneral'">
								<xsl:attribute name="selected">true</xsl:attribute>
							</xsl:if>
							Entertainment
						</option>
						<option value="foodanddrink">
							<xsl:if test="normalize-space(.) = 'foodanddrink'">
								<xsl:attribute name="selected">true</xsl:attribute>
							</xsl:if>
							Food &amp; Drink
						</option>
					</select>
				</xsl:when>
				<xsl:otherwise>
					<input>
						<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
						<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
						<xsl:attribute name="type"><xsl:value-of select="@type"/></xsl:attribute>
					</input>
				</xsl:otherwise>
			</xsl:choose>
		</td>
	</tr>
</xsl:template>

<!-- Copy all and apply templates -->
<xsl:template match="@*|node()">
	<xsl:copy>
		<xsl:apply-templates select="@*|node()" />
	</xsl:copy>
</xsl:template>


</xsl:stylesheet>
