<?xml version="1.0"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

	<xsl:template match="/">
		<html lang="en">
			<head>
				<title><xsl:apply-templates select="document/header/title/node()"/></title>
				<link rel="stylesheet" type="text/css" href="styles/addressbook.css" />
				<link rel="schema.DC" href="http://purl.org/dc/elements/1.1/" />
				<meta name="DC.title" lang="en" content="{/document/header/title}" />
				<meta name="DC.type" scheme="DCMIType" content="Text" />
				<meta name="DC.format" scheme="IMT" content="text/html" />
				<meta name="DC.language" scheme="RFC1766" content="en" />
				<meta name="DC.coverage" content="CocoonGT 2006" />
				<meta name="DC.rights" content="© Apache.org" />
				<meta name="DC.publisher" content="CocoonGT 2006" />
			</head>
			<body>
				<xsl:apply-templates select="/document"/>
			</body>
		</html>
	</xsl:template>

	<xsl:template match="/document">
		<div id="screen"><xsl:apply-templates select="body"/></div>
	</xsl:template>

	<xsl:template match="section">
		<xsl:choose> <!-- stupid test for the hierarchy depth -->
			<xsl:when test="../../../section">
				<h3 class="heading3"><xsl:apply-templates select="title/node()"/></h3>
			</xsl:when>
			<xsl:when test="../../section">
				<h2 class="heading2"><xsl:apply-templates select="title/node()"/></h2>
			</xsl:when>
			<xsl:when test="../section">
				<h1 class="heading1"><xsl:apply-templates select="title/node()"/></h1>
			</xsl:when>
			<xsl:otherwise>
				<h1 class="heading1"><xsl:apply-templates select="title/node()"/></h1>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:apply-templates select="*[name()!='title']"/>
	</xsl:template>

	<xsl:template match="header"/>

	<xsl:template match="title"/>

	<xsl:template match="body">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="link">
		<a href="{@href}">
			<xsl:apply-templates/>
		</a>
	</xsl:template>
	
	<xsl:template match="navigation/link|member-navigation/link">
		<a href="{@href}">
			<xsl:choose>
				<xsl:when test="@title">
					<xsl:attribute name="title"><xsl:value-of select="@title"/></xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="title"><xsl:value-of select="."/></xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:apply-templates/>
		</a><br />
	</xsl:template>

	<xsl:template match="anchor">
		<a name="{@id}"></a>
	</xsl:template>

	<xsl:template match="img">
		<img src="{@src}" alt="{@alt}" />
	</xsl:template>

	<xsl:template match="content">
		<xsl:apply-templates/> 
	</xsl:template>

	<!-- Catches all unrecognised elements as they are -->
	<xsl:template match="*|@*|node()|text()">
		<xsl:copy><xsl:apply-templates select="*|@*|node()|text()"/></xsl:copy>
	</xsl:template>

</xsl:stylesheet>
