<?xml version="1.0" encoding="ISO-8859-1"?>
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

<!--
    [XSL-XSLT] This stylesheet automatically updated from an IE5-compatible XSL stylesheet to XSLT.
    The following problems which need manual attention may exist in this stylesheet:
    -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<!-- [XSL-XSLT] Simulate lack of built-in templates -->
	<xsl:template match="@*|/|node()"/>
	<!-- Match The Root Node -->
	<xsl:template match="/">
		<HTML>
		<BODY>
		<xsl:apply-templates select="*"></xsl:apply-templates>
		</BODY>
		</HTML>
	</xsl:template>
	<!-- Match Everything Else -->
	<xsl:template match="*|@*|text()|node()|comment()|processing-instruction()">
		<xsl:copy>
			<xsl:apply-templates select="*|@*|text()|node()|comment()|processing-instruction()"></xsl:apply-templates>
		</xsl:copy>
	</xsl:template>
	<!-- Individual Templates -->
	<xsl:template match="backslash">
		<table border="0" width="100%">
			<tr>
				<td colspan="5" STYLE="background-color : #B0E0E6; font : x-small Arial, Helvetica, sans-serif;">
					<CENTER>
					<b>Current News from Slashdot</b>
					</CENTER>
				</td>
			</tr>
			<xsl:apply-templates select="story"></xsl:apply-templates>
		</table>
	</xsl:template>
	<xsl:template match="story">
		<tr>
			<xsl:if test="(position() mod 2) = 1">
				<xsl:attribute name="STYLE">background-color : lightgrey; font : x-small Arial, Helvetica, sans-serif;</xsl:attribute>
			</xsl:if>
			<xsl:if test="(position() mod 2) = 0">
				<xsl:attribute name="STYLE">font : x-small Arial, Helvetica, sans-serif;</xsl:attribute>
			</xsl:if>
			<td>
			<CENTER>
			<IMG WIDTH="25" HEIGHT="25" BORDER="0">
				<xsl:attribute name="SRC"><xsl:value-of select="image"></xsl:value-of></xsl:attribute>
				<xsl:attribute name="ALT"><xsl:value-of select="image"></xsl:value-of></xsl:attribute>
			</IMG>
			</CENTER>
			</td>
			<td STYLE="font : bold;" width="50%">
				<a>
					<xsl:attribute name="HREF"><xsl:value-of select="url"></xsl:value-of></xsl:attribute>
					<xsl:attribute name="STYLE">text-decoration : none;</xsl:attribute>
					<xsl:attribute name="TARGET">_blank</xsl:attribute>
					<xsl:value-of select="title"></xsl:value-of>
				</a>
			</td>
			<td STYLE="font-variant : small-caps;">
				<xsl:value-of select="topic"></xsl:value-of>
			</td>
			<td STYLE="font-variant : small-caps;">
				<xsl:value-of select="author"></xsl:value-of>
			</td>
			<td>
				<xsl:value-of select="time"></xsl:value-of>
			</td>
		</tr>
	</xsl:template>
</xsl:stylesheet>
