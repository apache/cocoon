<?xml version="1.0"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:col="http://apache.org/cocoon/collection/1.0" 
  xmlns:dav="DAV:" 
  version="1.0">
  
  <xsl:import href="layout.xsl" />
  <xsl:output indent="yes"/>
  
  <xsl:param name="base"/>
  
  <xsl:param name="userspath"/>
  <xsl:param name="rolespath"/>
  <xsl:param name="groupspath"/>
  
  <xsl:template name="body">
    <row>
      <xsl:call-template name="top" />
    </row>
    <row>
      <xsl:call-template name="bottom" />
    </row>
  </xsl:template>
  
  <xsl:template name="top">
    <xsl:apply-templates select="/document/users"/>
  </xsl:template>
  
  <xsl:template name="bottom">
    <xsl:apply-templates select="/document/groups"/>
  </xsl:template>

  <xsl:template match="/document/users">
    <column title="Users">
      <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2" width="100%" align="center">
        <font size="+0" face="arial,helvetica,sanserif" color="#000000">
          <tr>
            <td align="left">
              <b>User</b>
            </td>
            <td align="left"/>
            <td align="left">
              <b>Roles</b>
            </td>
            <td align="left"/>
            <td align="left">
              <b>Groups</b>
            </td>
            <td align="left"/>
            <td align="right"/>
          </tr>
          <xsl:for-each select="user">
            <xsl:variable name="useruri"  select="uri/text()" />
            <xsl:variable name="username" select="substring-after($useruri,concat($userspath,'/'))" />
            <tr>
              <form action="{$base}/removeobject" method="post">
                <input type="hidden" name="objecturi" value="{$useruri}"/>
                <td align="left">
                  <xsl:value-of select="$username"/>
                </td>
                <td align="left">
                  <input type="submit" name="doRemoveUser" value="Delete user"/>
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
              <form action="{$base}/removemember" method="post">
                <input type="hidden" name="subjecturi" value="{$useruri}"/>
                <td align="left">
                  <select name="objecturi">
                    <xsl:for-each select="/document/groups/group[member=$useruri]">
                      <xsl:variable name="groupuri" select="uri/text()" />
                      <xsl:variable name="groupname" select="substring-after($groupuri,concat($groupspath,'/'))" />
                      <option value="{$groupuri}">
                        <xsl:value-of select="$groupname"/>
                      </option>
                    </xsl:for-each>
                  </select>
                </td>
                <td align="left">
                  <input type="submit" name="doRemoveGroupMember" value="Remove group"/>
                </td>
              </form>
            </tr>
            <tr>
              <td align="left"/>
              <td align="left"/>
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
              </form>
              <form action="{$base}/addmember" method="post">
                <input type="hidden" name="subjecturi" value="{$useruri}"/>
                <td align="left">
                  <select name="objecturi" size="1">
                    <xsl:for-each select="/document/groups/group">
                      <xsl:variable name="groupuri" select="uri/text()"/>
                      <xsl:variable name="groupname" select="substring-after($groupuri,concat($groupspath,'/'))"/>
                      <option value="{$groupuri}">
                        <xsl:value-of select="$groupname"/>
                      </option>
                    </xsl:for-each>
                  </select>
                </td>
                <td align="left">
                  <input type="submit" name="doAddGroupMember" value="Add group"/>
                </td>
              </form>
            </tr>
          </xsl:for-each>
          <tr>
            <form action="{$base}/adduser" method="post">
              <td align="left">
                <input name="username" type="text" size="10" maxlength="40"/>
              </td>
              <td align="left">
                <input name="password" type="password" size="10" maxlength="40"/>
              </td>
              <td align="left"/>
              <td align="left"/>
              <td align="left"/>
              <td align="right">
                <input type="submit" name="doAddUser" value="Add user"/>
              </td>
            </form>
          </tr>
        </font>
      </table>
    </column>
  </xsl:template>
    
  <xsl:template match="/document/groups">
    <column title="Groups">
      <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2" width="100%" align="center">
        <font size="+0" face="arial,helvetica,sanserif" color="#000000">
          <tr>
            <td align="left">
              <b>Group</b>
            </td>
            <td align="right"/>
          </tr>
          <xsl:for-each select="group">
            <xsl:variable name="groupuri"  select="uri/text()" />
            <xsl:variable name="groupname" select="substring-after($groupuri,concat($groupspath,'/'))" />
            <tr>
              <form action="{$base}/removeobject" method="post">
                <input type="hidden" name="objecturi" value="{$groupuri}"/>
                <td align="left">
                  <xsl:value-of select="$groupname"/>
                </td>
                <td align="right">
                  <input type="submit" name="doRemovePrincipalGroup" value="Delete group"/>
                </td>
              </form>
            </tr>
          </xsl:for-each>
          <tr>
            <form action="{$base}/addgroup" method="post">
              <td align="left">
                <input name="groupname" type="text" size="15" maxlength="40"/>
              </td>
              <td align="right">
                <input type="submit" name="doAddPrincipalGroup" value="Add group"/>
              </td>
            </form>
          </tr>
        </font>
      </table>
    </column>
  </xsl:template>
</xsl:stylesheet>
