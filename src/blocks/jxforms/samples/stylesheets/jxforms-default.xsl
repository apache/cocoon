<?xml version="1.0" encoding="UTF-8"?>
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
  This stylesheet merges a JXForms document into 
  a final document. It includes other presentational
  parts of a page orthogonal to the xmlform.

  author: Ivelin Ivanov, ivelin@apache.org, May 2002
  author: Konstantin Piroumian <kpiroumian@protek.com>, September 2002
  author: Simon Price <price@bristol.ac.uk>, September 2002

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xf="http://apache.org/cocoon/jxforms/1.0"
	exclude-result-prefixes="xalan" >

        <xsl:template match="/">
           <xsl:apply-templates />
        </xsl:template>

	<xsl:template match="xf:form">
		<xf:form method="post">
			<xsl:copy-of select="@*" />
			<br/>
			<table align="left" border="0">
				<tr>
					<td align="left" colspan="3" with="760">
						<h4 class="samplesGroup">
							<xsl:value-of select="xf:label"/>
						</h4>
						<br/>
					</td>
				</tr>
				<xsl:if test="count(error/xf:violation) > 0">
					<tr>
						<td align="left" colspan="3"
							class="{error/xf:violation[1]/@class}">
							<p>* There are [<b><xsl:value-of
								select="count(error/xf:violation)"/></b>] 
								errors. Please fix these errors and submit the
								form again.</p>
							<p>
								<xsl:variable name="localViolations"
									select=".//xf:*[ child::xf:violation ]"/>
								<xsl:for-each select="error/xf:violation">
									<xsl:variable name="eref" select="./@ref"/>
									<xsl:if
										test="count ($localViolations[ @ref=$eref ]) = 0"
										>* <xsl:value-of select="." /> <br/> </xsl:if>
								</xsl:for-each>
							</p>
							<p/>
						</td>
					</tr>
				</xsl:if>
				<xsl:for-each select="*[name() != 'xf:submit']">
					<xsl:choose>
						<xsl:when test="name() = 'error'"/>
						<xsl:when test="name() = 'xf:label'"/>
						<xsl:when test="xf:*">
							<xsl:apply-templates select="."/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:copy-of select="."/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
				<tr>
					<td align="center" colspan="3">
						<xsl:for-each select="*[name() = 'xf:submit']">
							<xsl:copy-of select="." />
							<xsl:text>
							</xsl:text>
						</xsl:for-each>
					</td>
				</tr>
			</table>
		</xf:form>
	</xsl:template>
	<xsl:template match="xf:repeat">
		<tr width="100%">
			<td colspan="3" width="100%">
				<table class="repeat">
					<xsl:apply-templates select="*"/>
				</table>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="xf:group">
		<tr width="100%">
			<td width="100%" colspan="2">
				<table class="group" border="0">
					<tr>
						<td align="left">
							<xsl:value-of select="xf:label" />
						</td>
					</tr>
					<xsl:apply-templates select="*"/>
				</table>
			</td>
		</tr>
	</xsl:template>
	<xsl:template match="xf:output[@form]">
	    <br clear="all"/>
	    <br/>
		<font size="-1">
			<code> <xsl:value-of select="xf:label" /> : <xsl:copy-of
				select="." /> </code>
		</font>
	</xsl:template>
	<xsl:template match="xf:label"/>
	<xsl:template match="xf:*">
		<tr>
			<td align="left" valign="top">
				<p class="label">
					<xsl:value-of select="xf:label" />
				</p>
			</td>
			<td align="left">
				<table class="plaintable">
					<tr>
						<td align="left">
							<xsl:copy-of select="." />
						</td>
						<xsl:if test="xf:violation">
							<td align="left" class="{xf:violation[1]/@class}"
								width="100%">
								<xsl:for-each select="xf:violation">* 
									<xsl:value-of select="." /> <br/> </xsl:for-each>
							</td>
						</xsl:if>
					</tr>
				</table>
				<xsl:if test="xf:help">
					<div class="help">
						<xsl:value-of select="xf:help" />
					</div>
					<br />
				</xsl:if>
			</td>
		</tr>
	</xsl:template>
   <!-- copy all the rest of the markup which is not recognized above -->
   <xsl:template match="*">
      <xsl:copy><xsl:copy-of select="@*" /><xsl:apply-templates /></xsl:copy>
   </xsl:template>

   <xsl:template match="text()">
      <xsl:value-of select="." />
   </xsl:template>

</xsl:stylesheet>
