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
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:col="http://apache.org/cocoon/collection/1.0" 
  version="1.0">
  
  <xsl:output indent="yes"/>
  
  <xsl:param name="base"/>
  <xsl:param name="path"/>
  <xsl:param name="type"/>
  
  <xsl:template match="/">
    <document>
      <header>
        <title>Jakarta Slide example</title>
        <tab title="users" href="{$base}/users"/>
        <tab title="content" href="{$base}/content/{$path}"/>
        <tab title="properties" href="{$base}/properties/{$path}"/>
        <tab title="permissions" href="{$base}/permissions/{$path}"/>
        <tab title="locks" href="{$base}/locks/{$path}"/>
        <tab title="logout" href="{$base}/logout"/>
      </header>
      <body>
        <xsl:call-template name="body" />
      </body>
    </document>
  </xsl:template>
  
  <xsl:template name="body">
    <row>
      <xsl:call-template name="left" />
      <xsl:call-template name="middle" />
    </row>
  </xsl:template>
  
  <xsl:template name="left">
    <column title="Navigation">
      <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2" width="100%" align="center">
        <tr>
          <td width="100%" bgcolor="#ffffff" align="left">
            <br/>
          </td>
        </tr>
        <xsl:for-each select="/document/col:collection/col:resource|/document/col:collection/col:collection">
          <tr>
            <td width="100%" bgcolor="#ffffff" align="left">
              <font size="+0" face="arial,helvetica,sanserif" color="#000000">
                <xsl:choose>
                  <xsl:when test="$path != ''">
                    <a href="{$base}/{$type}/{$path}/{@name}">
                      <xsl:value-of select="@name"/>
                    </a>
                  </xsl:when>
                  <xsl:otherwise>
                    <a href="{$base}/{$type}/{@name}">
                      <xsl:value-of select="@name"/>
                    </a>
                  </xsl:otherwise>
                </xsl:choose>
              </font>
            </td>
          </tr>
        </xsl:for-each>
      </table>
    </column>
  </xsl:template>

  <xsl:template name="middle">
  </xsl:template>
  
</xsl:stylesheet>
