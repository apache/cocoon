<?xml version="1.0"?>

<!-- Written by James Birchfield "jmbirchfield@proteus-technologies.com" -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/XSL/Transform/1.0">

  <xsl:template match="page">
   <xsl:processing-instruction name="cocoon-format">type="text/html"</xsl:processing-instruction>
   <html>
    <head>
     <title>
      LDAP Search Results
     </title>
    </head>
    <body bgcolor="#404040">
			<center>
		<h1><font color="cornsilk">LDAP Search Results</font></h1>
			<table width="80%" bgcolor="#000000" border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td>
						<xsl:apply-templates/>
					</td>
				</tr>
			</table>
		</center>
    </body>
   </html>
  </xsl:template>

 <xsl:template match="ldapsearch/searchresult">
	<table cellspacing="0" bordercolor="#000000" bgcolor="#000000" border="1" width="100%" cellpadding="0" cellspacing="0">
		<tr>
			<th colspan="2" bgcolor="tan">
				<xsl:value-of select="@ID"/>
			</th>
		</tr>
		<tr>
			<th width="20%" align="left" bgcolor="#a0a0a0">
				<xsl:text>p. </xsl:text><xsl:value-of select="telephonenumber"/><br/>
				<xsl:text>f. </xsl:text><xsl:value-of select="facsimiletelephonenumber"/><br/>
			</th>
			<td bgcolor="#ffffff" width="80%">
				<table border="0">
					<tr>
						<td>
							<xsl:value-of select="sn"/> <xsl:text>, </xsl:text> <xsl:value-of select="givenname"/>
						</td>
					</tr>
					<tr>
						<td>
							<xsl:value-of select="ou"/>
						</td>
					</tr>
					<tr>
						<td>
							<xsl:value-of select="title"/>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td colspan="2" bgcolor="#d0d0d0">
				<xsl:text>E-mail address: </xsl:text>
					<a>
						<xsl:attribute name="href">
							<xsl:text>mailto:</xsl:text><xsl:value-of select="mail"/>
						</xsl:attribute>
						<xsl:value-of select="mail"/>
					</a>
			</td>
		</tr>
	</table>
	<p/>
  </xsl:template>
 
</xsl:stylesheet>

