<?xml version="1.0"?>

<!-- $Id: sunrise-user.xsl,v 1.2 2003/05/06 14:12:55 vgritsenko Exp $ 

-->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<xsl:param name="password"/>
<xsl:param name="name"/>


<xsl:template match="authentication">
  <authentication>

	<xsl:apply-templates select="users"/>

  </authentication>
</xsl:template>

<xsl:template match="users">
<xsl:apply-templates select="user"/>
</xsl:template>

<xsl:template match="user">

<xsl:if test="normalize-space(name) = $name and normalize-space(password) = $password">
	<ID><xsl:value-of select="name"/></ID>
	<role><xsl:value-of select="role"/></role>
	<data>
		<name><xsl:value-of select="name"/></name>
		<role><xsl:value-of select="role"/></role>
		<ID><xsl:value-of select="name"/></ID>
		<user><xsl:value-of select="name"/></user>
		<title><xsl:value-of select="title"/></title>
		<firstname><xsl:value-of select="firstname"/></firstname>
		<lastname><xsl:value-of select="lastname"/></lastname>
		<company><xsl:value-of select="company"/></company>
		<street><xsl:value-of select="street"/></street>
		<zipcode><xsl:value-of select="zipcode"/></zipcode>
		<city><xsl:value-of select="city"/></city>
		<country><xsl:value-of select="country"/></country>
		<phone><xsl:value-of select="phone"/></phone>
		<fax><xsl:value-of select="fax"/></fax>
		<email><xsl:value-of select="email"/></email>
		<bankid><xsl:value-of select="bankid"/></bankid>
		<bankname><xsl:value-of select="bankname"/></bankname>
		<accountid><xsl:value-of select="accountid"/></accountid>
	</data>
</xsl:if>
</xsl:template>

</xsl:stylesheet>
