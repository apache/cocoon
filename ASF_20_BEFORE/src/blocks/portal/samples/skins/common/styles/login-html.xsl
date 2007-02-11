<?xml version="1.0"?>

<!--+ $Id: login-html.xsl,v 1.4 2003/12/11 14:41:32 cziegeler Exp $ 
    |
    | Description: Login page to HTML
    |
    +-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="resource"/>

<xsl:template match="content">
<table bgColor="#ffffff" border="0" cellPadding="0" cellSpacing="0" width="100%"><tbody>
  <tr>
	<td bgColor="#ffffff" align="center">
	  <br/>
        <xsl:apply-templates/>
      <br/>
    </td>
  </tr>
  <tr>
    <td bgColor="#ffffff" width="15">
	  <img height="1" src="images/space.gif" width="15"/>
    </td>
  </tr>
  <tr>
    <td bgColor="#ffffff" align="center">
	  <br/>
         If you are not already registered, use this guest login:
		 <br/><br/>
		 User:	<b>guest</b>
		 Password:
		 <b>guest</b>
		 <br/><br/>Or use this administrator login:<br/>
		 User:
		 <b>cocoon</b>
		 Password:
		 <b>cocoon</b>
	   <br/><br/>
     </td>
   </tr>
</tbody></table>
</xsl:template>

<xsl:template match="form">
	<form method="post" target="_top">
	    <xsl:choose>
	        <xsl:when test="$resource=''">
  		        <xsl:attribute name="action"><xsl:value-of select="normalize-space(url)"/>?resource=portal</xsl:attribute>
	        </xsl:when>
	        <xsl:otherwise>
        		<xsl:attribute name="action"><xsl:value-of select="normalize-space(url)"/>?resource=<xsl:value-of select="$resource"/></xsl:attribute>
	        </xsl:otherwise>
	    </xsl:choose>
		<table>
		        <xsl:apply-templates select="field"/><br/>
		</table>
		<input type="submit" value="Login"></input>
	</form>
</xsl:template>

<xsl:template match="field">
	<tr>
		<td>
			<xsl:value-of select="@description"/>:
		</td>
		<td>
			<input>
				<xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
				<xsl:attribute name="type"><xsl:value-of select="@type"/></xsl:attribute>
				<xsl:attribute name="size"><xsl:value-of select="@length"/></xsl:attribute>
			</input>
		</td>
	</tr>
</xsl:template>

<!-- Copy all and apply templates -->
<xsl:template match="@*|node()">
	<xsl:copy>
		<xsl:apply-templates select="@*|node()" />
	</xsl:copy>
</xsl:template>

</xsl:stylesheet>
