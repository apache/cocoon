<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pl="http://apache.org/cocoon/principal/1.0" version="1.0">

  <xsl:output indent="yes"/>

  <xsl:template match="/">
    <document>
      <header>
        <title>Jakarta Slide example</title>
        <tab title="users" href="/cocoon/samples/slide/users/"/>
        <tab title="content" href="/cocoon/samples/slide/content/"/>
        <tab title="properties" href="/cocoon/samples/slide/properties/"/>
        <tab title="permissions" href="/cocoon/samples/slide/permissions/"/>
        <tab title="locks" href="/cocoon/samples/slide/locks/"/>
        <tab title="logout" href="/cocoon/samples/slide/logout.html"/>
      </header>
      <body>
        <row>
          <xsl:apply-templates select="pl:list"/>
        </row>
      </body>
    </document>
  </xsl:template>

  <xsl:template match="pl:list">
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
          <xsl:for-each select="pl:principal">
            <tr>
              <form action="" method="post">
                <input type="hidden" name="cocoon-principal-name" value="{@name}"/>
                <td align="left">
                  <xsl:value-of select="@name"/>
                </td>
                <td align="left">
                  <input type="submit" name="doRemovePrincipal" value="Delete user"/>
                </td>
              </form>
              <td align="left">
                <xsl:value-of select="@role"/>
              </td>
              <form action="" method="post">
                <input type="hidden" name="cocoon-principal-name" value="{@name}"/>
                <td align="left">
                  <xsl:variable name="name" select="@name"/>
                  <select name="cocoon-principal-group-name" size="{count(../pl:group/pl:principal[@name=$name])}">
                    <xsl:for-each select="../pl:group/pl:principal[@name=$name]">
                      <option>
                        <xsl:value-of select="../@name"/>
                      </option>
                    </xsl:for-each>
                  </select>
                </td>
                <td align="left">
                  <input type="submit" name="doRemovePrincipalGroupMember" value="Remove group"/>
                </td>
              </form>
            </tr>
            <tr>
              <td align="left"/>
              <td align="left"/>
              <td align="left"/>
              <form action="" method="post">
                <input type="hidden" name="cocoon-principal-name" value="{@name}"/>
                <td align="left">
                  <xsl:variable name="name" select="@name"/>
                  <select name="cocoon-principal-group-name" size="1">
                    <xsl:for-each select="../pl:group">
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
            <form action="" method="post">
              <td align="left">
                <input name="cocoon-principal-name" type="text" size="10" maxlength="40"/>
              </td>
              <td align="left"/>
              <td align="left">
                <input name="cocoon-principal-role" type="text" size="10" maxlength="40"/>
              </td>
              <td align="left">Password:</td>
              <td align="left">
                <input name="cocoon-principal-password" type="text" size="10" maxlength="40"/>
              </td>
              <td align="right">
                <input type="submit" name="doAddPrincipal" value="Add user"/>
              </td>
            </form>
          </tr>
        </font>
      </table>
    </column>

    <column title="Groups">
      <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2" width="100%" align="center">
        <font size="+0" face="arial,helvetica,sanserif" color="#000000">
          <tr>
            <td align="left">
              <b>Group</b>
            </td>
            <td align="right"/>
          </tr>
          <xsl:for-each select="pl:group">
            <tr>
              <form action="" method="post">
                <input type="hidden" name="cocoon-principal-group-name" value="{@name}"/>
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
            <form action="" method="post">
              <td align="left">
                <input name="cocoon-principal-group-name" type="text" size="15" maxlength="40"/>
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
