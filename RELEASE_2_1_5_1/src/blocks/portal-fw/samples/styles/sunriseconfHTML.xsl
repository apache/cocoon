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

<!-- $Id: sunriseconfHTML.xsl,v 1.3 2004/03/06 02:25:39 antonio Exp $ 

 Description: Portal User configuration to HTML. This stylesheet is
              used for the administrator when he manages the users

-->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="configuration">
	<xsl:variable name="role" select="normalize-space(role)"/>

	<html>
		<head>
			<title>Portal User Management</title>
		</head>
		<body text="#0B2A51" link="#0B2A51" vlink="#666666" bgColor="#cccccc">
		<table bgColor="#ffffff" border="0" cellPadding="0" cellSpacing="0" width="100%"><tbody>
		    	<tr>
			        	<td>
			            	<table border="0" cellPadding="0" cellSpacing="2" height="100%" width="100%"><tbody>
<!-- Start Header -->				<tr>
			                  	<td bgcolor="#AAB9BF" noWrap="" colspan="3">
								 <img src="sunspotdemoimg-space.gif" height="5"/>
							</td>
						</tr>
                					<tr>
			                  			<td bgcolor="#AAB9BF" noWrap="">	
								<img src="sunspotdemoimg-logo.jpg"/>
		                  				</td>
                  						<td bgcolor="#AAB9BF" align="center" valign="bottom" colspan="2">
                    							<font face="Arial, Helvetica, sans-serif" size="6" color="#46627A">
									<b>Portal User Management</b>
								</font>
                  						</td>
			              		</tr>
	                				<tr>
                  						<td noWrap="" width="10%" bgcolor="#cccccc">
								<img src="sunspotdemoimg-space.gif" height="10"/>
                  						</td>
                  						<td width="90%" bgcolor="#cccccc" colspan="2">
								<img src="sunspotdemoimg-space.gif"/>
							</td>
                					</tr>
<!-- Ende Header -->
<!-- Start Content -->
						<tr>
                  						<td bgcolor="#46627a">
								<img src="sunspotdemoimg-space.gif"/>
							</td>
							<td><img src="sunspotdemoimg-space.gif" width="10"/></td>
                  						<td align="center"><img src="sunspotdemoimg-space.gif" height="10"/>
<!-- add new role -->						<xsl:if test="menue/addrole">
									<form method="post">
										<xsl:attribute name="action"><xsl:value-of select="normalize-space(uri)"/></xsl:attribute>
										<input type="hidden" value="addrole" name="authstate"/>	
										<table cellpadding="2" cellspacing="0" border="0" bgcolor="#46627A" width="60%">
											<tr>	
												<td>
													<table cellpadding="0" cellspacing="0" border="0" bgcolor="#ffffff" width="100%">
														<tr>
															<td colspan="4"><img src="sunspotdemoimg-space.gif" height="10"/></td>
														</tr>
														<tr>
															<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
															<td width="1%"><img src="sunspotdemoimg-kast_m.gif"/></td>
															<td width="1%">
																<font face="Arial, Helvetica, sans-serif" size="2">
																	<input type="text" name="authrole" size="20"/>
																</font>
															</td>
															<td width="97%">
																<img src="sunspotdemoimg-space.gif" width="10"/>
																<font face="Arial, Helvetica, sans-serif" size="2">
																	<input type="submit" value="Create New Role"/>
																</font>
															</td>
														</tr>
														<tr>
															<td colspan="4"><img src="sunspotdemoimg-space.gif" height="10"/></td>
														</tr>
													</table>
												</td>
											</tr>
										</table>
									</form>
								</xsl:if>
<!-- delete a role -->						<xsl:if test="menue/delrole">
									<form method="post">
										<xsl:attribute name="action"><xsl:value-of select="normalize-space(uri)"/></xsl:attribute>
										<input type="hidden" value="delrole" name="authstate"/>	
										<table cellpadding="2" cellspacing="0" border="0" bgcolor="#46627A" width="60%">
											<tr>	
												<td>
													<table cellpadding="0" cellspacing="0" border="0" bgcolor="#ffffff" width="100%">
														<tr>
															<td colspan="4"><img src="sunspotdemoimg-space.gif" height="10"/></td>
														</tr>
														<tr>
															<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
															<td width="1%"><img src="sunspotdemoimg-kast_m.gif"/></td>
															<td width="1%">
																<font face="Arial, Helvetica, sans-serif" size="2">
																	<select name="authrole">
																		<xsl:for-each select="roles/role">
																			<option>
																				<xsl:attribute name="value">
																					<xsl:value-of select="normalize-space(.)"/>
																				</xsl:attribute>
																				<xsl:value-of select="normalize-space(.)"/>
																			</option>
																		</xsl:for-each>
																	</select>
																</font>
																<img src="sunspotdemoimg-space.gif" width="90" height="1"/>
															</td>
															<td width="97%">
																<img src="sunspotdemoimg-space.gif" width="10"/>
																<font face="Arial, Helvetica, sans-serif" size="2">
																	<input type="submit" value="Delete Role"/>
																</font>
															</td>
														</tr>
														<tr>
															<td colspan="4"><img src="sunspotdemoimg-space.gif" height="10"/></td>
														</tr>
													</table>
												</td>
											</tr>
										</table>
									</form>
								</xsl:if>
<!-- User: select role -->						<table cellpadding="2" cellspacing="0" border="0" bgcolor="#46627A" width="60%">
									<tr>	
										<td>
											<table cellpadding="0" cellspacing="0" border="0" bgcolor="#ffffff" width="100%">
												<tr>
													<td colspan="4"><img src="sunspotdemoimg-space.gif" height="10"/></td>
												</tr>
												<form method="post">
													<xsl:attribute name="action">
														<xsl:value-of select="normalize-space(uri)"/>
													</xsl:attribute>
													<input type="hidden" value="selrole" name="authstate"/>
													<tr>
														<td width="1%">
															<img src="sunspotdemoimg-space.gif"/>
														</td>
														<td width="1%"><img src="sunspotdemoimg-kast_o.gif"/></td>
														<td width="1%" colspan="2">
															<font face="Arial, Helvetica, sans-serif" size="2">
																<b>User</b>
															</font>
														</td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif">
															<img src="sunspotdemoimg-space.gif"/>
														</td>
														<td colspan="2"><img src="sunspotdemoimg-space.gif" height="20"/></td>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif" width="30"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif">
															<img src="sunspotdemoimg-kast.gif"/></td>
														<td width="1%">
															<font face="Arial, Helvetica, sans-serif" size="2">
																<select name="authrole">
																	<xsl:for-each select="roles/role">
																		<option>
																			<xsl:attribute name="value">
																				<xsl:value-of select="normalize-space(.)"/>
																			</xsl:attribute>
    	 						            											<xsl:if test="normalize-space(.)=$role">
																				<xsl:attribute name="selected">
																					true
																				</xsl:attribute>
																			</xsl:if>
																			<xsl:value-of select="normalize-space(.)"/>
																		</option>
																	</xsl:for-each>
																</select>
															</font>
															<img src="sunspotdemoimg-space.gif" width="90" height="1"/>
														</td>
														<td>
															<img src="sunspotdemoimg-space.gif" width="10"/>
															<font face="Arial, Helvetica, sans-serif" size="2">
																<input type="submit" value="Choose Role"/>
															</font>
														</td>
													</tr>
												</form>
												<tr>
													<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
													<td width="1%" background="sunspotdemoimg-line_bg.gif" valign="bottom">
														<img src="sunspotdemoimg-line_end.gif"/>
													</td>
													<td colspan="2"><img src="sunspotdemoimg-space.gif" height="20"/></td>
												</tr>	
<!-- User: select user or new user -->
												<xsl:if test="role and not(user)">
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif"><img src="sunspotdemoimg-kast.gif"/></td>
														<td width="1%">
															<img src="sunspotdemoimg-space.gif"/>	
														</td>
														<form method="post">
															<xsl:attribute name="action">
																<xsl:value-of select="normalize-space(uri)"/>
															</xsl:attribute>
															<input type="hidden" value="adduser" name="authstate"/>
												                                    <input type="hidden" name="authrole">
																<xsl:attribute name="value">
																	<xsl:value-of select="$role"/>
																</xsl:attribute>
															</input>
															<td>
																<img src="sunspotdemoimg-space.gif" width="10"/>
																<input type="submit" value="Create New User"/>	
															</td>
														</form>
													</tr>
													<tr>
														<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
														<td width="1%" background="sunspotdemoimg-line_bg.gif" valign="bottom">
															<img src="sunspotdemoimg-line_end.gif"/>
														</td>
														<td colspan="2"><img src="sunspotdemoimg-space.gif" height="20"/></td>
													</tr>
													<xsl:if test="users/user/ID">
														<tr>
															<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
															<td width="1%">
																<img src="sunspotdemoimg-kast_u.gif"/>
															</td>
															<form method="post">
																<xsl:attribute name="action">
																	<xsl:value-of select="normalize-space(uri)"/>
																</xsl:attribute>
																<input type="hidden" value="seluser" name="authstate"/>
												                                                 <input type="hidden" name="authrole">
																	<xsl:attribute name="value">
																		<xsl:value-of select="$role"/>
																	</xsl:attribute>
																</input>
																<td width="1%">
																	<select name="authid">
																		<xsl:for-each select="users/user/ID">
																			<option>
																				<xsl:attribute name="value">
																					<xsl:value-of select="normalize-space(.)"/>
																				</xsl:attribute>
																				<xsl:if test="normalize-space(.)=$role">
																					<xsl:attribute name="selected">
																						true
																					</xsl:attribute>
																				</xsl:if>
																				<xsl:value-of select="normalize-space(.)"/>
																			</option>
																		</xsl:for-each>
																	</select>	
																</td>
																<td>
																	<img src="sunspotdemoimg-space.gif" width="10"/>
																	<input type="submit" value="Choose User"/>	
																</td>
															</form>
														</tr>
													</xsl:if>
												</xsl:if>
												<xsl:if test="user">	
													<xsl:variable name="user" select="normalize-space(user)"/>
													<form method="post">
					 									<xsl:attribute name="action"><xsl:value-of select="normalize-space(uri)"/></xsl:attribute>
														<input type="hidden" value="chguser" name="authstate"/>
														<input type="hidden" name="authuser">
															<xsl:choose>
																<xsl:when test="$user='error'">
																	<xsl:attribute name="value">new</xsl:attribute>
																</xsl:when>
																<xsl:otherwise>
																	<xsl:attribute name="value">
																		<xsl:value-of select="normalize-space(user)"/>
																	</xsl:attribute>
																</xsl:otherwise>
															</xsl:choose>
														</input>
														<input type="hidden" name="autholdrole" value="{$role}"/>
														<input type="hidden" name="autholdpassword" value="{normalize-space(uservalues/data/password)}"/>
														<tr>
															<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
															<td width="1%" background="sunspotdemoimg-line_bg.gif">
																<img src="sunspotdemoimg-kast.gif"/>
															</td>
															<td width="1%" colspan="2">
																<font face="Arial, Helvetica, sans-serif" size="2">
																	<b>User Information</b>
																</font>
															</td>
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
																	<option>
																		<xsl:if test="normalize-space(uservalues/data/title)='Mr.'">
																			<xsl:attribute name="selected">
																				true
																			</xsl:attribute>
																		</xsl:if>
                                                       														Mr.
																	</option>
																	<option>
																		<xsl:if test="normalize-space(uservalues/data/title)='Mrs.'">
																			<xsl:attribute name="selected">
																				true
																			</xsl:attribute>
																		</xsl:if>
                                                         														Mrs.
																	</option>
																</select>
															</td>
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
																<xsl:choose>
																	<xsl:when test="$user='new'">
																		<input type="text" name="firstname" value="??"/>
																	</xsl:when>
																	<xsl:when test="$user='error'">
																		<input type="text" name="firstname" value="{uservalues/data/firstname}"/> 
																	</xsl:when>
																	<xsl:otherwise>
																		<input type="text" name="firstname" value="{uservalues/data/firstname}"/>
																	</xsl:otherwise>
																</xsl:choose>
															</td>
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
																<xsl:choose>
																	<xsl:when test="$user='new'">
																		<input type="text" name="lastname" value="??"/>
																	</xsl:when>
																	<xsl:otherwise>
																		<input type="text" name="lastname">
																			<xsl:attribute name="value">
																				<xsl:value-of select="uservalues/data/lastname"/>
																			</xsl:attribute>
																		</input>
																	</xsl:otherwise>
																</xsl:choose>
															</td>
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
																<font face="Arial, Helvetica, sans-serif" size="2">
																	<img src="sunspotdemoimg-space.gif" width="10"/>
																	<xsl:choose>
																		<xsl:when test="$user='new'">
																			<input type="text" name="authid" value="??"/>
																		</xsl:when>
																		<xsl:when test="$user='error'">
																			<input type="text" name="authid" value="{uservalues/data/ID}"/>
																		</xsl:when>
																		<xsl:otherwise>
																			<xsl:value-of select="uservalues/data/ID"/>
																			<input type="hidden" name="authid">
																				<xsl:attribute name="value">
																					<xsl:value-of select="normalize-space(uservalues/data/ID)"/>
																				</xsl:attribute>
																			</input>
																		</xsl:otherwise>
																	</xsl:choose>
																</font>
															</td>
														</tr>
														<xsl:if test="$user='error'">
															<tr>
																<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
																<td width="1%" background="sunspotdemoimg-line_bg.gif">
																	<img src="sunspotdemoimg-space.gif"/>
																</td>
																<td width="1%">
																	<font face="Arial, Helvetica, sans-serif" size="2">&#160;</font>
																</td>
																<td>
																	<font face="Arial, Helvetica, sans-serif" size="2" color="ff1111">
																		<img src="sunspotdemoimg-space.gif" width="10"/>
																		User already exists
																	</font>
																</td>
															</tr>
														</xsl:if>
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
																<xsl:choose>
																	<xsl:when test="$user='new'">
																		<input type="password" name="password" value=""/>
																	</xsl:when>
																	<xsl:otherwise>
																		<input type="password" name="password" value="{uservalues/data/password}"/>
																	</xsl:otherwise>
																</xsl:choose>
															</td>
														</tr>
														<tr>
															<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
															<td width="1%" background="sunspotdemoimg-line_bg.gif">
																<img src="sunspotdemoimg-space.gif"/>
															</td>
															<td width="1%">
																<font face="Arial, Helvetica, sans-serif" size="2">
																	Role:
																</font>
															</td>
															<td>
																<img src="sunspotdemoimg-space.gif" width="10"/>
																<select name="authrole">
																	<xsl:for-each select="roles/role">
																		<option>
																			<xsl:attribute name="value">
																				<xsl:value-of select="normalize-space(.)"/>
																			</xsl:attribute>
																			<xsl:if test="normalize-space(.)=$role">
																				<xsl:attribute name="selected">
																					true
																				</xsl:attribute>
																			</xsl:if>
																			<xsl:value-of select="normalize-space(.)"/>
																		</option>
																	</xsl:for-each>
																</select>
															</td>
														</tr>
														<tr>
															<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
															<td width="1%" background="sunspotdemoimg-line_bg.gif" valign="bottom">
																<img src="sunspotdemoimg-line_end.gif"/>
															</td>
															<td colspan="2"><img src="sunspotdemoimg-space.gif" height="20"/></td>
														</tr>
														<tr>
															<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
															<td width="1%" background="sunspotdemoimg-line_bg.gif">
																<img src="sunspotdemoimg-kast_url_u.gif"/>
															</td>
															<xsl:choose>
																<xsl:when test="not($user='new') and not($user='error')">
																	<td width="1%">
																		<font face="Arial, Helvetica, sans-serif" size="2">
																			<input type="checkbox" value="true" name="authdeluser"/>	
																		</font>			
																	</td>
																	<td width="97%">
																		<img src="sunspotdemoimg-space.gif" width="10"/>
																		<font face="Arial, Helvetica, sans-serif" size="2">
																			Delete User
																		</font>
																	</td>
																</xsl:when>
																<xsl:otherwise>
																	<td width="1%">
																		<img src="sunspotdemoimg-space.gif" width="10"/>		
																	</td>
																	<td width="97%">
																		<img src="sunspotdemoimg-space.gif" width="10"/>
																	</td>
																</xsl:otherwise>
															</xsl:choose>
														</tr>
														<tr>
															<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
															<td width="1%" background="sunspotdemoimg-line_bg.gif" valign="bottom">
																<img src="sunspotdemoimg-line_end.gif"/>
															</td>
															<td colspan="2"><img src="sunspotdemoimg-space.gif" height="20"/></td>
														</tr>
														<xsl:if test="addeduser">	
															<tr>
																<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
																<td width="1%" background="sunspotdemoimg-line_bg.gif">
																	<img src="sunspotdemoimg-kast.gif"/>
																</td>
																<td width="1%" colspan="2">
																	<font face="Arial, Helvetica, sans-serif" size="2">
																		User <xsl:value-of select="normalize-space(addeduser)"/> created.
																	</font>
																</td>
															</tr>
														</xsl:if>
														<tr>
															<td width="1%"><img src="sunspotdemoimg-space.gif"/></td>
															<td width="1%">
																<img src="sunspotdemoimg-kast_url_u.gif"/>
															</td>
															<td width="1%"><img src="sunspotdemoimg-space.gif" width="10"/></td>
															<td>
																<img src="sunspotdemoimg-space.gif" width="10"/>
																<xsl:choose>
																	<xsl:when test="$user='new' or $user='error'">
																		<input type="submit" name="Create New" value="Create New"/>
																	</xsl:when>
																	<xsl:otherwise>
																		<input type="submit" name="Change" value="Change"/>
																	</xsl:otherwise>
																</xsl:choose>
															</td>
														</tr>
													</form>
												</xsl:if>
												<tr>
													<td colspan="4"><img src="sunspotdemoimg-space.gif" height="10"/></td>
												</tr>
											</table>
										</td>
									</tr>
								</table>
								<img src="sunspotdemoimg-space.gif" height="20"/>
							</td>
			                		</tr>
<!-- Ende Content -->

<!-- Start Bottom -->
						<tr>   
          							<td bgcolor="#AAB9BF" noWrap="" colspan="3">
								<img src="sunspotdemoimg-space.gif" height="8"/>
							</td>
						</tr>
<!-- Ende Bottom -->

					</tbody></table>
				</td>
			</tr>
		</tbody></table>
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
