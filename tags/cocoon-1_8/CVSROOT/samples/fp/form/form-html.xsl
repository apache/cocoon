<?xml version="1.0"?>
<!-- Written by Jeremy Quinn "sharkbait@mac.com" -->

<xsl:stylesheet 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:fp="http://apache.org/cocoon/XSP/FP/1.0"
	version="1.0">

<xsl:template match="/">
	<HTML>
		<HEAD>
			<TITLE>FP TagLib Example</TITLE>
			<STYLE TYPE="text/css"><![CDATA[
				td {font-family:Verdana;font-size:10px;color:black;font-weight:bold}
    			.menu {font-weight:normal}
    			.title {font-size:20px}
    			.processes {font-size:9px;font-weight:normal}
    			.process {color:green}
    			.error {font-size:9px;font-weight:bold; color:red}
    			.errors {font-size:9px;font-weight:normal}
    			]]></STYLE>
		</HEAD>
		<BODY BGCOLOR="#FFFFFF">
			<TABLE BORDER="0" CELLPADDING="0" CELLSPACING="10">
				<TR VALIGN="TOP">
					<TD>
						<xsl:apply-templates select="*/menu"/>
					</TD><TD>
						<xsl:apply-templates select="*/title"/>
						<xsl:apply-templates select="*/date"/>
						<xsl:apply-templates select="*/method"/>
						<xsl:apply-templates select="*/task"/>
						<xsl:apply-templates select="*/form"/>
					</TD>
				</TR>
			</TABLE>
		</BODY>
	</HTML>
</xsl:template>

<xsl:template match="form">
	<FORM METHOD="POST">
		<xsl:for-each select="@*">
			<xsl:copy/>
		</xsl:for-each>
		<TABLE BORDER="0" CELLPADDING="4" CELLSPACING="10">
			<xsl:apply-templates/>
		</TABLE>
	</FORM>
</xsl:template>

<xsl:template match="input[@type='checkbox']">
	<TR BGCOLOR="#CCCCCC" VALIGN="TOP">
		<TD></TD>
		<TD>
			<INPUT>
				<xsl:call-template name="copy-form-attributes"/>
			</INPUT>
			<xsl:value-of select="@value"/>
		</TD>
		<xsl:call-template name="show-processes"/>
		<xsl:call-template name="show-errors"/>
	</TR>
</xsl:template>

<xsl:template match="input[@type='hidden']">
	<INPUT>
		<xsl:call-template name="copy-form-attributes"/>
		<xsl:for-each select="*">
			<xsl:attribute name="{name(.)}"><xsl:value-of select="normalize-space(.)"/></xsl:attribute>
		</xsl:for-each>
	</INPUT>
</xsl:template>

<xsl:template match="input[@type='select']">
	<xsl:variable name="selection"><xsl:value-of select="selection"/></xsl:variable>
	<TR BGCOLOR="#CCCCCC" VALIGN="TOP">
		<TD ALIGN="RIGHT"><xsl:value-of select="@label"/></TD>
		<TD>
			<SELECT>
				<xsl:call-template name="copy-form-attributes"/>
				<xsl:for-each select="value/option">
					<xsl:copy>
						<xsl:apply-templates select="@*"/>
						<xsl:if test="@value = $selection">
							<xsl:attribute name="selected">selected</xsl:attribute>
						</xsl:if>
						<xsl:apply-templates/>
					</xsl:copy>
				</xsl:for-each>
				<xsl:for-each select="value/*">
					<xsl:copy-of select="option"/>
				</xsl:for-each>
			</SELECT>
		</TD>
		<xsl:call-template name="show-processes"/>
		<xsl:call-template name="show-errors"/>
	</TR>
</xsl:template>

<xsl:template match="input[@type='textarea']">
	<xsl:variable name="value">
		<xsl:call-template name="get-nested-content">
			<xsl:with-param name="content" select="."/>
		</xsl:call-template>
	</xsl:variable>
	<TR BGCOLOR="#CCCCCC" VALIGN="TOP">
		<TD ALIGN="RIGHT"><xsl:value-of select="@label"/></TD>
		<TD>
			<TEXTAREA ROWS="10" COLS="40"><xsl:call-template name="copy-form-attributes"/><xsl:copy-of select="$value"/></TEXTAREA>
		</TD>
		<xsl:call-template name="show-processes"/>
		<xsl:call-template name="show-errors"/>
	</TR>
</xsl:template>

<xsl:template match="input">
	<xsl:variable name="value">
		<xsl:call-template name="get-nested-content">
			<xsl:with-param name="content" select="."/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:if test="$value">
		<TR BGCOLOR="#CCCCCC" VALIGN="TOP">
			<TD ALIGN="RIGHT"><xsl:value-of select="@label"/></TD>
			<TD>
				<INPUT>
					<xsl:call-template name="copy-form-attributes"/>
					<xsl:attribute name="value"><xsl:value-of select="normalize-space($value)"/></xsl:attribute>
				</INPUT>
			</TD>
			<xsl:call-template name="show-processes"/>
			<xsl:call-template name="show-errors"/>
		</TR>
	</xsl:if>
</xsl:template>

<xsl:template name="copy-form-attributes">
	<xsl:for-each select="@*[substring(name(), 1, 3) != 'fp-']">
		<xsl:copy/>
	</xsl:for-each>
</xsl:template>

<xsl:template name="show-processes">
	<TD CLASS="processes">
		<xsl:for-each select="@*[substring(name(), 1, 3) = 'fp-']">
			<xsl:value-of select="name(.)"/>: <span class="process"><xsl:value-of select="."/></span><br/>
		</xsl:for-each>
	</TD>
</xsl:template>

<xsl:template name="show-errors">
	<xsl:variable name="code"><xsl:value-of select="@fp-error"/></xsl:variable>
	<TD CLASS="errors">
		<xsl:if test="$code!=''">
			<xsl:for-each select="/*/fp-error[@id=$code]/*">
			<xsl:value-of select="name(.)"/>: <span class="error"><xsl:value-of select="."/></span><br/>
			</xsl:for-each>
		</xsl:if>
	</TD>
</xsl:template>

<xsl:template match="menu">
	<xsl:variable name="action"><xsl:value-of select="@action"/></xsl:variable>
	<TABLE BORDER="0" CELLPADDING="0" CELLSPACING="2">
		<xsl:for-each select="title">
			<TR>
				<TD CLASS="menu">
					<xsl:element name="a">
						<xsl:attribute name="href"><xsl:value-of select="$action"/><xsl:value-of select="position(.)"/></xsl:attribute>
						<xsl:value-of select="."/>
					</xsl:element>
				</TD>
			</TR>
		</xsl:for-each>
	</TABLE>
</xsl:template>

<xsl:template match="title">
	<span class="title"><xsl:value-of select="."/></span><br/>
</xsl:template>

<xsl:template match="method">
	Method: <xsl:value-of select="."/><br/>
</xsl:template>

<xsl:template match="date">
	Last Modified: <xsl:value-of select="."/><br/>
</xsl:template>

<xsl:template match="task">
	Task: <xsl:value-of select="."/><br/>
	Item:  <xsl:value-of select="//input[@name='item']"/><br/>
	Errors:  <xsl:value-of select="../@fp-errors"/><br/><br/><br/>
</xsl:template>

<xsl:template name="get-nested-content">
	<xsl:param name="content"/>
	<xsl:choose>
		<xsl:when test="$content/*">
			<xsl:apply-templates select="$content/*"/>
		</xsl:when>
		<xsl:otherwise><xsl:value-of select="$content"/></xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="@*|node()" priority="-1">
	<xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
</xsl:template>

</xsl:stylesheet>
