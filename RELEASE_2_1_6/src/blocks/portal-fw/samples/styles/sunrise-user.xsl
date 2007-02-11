<?xml version="1.0"?>
<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!-- $Id: sunrise-user.xsl,v 1.3 2004/03/06 02:25:39 antonio Exp $ 

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
