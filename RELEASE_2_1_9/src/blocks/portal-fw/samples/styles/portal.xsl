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

<!-- $Id: portal.xsl,v 1.3 2004/03/06 02:25:39 antonio Exp $ 

-->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="ROWSET">
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="ROW">
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="userdelta">
	<xsl:choose>
	<xsl:when test="user-delta">
            <xsl:apply-templates select="user-delta"/>
	</xsl:when>
	<xsl:otherwise>
	<user-delta/>
	</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="roledelta">
	<xsl:choose>
		<xsl:when test="*">
      	      <xsl:apply-templates/>
		</xsl:when>
		<xsl:otherwise>
			<role-delta/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="statusprofile">
	<xsl:choose>
		<xsl:when test="*">
      	      <xsl:apply-templates/>
		</xsl:when>
		<xsl:otherwise>
			<status-profile/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>


<xsl:template match="loaduser">
	<xsl:apply-templates select="authentication/users"/>
</xsl:template>


<xsl:template match="authentication/users">
	<users>
		<xsl:for-each select="user">
			<xsl:call-template name="includeuser"/>
		</xsl:for-each>
	</users>
</xsl:template>


<xsl:template name="includeuser">
	<xsl:variable name="type"><xsl:value-of select="normalize-space(ancestor::loaduser/info/type)"/></xsl:variable>
	<xsl:variable name="name"><xsl:value-of select="normalize-space(ancestor::loaduser/info/ID)"/></xsl:variable>
  	<xsl:variable name="role"><xsl:value-of select="normalize-space(ancestor::loaduser/info/role)"/></xsl:variable>
	
	<xsl:choose>
		<xsl:when test="normalize-space(role) = $role and ($type='users' or normalize-space(name) = $name)"> 
			<user>
				<ID><xsl:value-of select="name"/></ID>
				<role><xsl:value-of select="role"/></role>
				<data>
					<password><xsl:value-of select="password"/></password>
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
			</user>
		</xsl:when>
		<xsl:when test="$type='users' and string-length($role) = 0">
			<user>
				<ID><xsl:value-of select="name"/></ID>
				<role><xsl:value-of select="role"/></role>
				<data>
					<password><xsl:value-of select="password"/></password>
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
			</user>
		</xsl:when>
	</xsl:choose>
</xsl:template>

<xsl:template match="role">
	<xsl:apply-templates select="role-delta"/>
</xsl:template>

<xsl:template match="roles">
	<roles>
		<xsl:for-each select="role">
			<role><xsl:value-of select="."/></role>
		</xsl:for-each>
	</roles>
</xsl:template>

<!-- Copy all and apply templates -->
<xsl:template match="@*|node()">
	<xsl:copy>
		<xsl:apply-templates select="@*|node()" />
	</xsl:copy>
</xsl:template>

</xsl:stylesheet>
