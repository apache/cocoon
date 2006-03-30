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
  
  <xsl:import href="layout.xsl" />
  <xsl:output indent="yes"/>
  
  <xsl:param name="base"/>
  <xsl:param name="path"/>
  <xsl:param name="type">locks</xsl:param>
  
  <xsl:param name="userspath"/>
  <xsl:param name="rolespath"/>
  <xsl:param name="groupspath"/>
  <xsl:param name="actionspath"/>  
  
  <xsl:template name="middle">
    <column title="Locks">
      <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2" width="100%" align="center">
        <font size="+0" face="arial,helvetica,sanserif" color="#000000">
          <tr>
            <td align="left">
              <b>Subject</b>
            </td>
            <td align="left">
              <b>Type</b>
            </td>
            <td align="left">
              <b>Expiration (mins)</b>
            </td>
            <td align="left">
              <b>Inheritable</b>
            </td>
            <td align="left">
              <b>Exclusive</b>
            </td>
            <td align="right"/>
          </tr>
          <xsl:for-each select="/document/locks/lock">
            <tr bgcolor="#eeeeee">
              <td align="left">
                <xsl:value-of select="subject"/>
              </td>
              <td align="left">
                <xsl:value-of select="type"/>
              </td>
              <td align="left">
                <xsl:value-of select="expiration"/>
              </td>
              <td align="left">
                <xsl:value-of select="inheritable"/>
              </td>
              <td align="left">
                <xsl:value-of select="exclusive"/>
              </td>
              <td align="right">
                <form action="{$base}/removelock" method="post">
                  <input type="hidden" name="resourcePath" value="{$path}"/>
                  <input type="hidden" name="objectUri" value="{object}"/>
                  <input type="hidden" name="lockId" value="{id}"/>
                  <input type="submit" name="doRemoveLock" value="Delete"/>
                </form>
              </td>
            </tr>
          </xsl:for-each>
          <tr>
            <form action="{$base}/addlock" method="post">
              <input type="hidden" name="resourcePath" value="{$path}"/>
              <td align="left">
                <select name="subject">
                  <option>self</option>
                  <option>owner</option>
                  <option>authenticated</option>
                  <option>unauthenticated</option>
                  <option>all</option>
                  <xsl:for-each select="/document/roles/role">
                    <xsl:variable name="roleuri" select="uri/text()" />
                    <xsl:variable name="rolename" select="substring-after($roleuri,concat($rolespath,'/'))"/>
                    <option value="{$roleuri}">
                      role: <xsl:value-of select="$rolename"/>
                    </option>
                  </xsl:for-each>
                  <xsl:for-each select="/document/users/user">
                    <xsl:variable name="useruri" select="uri/text()" />
                    <xsl:variable name="username" select="substring-after($useruri,concat($userspath,'/'))"/>
                    <option value="{$useruri}">
                      user: <xsl:value-of select="$username"/>
                    </option>
                  </xsl:for-each>
                </select>
              </td>
              <td align="left">
                <select name="type">
                  <option>all</option>
                  <option>default</option>
                  <xsl:for-each select="/document/privileges/privilege">
                    <xsl:variable name="actionuri" select="uri/text()" />
                    <xsl:variable name="actionname" select="substring-after($actionuri,concat($actionspath,'/'))"/>
                    <option value="{$actionuri}">
                      <xsl:value-of select="$actionname"/>
                    </option>
                  </xsl:for-each>
                </select>
              </td>
              <td align="left">
                <input name="expiration" type="text" size="15" maxlength="40" value="1"/>
              </td>
              <td align="left">
                <select name="inheritable">
                  <option>false</option>
                  <option>true</option>
                </select>
              </td>
              <td align="left">
                <select name="exclusive">
                  <option>false</option>
                  <option>true</option>
                </select>
              </td>
              <td align="right">
                <input type="submit" name="doAddLock" value="Add/Modify"/>
              </td>
            </form>
          </tr>
        </font>
      </table>
    </column>
  </xsl:template>
  
</xsl:stylesheet>
