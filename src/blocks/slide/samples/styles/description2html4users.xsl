<?xml version="1.0"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:col="http://apache.org/cocoon/collection/1.0" 
  xmlns:dav="DAV:" 
  version="1.0">

  <xsl:output indent="yes"/>
  <xsl:param name="base">/samples/slide</xsl:param>

  <xsl:template match="/">
    <document>
      <header>
        <title>Jakarta Slide example</title>
        <tab title="users" href="{$base}/users/"/>
        <tab title="content" href="{$base}/content/"/>
        <tab title="properties" href="{$base}/properties/"/>
        <tab title="permissions" href="{$base}/permissions/"/>
        <tab title="locks" href="{$base}/locks/"/>
        <tab title="logout" href="{$base}/logout.html"/>
      </header>
      <body>
        <row>
          <xsl:apply-templates select="/users/users/*|/users/groups/*"/>
        </row>
      </body>
    </document>
  </xsl:template>

  <xsl:template match="/users/users/col:collection[@name='users']">
    <column title="Users">
      <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2" width="100%" align="center">
        <font size="+0" face="arial,helvetica,sanserif" color="#000000">
          <tr>
            <td align="left">
              <b>User</b>
            </td>
            <td align="left"/>
            <td align="left">
              <b>Role</b>
            </td>
            <td align="left">
              <b>Groups</b>
            </td>
            <td align="left"/>
            <td align="right"/>
          </tr>
          <xsl:for-each select="col:collection|col:resource">
            <xsl:variable name="name"><xsl:value-of select="@name"/></xsl:variable>
            <xsl:variable name="uri">/users/<xsl:value-of select="@name"/></xsl:variable>
            <tr>
              <form action="{$base}/removeuser.do" method="post">
                <input type="hidden" name="username" value="{@name}"/>
                <td align="left">
                  <xsl:value-of select="@name"/>
                </td>
                <td align="left">
                  <input type="submit" name="doRemoveUser" value="Delete user"/>
                </td>
              </form>
              <td align="left">
                <xsl:value-of select="/users/roles/*/*[col:properties/dav:group-member-set/dav:href=$uri]/@name"/>
              </td>
              <form action="{$base}/removemember.do" method="post">
                <input type="hidden" name="username" value="{@name}"/>
                <td align="left">
                  <xsl:variable name="name" select="@name"/>
                  <select name="groupname" size="{count(/users/groups/*/*[col:properties/dav:group-member-set/dav:href=$uri])}">
                    <xsl:for-each select="/users/groups/*/*[col:properties/dav:group-member-set/dav:href=$uri]">
                      <option>
                        <xsl:value-of select="@name"/>
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
              <td align="left"/>
              <form action="{$base}/addmember.do" method="post">
                <input type="hidden" name="username" value="{@name}"/>
                <td align="left">
                  <xsl:variable name="name" select="@name"/>
                  <select name="groupname" size="1">
                    <xsl:for-each select="/users/groups/*/*">
                      <option>
                        <xsl:value-of select="@name"/>
                      </option>
                    </xsl:for-each>
                  </select>
                </td>
                <td align="left">
                  <input type="submit" name="doAddPrincipalGroupMember" value="Add group"/>
                </td>
              </form>
            </tr>
          </xsl:for-each>
          <tr>
            <form action="{$base}/adduser.do" method="post">
              <td align="left">
                <input name="username" type="text" size="10" maxlength="40"/>
              </td>
              <td align="left"/>
              <td align="left">
                <input name="role" type="text" size="10" maxlength="40"/>
              </td>
              <td align="left">Password:</td>
              <td align="left">
                <input name="password" type="text" size="10" maxlength="40"/>
              </td>
              <td align="right">
                <input type="submit" name="doAddUser" value="Add user"/>
              </td>
            </form>
          </tr>
        </font>
      </table>
    </column>
  </xsl:template>
  
  <xsl:template match="/users/groups/col:collection[@name='groups']">
    <column title="Groups">
      <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2" width="100%" align="center">
        <font size="+0" face="arial,helvetica,sanserif" color="#000000">
          <tr>
            <td align="left">
              <b>Group</b>
            </td>
            <td align="right"/>
          </tr>
          <xsl:for-each select="col:collection|col:resource">
            <tr>
              <form action="{$base}/removegroup.do" method="post">
                <input type="hidden" name="groupname" value="{@name}"/>
                <td align="left">
                  <xsl:value-of select="@name"/>
                </td>
                <td align="right">
                  <input type="submit" name="doRemovePrincipalGroup" value="Delete group"/>
                </td>
              </form>
            </tr>
          </xsl:for-each>
          <tr>
            <form action="{$base}/addgroup.do" method="post">
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
