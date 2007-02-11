<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="root">
	    <page language="{@language}">
	                <title>
	                    <xsl:value-of select="title" />
	                </title>
				<h2>
					<font color="navy">
						<xsl:value-of select="title"/>
					</font>
      			     <xsl:apply-templates select="form" />
				</h2>
				<h5><xsl:value-of select="sub-title"/></h5>
				<hr align="left" noshade="noshade" size="1"/>
				<small><font color="red"><i><xsl:apply-templates select="annotation"/></i></font></small>
				
				<xsl:apply-templates select="content" />
				
				<hr align="left" noshade="noshade" size="1"/> 
				<xsl:apply-templates select="bottom"/>
		</page>
	</xsl:template>
	
	<xsl:template match="menu">
		<font size="-1">
			<xsl:for-each select="item">
				<xsl:apply-templates select="."/>
				<xsl:if test="position() != last()"><xsl:text> | </xsl:text></xsl:if>
			</xsl:for-each>
		</font>	
	</xsl:template>
	
	<xsl:template match="link">
		<a href="{href}"><xsl:value-of select="normalize-space(title)"/></a>
	</xsl:template>
	
	<xsl:template match="item/title">
		<font color="maroon"><xsl:copy-of select="normalize-space(.)"/></font>
	</xsl:template>
	
	<xsl:template match="content">
		<xsl:apply-templates />
	</xsl:template>
	
	<xsl:template match="para">
		<p>
			<font color="navy"><b><xsl:number format="0. "/> <xsl:value-of select="@name"/> </b>: <xsl:value-of select="@title"/></font><br/>
			<font size="-1"><xsl:apply-templates select="text() | strong"/></font>
		</p>	
	</xsl:template>
	
	<xsl:template match="strong">
	    <b><xsl:apply-templates select="text()"/></b>
	</xsl:template>
	
	<xsl:template match="bottom">
		<small><b><xsl:value-of select="copyright"/></b></small>		
	</xsl:template>
	
	<xsl:template match="form">
	    <form>
	        <xsl:copy-of select="@*" />
	        <xsl:apply-templates />
	    </form>
	</xsl:template>
	
	<xsl:template match="input">
	    <input>
	        <xsl:copy-of select="@*" />
	        <xsl:apply-templates />	        
	    </input>
	</xsl:template>
	
</xsl:stylesheet>
