<?xml version="1.0"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:col="http://apache.org/cocoon/collection/1.0" 
  xmlns:dav="DAV:" 
  version="1.0">

  <xsl:output indent="yes"/>
  <xsl:param name="base">/samples/slide</xsl:param>
  <xsl:param name="path" />
  
  <xsl:template match="/">
    <document>
      <header>
        <title>Jakarta Slide example</title>
        <tab title="users" href="{$base}/users/"/>
        <tab title="content" href="{$base}/content/{$path}"/>
        <tab title="properties" href="{$base}/properties/{$path}"/>
        <tab title="permissions" href="{$base}/permissions/{$path}"/>
        <tab title="locks" href="{$base}/locks/{$path}"/>
        <tab title="logout" href="{$base}/logout.html"/>
      </header>
      <body>
        <row>
          <xsl:apply-templates select="/document/col:collection|/document/col:resource"/>
        </row>
      </body>
    </document>
  </xsl:template>

  <xsl:template match="col:collection|col:resource">
    <column title="Navigation">
      <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2" width="100%" align="center">
        <tr>
          <td width="100%" bgcolor="#ffffff" align="left">
            <br/>
          </td>
        </tr>
        <xsl:for-each select="col:collection|col:resource">
          <tr>
            <td width="100%" bgcolor="#ffffff" align="left">
              <font size="+0" face="arial,helvetica,sanserif" color="#000000">
                <xsl:choose>
                  <xsl:when test="$path = ''">
	                <a href="{$base}/permissions/{@name}">
	                  <xsl:value-of select="@name"/>
	                </a>
                  </xsl:when>
                  <xsl:otherwise>
	                <a href="{$base}/permissions/{$path}/{@name}">
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
                <xsl:value-of select="action"/>
              </td>
              <td align="left">
                <xsl:value-of select="inheritable"/>
              </td>
              <td align="left">
                <xsl:value-of select="negative"/>
              </td>
              <td align="right">
                <form action="{$base}/removePermission.do" method="post">
                  <input type="hidden" name="resourcePath" value="{$path}"/>
                  <input type="hidden" name="subject" value="{subject}"/>
                  <input type="hidden" name="action" value="{action}"/>
                  <input type="hidden" name="inheritable" value="{inheritable}"/>
                  <input type="hidden" name="negative" value="{negative}"/>
                  <input type="submit" name="doRemovePermission" value="Delete"/>
                </form>
              </td>
            </tr>
          </xsl:for-each>
          <tr>
            <form action="{$base}/addPermission.do" method="post">
              <input type="hidden" name="resourcePath" value="{$path}"/>
              <td align="left">
                <select name="subject">
                  <option>self</option>
                  <option>owner</option>
                  <option>authenticated</option>
                  <option>unauthenticated</option>
                  <option>all</option>
                  <xsl:for-each select="/document/roles/role">
                    <option>
                      <xsl:value-of select="name"/>
                    </option>
                  </xsl:for-each>
                  <xsl:for-each select="/document/users/user">
                    <option>
                      <xsl:value-of select="name"/>
                    </option>
                  </xsl:for-each>
                  <xsl:for-each select="/document/groups/group">
                    <option>
                      <xsl:value-of select="name"/>
                    </option>
                  </xsl:for-each>
                </select>
              </td>
              <td align="left">
                <select name="action">
                  <option>all</option>
                  <option>default</option>
                  <xsl:for-each select="/document/actions/action">
                    <option>
                      <xsl:value-of select="name"/>
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
