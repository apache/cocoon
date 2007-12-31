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

<xsl:param name="user"/>
<xsl:param name="title"/>
<xsl:param name="base"/>

<xsl:template match="/">
  <html>
    <head>
			<title><xsl:value-of select="$title"/></title>
      <link type="text/css" rel="stylesheet" href="{$base}css/page.css"/>
    </head>
    <body>
      <table bgColor="#ffffff" border="0" cellPadding="0" cellSpacing="0" width="100%"><tbody> 
        <!-- header row -->
        <tr>
          <td colspan="2"> 
            <table border="0" cellPadding="0" cellSpacing="0" width="100%"><tbody> 
              <tr>
                <td colspan="2" noWrap="" height="1%" bgcolor="#294563">
                  <img height="5" src="{$base}images/space.gif" width="100%"/>
                </td>
              </tr>
              <tr> 
                <td colspan="2" bgcolor="#294563" height="98%" align="center" valign="middle" width="100%">
                  <img src="{$base}images/portal-logo.gif" width="250" height="90" />
                </td>
              </tr>
              <tr valign="bottom"> 
                <td height="99%" bgcolor="#294563" width="99%" align="right">
                  <xsl:if test="$user!='anonymous'">
                    <a href="{$base}logout"><img src="{$base}images/logout-door.gif" width="18" height="22" border="0"/></a>
                  </xsl:if>
                  <img height="5" src="{$base}images/space.gif" width="5"/>
                </td>
                <td height="99%" bgcolor="#294563" width="1%" align="right">
                  <xsl:if test="$user!='anonymous'">
                    <a href="{$base}logout" style="color:#CFDCED;font-size:75%;">Logout</a>&#160;
										<a href="{$base}tools/" style="color:#CFDCED;font-size:75%;">Tools</a>&#160;
                  </xsl:if>
                  <img height="5" src="{$base}images/space.gif" width="5"/>
                </td>
              </tr>
            </tbody></table>
          </td>
        </tr>
        <!-- end header row -->
        <!-- content/tab row -->
        <tr>
          <td>
            <xsl:apply-templates/>
          </td>
        </tr>
        <!-- end content/tab row -->
        <!-- footer row -->
        <tr>
          <td colspan="2"> 
            <table border="0" cellPadding="0" cellSpacing="0" width="100%"><tbody> 
              <tr> 
                <td colspan="2" noWrap="" height="30" bgcolor="#294563">
                  <img height="1" src="{$base}images/space.gif" width="1"/>
                </td>
              </tr>
            </tbody></table>
          </td>
        </tr>
        <!-- end of footer row -->
      </tbody></table>
    </body>
  </html>

</xsl:template>

<!-- make links relative -->
<xsl:template match="a[not(@target)]">
	<a><xsl:apply-templates select="@*"/><xsl:attribute name="href"><xsl:value-of select="concat($base,@href)"/></xsl:attribute><xsl:apply-templates/></a>
</xsl:template>
<!-- make images relative -->
<xsl:template match="img">
	<img>
		<xsl:apply-templates select="@*"/>
		<xsl:attribute name="src"><xsl:value-of select="concat($base,@src)"/></xsl:attribute>
	</img>
</xsl:template>

<!-- Copy all and apply templates -->

<xsl:template match="@*|node()">
   <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
   </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
