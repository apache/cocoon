<?xml version="1.0"?>
<!-- Written by Jeremy Quinn "sharkbait@mac.com" -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="page">
		<xsl:processing-instruction name="cocoon-format">type="text/html"</xsl:processing-instruction>
		<html>
			<head>
				<title><xsl:value-of select="title"/></title>
				<meta name="author">
    				<xsl:attribute name="content"><xsl:value-of select="author/name"/> - <xsl:value-of select="author/address"/></xsl:attribute>
    			</meta>
    			<style type="text/css"><![CDATA[
    				.menutext {font-family:Verdana;font-size:10px;color:white;font-weight:normal}
     				.menutitle {font-family:Verdana;font-size:28px;color:white;font-weight:bold}
   					.bodytext {font-family:Verdana;font-size:12px;color:black}
   					.date {font-family:Verdana;font-size:9px;color:black}
   					.commandtext {font-family:Verdana;font-size:9px}
   					.bodytitle {font-family:Verdana;font-size:18px;color:black;font-weight:bold}
   					a {text-decoration: none}
					a:link {color: orange}
					a:visited {color: orange}
					a:active {color: red}
					a:hover {text-decoration: underline; color: red}
					.menutext a:link {color: white}
					.menutext a:visited {color: white}
    			]]></style>
			</head>
			<body bgcolor="white"><center>
				<table cellspacing="0" cellpadding="5" width="580" border="0">
					<tr valign="top">
						<td bgcolor="#006699" class="menutext" width="180">
							<p class="menutitle"><xsl:value-of select="title"/></p>
							<xsl:for-each select="item">
								<p class="menutext">
									<a>
										<xsl:attribute name="href">#<xsl:value-of select="position()"/></xsl:attribute>
										<xsl:value-of select="title"/>
									</a>
								</p>
							</xsl:for-each>
						</td>
						<td>
							<xsl:apply-templates select="item"/>
						</td>
					</tr>
					<tr valign="bottom">
						<td bgcolor="#006699" class="date">
							<xsl:value-of select="author/name"/><br/>
							<xsl:value-of select="author/address"/>
						</td>
					</tr>
				</table>
			</center></body>
		</html>
	</xsl:template>
	
	<xsl:template match="item">
		<a>
			<xsl:attribute name="name"><xsl:value-of select="position()"/></xsl:attribute>
			<table cellspacing="0" cellpadding="5" width="100%" border="0">
				<tr valign="top">
					<td class="bodytitle">
						<xsl:value-of select="title"/>
					</td>
					<td rowspan="3" align="right">
						<xsl:apply-templates select="figure"/>
					</td>
				</tr>
				<tr valign="top">
					<td class="date">
						<xsl:value-of select="date"/>
					</td>
				</tr>
				<tr valign="top">
					<td class="commandtext">
						<a>
							<xsl:attribute name="href">form/item-edit.xml?item=<xsl:value-of select="position()"/></xsl:attribute>
							<xsl:text>[edit]</xsl:text>
						</a>
						<a>
							<xsl:attribute name="href">form/item-add.xml?item=<xsl:value-of select="position()"/></xsl:attribute>
							<xsl:text>[add]</xsl:text>
						</a>
					</td>
				</tr>
				<tr valign="top">
					<td colspan="2" class="bodytext">
						<xsl:apply-templates select="body"/>
					</td>
				</tr>
			</table>
		</a><hr/>
	</xsl:template>
	
	<xsl:template match="figure">
		<xsl:if test=".!=''">
			<img width="64" height="64" border="1">
				<xsl:attribute name="src"><xsl:value-of select="."/></xsl:attribute>
			</img>
		</xsl:if>
	</xsl:template>

	<xsl:template match="body">
		<xsl:copy>
			<xsl:apply-templates select="*|@*|text()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="*|@*|text()">
		<xsl:copy>
			<xsl:apply-templates select="*|@*|text()"/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
