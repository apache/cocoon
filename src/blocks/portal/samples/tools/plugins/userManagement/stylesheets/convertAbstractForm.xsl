<?xml version="1.0"?>
<!--
  Copyright 1999-2005 The Apache Software Foundation

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
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:i18n="http://apache.org/cocoon/i18n/2.1">

<xsl:param name="mode"/>

<xsl:template match="/">
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="formularImage">
	<xsl:choose>
		<xsl:when test="string-length(src/.) &gt; 0">
			<img>
				<xsl:attribute name="src">userImages/<xsl:value-of select="src/."/></xsl:attribute>
			</img>
		</xsl:when>
		<xsl:otherwise>
			<p style="font-size:9px;" align="center"><i18n:text i18n:key="userManagement.userData_nopicture"/></p>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="input">
	<xsl:if test="not (@type = 'submit' and $mode = 'readonly')">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:if>
</xsl:template>

<xsl:template match="abstractFormular">
	
	<xsl:variable name="abstractForm" select="document('cocoon:/page/abstractForm')"/>
	<xsl:variable name="currentNode" select="current()"/>
	
	<xsl:for-each select="$abstractForm/abstractFormStyle/items">
		<tr>
			<td colspan="2" style="background-color:#cccccc; font-size:13px; color:#ffffff">
				<b>
					<i18n:text>
						<xsl:attribute name="i18n:key"><xsl:value-of select="@name"/></xsl:attribute>
					</i18n:text>
				</b>
			</td>
		</tr>
		<xsl:for-each select="item">
			<xsl:apply-templates mode="getData">
				<xsl:with-param name="item"><xsl:value-of select="."/></xsl:with-param>
				<xsl:with-param name="currentNode" select="$currentNode"/>
			</xsl:apply-templates>
		</xsl:for-each>
		<tr><td colspan="2">&#160;</td></tr>
	</xsl:for-each>
</xsl:template>

<xsl:template match="@*|node()" mode="getData">
	<xsl:param name="item"/>
	<xsl:param name="currentNode"/>
	


	<xsl:for-each select="$currentNode/tr">
		<xsl:if test="td[1] = $item">
			<tr>
				<td style="border-bottom-style:dotted;border-bottom-color:#cccccc;border-bottom-width:1px;">
					<i18n:text>
						<xsl:attribute name="i18n:key">userManagement.userData_<xsl:value-of select="$item"/></xsl:attribute>
					</i18n:text>:
				</td>
				
				<xsl:copy-of select="td[2]"/>
			</tr>
		</xsl:if>
	</xsl:for-each>

</xsl:template>

<xsl:template match="@*|node()" priority="-1">
	<xsl:copy>
		<xsl:apply-templates select="@*|node()" />
	</xsl:copy>
</xsl:template>


</xsl:stylesheet>
