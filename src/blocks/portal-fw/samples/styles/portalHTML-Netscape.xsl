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

<!-- $Id: portalHTML-Netscape.xsl,v 1.3 2004/03/06 02:25:39 antonio Exp $ 

 Description: Portal to HTML - Alternative for netscape (swapping coplet icons and title)

-->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- The main element -->

<xsl:template match="portal">
<html>
	<head>
    	<title>Portal</title>
    	</head>
	<body text="#0B2A51" link="#0B2A51" vlink="#666666" bgcolor="#cccccc">
		<xsl:attribute name="bgcolor"><xsl:value-of select="layout/portal/background/color"/></xsl:attribute>
		<xsl:attribute name="text"><xsl:value-of select="layout/portal/font/color"/></xsl:attribute>
		<table border="0" cellPadding="0" cellSpacing="0" width="100%" height="100%">
		<xsl:attribute name="bgcolor"><xsl:value-of select="layout/portal/background/color"/></xsl:attribute>
			<tr>
				<xsl:if test="header">
					<td noWrap="" width="193" valign="top" rowspan="2" bgcolor="cccccc">
			     		<xsl:apply-templates select="header"/>
					</td>
				</xsl:if>
				<td valign="top">
		  			<xsl:apply-templates select="columns"/>
			                </td>
			</tr>
			<tr>
				<td>
					<xsl:if test="footer">
						<xsl:apply-templates select="footer"/>
					</xsl:if>
				</td>
			</tr>
		</table>
	</body>
</html>
</xsl:template>

<!-- the header -->

<xsl:template match="header">
	<img height="2" src="sunspotdemoimg-space.gif" width="1"/>
	<table border="0" cellPadding="0" cellSpacing="0" width="100%" bgcolor="#ffffff"><tbody>
		<tr>
			<td valign="top">
				<table border="0" cellPadding="0" cellSpacing="2" width="100%"><tbody>
					<tr>
						<td noWrap="" width="1%" bgcolor="#46627A" >
							<img src="sunspotdemoimg-space.gif" width="15" height="1"/>
						</td>
						<td width="99%" bgcolor="#46627A">
							<font face="Arial, Helvetica, sans-serif" size="2" color="#FFFFFF">
								<b><img height="1" src="sunspotdemoimg-space.gif" width="5"/>Cocoon Portal</b>
							</font>
						</td>
					</tr>
					<tr>
						<td noWrap="" width="1%" bgcolor="#cccccc">
							<img src="sunspotdemoimg-space.gif"/>
						</td>
						<td valign="top" bgcolor="#cccccc">
							<br/>
							<img height="1" src="sunspotdemoimg-space.gif" width="10"/>
           							<font face="Arial, Helvetica, sans-serif" size="2" color="#0B2A51">
								<b>
									<xsl:value-of select="ancestor::portal/personal-profile/greeting"/>
								</b>
								<br/><br/>
								<xsl:if test="coplet">
									<xsl:apply-templates select="coplet"/>
								</xsl:if>
                						</font>
							<br/>
						</td>
					</tr>
				</tbody></table>
			</td>
		</tr>
	</tbody></table>
</xsl:template>

<!-- the footer -->

<xsl:template match="footer">
	<xsl:apply-templates/>
</xsl:template>

<!-- The content of the portal -->

<xsl:template match="columns">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
	<tr>
		<td><img src="sunspotdemoimg-space.gif" width="10"/></td>
	</tr>
	<tr>
		<td><img src="sunspotdemoimg-space.gif" width="10"/></td>
		<xsl:for-each select="column">
				<xsl:sort select="@position"/>
				<xsl:apply-templates select="."/>
		</xsl:for-each>
		<td><img src="sunspotdemoimg-space.gif" width="10"/></td>
	</tr>
</table>
</xsl:template>

<!-- The content of each column -->

<xsl:template match="column">
<td vAlign="top">
<xsl:attribute name="width"><xsl:value-of select="@width"/></xsl:attribute>
<table width="100%" border="0" cellspacing="0" cellpadding="0">
	<xsl:for-each select="coplet">
		<xsl:sort select="@position"/>
		<tr>
			<td><img src="sunspotdemoimg-space.gif" width="10"/></td>
			<td vAlign="top">
				<xsl:apply-templates select="."/>
			</td>
		</tr>
	</xsl:for-each>
</table>
</td>
</xsl:template>

<!-- One coplet -->

<xsl:template match="coplet">
	<xsl:variable name="cmd"><xsl:value-of select="ancestor::portal/configuration/uri"/>&amp;portalcmd=</xsl:variable>
	<xsl:variable name="copletident"><xsl:value-of select="@id"/>_<xsl:value-of select="@number"/></xsl:variable>
<table border="0" cellSpacing="0" width="100%">
	<tr vAlign="top">
		<td align="left">
			<xsl:attribute name="bgColor"><xsl:value-of select="ancestor::portal/layout/coplets/title/background/color"/></xsl:attribute>
			<font>
				<xsl:attribute name="color"><xsl:value-of select="ancestor::portal/layout/coplets/title/font/color"/></xsl:attribute>
				<xsl:attribute name="face"><xsl:value-of select="ancestor::portal/layout/coplets/title/font/type"/></xsl:attribute>
				<xsl:attribute name="size"><xsl:value-of select="ancestor::portal/layout/coplets/title/font/size"/></xsl:attribute>
				
				<!-- customize -->
				<xsl:if test="configuration/customizable='true' and (not(status/customize) or status/customize='false')">
					<xsl:variable name="customize"><xsl:value-of select="$cmd"/>customize_<xsl:value-of select="$copletident"/></xsl:variable>
					<a>
						<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($customize), ' ', '')"/></xsl:attribute>
						<img src="sunspotdemoimg-customize.gif" border="0" alt="Customize"/>
					</a>
				</xsl:if>
				
			<!-- minimize/maximize -->
			<xsl:if test="configuration/sizable='true' and status/size/@formpath">
				<xsl:variable name="linkurlmax"><xsl:value-of select="$cmd"/>minimize_<xsl:value-of select="$copletident"/></xsl:variable>
				<xsl:variable name="linkurlmin"><xsl:value-of select="$cmd"/>maximize_<xsl:value-of select="$copletident"/></xsl:variable>
				<a>
					<xsl:choose>
						<xsl:when test="status/size='max'">
							<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurlmax), ' ', '')"/></xsl:attribute>
							<img src="sunspotdemoimg-minimize.gif" border="0" alt="Minimize"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurlmin), ' ', '')"/></xsl:attribute>
							<img src="sunspotdemoimg-maximize.gif" border="0" alt="Maximize"/>
						</xsl:otherwise>
					</xsl:choose>
				</a>
			</xsl:if>
			<!-- show/ hide -->
			<xsl:choose>
				<xsl:when test="status/visible/@formpath and configuration/mandatory='false'">
					<xsl:variable name="linkurlshow"><xsl:value-of select="$cmd"/>show_<xsl:value-of select="$copletident"/></xsl:variable>
					<xsl:variable name="linkurlhide"><xsl:value-of select="$cmd"/>hide_<xsl:value-of select="$copletident"/></xsl:variable>
					<a>
						<xsl:choose>
							<xsl:when test="status/visible='true'">
								<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurlhide), ' ', '')"/></xsl:attribute>
								<img src="sunspotdemoimg-hide.gif" border="0" alt="Hide"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurlshow), ' ', '')"/></xsl:attribute>
								<img src="sunspotdemoimg-show.gif" border="0" alt="Show"/>
							</xsl:otherwise>
						</xsl:choose>
					</a>
				</xsl:when>
				<xsl:otherwise>
					<img src="sunspotdemoimg-space.gif" border="0"/>
				</xsl:otherwise>
			</xsl:choose>
			<!-- mandatory/delete -->
			<xsl:if test="configuration/mandatory='false'">
				<xsl:variable name="linkurlmand"><xsl:value-of select="$cmd"/>delete_<xsl:value-of select="$copletident"/></xsl:variable>
				<a>
					<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurlmand), ' ', '')"/></xsl:attribute>
					<img src="sunspotdemoimg-delete.gif" border="0" alt="Delete"/>
				</a>
			</xsl:if>
			</font>
		</td>
		<td align="right">
			<xsl:attribute name="bgColor"><xsl:value-of select="ancestor::portal/layout/coplets/title/background/color"/></xsl:attribute>
			<font>
				<xsl:attribute name="color"><xsl:value-of select="ancestor::portal/layout/coplets/title/font/color"/></xsl:attribute>
				<xsl:attribute name="face"><xsl:value-of select="ancestor::portal/layout/coplets/title/font/type"/></xsl:attribute>
				<xsl:attribute name="size"><xsl:value-of select="ancestor::portal/layout/coplets/title/font/size"/></xsl:attribute>	
				<img height="1" src="sunspotdemoimg-space.gif" width="5"/>				
				<b><xsl:value-of select="title"/></b>
			</font>
		</td>
	</tr>
	<xsl:if test="content">
		<tr>
			<td colSpan="2">
			<font>
				<xsl:attribute name="color"><xsl:value-of select="ancestor::portal/layout/coplets/content/font/color"/></xsl:attribute>
				<xsl:attribute name="face"><xsl:value-of select="ancestor::portal/layout/coplets/content/font/type"/></xsl:attribute>
				<xsl:attribute name="size"><xsl:value-of select="ancestor::portal/layout/coplets/content/font/size"/></xsl:attribute>
				<xsl:apply-templates select="content"/>
			</font>
			</td>
		</tr>
	</xsl:if>
</table>
</xsl:template>

<xsl:template match="content">
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="td">
	<td><xsl:for-each select="@*"><xsl:copy/></xsl:for-each>
	<xsl:if test="not(font) and not(FONT)">
		<font>
			<xsl:attribute name="color"><xsl:value-of select="ancestor::portal/layout/coplets/content/font/color"/></xsl:attribute>
			<xsl:attribute name="face"><xsl:value-of select="ancestor::portal/layout/coplets/content/font/type"/></xsl:attribute>
			<xsl:attribute name="size"><xsl:value-of select="ancestor::portal/layout/coplets/content/font/size"/></xsl:attribute>
			<xsl:apply-templates/>
		</font>
	</xsl:if>
	<xsl:if test="font or FONT">
		<xsl:apply-templates/>
	</xsl:if></td>
</xsl:template>

<xsl:template match="TD">
	<td><xsl:for-each select="@*"><xsl:copy/></xsl:for-each>
            <xsl:if test="not(font) and not(FONT)">
		<font>
			<xsl:attribute name="color"><xsl:value-of select="ancestor::portal/layout/coplets/content/font/color"/></xsl:attribute>
			<xsl:attribute name="face"><xsl:value-of select="ancestor::portal/layout/coplets/content/font/type"/></xsl:attribute>
			<xsl:attribute name="size"><xsl:value-of select="ancestor::portal/layout/coplets/content/font/size"/></xsl:attribute>
			<xsl:apply-templates/>
		</font>
	</xsl:if>
	<xsl:if test="font or FONT">
		<xsl:apply-templates/>
	</xsl:if></td>
</xsl:template>


  <xsl:template match="link">
    <a>
        <xsl:if test="target">
            <xsl:attribute name="target"><xsl:value-of select="normalize-space(target)"/></xsl:attribute>
        </xsl:if>     
        <xsl:attribute name="href"><xsl:value-of select="normalize-space(url)"/></xsl:attribute>
       <xsl:value-of select="normalize-space(text)"/>
    </a>
  </xsl:template>

  <!-- Copy all and apply templates -->
  <xsl:template match="@*|node()">
   <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
   </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
