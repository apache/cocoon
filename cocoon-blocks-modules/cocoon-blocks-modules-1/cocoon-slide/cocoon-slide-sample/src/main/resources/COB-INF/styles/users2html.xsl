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
  
  <xsl:param name="userspath"/>
  <xsl:param name="rolespath"/>
   
  <xsl:template name="left">
    <xsl:apply-templates select="/document/users"/>
  </xsl:template>
  
  <xsl:template name="middle">
    <xsl:apply-templates select="/document/roles"/>
  </xsl:template>

  <xsl:template match="/document/users">
    <column title="Users">
      <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2" width="100%" align="center">
        <font size="+0" face="arial,helvetica,sanserif" color="#000000">
          <tr>
            <td align="left">
              <b>User</b>
            </td>
            <td/>
            <td align="left">
              <b>Password</b>
            </td>
            <td/>
            <td align="left">
              <b>Roles</b>
            </td>
            <td align="right"/>
          </tr>
          <xsl:for-each select="user">
            <xsl:variable name="useruri"  select="uri/text()" />
            <xsl:variable name="username" select="substring-after($useruri,concat($userspath,'/'))" />
            <tr>
              <td align="left">
                <xsl:value-of select="$username"/><br/>
              </td>
              <td align="left">
                <form action="{$base}/removeobject">
                  <input type="hidden" name="objecturi" value="{$useruri}"/>
                  <input type="submit" name="doRemoveUser" value="Delete"/>
                </form>
              </td>
              <form action="{$base}/changepwd" method="post">
                <input type="hidden" name="username" value="{$username}"/>
                <td align="left">
                  <input type="password" name="password" size="10" maxlength="40"/>
                </td>
                <td>
                  <input type="submit" name="doSetPassword" value="Change"/>
                </td>
              </form>
              <form action="{$base}/removemember" method="post">
                <input type="hidden" name="subjecturi" value="{$useruri}"/>
                <td align="left">
                  <select name="objecturi">
                    <xsl:for-each select="/document/roles/role[member=$useruri]">
                      <xsl:variable name="roleuri" select="uri/text()" />
                      <xsl:variable name="rolename" select="substring-after($roleuri,concat($rolespath,'/'))" />
                      <option value="{$roleuri}">
                        <xsl:value-of select="$rolename"/>
                      </option>
                    </xsl:for-each>
                  </select>
                </td>
                <td align="left">
                  <input type="submit" name="doRemoveRoleMember" value="Remove role"/>
                </td>
              </form>
            </tr>
            <tr>
              <td colspan="4"/>
              <form action="{$base}/addmember" method="post">
                <input type="hidden" name="subjecturi" value="{$useruri}"/>
                <td align="left">
                  <select name="objecturi" size="1">
                    <xsl:for-each select="/document/roles/role">
                      <xsl:variable name="roleuri" select="uri/text()"/>
                      <xsl:variable name="rolename" select="substring-after($roleuri,concat($rolespath,'/'))"/>
                      <option value="{$roleuri}">
                        <xsl:value-of select="$rolename"/>
                      </option>
                    </xsl:for-each>
                  </select>
                </td>
                <td align="left">
                  <input type="submit" name="doAddRoleMember" value="Add role"/>
                </td>
                <td align="left"/>
              </form>
            </tr>
          </xsl:for-each>
          <tr>
            <form action="{$base}/adduser" method="post">
              <td align="left">
                <input name="username" type="text" size="10" maxlength="40"/>
              </td>
              <td align="left"/>
              <td align="left">
                <input name="password" type="password" size="10" maxlength="40"/>
              </td>
              <td colspan="2"/>
              <td align="right">
                <input type="submit" name="doAddUser" value="Add user"/>
              </td>
            </form>
          </tr>
        </font>
      </table>
    </column>
  </xsl:template>
    
  <xsl:template match="/document/roles">
    <column title="Roles">
      <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2" width="100%" align="center">
        <font size="+0" face="arial,helvetica,sanserif" color="#000000">
          <tr>
            <td align="left">
              <b>Role</b>
            </td>
            <td align="right"/>
          </tr>
          <xsl:for-each select="role">
            <xsl:variable name="roleuri"  select="uri/text()" />
            <xsl:variable name="rolename" select="substring-after($roleuri,concat($rolespath,'/'))" />
            <tr>
              <form action="{$base}/removeobject" method="post">
                <input type="hidden" name="objecturi" value="{$roleuri}"/>
                <td align="left">
                  <xsl:value-of select="$rolename"/>
                </td>
                <td align="right">
                  <input type="submit" name="doRemovePrincipalRole" value="Remove role"/>
                </td>
              </form>
            </tr>
          </xsl:for-each>
          <tr>
            <form action="{$base}/addrole" method="post">
              <td align="left">
                <input name="rolename" type="text" size="15" maxlength="40"/>
              </td>
              <td align="right">
                <input type="submit" name="doAddPrincipalRole" value="Add role"/>
              </td>
            </form>
          </tr>
        </font>
      </table>
    </column>
  </xsl:template>
</xsl:stylesheet>
