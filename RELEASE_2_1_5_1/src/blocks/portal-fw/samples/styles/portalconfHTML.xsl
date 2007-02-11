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

<!-- $Id: portalconfHTML.xsl,v 1.4 2004/03/06 02:25:39 antonio Exp $ 

 Description: Portal Configuration to HTML

-->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Frameset -->

<xsl:template match="pageset">
	<frameset border="0" frameBorder="NO" frameSpacing="2">
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
	<frame frameborder="0" marginHeight="0" marginWidth="0" noResize="">
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

<!-- /Frameset -->

<xsl:template name="inputfield">
	<xsl:choose>
		<xsl:when test="@formtype='BOOLEAN'">
			<select>
				<xsl:attribute name="name"><xsl:value-of select="@formpath"/></xsl:attribute>
				<option value="true"><xsl:if test="normalize-space(.)='true'">
						<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>Yes
				</option>
				<option value="false"><xsl:if test="normalize-space(.)='false'">
					<xsl:attribute name="selected">true</xsl:attribute>
					</xsl:if>No
				</option>
			</select>
		</xsl:when>
		<xsl:when test="@formtype='CARDINAL'">
			<input>
				<xsl:attribute name="name"><xsl:value-of select="@formpath"/></xsl:attribute>
				<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
				<xsl:attribute name="type">text</xsl:attribute>
				<xsl:attribute name="size">10</xsl:attribute>
			</input>
		</xsl:when>
		<xsl:when test="@formtype='INTEGER'">
			<input>
				<xsl:attribute name="name"><xsl:value-of select="@formpath"/></xsl:attribute>
				<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
				<xsl:attribute name="type">text</xsl:attribute>
				<xsl:attribute name="size">10</xsl:attribute>
			</input>
		</xsl:when>
		<xsl:when test="@formtype='STRING'">
			<input>
				<xsl:attribute name="name"><xsl:value-of select="@formpath"/></xsl:attribute>
				<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
				<xsl:attribute name="type">text</xsl:attribute>
			</input>
		</xsl:when>
		<xsl:otherwise>
			<select>
				<xsl:attribute name="name"><xsl:value-of select="@formpath"/></xsl:attribute>
				<xsl:variable name="typename"><xsl:value-of select="@formtype"/></xsl:variable>
				<xsl:variable name="value" select="normalize-space(.)"/>
				<xsl:for-each select="ancestor::portalconf/typedefs/typedef[@name=$typename]/value">
					<option>
						<xsl:attribute name="value"><xsl:value-of select="normalize-space(.)"/></xsl:attribute>
						<xsl:if test="normalize-space(.)=$value">
							<xsl:attribute name="selected">true</xsl:attribute>
						</xsl:if>
						<xsl:value-of select="@name"/>
					</option>
				</xsl:for-each>
			</select>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<!-- Portal Configuration -->

<xsl:template match="portalconf">
<html>
<head>
    <title>Portal Configuration</title>
</head>
<body text="#0B2A51" link="#0B2A51" vlink="#666666">
<xsl:attribute name="bgcolor">
	<xsl:value-of select="layout-profile/portal/layouts/layout/background/color"/>
</xsl:attribute>

<table border="0" cellPadding="0" cellSpacing="0" height="100%" width="100%">
	<tr>

<!-- menue -->
		<td height="100%" noWrap="" width="193" valign="top" bgcolor="cccccc">
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
										<xsl:variable name="linkurl"><xsl:value-of select="configuration/portal"/>?portalprofile=<xsl:value-of select="configuration/profile"/></xsl:variable>
										<b><a target="_top">
											<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute>
											<blockquote>Your Portal</blockquote>
										</a></b>
									</font>
								</td>
							</tr>
							<tr>
								<td bgcolor="#46627A" width="1%">
									<img src="sunspotdemoimg-space.gif" width="20" height="40"/>
								</td>
								<td bgcolor="#cccccc">
									<Bbr/>
									<font face="Arial, Helvetica, sans-serif" size="2">
  										<xsl:variable name="linkurl"><xsl:value-of select="configuration/portal"/>?portalprofile=<xsl:value-of select="configuration/profile"/>&amp;portalcmd=save</xsl:variable>
										<b><a target="_top">
											<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute>
                                            <blockquote>Save</blockquote>					
										</a></b>
									</font>
									<font face="Arial, Helvetica, sans-serif" size="1">
                                                                                                                        <p align="center">If you change the portal layout, you first have to accept the changes with "Change Layout"</p>
									</font>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</td>
<!-- /menue -->

<!-- content -->
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
								<xsl:apply-templates select="layout-profile"/>
							</td>
						</tr>
						<tr>
							<td>
								<xsl:apply-templates select="portal-profile"/>
							</td>
						</tr>
						<tr>
							<td><img src="sunspotdemoimg-space.gif" height="10"/></td>
						</tr>
						<tr>
							<td>
								<xsl:apply-templates select="coplets-profile"/>
            				</td>
						</tr>
					</table>
				</td>
				<td><img src="sunspotdemoimg-space.gif" width="20"/></td>
				</tr></table></td>
<!-- /content -->

			</tr>
		</table>
	</body>
</html>
</xsl:template>

<!-- /Portal Configuration -->

<!-- Portal Administration -->

<xsl:template match="portaladminconf">
<html>
	<head>
		<title>Portal Administration</title>
	</head>
	<body text="#0B2A51" link="#0B2A51" vlink="#666666" bgColor="#cccccc">
		<table bgColor="#ffffff" border="0" cellPadding="0" cellSpacing="0" width="100%"><tbody>
        		<tr>
       			<td>
		            	<table border="0" cellPadding="0" cellSpacing="2" height="100%" width="100%"><tbody>

<!-- Header -->
					<tr>
		                  			<td bgcolor="#AAB9BF" noWrap="" colspan="3">
								 <img src="sunspotdemoimg-space.gif" height="5"/>
						</td>
					</tr>
                				<tr>
		                  			<td bgcolor="#AAB9BF" noWrap="">	
								<img src="sunspotdemoimg-logo.jpg"/>
                  					</td>
                  					<td bgcolor="#AAB9BF" valign="bottom" align="center" colspan="2">
                    							<font face="Arial, Helvetica, sans-serif" size="6" color="#46627A">
									<b>Portal Administration</b>
								</font>
		                  			</td>
                				</tr>
		                		<tr>
                  					<td noWrap="" width="10%" bgcolor="#cccccc">
								<img src="sunspotdemoimg-space.gif" height="10"/>
                  					</td>
		                  			<td width="90%" bgcolor="#cccccc" colspan="2">
								&#160;
						</td>
                				</tr>
<!-- /Header -->	

<!-- Hauptseite -->	
					<xsl:choose>
						<xsl:when test="state = 'main' or state = 'mainrole'">
							<tr>
<!-- Menue -->
     				      			<td bgcolor="#cccccc">
									<img src="sunspotdemoimg-space.gif"/>
								</td>

								<td>
									<img src="sunspotdemoimg-space.gif" width="10" height="1"/>
								</td>
		                 				<td align="center">
									<img src="sunspotdemoimg-space.gif" height="10"/>
									<table cellpadding="2" cellspacing="0" border="0" bgcolor="#46627A" width="60%">
										<tr>	
											<td>
												<table cellpadding="0" cellspacing="0" border="0" width="100%" bgcolor="#ffffff">
													<tr>
														<td colspan="4"><img src="sunspotdemoimg-space.gif" height="10"/></td>
													</tr>
													<tr>
														<td width="1%">
															<img src="sunspotdemoimg-space.gif" width="30"/>
														</td>
														<td width="1%"><img src="sunspotdemoimg-kast_m.gif"/></td>
														<td width="98%" colspan="2">
															<xsl:variable name="linkurl"><xsl:value-of select="configuration/uri"/>&amp;portaladmin=main&amp;portaladmin_coplets=cleancache</xsl:variable>
															<font face="Arial, Helvetica, sans-serif" size="2">
																<a><xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute>Clear Profile Cache</a>
															</font>
														</td>
													</tr>
													<tr>
														<td colspan="4">
															<img src="sunspotdemoimg-space.gif" height="10"/>
														</td>
													</tr>
												</table>
											</td>
										</tr>
									</table>
									<br/>
									<table cellpadding="2" cellspacing="0" border="0" bgcolor="#46627A" width="60%">
										<tr>	
											<td>
												<table cellpadding="0" cellspacing="0" border="0" width="100%" bgcolor="#ffffff">
													<tr>
														<td colspan="4"><img src="sunspotdemoimg-space.gif" height="10"/></td>
													</tr>
													<tr>
														<td width="1%">
															<img src="sunspotdemoimg-space.gif" width="30"/>
														</td>
														<td width="1%"><img src="sunspotdemoimg-kast_m.gif"/></td>
														<td width="98%" colspan="2">
															<xsl:variable name="linkurl"><xsl:value-of select="configuration/uri"/>&amp;portaladmin=coplets</xsl:variable>
															<font face="Arial, Helvetica, sans-serif" size="2">
																<a><xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute>Change coplet Profile</a>
															</font>
														</td>
													</tr>
													<tr>
														<td width="1%">
															<img src="sunspotdemoimg-space.gif" width="30"/>
														</td>
														<td width="1%"><img src="sunspotdemoimg-kast_m.gif"/></td>
														<td width="98%" colspan="2">
															<font face="Arial, Helvetica, sans-serif" size="2">
															     After you have changed the coplet Profile, you have to logout and login again, before you can edit other profiles.
															</font>
														</td>
													</tr>
													<tr>
														<td colspan="4">
															<img src="sunspotdemoimg-space.gif" height="10"/>
														</td>
													</tr>
												</table>
											</td>
										</tr>
									</table>
									<br/>
									<table cellpadding="2" cellspacing="0" border="0" bgcolor="#46627A" width="60%">
										<tr>	
											<td>
												<table cellpadding="0" cellspacing="0" border="0" width="100%" bgcolor="#ffffff">
													<tr>
														<td colspan="4"><img src="sunspotdemoimg-space.gif" height="10"/></td>
													</tr>
													<tr>
														<td width="1%">
															<img src="sunspotdemoimg-space.gif" width="30"/>
														</td>
														<td width="1%"><img src="sunspotdemoimg-kast_m.gif"/></td>
														<td width="98%" colspan="2">
															<font face="Arial, Helvetica, sans-serif" size="2">
															<xsl:attribute name="color">
																<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
															</xsl:attribute>
																<xsl:variable name="linkurl"><xsl:value-of select="configuration/uri"/>&amp;portaladmin=global</xsl:variable>
																<a><xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute>Change global Profile</a>
															</font>
														</td>
													</tr>
													<tr>
														<td width="1%">
															<img src="sunspotdemoimg-space.gif" width="30"/>
														</td>
														<td width="1%"><img src="sunspotdemoimg-kast_m.gif"/></td>
														<td width="98%" colspan="2">
															<font face="Arial, Helvetica, sans-serif" size="2">
															     After you have changed the global Profile, you have to logout and login again, before you can edit other profiles.
															</font>
														</td>
													</tr>
													<tr>
														<td colspan="4">
															<img src="sunspotdemoimg-space.gif" height="10"/>
														</td>
													</tr>
												</table>
											</td>
										</tr>
									</table>
									<br/>
									<table cellpadding="2" cellspacing="0" border="0" bgcolor="#46627A" width="60%">
										<tr>	
											<td>
												<table cellpadding="0" cellspacing="0" border="0" width="100%" bgcolor="#ffffff">
													<tr>
														<td colspan="4"><img src="sunspotdemoimg-space.gif" height="10"/></td>
													</tr>
													<xsl:variable name="acturl"><xsl:value-of select="configuration/uri"/>&amp;portaladmin=role</xsl:variable>
													<form method="post">
														<xsl:attribute name="action"><xsl:value-of select="translate(normalize-space($acturl), ' ', '')"/></xsl:attribute>
													<tr>
														<td width="1%">
															<img src="sunspotdemoimg-space.gif" width="30" height="1"/>
														</td>
														<td width="1%"><img src="sunspotdemoimg-kast_o.gif"/></td>
														<td colspan="2">
															<font face="Arial, Helvetica, sans-serif" size="2">
																<b>Change role profile</b>
															</font>
														</td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif">
															<img src="sunspotdemoimg-kast.gif"/>
														</td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																Rolename:
															</font>
														</td>
														<td width="97%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
															<select name="portalrole">
															<xsl:for-each select="roles/role">
															<option>
																<xsl:attribute name="value">
																	<xsl:value-of select="normalize-space(.)"/>
																</xsl:attribute>
																<xsl:value-of select="normalize-space(.)"/>
															</option>
															</xsl:for-each>
															</select>		
														</td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td background="sunspotdemoimg-line_bg.gif" width="1%">
															<img src="sunspotdemoimg-space.gif" height="20"/>
														</td>
														<td colspan="2"><img src="sunspotdemoimg-space.gif"/></td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%">
															<img src="sunspotdemoimg-kast_url_u.gif"/>
														</td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																<input type="submit" value="Change Role Profile"/>
															</font>
														</td>
														<td width="97%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
														</td>
													</tr>
												 	</form>
													<tr>
														<td width="1%">
															<img src="sunspotdemoimg-space.gif" width="30"/>
														</td>
														<td width="1%"><img src="sunspotdemoimg-kast_m.gif"/></td>
														<td width="98%" colspan="2">
															<font face="Arial, Helvetica, sans-serif" size="2">
															     After you have changed a Role Profile, you have to logout and login again, before you can edit other profiles.
															</font>
														</td>
													</tr>
													<tr>
														<td colspan="4">
															<img src="sunspotdemoimg-space.gif" height="10"/>
														</td>
													</tr>
												</table>
											</td>
										</tr>
									</table>
									<br/>
					
									<!-- Role selection for user selection -->
									<table cellpadding="2" cellspacing="0" border="0" bgcolor="#46627A" width="60%">
										<tr>	
											<td>
												<table cellpadding="0" cellspacing="0" border="0" width="100%" bgcolor="#ffffff">
													<tr>
														<td colspan="4"><img src="sunspotdemoimg-space.gif" height="10"/></td>
													</tr>
													<xsl:variable name="acturl"><xsl:value-of select="configuration/uri"/>&amp;portaladmin=mainrole</xsl:variable>
													<form method="post">
														<xsl:attribute name="action"><xsl:value-of select="translate(normalize-space($acturl), ' ', '')"/></xsl:attribute>
													<tr>
														<td width="1%">
															<img src="sunspotdemoimg-space.gif" width="30" height="1"/>
														</td>
														<td width="1%"><img src="sunspotdemoimg-kast_o.gif"/></td>
														<td colspan="2">
															<font face="Arial, Helvetica, sans-serif" size="2">
																<b>Change User Profile</b>
															</font>
														</td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif">
															<img src="sunspotdemoimg-kast.gif"/>
														</td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																Rolename:
															</font>
														</td>
														<td width="97%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
															<xsl:variable name="selectedrole"><xsl:value-of select="roleusers/name"/></xsl:variable>
															<xsl:variable name="selectedstate"><xsl:value-of select="state"/></xsl:variable>
															<select name="portalrole">
															<xsl:for-each select="roles/role">
															<option>
																<xsl:if test="normalize-space($selectedstate) = 'mainrole' and normalize-space($selectedrole) = normalize-space(.)">
																	<xsl:attribute name="selected">true</xsl:attribute>
																</xsl:if>
																<xsl:attribute name="value">
																	<xsl:value-of select="normalize-space(.)"/>
																</xsl:attribute>
																<xsl:value-of select="normalize-space(.)"/>
															</option>
															</xsl:for-each>
															</select>		
														</td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td background="sunspotdemoimg-line_bg.gif" width="1%">
															<img src="sunspotdemoimg-space.gif" height="20"/>
														</td>
														<td colspan="2"><img src="sunspotdemoimg-space.gif"/></td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%">
															<img src="sunspotdemoimg-kast_url_u.gif"/>
														</td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																<input type="submit" value="Select Role"/>
															</font>
														</td>
														<td width="97%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
														</td>
													</tr>
												 	</form>
													<xsl:if test="state = 'mainrole'">
														<xsl:variable name="acturl2"><xsl:value-of select="configuration/uri"/>&amp;portaladmin=user</xsl:variable>
														<form method="post">
															<xsl:attribute name="action"><xsl:value-of select="translate(normalize-space($acturl2), ' ', '')"/></xsl:attribute>
															<input name="portalrole" type="hidden">
																<xsl:attribute name="value"><xsl:value-of select="roleusers/name"/></xsl:attribute>
															</input>
															<tr>
																<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
																<td width="1%" background="sunspotdemoimg-line_bg.gif">
																	<img src="sunspotdemoimg-kast.gif"/>
																</td>
																<td width="1%">
																	<font face="Arial, Helvetica, sans-serif" size="2">
																		User:
																	</font>
																</td>
																<td width="97%">
																<img src="sunspotdemoimg-space.gif" width="10"/>
																<select name="portalid">
																<xsl:for-each select="roleusers/users/user">
																	<option>
																		<xsl:attribute name="value">
																			<xsl:value-of select="normalize-space(ID)"/>
																		</xsl:attribute>
																		<xsl:value-of select="normalize-space(ID)"/>
																	</option>
																</xsl:for-each>
																</select>		
																</td>
															</tr>
															<tr>
																<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
																<td background="sunspotdemoimg-line_bg.gif" width="1%">
																	<img src="sunspotdemoimg-space.gif" height="20"/>
																</td>
																<td colspan="2"><img src="sunspotdemoimg-space.gif"/></td>
															</tr>
															<tr>
																<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
																<td width="1%">
																	<img src="sunspotdemoimg-kast_url_u.gif"/>
																</td>
																<td width="1%">
																	<font face="Arial, Helvetica, sans-serif" size="2">
																		<input type="submit" value="Change User Profile"/>
																	</font>
																</td>
																<td width="97%">
																	<img src="sunspotdemoimg-space.gif" width="10"/>
																</td>
															</tr>
													 	</form>
														<tr>
															<td width="1%">
																<img src="sunspotdemoimg-space.gif" width="30"/>
															</td>
															<td width="1%"><img src="sunspotdemoimg-kast_m.gif"/></td>
															<td width="98%" colspan="2">
																<font face="Arial, Helvetica, sans-serif" size="2">
															     After you have changed a user Profile, you have to logout and login again, before you can edit other profiles.
																</font>
															</td>
														</tr>
													</xsl:if>
													<tr>
														<td colspan="4">
															<img src="sunspotdemoimg-space.gif" height="10"/>
														</td>
													</tr>
												</table>
											</td>
										</tr>
									</table>
									<br/>
								</td>
							</tr>
						</xsl:when>
<!--  /Hauptseite -->

<!--  Bearbeiten -->
						<xsl:when test="state='role' or state='user' or state='global'">
							<tr>

<!--  Menue -->
 								<td bgcolor="#cccccc" valign="top" width="193">
									<img src="sunspotdemoimg-space.gif" width="1" height="2"/>
									<table bgColor="#ffffff" border="0" cellPadding="0" cellSpacing="2" width="100%">
										<tr>
											<td bgcolor="#46627A" width="1%">
												<img src="sunspotdemoimg-space.gif" width="20" height="40"/>
											</td>
											<td bgcolor="#cccccc">
												<br/>
												<font face="Arial, Helvetica, sans-serif" size="2">
													<xsl:variable name="linkurl"><xsl:value-of select="configuration/uri"/>&amp;portaladmin=main</xsl:variable>
													<b><a>
														<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute>
														<blockquote>Main</blockquote>
													</a></b>
												</font>
											</td>
										</tr>
										<tr>
											<td bgcolor="#46627A" width="1%">
												<img src="sunspotdemoimg-space.gif" width="20" height="40"/>
											</td>
											<td bgcolor="#cccccc">
												<br/>
												<font face="Arial, Helvetica, sans-serif" size="2"><b>
													<xsl:variable name="linkurl"><xsl:value-of select="configuration/uri"/>&amp;portaladmin=main&amp;portalcmd=save</xsl:variable>
													<a><xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute>
														<blockquote>Save</blockquote>
													</a>
												</b></font>
									<font face="Arial, Helvetica, sans-serif" size="1">
                                           <p align="center">If you have changed the layout, you have to accept this first by "Change Layout".</p>
									</font>
											</td>
										</tr>
									</table>
								</td>
<!-- /Menue -->

<!-- Content -->
                  							<td align="center">
									<table border="0" cellPadding="0" cellSpacing="0" width="100%">
										<xsl:attribute name="bgcolor">
											<xsl:value-of select="portalconf/layout-profile/portal/layouts/layout/background/color"/>
										</xsl:attribute>
										<tr><td><img src="sunspotdemoimg-space.gif" width="1" height="20"/></td></tr>
										<tr>
											<td width="1%"><img src="sunspotdemoimg-space.gif" width="20"/></td>
											<td>
											<xsl:choose>
												<xsl:when test="state ='global'">
													<font face="Arial, Helvetica, sans-serif" size="5">
														<xsl:attribute name="color">
															<xsl:value-of select="portalconf/layout-profile/portal/layouts/layout/font/color"/>
														</xsl:attribute>
														<b>Global Profile</b>
													</font>
												</xsl:when>
												<xsl:when test="state ='role'">
													<font face="Arial, Helvetica, sans-serif" size="5">
														<xsl:attribute name="color">
															<xsl:value-of select="portalconf/layout-profile/portal/layouts/layout/font/color"/>
														</xsl:attribute>
														<b>Role Profile: <xsl:value-of select="role"/></b>
													</font>
												</xsl:when>
												<xsl:when test="state ='user'">
													<font face="Arial, Helvetica, sans-serif" size="5">
														<xsl:attribute name="color">
															<xsl:value-of select="portalconf/layout-profile/portal/layouts/layout/font/color"/>
														</xsl:attribute>															
														<b>User Profile: <xsl:value-of select="role"/>/<xsl:value-of select="id"/></b>
													</font>
												</xsl:when>
											</xsl:choose>
											<br/><br/>
											<table border="0" width="100%" cellpadding="0" cellspacing="0">
												<tr>
													<td>
														<xsl:apply-templates select="portalconf/layout-profile"/>
													</td>
												</tr>
												<tr>
													<td>
														<xsl:apply-templates select="portalconf/portal-profile"/>
													</td>
												</tr>
												<tr>
													<td>
														<img src="sunspotdemoimg-space.gif" height="10"/>
													</td>
												</tr>
												<tr>
													<td>
														<xsl:apply-templates select="portalconf/coplets-profile"/>
            			            								</td>
												</tr>
												<tr>
													<td>
														<img src="sunspotdemoimg-space.gif" height="10"/>
													</td>
												</tr>
												<tr>
													<td>
														<xsl:for-each select="portalconf/coplets-profile">
															<xsl:call-template name="admin_coplets-profile"/>
														</xsl:for-each>
            				            							</td>
												</tr>
											</table>
										</td>
										<td><img src="sunspotdemoimg-space.gif" width="20"/></td>
									</tr>
								</table>
							</td>
						</tr>
						</xsl:when>
						<xsl:when test="state='coplets'"> 
							<tr>

<!--  Menue -->
 								<td bgcolor="#cccccc" valign="top" width="193">
									<img src="sunspotdemoimg-space.gif" width="1" height="2"/>
									<table bgColor="#ffffff" border="0" cellPadding="0" cellSpacing="2" width="100%">
										<tr>
											<td bgcolor="#46627A" width="1%">
												<img src="sunspotdemoimg-space.gif" width="20" height="40"/>
											</td>
											<td bgcolor="#cccccc">
												<br/>
												<font face="Arial, Helvetica, sans-serif" size="2">
													<xsl:variable name="linkurl"><xsl:value-of select="configuration/uri"/>&amp;portaladmin=main</xsl:variable>
													<b><a>
														<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute>
														<blockquote>Main</blockquote>
													</a></b>
												</font>
											</td>
										</tr>
										<tr>
											<td bgcolor="#46627A" width="1%">
												<img src="sunspotdemoimg-space.gif" width="20" height="40"/>
											</td>
											<td bgcolor="#cccccc">
												<br/>
												<font face="Arial, Helvetica, sans-serif" size="2"><b>
													<xsl:variable name="linkurl"><xsl:value-of select="configuration/uri"/>&amp;portaladmin=main&amp;portaladmin_coplets=save</xsl:variable>
													<a>
														<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute>
														<blockquote>Save</blockquote>
													</a>
												</b></font>
											</td>
										</tr>
									</table>
								</td>
<!-- /Menue -->

<!-- Content -->
			                  	<td><img src="sunspotdemoimg-space.gif" height="1" width="10"/></td>
								<!-- Present list of coplets for editing-->
								<td align="center">
									<table cellpadding="2" cellspacing="0" border="0" bgcolor="#46627A" width="400" nowrap="">
										<tr>	
											<td>
												<table cellpadding="0" cellspacing="0" border="0" width="100%" bgcolor="#ffffff">
													<tr>
														<td colspan="4"><img src="sunspotdemoimg-space.gif" height="10" width="1"/></td>
													</tr>
													<xsl:variable name="acturl"><xsl:value-of select="configuration/uri"/>&amp;portaladmin=coplet</xsl:variable>
													<form method="post">
														<xsl:attribute name="action"><xsl:value-of select="translate(normalize-space($acturl), ' ', '')"/></xsl:attribute>
													<tr>
														<td width="1%">
															<img src="sunspotdemoimg-space.gif" width="30" height="1"/>
														</td>
														<td width="1%"><img src="sunspotdemoimg-kast_o.gif"/></td>
														<td colspan="2">
															<font face="Arial, Helvetica, sans-serif" size="2">
																<b>Edit coplet</b>
															</font>
														</td>
													</tr>
													<tr>
																	<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
																	<td width="1%" background="sunspotdemoimg-line_bg.gif">
																		<img src="sunspotdemoimg-kast.gif"/>
																	</td>
																	<td width="98%" colspan="2">
																		<font face="Arial, Helvetica, sans-serif" size="2">
																			coplet:
																		</font>
																		<img src="sunspotdemoimg-space.gif" width="10"/>
																		<select name="portalcoplet">
																		<xsl:for-each select="coplets/coplets-profile/coplets/coplet">
																		<option>
																			<xsl:attribute name="value">
																				<xsl:value-of select="normalize-space(@id)"/>
																			</xsl:attribute>
																			<xsl:value-of select="normalize-space(title)"/>
																		</option>
																		</xsl:for-each>
																		</select>		
																	</td>
																</tr>
																<tr>
																	<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
																	<td background="sunspotdemoimg-line_bg.gif" width="1%">
																		<img src="sunspotdemoimg-space.gif" height="20"/>
																	</td>
																	<td colspan="2"><img src="sunspotdemoimg-space.gif"/></td>
																</tr>
																<tr>
																	<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
																	<td width="1%">
																		<img src="sunspotdemoimg-kast_url_u.gif"/>
																	</td>
																	<td width="1%">
																		<font face="Arial, Helvetica, sans-serif" size="2">
																			<input type="submit" value="Change coplet"/>
																		</font>
																	</td>
																	<td width="97%">
																		<img src="sunspotdemoimg-space.gif" width="10"/>
																	</td>
																</tr>
												 				</form>
																<tr>
																	<td colspan="4">
																		<img src="sunspotdemoimg-space.gif" height="10"/>
																	</td>
																</tr>
															</table>
														</td>
													</tr>
												</table>
												<br/>
								
									<!-- Present list of coplets for deleting-->
												<table cellpadding="2" cellspacing="0" border="0" bgcolor="#46627A" width="400">
													<tr>	
														<td>
															<table cellpadding="0" cellspacing="0" border="0" width="100%" bgcolor="#ffffff">
																<tr>
																	<td colspan="4"><img src="sunspotdemoimg-space.gif" height="10"/></td>
																</tr>
																<xsl:variable name="acturl"><xsl:value-of select="configuration/uri"/>&amp;portaladmin=coplets&amp;portaladmin_coplets=delete</xsl:variable>
																<form method="post">
																<xsl:attribute name="action"><xsl:value-of select="translate(normalize-space($acturl), ' ', '')"/></xsl:attribute>
																<tr>
																	<td width="1%">
																		<img src="sunspotdemoimg-space.gif" width="30" height="1"/>
																	</td>
																	<td width="1%"><img src="sunspotdemoimg-kast_o.gif"/></td>
																	<td colspan="2">
																		<font face="Arial, Helvetica, sans-serif" size="2">
																			<b>Delete coplet</b>
																		</font>
																	</td>
																</tr>
																<tr>
																	<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
																	<td width="1%" background="sunspotdemoimg-line_bg.gif">
																		<img src="sunspotdemoimg-kast.gif"/>
																	</td>		
																	<td width="98%" colspan="2">
																		<font face="Arial, Helvetica, sans-serif" size="2">
																			coplet:
																		</font>
																		<img src="sunspotdemoimg-space.gif" width="10"/>
																		<select name="portalcoplet">
																			<xsl:for-each select="coplets/coplets-profile/coplets/coplet">
																			<option>
																			<xsl:attribute name="value">
																				<xsl:value-of select="normalize-space(@id)"/>
																			</xsl:attribute>
																			<xsl:value-of select="normalize-space(title)"/>
																			</option>
																			</xsl:for-each>
																		</select>		
																	</td>
																</tr>
																<tr>
																	<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
																	<td background="sunspotdemoimg-line_bg.gif" width="1%">
																		<img src="sunspotdemoimg-space.gif" height="20"/>
																	</td>
																	<td colspan="2"><img src="sunspotdemoimg-space.gif"/></td>
																</tr>
																<tr>
																	<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
																	<td width="1%">
																		<img src="sunspotdemoimg-kast_url_u.gif"/>
																	</td>
																	<td width="1%">
																		<font face="Arial, Helvetica, sans-serif" size="2">
																			<input type="submit" value="Delete coplet"/>
																		</font>
																	</td>
																	<td width="97%">
																		<img src="sunspotdemoimg-space.gif" width="10"/>
																	</td>
																</tr>
												 				</form>
																<tr>
																	<td colspan="4">
																		<img src="sunspotdemoimg-space.gif" height="10"/>
																	</td>
																</tr>
															</table>
														</td>
													</tr>
												</table>
												<br/>
								
									<!--New coplet-->
												<xsl:variable name="acturl"><xsl:value-of select="configuration/uri"/>&amp;portaladmin=coplet&amp;portaladmin_coplets=new</xsl:variable>
												<form method="post">
													<xsl:attribute name="action"><xsl:value-of select="translate(normalize-space($acturl), ' ', '')"/></xsl:attribute>
												<table cellpadding="2" cellspacing="0" border="0" bgcolor="#46627A" width="400">
										<tr>	
											<td>
												
												<table cellpadding="0" cellspacing="0" border="0" width="100%" bgcolor="#ffffff">
													<tr>
														<td colspan="4"><img src="sunspotdemoimg-space.gif" height="10"/></td>
													</tr>
													
													<tr>
														<td width="1%">
															<img src="sunspotdemoimg-space.gif" width="30" height="1"/>
														</td>
														<td width="1%"><img src="sunspotdemoimg-kast_o.gif"/></td>
														<td colspan="2">
															<font face="Arial, Helvetica, sans-serif" size="2">
																<b>New coplet</b>
															</font>
														</td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif">
															<img src="sunspotdemoimg-kast.gif"/>
														</td>
														<td width="98%" colspan="2">
															<font face="Arial, Helvetica, sans-serif" size="2">
																Titel:
															</font>
															<img src="sunspotdemoimg-space.gif" width="10"/>
															<input name="portaladmin_title" type="text" size="25"/>
														</td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td background="sunspotdemoimg-line_bg.gif" width="1%">
															<img src="sunspotdemoimg-space.gif" height="20"/>
														</td>
														<td colspan="2"><img src="sunspotdemoimg-space.gif"/></td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%">
															<img src="sunspotdemoimg-kast_url_u.gif"/>
														</td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																<input type="submit" value="Create New coplet"/>
															</font>
														</td>
														<td width="97%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
														</td>
													</tr>
												 	
													<tr>
														<td colspan="4">
															<img src="sunspotdemoimg-space.gif" height="10"/>
														</td>
													</tr>
												</table>
												
											</td>
										</tr>
									</table>
									</form>
					</td>
				</tr>
			</xsl:when>
			<xsl:otherwise> <!-- otherwise means state='coplet' -->
				<tr>

<!--  Menue -->
 					<td bgcolor="#cccccc" valign="top" width="193">
								<img src="sunspotdemoimg-space.gif" width="1" height="2"/>
									<table bgColor="#ffffff" border="0" cellPadding="0" cellSpacing="2" width="100%">
										<tr>
											<td bgcolor="#46627A" width="1%">
												<img src="sunspotdemoimg-space.gif" width="20" height="40"/>
											</td>
											<td bgcolor="#cccccc">
												<br/>
												<font face="Arial, Helvetica, sans-serif" size="2">
													<xsl:variable name="linkurl"><xsl:value-of select="configuration/uri"/>&amp;portaladmin=coplets</xsl:variable>
													<b><a>
														<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute>
														<center>List of coplets</center>
													</a></b>
												</font>
											</td>
										</tr>
										<tr>
											<td bgcolor="#46627A" width="1%">
												<img src="sunspotdemoimg-space.gif" width="20" height="40"/>
											</td>
											<td bgcolor="#cccccc">
												<br/>
												<font face="Arial, Helvetica, sans-serif" size="2"><b>
													<xsl:variable name="linkurl"><xsl:value-of select="configuration/uri"/>&amp;portaladmin=coplets&amp;portaladmin_coplets=save</xsl:variable>
													<a>
														<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute>
														<center>Save</center>
													</a>
												</b></font>
									<font face="Arial, Helvetica, sans-serif" size="1">
                                                                                                                        <p align="center">If you have changed the coplet Profile, you have to accept them by "Change"</p>
									</font>
											</td>
										</tr>
									</table>
								</td>
<!-- /Menue -->

<!-- Content -->
			                  				<td align="center">
								</td>
								<td> <!-- Edit one coplet -->
												<table cellpadding="0" cellspacing="0" border="0" width="100%" bgcolor="#ffffff">
													<tr>
														<td colspan="4"><img src="sunspotdemoimg-space.gif" height="10"/></td>
													</tr>
													<xsl:variable name="acturl"><xsl:value-of select="configuration/uri"/>&amp;portaladmin=coplet&amp;portalcoplet=<xsl:value-of select="coplet/@id"/>&amp;portaladmin_coplets=change</xsl:variable>
													<form method="post">
														<xsl:attribute name="action"><xsl:value-of select="translate(normalize-space($acturl), ' ', '')"/></xsl:attribute>
													<tr>
														<td width="1%">
															<img src="sunspotdemoimg-space.gif" width="30" height="1"/>
														</td>
														<td width="1%"><img src="sunspotdemoimg-kast_o.gif"/></td>
														<td colspan="2">
															<font face="Arial, Helvetica, sans-serif" size="2">
																<b><xsl:value-of select="coplet/title"></xsl:value-of></b>
															</font>
														</td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif">
															<img src="sunspotdemoimg-kast.gif"/>
														</td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																Title
															</font>
														</td>
														<td width="97%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
															<input name="portaladmin_title" type="text" size="40">
																<xsl:attribute name="value"><xsl:value-of select="coplet/title"></xsl:value-of></xsl:attribute>
															</input>
														</td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif">
															<img src="sunspotdemoimg-kast.gif"/>
														</td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																Resource
															</font>
														</td>
														<td width="97%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
															<input name="portaladmin_resource" type="text" size="40" value="{coplet/resource/@uri}"/>
														</td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif">
															<img src="sunspotdemoimg-kast.gif"/>
														</td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																Transformation
															</font>
														</td>
														<td width="97%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
														</td>
													</tr>
													<xsl:for-each select="coplet/transformation/stylesheet">
														<tr>
															<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
															<td width="1%" background="sunspotdemoimg-line_bg.gif">
																<img src="sunspotdemoimg-kast.gif"/>
															</td>
															<td width="1%">
																<font face="Arial, Helvetica, sans-serif" size="2">
																	&#160;
																</font>
															</td>
															<td width="97%">
																<img src="sunspotdemoimg-space.gif" width="10"/>
																<font face="Arial, Helvetica, sans-serif" size="2">
																<input type="text" size="30">
																	<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
																	<xsl:attribute name="name">portaladmin_xsl_<xsl:value-of select="position()"/></xsl:attribute>
																</input>
																<input type="checkbox">
																	<xsl:attribute name="value"><xsl:value-of select="."/></xsl:attribute>
																	<xsl:attribute name="name">portaladmin_delxsl_<xsl:value-of select="position()"/></xsl:attribute>
																</input>delete
																</font>
															</td>
														</tr>
													</xsl:for-each>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif">
															<img src="sunspotdemoimg-kast.gif"/>
														</td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																&#160;
															</font>
														</td>
														<td width="97%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
															<font face="Arial, Helvetica, sans-serif" size="2">
															<input type="checkbox" name="portaladmin_newxsl" value="true"/>Add Transformation
															</font>
														</td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif">
															<img src="sunspotdemoimg-kast.gif"/>
														</td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																Active
															</font>
														</td>
														<td width="97%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
															<select name="portaladmin_active">
																<option value="true"><xsl:if test="normalize-space(coplet/configuration/active)='true'">
																	<xsl:attribute name="selected">true</xsl:attribute>
																	</xsl:if>Yes
																</option>
																<option value="false"><xsl:if test="normalize-space(coplet/configuration/active)='false'">
																	<xsl:attribute name="selected">true</xsl:attribute>
																	</xsl:if>No
																</option>
															</select>
														</td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif">
															<img src="sunspotdemoimg-kast.gif"/>
														</td>
														<td width="30%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																Mandatory
															</font>
														</td>
														<td width="60%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
															<select name="portaladmin_mand">
																<option value="true"><xsl:if test="normalize-space(coplet/configuration/mandatory)='true'">
																	<xsl:attribute name="selected">true</xsl:attribute>
																	</xsl:if>Yes
																</option>
																<option value="false"><xsl:if test="normalize-space(coplet/configuration/mandatory)='false'">
																	<xsl:attribute name="selected">true</xsl:attribute>
																	</xsl:if>No
																</option>
															</select>
														</td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif">
															<img src="sunspotdemoimg-kast.gif"/>
														</td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																Sizable
															</font>
														</td>
														<td width="97%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
															<select name="portaladmin_sizable">
																<option value="true"><xsl:if test="normalize-space(coplet/configuration/sizable)='true'">
																	<xsl:attribute name="selected">true</xsl:attribute>
																	</xsl:if>Yes
																</option>
																<option value="false"><xsl:if test="normalize-space(coplet/configuration/sizable)='false'">
																	<xsl:attribute name="selected">true</xsl:attribute>
																	</xsl:if>No
																</option>
															</select>
														</td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif">
															<img src="sunspotdemoimg-kast.gif"/>
														</td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																Evaluates Resizable
															</font>
														</td>
														<td width="97%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
															<select name="portaladmin_handsize">
																<option value="true"><xsl:if test="normalize-space(coplet/configuration/handlesSizable)='true'">
																	<xsl:attribute name="selected">true</xsl:attribute>
																	</xsl:if>Yes
																</option>
																<option value="false"><xsl:if test="normalize-space(coplet/configuration/handlesSizable)='false' or not(coplet/configuration/handlesSizable)">
																	<xsl:attribute name="selected">true</xsl:attribute>
																	</xsl:if>No
																</option>
															</select>
														</td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif">
															<img src="sunspotdemoimg-kast.gif"/>
														</td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																Evaluates Parameters
															</font>
														</td>
														<td width="97%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
															<select name="portaladmin_handpar">
																<option value="true"><xsl:if test="normalize-space(coplet/configuration/handlesParameters)='true' or not(coplet/configuration/handlesParameters)">
																	<xsl:attribute name="selected">true</xsl:attribute>
																	</xsl:if>Yes
																</option>
																<option value="false"><xsl:if test="normalize-space(coplet/configuration/handlesParameters)='false'">
																	<xsl:attribute name="selected">true</xsl:attribute>
																	</xsl:if>No
																</option>
															</select>
														</td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif">
															<img src="sunspotdemoimg-kast.gif"/>
														</td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																Own Configuration
															</font>
														</td>
														<td width="97%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
															<select name="portaladmin_customizable">
																<option value="true"><xsl:if test="normalize-space(coplet/configuration/customizable)='true'">
																	<xsl:attribute name="selected">true</xsl:attribute>
																	</xsl:if>Yes
																</option>
																<option value="false"><xsl:if test="normalize-space(coplet/configuration/customizable)='false' or not(coplet/configuration/customizable)">
																	<xsl:attribute name="selected">true</xsl:attribute>
																	</xsl:if>No
																</option>
															</select>
														</td>
													</tr>
													<xsl:if test="normalize-space(coplet/configuration/customizable)='true'">
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif">
															<img src="sunspotdemoimg-kast.gif"/>
														</td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																Configuration Resource
															</font>
														</td>
														<td width="97%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
															<input name="portaladmin_cust" type="text" size="40" value="{coplet/customization/@uri}"/>
														</td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif">
															<img src="sunspotdemoimg-kast.gif"/>
														</td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																Persistent Configuration
															</font>
														</td>
														<td width="97%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
															<select name="portaladmin_persistent">
																<option value="true"><xsl:if test="normalize-space(coplet/configuration/persistent)='true'">
																	<xsl:attribute name="selected">true</xsl:attribute>
																	</xsl:if>Yes
																</option>
																<option value="false"><xsl:if test="normalize-space(coplet/configuration/persistent)='false' or not(coplet/configuration/persistent)">
																	<xsl:attribute name="selected">true</xsl:attribute>
																	</xsl:if>No
																</option>
															</select>
														</td>
													</tr>
													</xsl:if>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td background="sunspotdemoimg-line_bg.gif" width="1%">
															<img src="sunspotdemoimg-space.gif" height="20"/>
														</td>
														<td colspan="2"><img src="sunspotdemoimg-space.gif"/></td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%">
															<img src="sunspotdemoimg-kast_url_u.gif"/>
														</td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																<input type="submit" name="portaladmin_submit" value="Change"/>
															</font>
														</td>
														<td width="97%">
															<img src="sunspotdemoimg-space.gif" width="10"/>
														</td>
													</tr>
												 	</form>
													<tr>
														<td colspan="4">
															<img src="sunspotdemoimg-space.gif" height="10"/>
														</td>
													</tr>
												</table>
									</td>
							</tr>
						</xsl:otherwise>
					</xsl:choose>
												
<!-- /Content -->

<!-- Bottom -->		
							<tr>   
		          						<td bgcolor="#AAB9BF" noWrap="" colspan="3">
									<img src="sunspotdemoimg-space.gif" height="8"/>
								</td>
							</tr>
<!-- /Bottom -->

						</tbody>
					</table>
				</td>
			</tr>
		</tbody></table>
	</body>
</html>
</xsl:template>

<!-- /Portal Administration -->

<!-- Portal-Layout -->

<xsl:template match="layout-profile">
<form method="post">
	<xsl:attribute name="action"><xsl:value-of select="normalize-space(ancestor::portalconf/configuration/uri)"/></xsl:attribute>
	<font face="Arial, Helvetica, sans-serif" size="3">
	<xsl:attribute name="color">
		<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
	</xsl:attribute>
		<b>Portal-Layout</b>
	</font>
	<table cellspacing="0" cellpadding="2" bgcolor="#46627A" width="100%" border="0">
		<tr>
			<td>
				<table border="0" width="100%" cellspacing="0" cellpadding="4">
				<xsl:attribute name="bgcolor">
					<xsl:value-of select="portal/layouts/layout/background/color"/>
				</xsl:attribute>
					<tbody>
						<tr>
							<td colspan="3">
								<font face="Arial, Helvetica, sans-serif" size="2">
								<xsl:attribute name="color">
									<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
								</xsl:attribute>
									<b>Colors</b>
								</font>
							</td>
						</tr>
						<tr>	
							<td colspan="3">
								<xsl:apply-templates select="portal/layouts/layout[not(@*)]"/>
							</td>
						</tr>
						<tr>
							<td width="20%">
									<img src="sunspotdemoimg-space.gif"/>
									<xsl:apply-templates select="portal/header"/>
								</td>
							<td width="80%" valign="bottom" colspan="2">
									<img src="sunspotdemoimg-space.gif"/>
									<xsl:if test="ancestor::portaladminconf and ancestor::portalconf/portal-profile/content/header/coplet">
									<font face="Arial, Helvetica, sans-serif" size="2">
									<xsl:attribute name="color">
									<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
									</xsl:attribute>
  									<xsl:variable name="copletid" select="ancestor::portalconf/portal-profile/content/header/coplet/@id"/>
									<xsl:value-of select="ancestor::portalconf/coplets-profile/coplets/coplet[@id = $copletid]/title"/>
									</font>
									</xsl:if>
								<img src="sunspotdemoimg-space.gif" width="10" height="1"/>
									<xsl:if test="ancestor::portaladminconf and ancestor::portalconf/portal-profile/content/header/coplet">
									<xsl:variable name="cmd"><xsl:value-of select="ancestor::portalconf/configuration/uri"/>&amp;portalcmd=</xsl:variable>
									<xsl:variable name="copletident"><xsl:value-of select="ancestor::portalconf/portal-profile/content/header/coplet/@id"/>_<xsl:value-of select="ancestor::portalconf/portal-profile/content/header/coplet/@number"/></xsl:variable>
									<xsl:variable name="linkurl"><xsl:value-of select="$cmd"/>delete_<xsl:value-of select="$copletident"/></xsl:variable>
									<a>
										<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute>
										<img src="sunspotdemoimg-delete.gif" border="0" alt="Delete"/>
									</a>
									</xsl:if>
								</td>
						</tr>
						<tr>
							<td>	
								<img src="sunspotdemoimg-space.gif"/>
								<xsl:apply-templates select="portal/footer"/>
							</td>
							<td>
								<img src="sunspotdemoimg-space.gif"/>
								<xsl:if test="ancestor::portaladminconf and ancestor::portalconf/portal-profile/content/footer/coplet">
  									<xsl:variable name="copletid" select="ancestor::portalconf/portal-profile/content/footer/coplet/@id"/>
									<xsl:value-of select="ancestor::portalconf/coplets-profile/coplets/coplet[@id = $copletid]/title"/>
								</xsl:if>
							</td>
							<td>
								<img src="sunspotdemoimg-space.gif"/>
									<xsl:if test="ancestor::portaladminconf and ancestor::portalconf/portal-profile/content/footer/coplet">
										<xsl:variable name="cmd">
											<xsl:value-of select="ancestor::portalconf/configuration/uri"/>
											&amp;portalcmd=
										</xsl:variable>
										<xsl:variable name="copletident">
											<xsl:value-of select="ancestor::portalconf/portal-profile/content/footer/coplet/@id"/>
												_
											<xsl:value-of select="ancestor::portalconf/portal-profile/content/footer/coplet/@number"/>
										</xsl:variable>
										<xsl:variable name="linkurl"><xsl:value-of select="$cmd"/>delete_<xsl:value-of select="$copletident"/></xsl:variable>
										<a>
											<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute>
											<img src="sunspotdemoimg-delete.gif" border="0" alt="Delete"/>
										</a>
									</xsl:if>
							</td>
						</tr>
						<tr>	
							<td colspan="3">
								<img src="sunspotdemoimg-space.gif"/>
							</td>
						</tr>
						<tr>	
							<td colspan="3">
								<font face="Arial, Helvetica, sans-serif" size="2">
									<xsl:attribute name="color">
										<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
									</xsl:attribute>
									<b>Columns</b><br/>The width of the columns can either be pixels or per cent.
									    For a per cent value please add the per cent character.
								</font>
							</td>
						</tr>
						<tr>
							<td colspan="3">
								<xsl:apply-templates select="portal/columns"/>
							</td>
						</tr>
						<tr>
							<td colspan="3">
								<input type="submit" value="Change Layout"/>
							</td>
						</tr>
					</tbody>			
				</table>
			</td>
		</tr>
	</table>
</form>
</xsl:template>

<!-- /Portal-Layout -->

<!-- Portal-Layout Farben -->

<xsl:template match="layout">
	<xsl:if test="descendant::*[@formdescription and @formpath and @formtype]">
		<table border="0" cellPadding="0" cellSpacing="0" width="100%">
			<xsl:attribute name="bgcolor">
				<xsl:value-of select="ancestor::layout-profile/portal/layouts/layout/background/color"/>
			</xsl:attribute>
			<tbody>
				<xsl:for-each select="descendant::*[@formdescription and @formpath and @formtype]">
					<tr>
						<td width="20%">
							<font face="Arial, Helvetica, sans-serif" size="2">
								<xsl:attribute name="color">
									<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
								</xsl:attribute>
								<xsl:value-of select="@formdescription"/>:
							</font>
						</td>
						<td>
							<img src="sunspotdemoimg-space.gif" width="5"/>
							<font face="Arial, Helvetica, sans-serif" size="2">
								<xsl:attribute name="color">
									<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
								</xsl:attribute>
								<xsl:call-template name="inputfield"/>
							</font>
						</td>
					</tr>
				</xsl:for-each>
			</tbody>
		</table>
	</xsl:if>
</xsl:template>

<!-- /Portal-Layout Farben -->

<!-- Portal-Layout Spalten -->

<xsl:template match="columns">
<xsl:for-each select="descendant::*[@formdescription and @formpath and @formtype]">	
	<table border="0" width="100%">
		<tr>
			<td colspan="2">
				<font face="Arial, Helvetica, sans-serif" size="2">	
				<xsl:attribute name="color">
					<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
				</xsl:attribute>		
					<xsl:value-of select="@formdescription"/>:
					<img src="sunspotdemoimg-space.gif" width="10" height="1"/>
					<xsl:call-template name="inputfield"/>
				</font>
			</td>
		</tr>
		<tr>
			<xsl:for-each select="ancestor::portalconf/portal-profile/content/column">
     		            <xsl:sort select="@position"/>
			<td valign="top">
				<xsl:attribute name="width"><xsl:value-of select="width"/></xsl:attribute>
				<table border="0" cellpadding="0" cellspacing="0">
					<xsl:for-each select="*[@formdescription and @formpath and @formtype]">
						<tr>
							<td>
								<font face="Arial, Helvetica, sans-serif" size="2">
								<xsl:attribute name="color">
									<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
								</xsl:attribute>
									<xsl:value-of select="@formdescription"/>:
								</font>
							</td>
							<td colspan="2">
								<font face="Arial, Helvetica, sans-serif" size="2">
								<xsl:attribute name="color">
									<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
								</xsl:attribute>
									<img src="sunspotdemoimg-space.gif" width="20" height="1"/>
									<xsl:call-template name="inputfield"/>
								</font>
							</td>
						</tr>
					</xsl:for-each>
				</table>
			</td>
			</xsl:for-each>
		</tr>
	</table>
</xsl:for-each>
</xsl:template>

<!-- /Portal-Layout Spalten -->

<!-- selected coplets-->

<xsl:template match="portal-profile">
<font face="Arial, Helvetica, sans-serif" size="3">
<xsl:attribute name="color">
	<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
</xsl:attribute>
	<b>Your coplets</b>
</font>
<table cellspacing="0" cellpadding="2" bgcolor="#46627A" width="100%" border="0">
	<tr>
		<td>
			<table border="0" width="100%" cellspacing="0" cellpadding="4">
			<xsl:attribute name="bgcolor">
				<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/background/color"/>
			</xsl:attribute>
				<tbody>
					<tr>
						<xsl:apply-templates select="content/column"/>
					</tr>
				</tbody>
			</table>
		</td>
	</tr>
</table>
</xsl:template>

<!-- / selected coplets -->

<!-- selected coplets columns -->

<xsl:template match="column">
<xsl:variable name="colfirst" select="@position=1"/>
<xsl:variable name="collast" select="@position=count(ancestor::content/column)"/>
<xsl:variable name="prevcol" select="(@position)-1"/>
<xsl:variable name="nextcol" select="(@position)+1"/>
<td valign="top">
	<xsl:attribute name="width"><xsl:value-of select="width"/></xsl:attribute>
	<table border="0" cellspacing="0" cellpadding="0">
		<tbody>
			<tr>
				<td colspan="3">
					<font face="Arial, Helvetica, sans-serif" size="2">
					<xsl:attribute name="color">
						<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
					</xsl:attribute>
						<b>coplet</b>
					</font>
				</td>
			</tr>
			<xsl:for-each select="coplets/coplet">
                  		    <xsl:sort select="@position"/>
                                        <xsl:variable name="pos" select="@position"/>
			    <xsl:variable name="notrowfirst" select="ancestor::coplets/coplet[@position&lt;$pos]"/>
			    <xsl:variable name="notrowlast" select="ancestor::coplets/coplet[@position&gt;$pos]"/>
			    <xsl:variable name="cmd"><xsl:value-of select="ancestor::portalconf/configuration/uri"/>&amp;portalcmd=</xsl:variable>
				<xsl:variable name="copletid" select="@id"/>
				<xsl:variable name="copletident"><xsl:value-of select="@id"/>_<xsl:value-of select="@number"/></xsl:variable>
				<xsl:variable name="copletconf" select="ancestor::portalconf/coplets-profile/coplets/coplet[@id=$copletid]"/>
				<tr valign="top">
					<td>
						<xsl:if test="not($colfirst)">
							<xsl:variable name="linkurl"><xsl:value-of select="$cmd"/>move_<xsl:value-of select="$copletident"/>_<xsl:value-of select="$prevcol"/></xsl:variable>
							<a><xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute><img src="sunspotdemoimg-left.gif" border="0" alt="Move to left"/></a>
						</xsl:if>
						<xsl:if test="not($collast)">
							<xsl:variable name="linkurl"><xsl:value-of select="$cmd"/>move_<xsl:value-of select="$copletident"/>_<xsl:value-of select="$nextcol"/></xsl:variable>
							<a><xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute><img src="sunspotdemoimg-right.gif" border="0" alt="Move to right"/></a>
						</xsl:if>
						<xsl:if test="$notrowfirst">
						    <xsl:for-each select="ancestor::coplets/coplet[@position&lt;$pos]">
                                    			            <xsl:sort select="@position" order="descending"/>
				                                                <xsl:if test="position()=1">
									<xsl:variable name="linkurl"><xsl:value-of select="$cmd"/>row_<xsl:value-of select="$copletident"/>_<xsl:value-of select="@position"/></xsl:variable>
									<a><xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute><img src="sunspotdemoimg-up.gif" border="0" alt="Move up"/></a>
								</xsl:if>
				                            </xsl:for-each>
						</xsl:if>
						<xsl:if test="$notrowlast">
						    <xsl:for-each select="ancestor::coplets/coplet[@position&gt;$pos]">
                                    			            <xsl:sort select="@position"/>
				                                                <xsl:if test="position()=1">
									<xsl:variable name="linkurl"><xsl:value-of select="$cmd"/>row_<xsl:value-of select="$copletident"/>_<xsl:value-of select="@position"/></xsl:variable>
									<a><xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute><img src="sunspotdemoimg-down.gif" border="0" alt="Move down"/></a>
								</xsl:if>
				                            </xsl:for-each>
						</xsl:if>
					</td>
					<td align="left">
						<img src="sunspotdemoimg-space.gif" width="5" height="1"/>
						<font face="Arial, Helvetica, sans-serif" size="2">
						<xsl:attribute name="color">
							<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
						</xsl:attribute>
							<xsl:value-of select="$copletconf/title"/>
						</font>
						<img src="sunspotdemoimg-space.gif" width="5" height="1"/>
					</td>
					<td align="right">
						<!-- minimize/maximize -->
						<xsl:variable name="linkurlmax"><xsl:value-of select="$cmd"/>minimize_<xsl:value-of select="$copletident"/></xsl:variable>
						<xsl:variable name="linkurlmin"><xsl:value-of select="$cmd"/>maximize_<xsl:value-of select="$copletident"/></xsl:variable>
						<xsl:if test="$copletconf/configuration/sizable='true' and status/size/@formpath">
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
						<xsl:variable name="linkurlshow"><xsl:value-of select="$cmd"/>show_<xsl:value-of select="$copletident"/></xsl:variable>
						<xsl:variable name="linkurlhide"><xsl:value-of select="$cmd"/>hide_<xsl:value-of select="$copletident"/></xsl:variable>
						<xsl:choose>
							<xsl:when test="status/visible/@formpath and $copletconf/configuration/mandatory='false'">
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
						</xsl:choose>
						<!-- mandatory/delete -->
						<xsl:variable name="linkurlmand"><xsl:value-of select="$cmd"/>delete_<xsl:value-of select="$copletident"/></xsl:variable>
						<xsl:if test="$copletconf/configuration/mandatory='false'">
							<a>
								<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurlmand), ' ', '')"/></xsl:attribute>
								<img src="sunspotdemoimg-delete.gif" border="0" alt="Delete"/>
							</a>
						</xsl:if>
					</td>
				</tr>
			</xsl:for-each>
		</tbody>
	</table>
</td>
</xsl:template>

<!-- /selected coplets columns -->

<!-- All coplets-->

<xsl:template match="coplets-profile">	
<font face="Arial, Helvetica, sans-serif" size="3">
<xsl:attribute name="color">
	<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
</xsl:attribute>
	<b>All coplets</b>
</font>
<table cellspacing="0" cellpadding="2" bgcolor="#46627A" width="100%" border="0">
	<tr>
		<td>
			<table border="0" width="100%" cellspacing="0" cellpadding="4">
			<xsl:attribute name="bgcolor">
				<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/background/color"/>
			</xsl:attribute>
				<tbody>
					<xsl:if test="ancestor::portaladminconf">
						<xsl:for-each select="coplets/coplet[configuration/active='true']">
							<tr>
							<td>
							<font face="Arial, Helvetica, sans-serif" size="2">
							<xsl:attribute name="color">
								<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
							</xsl:attribute>
								<xsl:value-of select="title"/>
							</font>
						</td>
						<td>
							<xsl:variable name="linkurl"><xsl:value-of select="ancestor::portalconf/configuration/uri"/>&amp;portalcmd=new_<xsl:value-of select="@id"/>_1</xsl:variable>
							<a>
								<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute>
								<font face="Arial, Helvetica, sans-serif" size="2">
								<xsl:attribute name="color">
									<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
								</xsl:attribute>
									Add
								</font>
							</a>
						</td>	
						<td>
							<xsl:variable name="linkurl"><xsl:value-of select="ancestor::portalconf/configuration/uri"/>&amp;portalcmd=new_<xsl:value-of select="@id"/>_header</xsl:variable>
							<a>
								<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute>
								<font face="Arial, Helvetica, sans-serif" size="2">
								<xsl:attribute name="color">
									<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
								</xsl:attribute>
									As Header
								</font>
							</a>
						</td>	
						<td>
							<xsl:variable name="linkurl"><xsl:value-of select="ancestor::portalconf/configuration/uri"/>&amp;portalcmd=new_<xsl:value-of select="@id"/>_footer</xsl:variable>
							<a>
								<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute>
								<font face="Arial, Helvetica, sans-serif" size="2">
								<xsl:attribute name="color">
									<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
								</xsl:attribute>
									As Footer
								</font>
							</a>
						</td>	
						</tr>
					</xsl:for-each>
				</xsl:if>
				<xsl:if test="not(ancestor::portaladminconf)">
					<xsl:for-each select="coplets/coplet[configuration/active='true' and configuration/mandatory='false']">
						<tr>
						<td>
							<font face="Arial, Helvetica, sans-serif" size="2">
							<xsl:attribute name="color">
								<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
							</xsl:attribute>
								<xsl:value-of select="title"/>
							</font>
						</td>
						<td>
							<xsl:variable name="linkurl"><xsl:value-of select="ancestor::portalconf/configuration/uri"/>&amp;portalcmd=new_<xsl:value-of select="@id"/>_1</xsl:variable>
							<a>
								<xsl:attribute name="href"><xsl:value-of select="translate(normalize-space($linkurl), ' ', '')"/></xsl:attribute>
								<font face="Arial, Helvetica, sans-serif" size="2">
								<xsl:attribute name="color">
									<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
								</xsl:attribute>
									Add
								</font>
							</a>
						</td>	
						</tr>
					</xsl:for-each>
				</xsl:if>
				
				</tbody>
			</table>
		</td>
	</tr>
</table>
</xsl:template>

<!-- /All coplets -->

<xsl:template name="admin_coplets-profile">	
<form method="post">
<xsl:attribute name="action">
	<xsl:value-of select="normalize-space(ancestor::portalconf/configuration/uri)"/>
</xsl:attribute>
<font face="Arial, Helvetica, sans-serif" size="3">
<xsl:attribute name="color">
<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
		</xsl:attribute>
<b>coplets Configuration</b></font>
<table cellspacing="0" cellpadding="2" bgcolor="#46627A" width="100%" border="0">
	<tr>
		<td>
			<table border="0" width="100%" bgcolor="#ffffff" cellspacing="0" cellpadding="4">
			<xsl:attribute name="bgcolor">
				<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/background/color"/>
			</xsl:attribute>
				<tbody>
				<xsl:for-each select="coplets/coplet">
					<tr>
						<td colspan="2">
							<font face="Arial, Helvetica, sans-serif" size="2">
<xsl:attribute name="color">
<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
		</xsl:attribute><b>coplet: <xsl:value-of select="title"/></b></font>
						</td>
					</tr>
					<xsl:for-each select="descendant::*[@formdescription and @formpath and @formtype]">	
					<tr>
						<td width="20%">
							<font face="Arial, Helvetica, sans-serif" size="2">	
<xsl:attribute name="color">
<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
		</xsl:attribute>		
							<xsl:value-of select="@formdescription"/>:
							</font>
						</td>
						<td>
						<font face="Arial, Helvetica, sans-serif" size="2">
<xsl:attribute name="color">
<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
		</xsl:attribute>
						<xsl:call-template name="inputfield"/>
						</font>
						</td>
					</tr>
					</xsl:for-each>
				</xsl:for-each>
				<tr><td><input type="submit" value="Change coplets"/></td></tr>
				</tbody>
			</table>
		</td>
	</tr>
	
</table>
</form>
</xsl:template>

<!-- the header -->

<xsl:template match="header">
	<xsl:if test="descendant::*[@formdescription and @formpath and @formtype]">
		<table border="0" cellPadding="0" cellSpacing="0" width="100%">
			<xsl:attribute name="bgcolor">
				<xsl:value-of select="ancestor::layout-profile/portal/layouts/layout/background/color"/>
			</xsl:attribute>
			<xsl:for-each select="descendant::*[@formdescription and @formpath and @formtype]">
			<tr>
				<td width="20%">
					<font face="Arial, Helvetica, sans-serif" size="2">
<xsl:attribute name="color">
<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
		</xsl:attribute>
							<xsl:value-of select="@formdescription"/>:
					<img src="sunspotdemoimg-space.gif" width="10" height="1"/>
					<xsl:call-template name="inputfield"/>
					</font>
				</td>
			</tr>
			</xsl:for-each>
		</table>
	</xsl:if>
</xsl:template>

<xsl:template match="footer">
	<xsl:if test="descendant::*[@formdescription and @formpath and @formtype]">
		<table border="0" cellPadding="0" cellSpacing="0" width="100%">
			<xsl:attribute name="bgcolor">
				<xsl:value-of select="ancestor::layout-profile/portal/layouts/layout/background/color"/>
			</xsl:attribute>
			<xsl:for-each select="descendant::*[@formdescription and @formpath and @formtype]">
			<tr>
				<td width="20%">
					<font face="Arial, Helvetica, sans-serif" size="2">
<xsl:attribute name="color">
<xsl:value-of select="ancestor::portalconf/layout-profile/portal/layouts/layout/font/color"/>
		</xsl:attribute>
							<xsl:value-of select="@formdescription"/>:
					<img src="sunspotdemoimg-space.gif" width="12" height="1"/>
					<xsl:call-template name="inputfield"/>
					</font>
				</td>
			</tr>
			</xsl:for-each>
		</table>
	</xsl:if>
</xsl:template>

  <!-- Copy all and apply templates -->
  <xsl:template match="@*|node()">
   <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
   </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
