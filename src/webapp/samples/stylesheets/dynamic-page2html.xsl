<?xml version="1.0" encoding="ISO-8859-1"?>
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

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp-request="http://apache.org/xsp/request/2.0"
  xmlns:xsp="http://apache.org/xsp">

  <xsl:include href="../common/style/xsl/html/simple-page2html.xsl"/>
 
  <xsl:template match="xsp-request:uri">
    <b><xsl:value-of select="."/></b>
  </xsl:template>

  <xsl:template match="xsp-request:parameter">
    <i><xsl:value-of select="@name"/></i>:<b><xsl:value-of select="."/></b>
  </xsl:template>

  <xsl:template match="xsp-request:parameter-values">
    <p>Parameter Values for "<xsl:value-of select="@name"/>":</p>

    <ul>
      <xsl:for-each select="xsp-request:value">
        <li>
	  <xsl:value-of select="."/>
	</li>
      </xsl:for-each>
    </ul>
  </xsl:template>

  <xsl:template match="xsp-request:parameter-names">
    <p>All Parameter Names:</p>

    <ul>
      <xsl:for-each select="xsp-request:name">
        <li>
	  <xsl:value-of select="."/>
	</li>
      </xsl:for-each>
    </ul>
  </xsl:template>

  <xsl:template match="xsp-request:headers">
    <p>Headers:</p>
    
    <ul>
      <xsl:for-each select="xsp-request:header">
	<li>
          <i><xsl:value-of select="@name"/></i>:
          <b><xsl:value-of select="."/></b>
	</li>
      </xsl:for-each>
    </ul>
    <br/>
  </xsl:template>

  <xsl:template match="xsp-request:header">
    <i><xsl:value-of select="@name"/></i>:<b><xsl:value-of select="."/></b>
  </xsl:template>

  <xsl:template match="xsp-request:header-names">
    <p>All Header names:</p>

    <ul>
      <xsl:for-each select="xsp-request:name">
        <li>
	  <xsl:value-of select="."/>
	</li>
      </xsl:for-each>
    </ul>
  </xsl:template>



  <xsl:template match="textarea/xsp-request:uri">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <xsl:template match="textarea/xsp-request:parameter">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <xsl:template match="textarea/xsp-request:parameter-values">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <xsl:template match="textarea/xsp-request:parameter-names">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <xsl:template match="textarea/xsp-request:headers">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <xsl:template match="textarea/xsp-request:header">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <xsl:template match="textarea/xsp-request:header-names">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-1" name="copy">
   <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
