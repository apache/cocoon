<?xml version="1.0"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!--+ @version $Id$ 
    |
    | Description: Login page to HTML
    |
    +-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="resource"/>

  <xsl:template match="content">
    <table id="personal_area"><tbody>
      <tr><td><br/><xsl:apply-templates/><br/></td></tr>
      <tr>
        <td>
          <br/>
          <p>
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
	        <br/><br/>
          </p>
        </td>
      </tr>
    </tbody></table>
  </xsl:template>

  <xsl:template match="form">
    <form method="post">
      <xsl:choose>
        <xsl:when test="$resource=''">
          <xsl:attribute name="action"><xsl:value-of select="normalize-space(url)"/>?resource=portal</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="action"><xsl:value-of select="normalize-space(url)"/>?resource=<xsl:value-of select="$resource"/></xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
      <table><tbody><xsl:apply-templates select="field"/></tbody></table>
      <input type="submit" value="Login"></input>
	</form>
  </xsl:template>

  <xsl:template match="field">
    <tr>
      <td align="left"><p><xsl:value-of select="@description"/>:</p></td>
      <td align="left"><input name="{@name}" type="{@type}" size="{@length}"/></td>
    </tr>
  </xsl:template>

  <!-- Copy all and apply templates -->
  <xsl:template match="@*|node()">
    <xsl:copy><xsl:apply-templates select="@*|node()" /></xsl:copy>
  </xsl:template>

</xsl:stylesheet>
