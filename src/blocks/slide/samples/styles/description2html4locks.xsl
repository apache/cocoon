<?xml version="1.0"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:collection="http://apache.org/cocoon/collection/1.0" 
  xmlns:dav="DAV:" 
  version="1.0">

  <xsl:output indent="yes"/>
  <xsl:param name="path" />
  
  <xsl:template match="/">
    <document>
      <header>
        <title>Jakarta Slide example</title>
        <tab title="users" href="../users/"/>
        <tab title="content" href="../content/{$path}"/>
        <tab title="properties" href="../properties/{$path}"/>
        <tab title="permissions" href="../permissions/{$path}"/>
        <tab title="locks" href="../locks/{$path}"/>
        <tab title="logout" href="../logout.html"/>
      </header>
      <body>
        <row>
          <xsl:apply-templates select="collection:resource|collection:collection"/>
        </row>
      </body>
    </document>
  </xsl:template>

  <xsl:template match="collection:resource|collection:collection">
    <column title="Navigation">
      <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2" width="100%" align="center">
        <tr>
          <td width="100%" bgcolor="#ffffff" align="left">
            <br/>
          </td>
        </tr>
        <xsl:for-each select="collection:resource|collection:collection">
          <tr>
            <td width="100%" bgcolor="#ffffff" align="left">
              <font size="+0" face="arial,helvetica,sanserif" color="#000000">
                <a href="../locks/{$path}/{@name}">
                  <xsl:value-of select="@name"/>
                </a>
              </font>
            </td>
          </tr>
        </xsl:for-each>
      </table>
    </column>

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
              <b>Expiration</b>
            </td>
            <td align="left">
              <b>Inheritable</b>
            </td>
            <td align="left">
              <b>Exclusive</b>
            </td>
            <td align="right"/>
          </tr>
          <xsl:for-each select="collection:locks/collection:lock">
            <tr bgcolor="#eeeeee">
              <td align="left">
                <xsl:value-of select="@subject"/>
              </td>
              <td align="left">
                <xsl:value-of select="@type"/>
              </td>
              <td align="left">
                <xsl:value-of select="@expiration"/>
              </td>
              <td align="left">
                <xsl:value-of select="@inheritable"/>
              </td>
              <td align="left">
                <xsl:value-of select="@exclusive"/>
              </td>
              <td align="right">
                <form action="../removelock.do" method="post">
                  <input type="hidden" name="resourcePath" value="/{$path}"/>
                  <input type="hidden" name="subject" value="{@subject}"/>
                  <input type="submit" name="doRemoveLock" value="Delete"/>
                </form>
              </td>
            </tr>
          </xsl:for-each>
          <tr>
            <form action="../addlock.do" method="post">
              <input type="hidden" name="resourcePath" value="/{$path}"/>
              <td align="left">
                <input name="subject" type="text" size="20" maxlength="40"/>
              </td>
              <td align="left">
                <input name="type" type="text" size="15" maxlength="40"/>
              </td>
              <td align="left">
                <input name="expiration" type="text" size="15" maxlength="40"/>
              </td>
              <td align="left">
                <select name="inheritable">
                  <option>true</option>
                  <option>false</option>
                </select>
              </td>
              <td align="left">
                <select name="exclusive">
                  <option>true</option>
                  <option>false</option>
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
