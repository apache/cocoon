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

<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:bu="http://apache.org/cocoon/browser-update/1.0"
		xmlns:xl="http://www.w3.org/1999/xlink"
	>
	
	
	<!-- first version of tag -->
	
	<xsl:template match="a[@bu:target]">
		<xsl:variable name="loc" select="generate-id()"/>
		<xsl:variable name="verb" select="substring-before(@bu:target,'#')"/>
		<xsl:variable name="id" select="substring-after(@bu:target,'#')"/>
		<xsl:variable name="params" select="substring-after(@href,'?')"/>
		<xsl:variable name="url">
			<xsl:choose>
				<xsl:when test="$params != ''"><xsl:value-of select="substring-before(@href,'?')"/></xsl:when>
				<xsl:otherwise><xsl:value-of select="@href"/></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="insertion">
			<xsl:choose>
				<xsl:when test="$verb = 'insert-before'">Insertion.Before</xsl:when>
				<xsl:when test="$verb = 'insert-after'">Insertion.After</xsl:when>
				<xsl:when test="$verb = 'insert-top'">Insertion.Top</xsl:when>
				<xsl:when test="$verb = 'insert-bottom'">Insertion.Bottom</xsl:when>
				<xsl:otherwise>Cocoon.Ajax.Insertion.Replace</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="effect-target">
			<xsl:choose>
				<xsl:when test="$verb = 'insert-before'">.previousSibling</xsl:when>
				<xsl:when test="$verb = 'insert-after'">.nextSibling</xsl:when>
				<xsl:when test="$verb = 'insert-top'">.firstChild</xsl:when>
				<xsl:when test="$verb = 'insert-bottom'">.lastChild</xsl:when>
				<xsl:otherwise></xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
    <script type="text/javascript">
      function ajax<xsl:value-of select="$loc"/>() { 
				var up = new Ajax.Updater(
					{success: "<xsl:value-of select="$id"/>"}, 
					"<xsl:value-of select="$url"/>", 
					{ 
						parameters: "<xsl:value-of select="$params"/>", 
						onFailure: Cocoon.Ajax.BrowserUpdater.handleError,
						onComplete: function(request) { new Effect.Highlight($("<xsl:value-of select="$id"/>")<xsl:value-of select="$effect-target"/>); },
						insertion: <xsl:value-of select="$insertion"/> 
					}
				);
      }
    </script>
		<a href="#" onclick="{concat('ajax',$loc,'();')}" title="{@title}"><xsl:apply-templates/></a>
	</xsl:template>
	
	
	<!-- Catches all unrecognised elements as they are-->
	<xsl:template match="*|@*|node()|text()" priority="-1">
		<xsl:copy><xsl:apply-templates select="*|@*|node()|text()"/></xsl:copy>
	</xsl:template>
</xsl:stylesheet>
