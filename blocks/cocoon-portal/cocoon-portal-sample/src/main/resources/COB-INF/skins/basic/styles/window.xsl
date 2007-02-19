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
<!-- SVN $Id$ -->
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="window">

<xsl:variable name="bgColor">
  <xsl:choose>
    <xsl:when test="@bgColor">
        <xsl:value-of select="@bgColor" />
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>#CCCCCC</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
</xsl:variable>

<table border="2" cellSpacing="0" cellpadding="0" width="100%">
	<tr vAlign="top">
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
            <xsl:if test="fullscreen-uri and not(normal-uri)">
                <a href="{fullscreen-uri}">
                  <img src="images/customize.gif" border="0" alt="Full Screen"/>
                </a>
            </xsl:if>
            <xsl:if test="maximize-uri">
                <a href="{maximize-uri}">
                  <img src="images/show.gif" border="0" alt="Maximize"/>
                </a>
            </xsl:if>
            <xsl:if test="normal-uri">
                <a href="{normal-uri}">
                  <img src="images/maximize.gif" border="0" alt="Normal"/>
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
	</tr>
	<tr>
		<td colSpan="2">
               <xsl:apply-templates select="content"/>
		</td>
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
