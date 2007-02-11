<?xml version="1.0"?>
<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!-- $Id: sunletconf.xsl,v 1.3 2004/03/06 02:25:39 antonio Exp $ 

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
