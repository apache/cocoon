<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">

<table bgColor="#ffffff" border="0" cellPadding="0" cellSpacing="0" width="100%"><tbody> 
<tr>    		
<td colspan="2"> 
	<table border="0" cellPadding="0" cellSpacing="2" width="100%">
	<tbody> 
	<tr> 
	<td colspan="2" noWrap="" height="1%" bgcolor="#AAB9BF"> <img height="5" src="sunspotdemoimg-space.gif" width="1"/></td>
	</tr>
	<tr> 
	<td noWrap="" align="center" bgcolor="#AAB9BF" height="98%"> <img src="sunspotdemoimg-logo.jpg" width="178" height="90"/></td>
		<td bgcolor="#AAB9BF" height="98%" align="center" valign="middle"> 
					<font face="Arial, Helvetica, sans-serif" size="6" color="#46627A">
		<b>Cocoon Portal</b>
	</font>
	</td>
	</tr>
	<tr> 
	<td align="center" height="1%" noWrap="" bgcolor="#cccccc" width="1%"> 
		<img src="sunspotdemoimg-space.gif" width="300" height="10"/></td>
		<td bgcolor="#cccccc" height="1%" width="98%"> <img height="10" src="sunspotdemoimg-space.gif" width="1"/> 
	</td>
	</tr>
	</tbody>
	</table>
</td>
</tr>
<tr>
<td>
</td>
<td>
  <xsl:apply-templates/>
</td>
</tr>
<tr>
<td colspan="2"> 
	<table border="0" cellPadding="0" cellSpacing="2" width="100%">
	<tbody> 
	<tr> 
	<td colspan="2" noWrap="" height="10" bgcolor="#cccccc"> <img height="1" src="sunspotdemoimg-space.gif" width="1"/></td>
	</tr>
	<tr> 
	<td colspan="2" noWrap="" height="30" bgcolor="#AAB9BF"> <img height="1" src="sunspotdemoimg-space.gif" width="1"/></td>
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
