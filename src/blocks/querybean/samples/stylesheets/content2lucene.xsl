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
<!-- CVS $Id: content2lucene.xsl 36225 2004-08-11 14:36:46Z vgritsenko $ -->
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:lucene="http://apache.org/cocoon/lucene/1.0" 
>
	<!-- store titles -->
	<xsl:template match="page/title|header/title">
		<title lucene:store="true"><xsl:apply-templates/></title>
	</xsl:template>
	
	<xsl:template match="faqs[@title]|book[@title]">
		<xsl:copy>
			<xsl:apply-templates select="@*[local-name() != 'title']"/>
			<title lucene:store="true"><xsl:value-of select="@title"/></title>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
		
  <xsl:template match="@*|node()" priority="-2"><xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy></xsl:template>
  <xsl:template match="text()" priority="-1"><xsl:value-of select="."/></xsl:template>
  <xsl:template match="comment()"></xsl:template>
  
  
</xsl:stylesheet> 