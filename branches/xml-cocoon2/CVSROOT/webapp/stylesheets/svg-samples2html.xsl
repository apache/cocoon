<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:svg="http://www.w3.org/2000/svg">

 <xsl:template match="/">
  <html>
   <head>
    <title>Apache Cocoon 2.0a3</title>
   </head>
   <body bgcolor="#ffffff" link="#0086b2" vlink="#00698c" alink="#743e75">
    <p align="center"><font size="+0" face="arial,helvetica,sanserif" color="#000000">The Apache Software Foundation is proud to present...</font></p>

    <p align="center"><img border="0" src="images/cocoon.gif"/></p>

    <p align="center"><font size="+0" face="arial,helvetica,sanserif" color="#000000"><b>version 2.0a3</b></font></p>

    <xsl:apply-templates/>

    <p align="center">
     <font size="-1">
      Copyright &#169; 1999-2000 <a href="http://www.apache.org">The Apache Software Foundation</a>.<br/>
      All rights reserved.
     </font>
    </p>
   </body>
  </html>
 </xsl:template>
 
 <xsl:template match="samples">
 	<table>
		<xsl:for-each select="group">
			<tr>
				<td>
					<svg:svg width="200" height="15">
						<svg:text x="10px" y="10px" style="font-family:sans; font-size:15px; fill: #0086b2; text-anchor:start">
							<xsl:value-of select="@name"/>
						</svg:text>
					</svg:svg>
				</td>
			</tr>
			<xsl:for-each select="sample">
				<tr>
					<td>
						<a href="{@href}">
							<svg:svg width="200" height="15">
								<svg:text x="20px" y="10px" style="font-family:sans; font-size:15px; fill: black; text-anchor:start"> 
									<xsl:value-of select="@name"/>
								</svg:text>
							</svg:svg>
						</a>
					</td>
				</tr>
			</xsl:for-each>
		</xsl:for-each>
	</table>
 </xsl:template>
 
</xsl:stylesheet>
