<?xml version="1.0"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:col="http://apache.org/cocoon/collection/1.0" 
  version="1.0">

  <xsl:import href="layout.xsl"/>
  <xsl:output indent="yes"/>
  
  <xsl:param name="base"/>
  <xsl:param name="path"/>
  <xsl:param name="type">permissions</xsl:param>

  <xsl:param name="userspath"/>
  <xsl:param name="rolespath"/>
  <xsl:param name="groupspath"/>
  <xsl:param name="actionspath"/>
  
  <xsl:template name="middle">
    <column title="Permissions">
      <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2" width="100%" align="center">
        <font size="+0" face="arial,helvetica,sanserif" color="#000000">
          <tr>
            <td align="left">
              <b>Principal</b>
            </td>
            <td align="left">
              <b>Privilege</b>
            </td>
            <td align="left">
              <b>Inheritable</b>
            </td>
            <td align="left">
              <b>Deny</b>
            </td>
            <td align="right"/>
          </tr>
          <xsl:for-each select="/document/permissions/permission">
            <tr>
              <td align="left">
                <xsl:value-of select="subject"/>
              </td>
              <td align="left">
                <xsl:value-of select="privilege"/>
              </td>
              <td align="left">
                <xsl:value-of select="inheritable"/>
              </td>
              <td align="left">
                <xsl:value-of select="negative"/>
              </td>
              <td align="right">
                <form action="{$base}/removePermission" method="post">
                  <input type="hidden" name="resourcePath" value="{$path}"/>
                  <input type="hidden" name="subject" value="{subject}"/>
                  <input type="hidden" name="privilege" value="{privilege}"/>
                  <input type="hidden" name="inheritable" value="{inheritable}"/>
                  <input type="hidden" name="negative" value="{negative}"/>
                  <input type="submit" name="doRemovePermission" value="Delete"/>
                </form>
              </td>
            </tr>
          </xsl:for-each>
          <tr>
            <form action="{$base}/addPermission" method="post">
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
                  <xsl:for-each select="/document/groups/group">
                    <xsl:variable name="groupuri" select="uri/text()" />
                    <xsl:variable name="groupname" select="substring-after($groupuri,concat($groupspath,'/'))"/>
                    <option value="{$groupuri}">
                      group: <xsl:value-of select="$groupname"/>
                    </option>
                  </xsl:for-each>
                </select>
              </td>
              <td align="left">
                <select name="action">
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
                <select name="inheritable">
                  <option>false</option>
                  <option>true</option>
                </select>
              </td>
              <td align="left">
                <select name="negative">
                  <option>false</option>
                  <option>true</option>
                </select>
              </td>
              <td align="right">
                <input type="submit" name="doAddPermission" value="Add/Modify"/>
              </td>
            </form>
          </tr>
        </font>
      </table>
    </column>
  </xsl:template>
</xsl:stylesheet>
