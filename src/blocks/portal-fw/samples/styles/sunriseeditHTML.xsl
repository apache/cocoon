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

<!-- $Id: sunriseeditHTML.xsl,v 1.3 2004/03/06 02:25:39 antonio Exp $ 

 Description: Portal User Management to HTML. This stylesheet is used
              if the user changes his own information.

-->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="configuration">
	<xsl:variable name="role" select="normalize-space(role)"/>

<html>
<head>
    <title>Portal User Managemenet</title>
</head>
<body text="#0B2A51" link="#0B2A51" vlink="#666666" bgColor="#ffffff">
<table border="0" cellPadding="0" cellSpacing="0" height="100%" width="100%">
	<tr>

<!-- menue -->
		<td height="100%" noWrap="" width="193" valign="top" bgcolor="#cccccc">
		<img height="2" src="sunspotdemoimg-space.gif" width="1"/>
			<table bgColor="#ffffff" border="0" cellPadding="0" cellSpacing="0" width="100%">
				<tr>
					<td>
						<table bgColor="#ffffff" border="0" cellPadding="0" cellSpacing="2" width="100%">
							<tr>
								<td bgcolor="#46627A" width="1%">
									<img src="sunspotdemoimg-space.gif" width="20" height="40"/>
								</td>
								<td bgcolor="#cccccc">
									<br/>
									<font face="Arial, Helvetica, sans-serif" size="2">
										<b><a target="_top" href="sunspotdemo-portal">
											<blockquote>Your Portal</blockquote>
										</a></b>
									</font>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</td>
<!-- /menue -->
		<td>
<!-- content -->
            		<table border="0" cellPadding="0" cellSpacing="2" height="100%" width="100%">
              			<tbody>
					<tr>
						<td><img src="sunspotdemoimg-space.gif" width="10"/></td>
                  				<td valign="top"><img src="sunspotdemoimg-space.gif" height="10"/><br/>
                                                	<table cellpadding="2" cellspacing="0" border="0" bgcolor="#46627A">
								<tr>
									<td>
										<table cellpadding="0" cellspacing="0" border="0" bgcolor="#ffffff">
											<tr>
												<td colspan="5"><img src="sunspotdemoimg-space.gif" height="10"/></td>
											</tr>
											<xsl:variable name="user" select="normalize-space(user)"/>
											<xsl:variable name="linkurl"><xsl:value-of select="normalize-space(uri)"/><xsl:value-of select="urlrewrite"/></xsl:variable>
											<form method="post" action="{translate(normalize-space($linkurl), ' ', '')}">
												<input type="hidden" name="authstate" value="chguser"/>
					                            	        	<input type="hidden" name="authrole" value="{$role}"/>
												<input type="hidden" name="authuser" value="{normalize-space(user)}"/>
												<input type="hidden" name="autholdrole" value="{$role}"/>
												<input type="hidden" name="autholdpassword" value="{normalize-space(uservalues/data/password)}"/>
												<tr>
													<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
													<td width="1%">
														<img src="sunspotdemoimg-kast_o.gif"/>
													</td>
													<td width="1%" colspan="2">
														<font face="Arial, Helvetica, sans-serif" size="2">
															<b>User Information</b>
														</font>
													</td>
													<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
												</tr>
												<tr>
													<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
													<td width="1%" background="sunspotdemoimg-line_bg.gif">
														<img src="sunspotdemoimg-space.gif"/>
													</td>
													<td width="1%">
														<font face="Arial, Helvetica, sans-serif" size="2">
															Title:
														</font>
													</td>
													<td>
														<img src="sunspotdemoimg-space.gif" width="10"/>
														<select name="title">
															<option><xsl:if test="normalize-space(uservalues/data/title)='Mr.'"><xsl:attribute name="selected">true</xsl:attribute></xsl:if>Mr.</option>
															<option><xsl:if test="normalize-space(uservalues/data/title)='Mrs.'"><xsl:attribute name="selected">true</xsl:attribute></xsl:if>Mrs.</option>
														</select>
													</td>
													<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
												</tr>
												<tr>
													<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
													<td width="1%" background="sunspotdemoimg-line_bg.gif">
														<img src="sunspotdemoimg-space.gif"/>
													</td>
													<td width="1%">
														<font face="Arial, Helvetica, sans-serif" size="2">
															Firstname:
														</font>
													</td>
													<td>
														<img src="sunspotdemoimg-space.gif" width="10"/>
														<input size="35" type="text" name="firstname" value="{normalize-space(uservalues/data/firstname)}"/>
													</td>
													<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
												</tr>
												<tr>
													<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
													<td width="1%" background="sunspotdemoimg-line_bg.gif">
														<img src="sunspotdemoimg-space.gif"/>
													</td>
													<td width="1%">
														<font face="Arial, Helvetica, sans-serif" size="2">
															Lastname:
														</font>
													</td>
													<td>
														<img src="sunspotdemoimg-space.gif" width="10"/>
														<input size="35" type="text" name="lastname" value="{normalize-space(uservalues/data/lastname)}"/>
													</td>
													<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
												</tr>
												<tr>
													<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
													<td width="1%" background="sunspotdemoimg-line_bg.gif">
														<img src="sunspotdemoimg-space.gif"/>
													</td>
													<td width="1%">
														<font face="Arial, Helvetica, sans-serif" size="2">
															Login:
														</font>
													</td>
													<td>
														<img src="sunspotdemoimg-space.gif" width="10"/>
														<font face="Arial, Helvetica, sans-serif" size="2">
															<xsl:value-of select="uservalues/data/ID"/>
															<input type="hidden" name="authid" value="{normalize-space(uservalues/data/ID)}"/>
														</font>
													</td>
													<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
												</tr>
												<tr>
													<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
													<td width="1%" background="sunspotdemoimg-line_bg.gif">
														<img src="sunspotdemoimg-space.gif"/>
													</td>
													<td width="1%">
														<font face="Arial, Helvetica, sans-serif" size="2">
															Password:
														</font>
													</td>
													<td>
														<img src="sunspotdemoimg-space.gif" width="10"/>
														<input size="35" type="password" name="password" value="{normalize-space(uservalues/data/password)}"/>
													</td>
													<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
												</tr>
												<tr>
													<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
													<td width="1%" background="sunspotdemoimg-line_bg.gif" valign="bottom">
														<img src="sunspotdemoimg-line_end.gif"/>
													</td>
													<td colspan="2"><img src="sunspotdemoimg-space.gif" height="20"/></td>
													<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
												</tr>
												<tr>
													<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
													<td width="1%">
														<img src="sunspotdemoimg-kast_url_u.gif"/>
													</td>
													<td width="1%"><img src="sunspotdemoimg-space.gif" width="10"/></td>
													<td>
														<img src="sunspotdemoimg-space.gif" width="10"/>
														<input type="submit" name="Change" value="Change"/>
													</td>
													<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
												</tr>
												</form>
												<tr>
													<td colspan="5"><img src="sunspotdemoimg-space.gif" height="10"/></td>
												</tr>
											</table>
										</td>
									</tr>
								</table>
								<img src="sunspotdemoimg-space.gif" height="20"/>
							</td>
                				</tr>
					</tbody>
				</table>
			</td>
<!-- /content -->

		</tr>
	</table>
</body>
</html>
</xsl:template>

  <!-- Copy all and apply templates -->
  <xsl:template match="@*|node()">
   <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
   </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
