<?xml version="1.0"?>
<!--
  Copyright 1999-2005 The Apache Software Foundation

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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
								xmlns:i18n="http://apache.org/cocoon/i18n/2.1">

	<!-- id of the current item -->
	<xsl:param name="current"/>
	<xsl:param name="currentSkin"/>
	
	<xsl:variable name="skin">
		<xsl:value-of select="$currentSkin"/>
		<!--xsl:call-template name="getSkin">
			<xsl:with-param name="skin4search"><xsl:value-of select="substring($currentSkin,0,string-length($currentSkin))"/></xsl:with-param>
		</xsl:call-template-->
	</xsl:variable>
	
	<xsl:template name="getSkin">
		<xsl:param name="skin4search"/>
		
		<xsl:choose>
			<xsl:when test="contains($skin4search,'/')">
				<xsl:call-template name="getSkin">
					<xsl:with-param name="skin4search"><xsl:value-of select="substring-after($skin4search,'/')"/></xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$skin4search"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	<!-- root -->
	<xsl:template match="/">
		<div>
			
			<xsl:apply-templates/>
			<br/>
		<table>
			<tr>
				<td><i><i18n:text key="copletManagement.actions.title"/>:</i></td>
				<td></td>				
				<td></td>
				<td></td>
			</tr>
			<tr>
			<td>
				<a><xsl:attribute name="href">tools/plugins/copletManagement/showTab?id=<xsl:value-of select="$current"/>&amp;action=save</xsl:attribute>Save</a>
			</td>
			</tr>
			<tr>
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/left.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.moveLeft"/></td>				
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/right.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.moveRight"/></td>				
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/up.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.moveUp"/></td>				
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/down.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.moveDown"/></td>				
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/del.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.removeItem"/></td>				
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/addTab.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.addTab"/></td>				
			</tr>
			<tr>
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/addCol.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.addCol"/></td>				
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/addRow.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.addRow"/></td>				
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/addCoplet.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.addCoplet"/></td>				
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/drillDown.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.drillDown"/></td>				
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/goUp.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.goUp"/></td>				
				<td></td>
				<td></td>
			</tr>
		</table>		</div>
	</xsl:template>
	
	<xsl:template match="named-item">
		<table style="border-width:1px; border-style:solid; border-color:#292c63; spacing:3px; padding:5px;" border="0" cellpadding="3" cellspacing="0" bordercolor="#292c63">
		  <tr>
		    <td bgcolor="#292c63">
				<div align="left">
					<xsl:if test="@parent">
						<a><xsl:attribute name="href">tools/plugins/copletManagement/showTab?id=<xsl:value-of select="@parent"/></xsl:attribute>
							<img border="0">
								<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/goUp.jpg</xsl:attribute>
							</img>
						</a>
					</xsl:if>
					<font color="#ffffff"><i18n:text key="copletManagement.items.tab"/>: <xsl:value-of select="@name"/></font>
				</div>
			</td>
		    <td bgcolor="#292c63">
				<div align="right">
					<xsl:if test="count(child::*) = 0">
						<xsl:call-template name="drawToolActions">
							<xsl:with-param name="id"><xsl:value-of select="$current"/></xsl:with-param>
							<xsl:with-param name="actionitem"><xsl:value-of select="@id"/></xsl:with-param>
							<xsl:with-param name="forceAddButtons">true</xsl:with-param>
							<xsl:with-param name="forceAddTabButton">true</xsl:with-param>
						</xsl:call-template>
					</xsl:if>
				</div>
			</td>
		  </tr>
		  <tr>
		    <td colspan="2">
				<xsl:apply-templates/>
			</td>
		  </tr>
		</table>
	</xsl:template>
	
	<xsl:template match="composite-layout">
		
		<!-- column -->
		<xsl:if test="@name = 'column'">
			<table style="border-width:1px; border-style:solid; border-color:#292c63; spacing:3px; padding:5px;" border="0" cellpadding="3" cellspacing="0" bordercolor="#386c84">
			  <tr>
				<td bgcolor="#386c84">
					<div align="left">
						<xsl:if test="@parent">
							<a><xsl:attribute name="href">tools/plugins/copletManagement/showTab?id=<xsl:value-of select="@parent"/></xsl:attribute>
								<img border="0">
									<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/goUp.jpg</xsl:attribute>
								</img>
							</a>
						</xsl:if>
						<font color="#ffffff"><i18n:text key="copletManagement.items.col"/></font>
					</div>
				</td>
				<td bgcolor="#386c84">
					<div align="right">
						<xsl:call-template name="drawToolActions">
							<xsl:with-param name="id"><xsl:value-of select="$current"/></xsl:with-param>
							<xsl:with-param name="actionitem"><xsl:value-of select="@id"/></xsl:with-param>
							<xsl:with-param name="forceDeleteButton">true</xsl:with-param>
							<xsl:with-param name="forceAddButtons">true</xsl:with-param>
							<xsl:with-param name="forceAddTabButton">true</xsl:with-param>
							<xsl:with-param name="forceDrillDownButton">true</xsl:with-param>
						</xsl:call-template>
					</div>
				</td>
			  </tr>
			  <tr>
				<td colspan="2">
					<table>
						<tr>
							<xsl:for-each select="item">
								<td>
									<xsl:apply-templates/> 
								</td>
							</xsl:for-each>
						</tr>
					</table>
				</td>
			  </tr>
			</table>	
		</xsl:if>
		
		<!-- row -->
		<xsl:if test="@name = 'row'">
			<table style="border-width:1px; border-style:solid; border-color:#292c63; spacing:3px; padding:5px;" border="0" cellpadding="3" cellspacing="0" bordercolor="#295163">
			  <tr>
				<td bgcolor="#295163">
					<div align="left">
						<xsl:if test="@parent">
							<a><xsl:attribute name="href">tools/plugins/copletManagement/showTab?id=<xsl:value-of select="@parent"/></xsl:attribute>
								<img border="0">
									<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/goUp.jpg</xsl:attribute>
								</img>
							</a>
						</xsl:if>
						<font color="#ffffff"><i18n:text key="copletManagement.items.row"/></font>
					</div>
				</td>
				<td bgcolor="#295163">
					<div align="right">
						<xsl:call-template name="drawToolActions">
							<xsl:with-param name="id"><xsl:value-of select="$current"/></xsl:with-param>
							<xsl:with-param name="actionitem"><xsl:value-of select="@id"/></xsl:with-param>
							<xsl:with-param name="forceDeleteButton">true</xsl:with-param>
							<xsl:with-param name="forceAddButtons">true</xsl:with-param>
							<xsl:with-param name="forceAddTabButton">true</xsl:with-param>
							<xsl:with-param name="forceDrillDownButton">true</xsl:with-param>
						</xsl:call-template>
					</div>
				</td>
			  </tr>
			  <tr>
				<td colspan="2">
					<xsl:for-each select="item">
						<xsl:apply-templates/> 
					</xsl:for-each>
				</td>
			  </tr>
			</table>
		</xsl:if>
		
		<!-- tab -->
		<xsl:if test="contains(@name,'tab')">
			<table style="border-width:1px; border-style:solid; border-color:#292c63; spacing:3px; padding:5px;" border="0" cellpadding="3" cellspacing="0" bordercolor="#3a3fa6">
			  <tr>
				<td bgcolor="#3a3fa6">
					<div align="left">
						<xsl:if test="@parent">
							<a><xsl:attribute name="href">tools/plugins/copletManagement/showTab?id=<xsl:value-of select="@parent"/></xsl:attribute>
								<img border="0">
									<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/goUp.jpg</xsl:attribute>
								</img>
							</a>
						</xsl:if>
						<font color="#ffffff"><i18n:text key="copletManagement.items.tabFolder"/></font>
					</div>
				</td>
				<td bgcolor="#3a3fa6">
					<div align="right">
						<xsl:call-template name="drawToolActions">
							<xsl:with-param name="id"><xsl:value-of select="$current"/></xsl:with-param>
							<xsl:with-param name="actionitem"><xsl:value-of select="@id"/></xsl:with-param>
							<xsl:with-param name="forceDeleteButton">false</xsl:with-param>
							<xsl:with-param name="forceAddTabButton">true</xsl:with-param>
						</xsl:call-template>
					</div>
				</td>
			  </tr>
			  <tr>
				<td colspan="2">
					<table>
						<tr>
							<xsl:for-each select="named-item">
								<td>
									<table style="border-width:1px; border-style:solid; border-color:#292c63; spacing:3px; padding:5px;" border="0" cellpadding="3" cellspacing="0" bordercolor="#292c63">
									  <tr>
										<td bgcolor="#292c63">
											<div align="left">
												<xsl:if test="@parent">
													<a><xsl:attribute name="href">tools/plugins/copletManagement/showTab?id=<xsl:value-of select="@parent"/></xsl:attribute>
														<img border="0">
															<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/goUp.jpg</xsl:attribute>
														</img>
													</a>
												</xsl:if>	
												<font color="#ffffff"><i18n:text key="copletManagement.items.tab"/>: <xsl:value-of select="@name"/></font>
											</div>
										</td>
										<td bgcolor="#292c63">
											<div align="right">
												<xsl:call-template name="drawToolActions">
													<xsl:with-param name="id"><xsl:value-of select="$current"/></xsl:with-param>
													<xsl:with-param name="actionitem"><xsl:value-of select="@id"/></xsl:with-param>
													<xsl:with-param name="forceMoveButtons">true</xsl:with-param>
													<xsl:with-param name="forceDeleteButton">true</xsl:with-param>
													<xsl:with-param name="forceDrillDownButton">true</xsl:with-param>
												</xsl:call-template>
											</div>
										</td>
									  </tr>
									  <tr>
										<td colspan="2">
											<xsl:value-of select="@name"/>
										</td>
									  </tr>
									</table>				
								</td>
							</xsl:for-each>
						</tr>
					</table>
				</td>
			  </tr>
			</table>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="coplet-layout|frame-layout">
		<table style="border-width:1px; border-style:solid; border-color:#292c63; spacing:3px; padding:5px;" border="0" cellpadding="3" cellspacing="0" bordercolor="#4e95b6">
		  <tr>
			<td bgcolor="#4e95b6">
				<div align="left">
					<xsl:if test="@parent">
						<a><xsl:attribute name="href">tools/plugins/copletManagement/showTab?id=<xsl:value-of select="@parent"/></xsl:attribute>
							<img border="0">
								<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/goUp.jpg</xsl:attribute>
							</img>
						</a>
					</xsl:if>	
					<font color="#ffffff"><i18n:text key="copletManagement.items.coplet"/></font>
				</div>
			</td>
			<td bgcolor="#4e95b6">
				<div align="right">
					<xsl:call-template name="drawToolActions">
						<xsl:with-param name="id"><xsl:value-of select="$current"/></xsl:with-param>
						<xsl:with-param name="actionitem"><xsl:value-of select="substring(coplet-instance-data/@id, 0, string-length(coplet-instance-data/@id)-1)"/></xsl:with-param>
						<xsl:with-param name="forceDeleteButton">true</xsl:with-param>
						<xsl:with-param name="forceDrillDownButton">true</xsl:with-param>
						<xsl:with-param name="forceEditCopletButton">true</xsl:with-param>
					</xsl:call-template>
				</div>
			</td>
		  </tr>
		  <tr>
			<td colspan="2">
				<xsl:choose>
					<xsl:when test="coplet-instance-data/.">
						Coplet: <xsl:value-of select="coplet-instance-data/."/>
 					</xsl:when>
 					<xsl:otherwise>
 						Coplet				
					</xsl:otherwise>
				</xsl:choose>
			</td>
		  </tr>
		</table>	
	</xsl:template>
	

	<xsl:template match="help" mode="foo">
		<a href="tools/plugins/copletManagement/showTab?action=save">Save</a> - <a href="tools/plugins/copletManagement/showTab?action=restore">Restore</a>
		<table>
			<tr>
				<td><i18n:text key="copletManagement.actions.title"/></td>
				<td></td>				
			</tr>
			<tr>
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/left.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.moveLeft"/></td>				
			</tr>
			<tr>
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/right.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.moveRight"/></td>				
			</tr>
			<tr>
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/up.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.moveUp"/></td>				
			</tr>
			<tr>
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/down.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.moveDown"/></td>				
			</tr>
			<tr>
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/del.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.removeItem"/></td>				
			</tr>
			<tr>
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/addTab.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.addTab"/></td>				
			</tr>
			<tr>
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/addCol.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.addCol"/></td>				
			</tr>
			<tr>
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/addRow.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.addRow"/></td>				
			</tr>
			<tr>
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/addCoplet.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.addCoplet"/></td>				
			</tr>
			<tr>
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/drillDown.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.drillDown"/></td>				
			</tr>
			<tr>
				<td>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/goUp.jpg</xsl:attribute>
					</img>
				</td>
				<td><i18n:text key="copletManagement.actions.goUp"/></td>				
			</tr>
		</table>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>
	
	<xsl:template name="drawToolActions">
		<xsl:param name="id"/>
		<xsl:param name="actionitem"/>
		<xsl:param name="forceMoveButtons"/>
		<xsl:param name="forceDeleteButton"/>
		<xsl:param name="forceAddTabButton"/>
		<xsl:param name="forceAddButtons"/>
		<xsl:param name="forceDrillDownButton"/>
		<xsl:param name="forceEditCopletButton"/>
		
		<xsl:if test="$forceEditCopletButton = 'true'">
			<a><xsl:attribute name="href">tools/plugins/copletManagement/editCoplet?actionitem=<xsl:value-of select="$actionitem"/>&amp;id=<xsl:value-of select="$id"/></xsl:attribute>
				<img border="0">
					<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/edit.jpg</xsl:attribute>
				</img>
			</a>
		</xsl:if>
		
		<xsl:if test="$forceDeleteButton = 'true'">
			<a><xsl:attribute name="href">tools/plugins/copletManagement/showTab?id=<xsl:value-of select="$id"/>&amp;action=del&amp;actionitem=<xsl:value-of select="$actionitem"/></xsl:attribute>
				<img border="0">
					<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/del.jpg</xsl:attribute>
				</img>
			</a>
		</xsl:if>
		
		<xsl:if test="count(../../node()) > 1 or $forceMoveButtons = 'true'">
		<xsl:choose>
			<xsl:when test="(../../@name) = 'row'">
				<a><xsl:attribute name="href">tools/plugins/copletManagement/showTab?id=<xsl:value-of select="$id"/>&amp;action=up&amp;actionitem=<xsl:value-of select="$actionitem"/></xsl:attribute>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/up.jpg</xsl:attribute>
					</img>
				</a>
				<a><xsl:attribute name="href">tools/plugins/copletManagement/showTab?id=<xsl:value-of select="$id"/>&amp;action=down&amp;actionitem=<xsl:value-of select="$actionitem"/></xsl:attribute>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/down.jpg</xsl:attribute>
					</img>
				</a>
			</xsl:when>
			<xsl:otherwise>
				<a><xsl:attribute name="href">tools/plugins/copletManagement/showTab?id=<xsl:value-of select="$id"/>&amp;action=up&amp;actionitem=<xsl:value-of select="$actionitem"/></xsl:attribute>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/left.jpg</xsl:attribute>
					</img>
				</a>
				<a><xsl:attribute name="href">tools/plugins/copletManagement/showTab?id=<xsl:value-of select="$id"/>&amp;action=down&amp;actionitem=<xsl:value-of select="$actionitem"/></xsl:attribute>
					<img border="0">
						<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/right.jpg</xsl:attribute>
					</img>
				</a>
			</xsl:otherwise>
		</xsl:choose>
		</xsl:if>
		
		<xsl:if test="$forceAddTabButton = 'true'">
			<a><xsl:attribute name="href">tools/plugins/copletManagement/showTab?id=<xsl:value-of select="$id"/>&amp;action=addTab&amp;actionitem=<xsl:value-of select="$actionitem"/></xsl:attribute>
				<img border="0">
					<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/addTab.jpg</xsl:attribute>
				</img>
			</a>
		</xsl:if>
		
		<xsl:if test="$forceAddButtons = 'true'">
			<a><xsl:attribute name="href">tools/plugins/copletManagement/showTab?id=<xsl:value-of select="$id"/>&amp;action=addRow&amp;actionitem=<xsl:value-of select="$actionitem"/></xsl:attribute>
				<img border="0">
					<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/addRow.jpg</xsl:attribute>
				</img>
			</a>
			<a><xsl:attribute name="href">tools/plugins/copletManagement/showTab?id=<xsl:value-of select="$id"/>&amp;action=addCol&amp;actionitem=<xsl:value-of select="$actionitem"/></xsl:attribute>
				<img border="0">
					<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/addCol.jpg</xsl:attribute>
				</img>
			</a>
			<a><xsl:attribute name="href">tools/plugins/copletManagement/showTab?id=<xsl:value-of select="$id"/>&amp;action=addCoplet&amp;actionitem=<xsl:value-of select="$actionitem"/></xsl:attribute>
				<img border="0">
					<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/addCoplet.jpg</xsl:attribute>
				</img>
			</a>
		</xsl:if>
		
		<xsl:if test="$forceDrillDownButton = 'true'">
			<a><xsl:attribute name="href">tools/plugins/copletManagement/showTab?id=<xsl:value-of select="$actionitem"/></xsl:attribute>
				<img border="0">
					<xsl:attribute name="src">toolImages/<xsl:value-of select="$skin"/>/drillDown.jpg</xsl:attribute>
				</img>
			</a>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>
