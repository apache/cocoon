<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- $Id: login-html.xsl,v 1.1 2003/03/09 00:05:32 pier Exp $ 

 Description: Login page to HTML

-->

<!-- Next transformation is "TO-html.xsl" -->

<xsl:template match="content">
	<content background="sunspotdemoimg-bg_menue1.gif">
		<table bgColor="#ffffff" border="0" cellPadding="0" cellSpacing="0" width="305"><tbody>
		<tr>
			<td>
				<table bgColor="#ffffff" border="0" cellPadding="0" cellSpacing="2" width="100%"><tbody>
					<tr>
						<td bgColor="#46627a" vAlign="middle" width="15">
                    						<img height="1" src="sunspotdemoimg-space.gif" width="15"/>
                  					</td>
                  					<td bgColor="#46627a" align="center">
                    						<font color="#ffffff" face="Verdana, Arial, Helvetica, sans-serif" size="2">
                      							<b>
                        							<center>Cocoon Portal Login</center>
                      							</b>
                    						</font>
                  					</td>
                				</tr>
                				<tr>
                  					<td bgColor="#cccccc" width="15">
				  			<IMG height="1" src="sunspotdemoimg-space.gif" width="15"/>
                  					</td>
                  					<td bgColor="#cccccc" align="center">
				  			<br/>
                        					<font face="Arial, Helvetica, sans-serif" size="2">
                          						<xsl:apply-templates/>
							</font>
							<br/>
                  					</td>
                				</tr>
                				<tr>
					            <td bgColor="#cccccc" width="15">
							<img height="1" src="sunspotdemoimg-space.gif" width="15"/>
                  					</td>
                  					<td bgColor="#cccccc" align="center">
				  			<br/>
                        					<font face="Verdana, Arial, Helvetica, sans-serif" size="2">
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
							</font>
							<br/><br/>
                  					</td>
                				</tr>
              			</tbody></table>
	          		</td>
        		</tr>
		</tbody></table>
	</content>
</xsl:template>

<xsl:template match="form">
	<form method="post" target="_top"><xsl:attribute name="action"><xsl:value-of select="normalize-space(url)"/></xsl:attribute>
      		<table>
		        <xsl:apply-templates select="field"/><br/>
		</table>
		<input type="submit" value="Login"></input>
	</form>
</xsl:template>

<xsl:template match="field">
	<tr>
		<td>
			<font face="Arial, Helvetica, sans-serif" size="2"><xsl:value-of select="@description"/>:</font>
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
