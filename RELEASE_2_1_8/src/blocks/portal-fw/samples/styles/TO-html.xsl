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

<!-- $Id: TO-html.xsl,v 1.3 2004/03/06 02:25:39 antonio Exp $ 

-->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="page">
   <html>
    <head>
     <title>
      <xsl:if test="@title"><xsl:value-of select="@title"/></xsl:if>
      <xsl:if test="not(@title)">Cocoon Portal</xsl:if>
     </title>
     <xsl:if test="@logout">
  	 <script language="JavaScript"> 
		function timeout() {
			window.setTimeout("test()", 1500); 
		}
		function test() {  
  			top.location.href = "sunspotdemofree-portal";
		} 
	 </script> 
     </xsl:if>
    </head>
     <xsl:apply-templates/>
   </html>
  </xsl:template>

<xsl:template match="content">
    <body text="#0B2A51" link="#0B2A51" vlink="#666666">
		<xsl:apply-templates select="@*"/>
      <xsl:if test="/page/@logout">
        <xsl:attribute name="onLoad">timeout()</xsl:attribute>
      </xsl:if>
      <xsl:if test="/page/@bgcolor">
        <xsl:attribute name="bgcolor"><xsl:value-of select="/page/@bgcolor"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="/page/@textcolor">
        <xsl:attribute name="text"><xsl:value-of select="/page/@textcolor"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="/page/@background">
        <xsl:attribute name="background"><xsl:value-of select="/page/@background"/></xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>										
    </body>
</xsl:template>

<xsl:template match="logoutcontent">
    <body text="#0B2A51" link="#0B2A51" vlink="#666666">
		<xsl:apply-templates select="@*"/>
      <xsl:if test="/page/@logout">
        <xsl:attribute name="onLoad">timeout()</xsl:attribute>
      </xsl:if>
      <xsl:if test="/page/@bgcolor">
        <xsl:attribute name="bgcolor"><xsl:value-of select="/page/@bgcolor"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="/page/@textcolor">
        <xsl:attribute name="text"><xsl:value-of select="/page/@textcolor"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="/page/@background">
        <xsl:attribute name="background"><xsl:value-of select="/page/@background"/></xsl:attribute>
      </xsl:if>
	<table border="0" cellPadding="0" cellSpacing="0" height="100%" width="100%">
			<tr>
				<td height="100%" noWrap="" width="193" valign="top" bgcolor="#46627a">
					<img height="2" src="sunspotdemoimg-space.gif" width="1"/>
				</td>
				<td>
					<table border="0" width="100%" cellspacing="0" cellpadding="0">
						<xsl:attribute name="bgcolor">
							<xsl:value-of select="portalconf/layout-profile/portal/layouts/layout/background/color"/>
						</xsl:attribute>
							
							<tr>
								<td width="1%"><img src="sunspotdemoimg-space.gif" width="20" height="1"/></td>
								<td align="center">
									<img src="sunspotdemoimg-space.gif" height="20" width="1"/>
									<table border="0" width="100%">
										<tr>
											<td>	
      <xsl:apply-templates/>	
	</td>
										</tr>
									</table>
								</td>
								<td><img src="sunspotdemoimg-space.gif" width="20"/></td>
							</tr>
						</table>
					</td>
				</tr>
		</table>									
    </body>
</xsl:template>

  <xsl:template match="paragraph">
    <xsl:if test="@title">
      <font face="Arial, Helvetica, sans-serif" size="3">
	  	<b>
        <xsl:value-of select="@title"/>
		</b>
      </font>
    </xsl:if>								
	<p>
   	<font face="Arial, Helvetica, sans-serif" size="2">
 			<xsl:apply-templates/>	
	 </font>
   </p>
  </xsl:template>

  <xsl:template match="logoutparagraph">							
	<p>
   	<font face="Arial, Helvetica, sans-serif" size="2">
 			<xsl:apply-templates/>	
	 </font>
   </p>
  </xsl:template>

  <xsl:template match="pageset">
      <frameset border="0" frameborder="0" framespacing="0" noresize="">
        <xsl:if test="@rows">
          <xsl:attribute name="rows"><xsl:value-of select="@rows"/></xsl:attribute>
        </xsl:if>
        <xsl:if test="@columns">
          <xsl:attribute name="cols"><xsl:value-of select="@columns"/></xsl:attribute>
        </xsl:if>
        <xsl:apply-templates/>
      </frameset>
  </xsl:template>

  <xsl:template match="pagepart">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="part">  
    <frame frameborder="0" border="0" noresize="" marginHeight="0" marginwidth="0">
      <xsl:attribute name="src"><xsl:value-of select="normalize-space(url)"/></xsl:attribute>
      <xsl:attribute name="name"><xsl:value-of select="@title"/></xsl:attribute>
      <xsl:if test="@scrolling">
        <xsl:attribute name="scrolling"><xsl:value-of select="@scrolling"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="@noresize">
        <xsl:attribute name="noresize"><xsl:value-of select="@noresize"/></xsl:attribute>
      </xsl:if>
    </frame>  
  </xsl:template>

  <xsl:template match="picture">
    <xsl:if test="action">
      <a href="{action/@url}">
        <img border="0">
          <xsl:attribute name="src"><xsl:value-of select="@url"/></xsl:attribute>
        </img>
      </a>
    </xsl:if>
    <xsl:if test="not(action)">    
      <img>
        <xsl:attribute name="src"><xsl:value-of select="@url"/></xsl:attribute>
      </img>
     </xsl:if>
  </xsl:template>

  <xsl:template match="picturedyn">
    <xsl:if test="action">
      <a href="{action/@url}">
        <img border="0">
          <xsl:attribute name="src"><xsl:value-of select="normalize-space(url)"/></xsl:attribute>
        </img>
      </a>
    </xsl:if>
    <xsl:if test="not(action)">    
      <img border="0">
        <xsl:attribute name="src"><xsl:value-of select="normalize-space(url)"/></xsl:attribute>
      </img>
     </xsl:if>
  </xsl:template>

  <xsl:template match="link">
    <a>
      <xsl:choose>
        <xsl:when test="starts-with(normalize-space(url), 'http')">
          <xsl:attribute name="target">_new</xsl:attribute>
        </xsl:when>
        <xsl:when test="target">
            <xsl:attribute name="target"><xsl:value-of select="normalize-space(target)"/></xsl:attribute>
        </xsl:when>     
        <xsl:otherwise>
          <xsl:attribute name="target">Main</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
    
      <xsl:attribute name="href"><xsl:value-of select="normalize-space(url)"/></xsl:attribute>
      <xsl:value-of select="normalize-space(text)"/>
    </a>
  </xsl:template>

  <xsl:template match="field">
	<td>
	<xsl:value-of select="@name"/>
	</td>
	<td>
		<input type="text" size="30">
        	<xsl:attribute name="name"><xsl:value-of select="@field"/></xsl:attribute>
        	<xsl:attribute name="value"><xsl:value-of select="@value"/></xsl:attribute>
		</input>
	</td>
  </xsl:template>

  <!-- Copy all and apply templates -->
  <xsl:template match="@*|node()">
   <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
   </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
