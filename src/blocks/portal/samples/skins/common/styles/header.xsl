<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">

<table bgColor="#ffffff" border="0" cellPadding="0" cellSpacing="0" width="100%"><tbody> 
<tr>
<td colspan="2"> 
	<table border="0" cellPadding="0" cellSpacing="0" width="100%">
	<tbody> 
		<tr> 
		<td colspan="2" noWrap="" height="1%" bgcolor="#294563">
			<img height="5" src="space.gif" width="1"/>
		</td>
		</tr>
		<tr> 
		<td bgcolor="#294563" height="98%" align="center" valign="middle" width="100%">
			<img src="portal-logo.gif" width="250" height="90" />
		</td>
		</tr>
		<tr> 
		<td align="center" height="1%" noWrap="" bgcolor="#294563" width="1%"> 
			<img src="space.gif" width="300" height="10"/>
		</td>
		</tr>
	</tbody>
	</table>
</td>
</tr>
<tr>
<td colspan="2">
  <xsl:apply-templates/>
</td>
</tr>
<tr>
<td colspan="2"> 
	<table border="0" cellPadding="0" cellSpacing="0" width="100%">
	<tbody> 
	<tr> 
	<td colspan="2" noWrap="" height="10" bgcolor="#CFDCED"> <img height="1" src="space.gif" width="1"/></td>
	</tr>
	<tr> 
	<td colspan="2" noWrap="" height="30" bgcolor="#294563"> <img height="1" src="space.gif" width="1"/></td>
	</tr>
	</tbody>
	</table>
</td>
</tr>
</tbody>
</table>




</xsl:template>

<!-- Copy all and apply templates -->

<xsl:template match="@*|node()">
   <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
   </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
