<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<!-- Process a tab  -->
<xsl:template match="tab-layout">
<!-- ~~~~~ Begin body table ~~~~~ -->
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <!-- ~~~~~ Begin tab row ~~~~~ -->
  <tr>
  <td>
  <table summary="tab bar" border="0" cellpadding="0" cellspacing="0" width="100%">
  <tr vAlign="top">
     <xsl:for-each select="named-item">
      <xsl:choose>
          <xsl:when test="@selected">
			<!-- ~~~~~ begin selected tab ~~~~~ -->
			<!-- ~~~~~ begin spacer between tabs ~~~~~ -->			
			<td width="5" Valign="bottom" bgcolor="#294563">
				<table summary="spacer" style="height: 1.8em" border="0" cellpadding="0" cellspacing="0">
					<tr>
						<td height="99%"><img height="5" src="images/space.gif" width="5"/></td>
					</tr>
					<tr>
						<td height="10" bgcolor="#4C6C8F"><img height="10" src="images/space.gif" width="5"/></td>
					</tr>
				</table>
			</td>
			<!-- ~~~~~ end spacer between tabs ~~~~~ -->			
			<td width="1" Valign="bottom" bgcolor="#294563">
				<table summary="selected tab" style="height: 1.8em" border="0" cellpadding="0" cellspacing="0">
					<tr>
						<td valign="top" width="5" bgcolor="#FFFFFF">
							<img height="5" width="5" alt="" src="images/tabSel-left.gif"/>
						</td>
						<td valign="middle" bgcolor="#FFFFFF">
							<font color="#ffffff" size="2" face="Arial, Helvetica, Sans-serif"><b>
								<a href="{@parameter}"><font color="#000000"><xsl:value-of select="@name"/></font></a></b>
							</font>
						</td>
						<td valign="top" width="5" bgcolor="#FFFFFF">
							<img height="5" width="5" alt="" src="images/tabSel-right.gif"/>
						</td>
					</tr>
					<tr>
						<td colspan="3" height="5" bgcolor="#FFFFFF"></td>
					</tr>
					
				</table>
			</td>
			<!-- ~~~~~ end selected tab ~~~~~ -->
			</xsl:when>
			<xsl:otherwise>
			<!-- ~~~~~ begin non selected tab ~~~~~ -->
			<!-- ~~~~~ begin spacer between tabs ~~~~~ -->			
			<td width="5" Valign="bottom" bgcolor="#294563">
				<table summary="non selected tab" style="height: 1.8em" border="0" cellpadding="0" cellspacing="0">
					<tr>
						<td height="99%"><img height="5" src="images/space.gif" width="5"/></td>
					</tr>
					<tr>
						<td height="10" bgcolor="#4C6C8F"><img height="10" src="images/space.gif" width="5"/></td>
					</tr>
				</table>
			</td>
			<!-- ~~~~~ end spacer between tabs ~~~~~ -->			
			<td width="1" Valign="bottom" bgcolor="#294563">
				<table summary="non selected tab" style="height: 1.8em" border="0" cellpadding="0" cellspacing="0">
					<tr>
						<td valign="top" width="5" bgcolor="#B2C4E0">
							<img height="5" width="5" alt="" src="images/tab-left.gif"/>
						</td>
						<td valign="middle" bgcolor="#B2C4E0" >
							<div class="tab">
								<a href="{@parameter}" style=" font-size : 85%; border: 0; color: #000066;">
										<xsl:value-of select="@name"/>
								</a>
							</div>
						</td>
						<td valign="top" width="5" bgcolor="#B2C4E0">
							<img height="5" width="5" alt="" src="images/tab-right.gif"/>
						</td>
					</tr>
					<tr>
						<td colspan="3" height="10" bgcolor="#4C6C8F"></td>
					</tr>
				</table>
			</td>
			<!-- ~~~~~ end non selected tab ~~~~~ -->
          </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
	<td width="99%" bgcolor="#294563">
		<!-- ~~~~~ last "blank" tab~~~~~ -->
		<table style="height: 2.0em" border="0" cellpadding="0" cellspacing="0" width="100%">
			<tr>
				<td height="99%" bgcolor="#294563" width="99%" align="right" valign="center">
					<img height="10" src="images/space.gif" width="1"/>
				</td>
			</tr>
			<tr>
				<td height="1" bgcolor="#4C6C8F" width="99%">
					<img height="10" src="images/space.gif" width="1"/>
				</td>
			</tr>
		</table>
	</td>
  </tr>
  </table>
  </td>
  </tr>
  <!-- ~~~~~ End tab row ~~~~~ -->
  <tr>
    <td bgcolor="#FFFFFF">
    	<table cellpadding="0" cellspacing="0" width="100%" 
    		style="border-width:2px;border-style:solid;border-color:#294563;border-top-width:0px">
			<tr>
				<td>
					<xsl:apply-templates select="named-item"/>
				</td>
			</tr>
		</table>	
    </td>
  </tr>
</table>
</xsl:template>

<xsl:template match="named-item">
  <xsl:apply-templates />
</xsl:template>

<!-- Copy all and apply templates -->

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
