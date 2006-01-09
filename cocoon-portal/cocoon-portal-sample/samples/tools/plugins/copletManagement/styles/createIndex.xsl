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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="rootId"/>
	
<xsl:template match="/">
	<xsl:apply-templates select="*" mode="firstnode"/>
</xsl:template>

<xsl:template match="*" mode="firstnode">
	
	<!-- calculate new id by the level of the node -->
	<!--xsl:variable name="newId"><xsl:value-of select="$prefix"/><xsl:value-of select="position()"/></xsl:variable-->
	
	<!-- check: is the current node the new root node? -->
	<xsl:choose>
		<xsl:when test="string-length($rootId) &lt;= 1 or not($rootId)">
			<xsl:copy>
				
				<xsl:apply-templates select="@*"/>
				<xsl:if test="not(@id)">

					<xsl:attribute name="id">1</xsl:attribute>
				</xsl:if>
				<!-- go on -->
				<xsl:apply-templates select=".">
					<xsl:with-param name="prefix">1.</xsl:with-param>
					<xsl:with-param name="parent">1</xsl:with-param>
 					<xsl:with-param name="parentSet">true</xsl:with-param>
				</xsl:apply-templates>
				
			</xsl:copy>
		</xsl:when>
		<xsl:otherwise>
			<xsl:apply-templates select=".">
				<xsl:with-param name="prefix">1.</xsl:with-param>
				<xsl:with-param name="parent">1</xsl:with-param>
				<xsl:with-param name="parentSet"></xsl:with-param>
			</xsl:apply-templates>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="node()">
	<xsl:param name="prefix"/>
	<xsl:param name="parent"/>
	<xsl:param name="parentSet"/>
	
	<xsl:for-each select="*">
		
		<!-- calculate new id by the level of the node -->
		<xsl:variable name="counter">
			<xsl:call-template name="getIndex">
				<xsl:with-param name="currentpos"><xsl:value-of select="position()"/></xsl:with-param>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="newId"><xsl:value-of select="$prefix"/><xsl:value-of select="string-length(normalize-space($counter))"/></xsl:variable>
		
		<!-- check: is the current node the new root node? -->
		<xsl:choose>
			<xsl:when test="(starts-with($newId, $rootId) and (string-length(substring-after($newId,$rootId)) = 0 or starts-with(substring-after($newId,$rootId),'.'))) or not($rootId)">
				<xsl:copy>
					
					<!-- set id, if it does not exist an is the right node -->
					<xsl:if test="not(@id) and name() = 'composite-layout' or name() = 'named-item' or name() = 'coplet-layout' or name() = 'coplet-instance-data' or name() = 'item' or name() = '' or name() = ''">
						<xsl:attribute name="id"><xsl:value-of select="$newId"/></xsl:attribute>
					</xsl:if>
					
					<!-- if the current node is not the root node, set calculated parent id -->
					<xsl:if test="$parentSet != 'true'">
						<xsl:attribute name="parent"><xsl:value-of select="$parent"/></xsl:attribute>
					</xsl:if>
					
					<!-- calculate parent: it is always the last composite-layout or named-item node -->
					<xsl:variable name="newParent">
						<xsl:choose>
							<xsl:when test="name(.) = 'composite-layout' or name(.) = 'named-item'">
								<xsl:value-of select="$newId"/>
						</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$parent"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					
					<!-- get all other attributes -->
					<xsl:apply-templates select="@*"/>
					
					<!-- go on -->
					<xsl:apply-templates select=".">
						<xsl:with-param name="prefix"><xsl:value-of select="$prefix"/><xsl:value-of select="string-length(normalize-space($counter))"/>.</xsl:with-param>
						<xsl:with-param name="parent"><xsl:value-of select="$newParent"/></xsl:with-param>
						<xsl:with-param name="parentSet">true</xsl:with-param>
					</xsl:apply-templates>
					
					<xsl:if test="name() = 'coplet-instance-data'"><xsl:value-of select="."/></xsl:if>
					
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
		
				<!-- calculate parent: it is always the last composite-layout or named-item node -->
				<xsl:variable name="newParent">
					<xsl:choose>
						<xsl:when test="name(.) = 'composite-layout' or name(.) = 'named-item'">
							<xsl:value-of select="$prefix"/><xsl:value-of select="position()"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$parent"/>
						</xsl:otherwise>
				</xsl:choose>
				</xsl:variable>
				
				<xsl:apply-templates select=".">
					<xsl:with-param name="prefix"><xsl:value-of select="$prefix"/><xsl:value-of select="string-length(normalize-space($counter))"/>.</xsl:with-param>
					<xsl:with-param name="parent"><xsl:value-of select="$newParent"/></xsl:with-param>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:for-each>
</xsl:template>

<xsl:template name="getIndex">
	<xsl:param name="currentpos"/>
	
	<xsl:for-each select="parent::node()/*[position() &lt;= $currentpos]">
		<xsl:if test="name() = 'composite-layout' or name() = 'named-item' or name() = 'coplet-layout' or name() = 'coplet-instance-data' or name() = 'item' or name() = '' or name() = ''">.</xsl:if>
	</xsl:for-each>
</xsl:template>

<xsl:template match="@*">
	<xsl:copy>
		<xsl:apply-templates select="@*|node()" />
	</xsl:copy>
</xsl:template>

</xsl:stylesheet>
