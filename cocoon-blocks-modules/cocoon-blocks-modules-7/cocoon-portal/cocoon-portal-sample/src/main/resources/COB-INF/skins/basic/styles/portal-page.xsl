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
  <xsl:param name="skin"/>
  <xsl:param name="rsrcprefix"/>

<xsl:template match="/">
  <html>
    <head>
      <title><xsl:value-of select="$title"/></title>
      <link type="text/css" rel="stylesheet" href="{$rsrcprefix}skin/{$skin}/css/page.css"/>
      <link type="text/css" rel="stylesheet" href="{$rsrcprefix}skin/{$skin}/css/wsrp.css"/>
	</head>
	<body>
	<table bgColor="#ffffff" border="0" cellPadding="0" cellSpacing="0" width="100%">
	<tbody> 
	<!-- header row -->
	<tr>
	<td colspan="2"> 
		<table border="2" cellPadding="0" cellSpacing="0" width="100%">
		<tbody> 
			<tr> 
				<td colspan="2" noWrap="" height="10" bgcolor="#DDDDDD">
				</td>
			</tr>
			<tr> 
				<td bgcolor="#CCCCCC" height="100" align="center" 
				valign="middle" width="100%">
					<font size="80pt">Cocoon Portal</font>
				</td>
			</tr>
			<tr> 
				<td colspan="2" noWrap="" height="10" bgcolor="#DDDDDD" align="right">
				    <xsl:if test="$user!='anonymous'">
						<a href="logout" style="color:#4C6C8F;font-size:75%;">
							Logout
						</a><br/>
						<a href="tools/" style="color:#4C6C8F;font-size:75%;">
							Tools
						</a>
				    </xsl:if>
				</td>
			</tr>
		</tbody>
		</table>
	</td>
	</tr>
	<!-- end header row -->
	<!-- content/tab row -->
	<tr>
	<td>
      <xsl:copy-of select="*"/>
	</td>
	</tr>
	<!-- end content/tab row -->
	<!-- footer row -->
	<tr>
	<td colspan="2"> 
		<table border="2" cellPadding="0" cellSpacing="0" width="100%">
		<tbody> 
		<tr> 
			<td colspan="2" noWrap="" height="10" bgcolor="#DDDDDD">
				<img height="1" src="{$rsrcprefix}skin/{$skin}/images/space.gif" width="1"/>
			</td>
		</tr>
		<tr> 
			<td colspan="2" noWrap="" height="30" bgcolor="#CCCCCC">
				<img height="1" src="{$rsrcprefix}skin/{$skin}/images/space.gif" width="1"/>
			</td>
		</tr>
		</tbody>
		</table>
	</td>
	</tr>
	<!-- end footer row -->
	</tbody>
	</table>
	</body>
</html>

</xsl:template>
</xsl:stylesheet>
