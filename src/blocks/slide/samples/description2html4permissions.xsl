<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:source="http://apache.org/cocoon/description/2.0" xmlns:dav="DAV:" xmlns:pl="http://apache.org/cocoon/principal/1.0" version="1.0">

  <xsl:output indent="yes"/>

  <xsl:template match="/">
    <document>
      <header>
        <title>Jakarta Slide example</title>
        <tab title="users" href="../users/"/>
        <tab title="content" href="../content/{substring-after(source:source/@uri,'://')}"/>
        <tab title="properties" href="../properties/{substring-after(source:source/@uri,'://')}"/>
        <tab title="permissions" href="../permissions/{substring-after(source:source/@uri,'://')}"/>
        <tab title="locks" href="../locks/{substring-after(source:source/@uri,'://')}"/>
        <tab title="logout" href="../logout.html"/>
      </header>
      <body>
        <row>
          <xsl:apply-templates select="document/source:source"/>
        </row>
      </body>
    </document>
  </xsl:template>

  <xsl:template match="source:source">
    <column title="Navigation">
      <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2" width="100%" align="center">
        <xsl:if test="@parent">
          <tr>
            <td width="100%" bgcolor="#ffffff" align="left">
              <a href="../permissions/{substring-after(@parent,'://')}">Back</a>
            </td>
          </tr>
        </xsl:if>
        <tr>
          <td width="100%" bgcolor="#ffffff" align="left">
            <br/>
          </td>
        </tr>
        <xsl:for-each select="source:children/source:source">
          <tr>
            <td width="100%" bgcolor="#ffffff" align="left">
              <font size="+0" face="arial,helvetica,sanserif" color="#000000">
                <a href="../permissions/{substring-after(@uri,'://')}">
                  <xsl:value-of select="@name"/>
                </a>
              </font>
            </td>
          </tr>
        </xsl:for-each>
      </table>
    </column>

    <column title="User permissions">
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
          <xsl:for-each select="source:permissions/source:permission[@principal]">
            <tr>
              <td align="left">
                <xsl:value-of select="@principal"/>
              </td>
              <td align="left">
                <xsl:value-of select="@privilege"/>
              </td>
              <td align="left">
                <xsl:value-of select="@inheritable"/>
              </td>
              <td align="left">
                <xsl:value-of select="@negative"/>
              </td>
              <td align="right">
                <form action="" method="post">
                  <input type="hidden" name="cocoon-source-uri" value="{../../@uri}"/>
                  <input type="hidden" name="cocoon-source-permission-principal" value="{@principal}"/>
                  <input type="hidden" name="cocoon-source-permission-privilege" value="{@privilege}"/>
                  <input type="hidden" name="cocoon-source-permission-inheritable" value="{@inheritable}"/>
                  <input type="hidden" name="cocoon-source-permission-negative" value="{@negative}"/>
                  <input type="submit" name="doRemovePrincipalPermission" value="Delete"/>
                </form>
              </td>
            </tr>
          </xsl:for-each>
          <tr>
            <form action="" method="post">
              <input type="hidden" name="cocoon-source-uri" value="{@uri}"/>
              <td align="left">
                <select name="cocoon-source-permission-principal">
                  <option>ALL</option>
                  <option>SELF</option>
                  <option>GUEST</option>
                  <xsl:for-each select="/document/pl:list/pl:principal">
                    <option>
                      <xsl:value-of select="@pl:name"/>
                    </option>
                  </xsl:for-each>
                </select>
              </td>
              <td align="left">
                <select name="cocoon-source-permission-privilege">
                  <option>all</option>
                  <option>read</option>
                  <option>write</option>
                  <option>read-acl</option>
                  <option>write-acl</option>
                  <option>read-source</option>
                  <option>create-source</option>
                  <option>remove-source</option>
                  <option>lock-source</option>
                  <option>read-locks</option>
                  <option>read-property</option>
                  <option>create-property</option>
                  <option>modify-property</option>
                  <option>remove-property</option>
                  <option>read-content</option>
                  <option>create-content</option>
                  <option>modify-content</option>
                  <option>remove-content</option>
                  <option>grant-permission</option>
                  <option>revoke-permission</option>
                </select>
              </td>
              <td align="left">
                <select name="cocoon-source-permission-inheritable">
                  <option>true</option>
                  <option>false</option>
                </select>
              </td>
              <td align="left">
                <select name="cocoon-source-permission-negative">
                  <option>true</option>
                  <option>false</option>
                </select>
              </td>
              <td align="right">
                <input type="submit" name="doAddPrincipalPermission" value="Add/Modify"/>
              </td>
            </form>
          </tr>
        </font>
      </table>
    </column>

    <column title="Group permissions">
      <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2" width="100%" align="center">
        <tr>
          <td align="left">
            <b>Group</b>
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
        <xsl:for-each select="source:permissions/source:permission[@group]">
          <tr>
            <td align="left">
              <xsl:value-of select="@group"/>
            </td>
            <td align="left">
              <xsl:value-of select="@privilege"/>
            </td>
            <td align="left">
              <xsl:value-of select="@inheritable"/>
            </td>
            <td align="left">
              <xsl:value-of select="@negative"/>
            </td>
            <td align="right">
              <form action="" method="post">
                <input type="hidden" name="cocoon-source-uri" value="{../../@uri}"/>
                <input type="hidden" name="cocoon-source-permission-principal-group" value="{@group}"/>
                <input type="hidden" name="cocoon-source-permission-privilege" value="{@privilege}"/>
                <input type="hidden" name="cocoon-source-permission-inheritable" value="{@inheritable}"/>
                <input type="hidden" name="cocoon-source-permission-negative" value="{@negative}"/>
                <input type="submit" name="doRemovePrincipalGroupPermission" value="Delete"/>
              </form>
            </td>
          </tr>
        </xsl:for-each>
        <tr>
          <form action="" method="post">
            <input type="hidden" name="cocoon-source-uri" value="{@uri}"/>
            <td align="left">
              <select name="cocoon-source-permission-principal-group">
                <xsl:for-each select="/document/pl:list/pl:group">
                  <option>
                    <xsl:value-of select="@pl:name"/>
                  </option>
                </xsl:for-each>
              </select>
            </td>
            <td align="left">
              <select name="cocoon-source-permission-privilege">
                <option>all</option>
                <option>read</option>
                <option>write</option>
                <option>read-acl</option>
                <option>write-acl</option>
                <option>read-source</option>
                <option>create-source</option>
                <option>remove-source</option>
                <option>lock-source</option>
                <option>read-locks</option>
                <option>read-property</option>
                <option>create-property</option>
                <option>modify-property</option>
                <option>remove-property</option>
                <option>read-content</option>
                <option>create-content</option>
                <option>modify-content</option>
                <option>remove-content</option>
                <option>grant-permission</option>
                <option>revoke-permission</option>
              </select>
            </td>
            <td align="left">
              <select name="cocoon-source-permission-inheritable">
                <option>true</option>
                <option>false</option>
              </select>
            </td>
            <td align="left">
              <select name="cocoon-source-permission-negative">
                <option>true</option>
                <option>false</option>
              </select>
            </td>
            <td align="right">
              <input type="submit" name="doAddPrincipalPermission" value="Add/Modify"/>
            </td>
          </form>
        </tr>
      </table>
    </column>
  </xsl:template>
</xsl:stylesheet>
