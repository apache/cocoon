<?xml version="1.0" encoding="iso-8859-1"?>
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

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rfc="http://apache.org/cocoon/Web3-Rfc/1.0">

	<xsl:template match="/">
		<html>
			<head>
				<title>Web3</title>
			</head>
			<body>
				<xsl:apply-templates/>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="rfc:tables">
		<xsl:if test="count(rfc:table) > 0">
			<h1>tables:</h1>
			<xsl:apply-templates/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="rfc:table">
		<h2>table name: <xsl:value-of select="@*[name(.)='name']"/>
		</h2>
		<h3>
			<xsl:value-of select="count(rfc:row)"/> Zeilen
		</h3>
		<xsl:if test="count(rfc:row) &gt; 0">
			<table border="1">
				<tr>
					<xsl:for-each select="rfc:row[1]/rfc:field">
						<th>
							<xsl:value-of select="@*[name(.)='name']"/>
						</th>
					</xsl:for-each>	
				</tr>
				<xsl:for-each select="rfc:row">
					<tr>
						<xsl:for-each select="rfc:field">
							<td>
								<xsl:value-of select="."/>
							</td>
						</xsl:for-each>
					</tr>
				</xsl:for-each>
			</table>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="rfc:export">
		<xsl:if test="count(rfc:field) > 0">
			<h1>fields:</h1>
			<table border="1">
				<tr>
					<th>field name</th>
					<th>value</th>
				</tr>
				<xsl:apply-templates select="rfc:field"/>
			</table>
		</xsl:if>
		<xsl:if test="count(rfc:structure) > 0">
			<h1>structures:</h1>
			<xsl:for-each select="rfc:structure">
				<h2>structure name: <xsl:value-of select="@*[name(.)='name']"/>
				</h2>
				<table border="1">
					<tr>
						<th>field name</th>
						<th>value</th>
					</tr>
					<xsl:apply-templates select="rfc:field"/>
				</table>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="rfc:field">
		<tr>
			<td>
				<xsl:value-of select="@*[name(.)='name']"/>
			</td>
			<td>
				<xsl:value-of select="."/>
			</td>
		</tr>
	</xsl:template>
	
</xsl:stylesheet>
