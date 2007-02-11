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
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="window">

<xsl:variable name="bgColor">
  <xsl:choose>
    <xsl:when test="@bgColor">
        <xsl:value-of select="@bgColor" />
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>#294563</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
</xsl:variable>

<table cellSpacing="0" cellpadding="0" width="100%">
	<tr vAlign="top">
		<td width="5" style="background-color:#294563;" valign="top"><img src="images/window-top-left.gif" border="0" width="5" height="5" alt="top-left"/></td>
		<td bgColor="{$bgColor}" valign="middle">
			<font>
				<xsl:attribute name="color">#ffffff</xsl:attribute>
				<xsl:attribute name="face">Arial</xsl:attribute>
				<xsl:attribute name="size">2</xsl:attribute>	
				<xsl:choose>
				    <xsl:when test="@title">
					    <b><xsl:value-of select="@title"/></b>
					</xsl:when>
				    <xsl:otherwise>
					    <b><xsl:value-of select="title"/></b>
					</xsl:otherwise>
				</xsl:choose>	
			</font>
	    </td>
		<td align="right" bgColor="{$bgColor}">
			<xsl:if test="basket-add-link">
				<a href="{basket-add-link}">
	  			    <img src="images/basket.gif" border="0" alt="Add Link"/>
	  			</a>
			</xsl:if>
			<xsl:if test="basket-add-content">
				<a href="{basket-add-content}">
	  			    <img src="images/basket.gif" border="0" alt="Add Content"/>
	  			</a>
			</xsl:if>
			<xsl:if test="edit-uri">
				<a href="{edit-uri}">
	  			    <img src="images/edit.gif" border="0" alt="Edit"/>
	  			</a>
			</xsl:if>
			<xsl:if test="help-uri">
				<a href="{help-uri}">
	  			    <img src="images/help.gif" border="0" alt="Help"/>
	  			</a>
			</xsl:if>
			<xsl:if test="view-uri">
				<a href="{view-uri}">
	  			    <img src="images/view.gif" border="0" alt="View"/>
	  			</a>
			</xsl:if>
			<xsl:if test="fullscreen-uri and not(maximize-uri)">
				<a href="{fullscreen-uri}">
	  			    <img src="images/customize.gif" border="0" alt="Full Screen"/>
	  			</a>
			</xsl:if>
			<xsl:if test="maxpage-uri">
				<a href="{maxpage-uri}">
	  			    <img src="images/show.gif" border="0" alt="Max Page"/>
	  			</a>
			</xsl:if>
			<xsl:if test="minpage-uri">
				<a href="{minpage-uri}">
	  			    <img src="images/show.gif" border="0" alt="Min Page"/>
	  			</a>
			</xsl:if>
			<xsl:if test="maximize-uri">
				<a href="{maximize-uri}">
	  			    <img src="images/maximize.gif" border="0" alt="Maximize"/>
	  			</a>
			</xsl:if>
			<xsl:if test="minimize-uri">
				<a href="{minimize-uri}">
	  			    <img src="images/minimize.gif" border="0" alt="Minimize"/>
	  			</a>
			</xsl:if>
			<xsl:if test="remove-uri">
				<a href="{remove-uri}">
	  			    <img src="images/delete.gif" border="0" alt="Delete"/>
	  			</a>
			</xsl:if>
		</td>
		<td width="5" style="background-color:#294563;" valign="top"><img src="images/window-top-right.gif" border="0" width="5" height="5" alt="top-right"/></td>
	</tr>
	<tr>
		<td width="5" style="background-image:url(images/window-border-left.gif);"><img src="images/space.gif" border="0" width="5" height="5" alt="space"/></td>
		<td colSpan="2">
               <xsl:apply-templates select="content"/>
		</td>
		<td width="5" style="background-image:url(images/window-border-right.gif);"><img src="images/space.gif" border="0" width="5" height="5" alt="space"/></td>
	</tr>
	<tr>
		<td width="5"><img src="images/window-bottom-left.gif" border="0" width="5" height="5" alt="top-left"/></td>
		<td colspan="2" style="background-image:url(images/window-border-bottom.gif);"></td>
		<td width="5"><img src="images/window-bottom-right.gif" border="0" width="5" height="5" alt="top-left"/></td>
	</tr>
</table>
</xsl:template>

<xsl:template match="content">
    <xsl:apply-templates/>
</xsl:template>

<!-- Copy all and apply templates -->
<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
