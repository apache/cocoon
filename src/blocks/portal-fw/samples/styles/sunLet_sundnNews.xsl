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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- $Id: sunLet_sundnNews.xsl,v 1.3 2004/03/06 02:25:39 antonio Exp $ 

-->

<xsl:template match="*|/"><xsl:apply-templates/></xsl:template>

<xsl:template match="text()|@*"><xsl:value-of select="."/></xsl:template>

<xsl:template match="news"><!-- Stylesheet to be used inside a coplet. Generates HTML from an XML-feed from http://w.moreover.com/ -->

	<table>
		<xsl:apply-templates select ="article">
		</xsl:apply-templates>
	</table>
</xsl:template>

<xsl:template match="article">
			<xsl:if test="position() &lt; 6">
				<tr bgcolor="#ffffff"><td><font face="Arial, Helvetica, sans-serif">
    			<a target="_blank"><xsl:attribute name="href"><xsl:value-of select="link"/></xsl:attribute>
				<font size="-1" color="#333333"><b><xsl:value-of select="title"/></b></font></a><br/>
				<a target="_blank"><xsl:attribute name="href"><xsl:value-of select="authorlink"/></xsl:attribute> 
    			<font size="-2" color="#46627A"><xsl:value-of select = "author"/></font></a>
				<font size="-2" color="#46627A">&#160;&#160;<xsl:value-of select="date"/></font>
    			</font></td></tr>
				<tr bgcolor="#ffffff"><td bgcolor="#ffffff" height="5"></td></tr>
			</xsl:if>
</xsl:template>

</xsl:stylesheet>
