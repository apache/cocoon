<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:source="http://apache.org/cocoon/description/2.0" xmlns:dav="DAV:" version="1.0">

  <xsl:output indent="yes"/>

  <xsl:template match="/">
    <document>
      <header>
        <title>Jakarta Slide example</title>
        <tab title="users" href="/cocoon/samples/slide/users/"/>
        <tab title="content" href="/cocoon/samples/slide/content/{substring-after(source:source/@uri,'://')}"/>
        <tab title="properties" href="/cocoon/samples/slide/properties/{substring-after(source:source/@uri,'://')}"/>
        <tab title="permissions" href="/cocoon/samples/slide/permissions/{substring-after(source:source/@uri,'://')}"/>
        <tab title="locks" href="/cocoon/samples/slide/locks/{substring-after(source:source/@uri,'://')}"/>
        <tab title="logout" href="/cocoon/samples/slide/logout.html"/>
      </header>
      <body>
        <row>
          <xsl:apply-templates select="source:source"/>
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
              <a href="/cocoon/samples/slide/permissions/{substring-after(@parent,'://')}">Back</a>
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
                <a href="/cocoon/samples/slide/locks/{substring-after(@uri,'://')}">
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
          <xsl:for-each select="source:locks/source:lock">
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
                <form action="" method="post">
                  <input type="hidden" name="cocoon-source-uri" value="{../../@uri}"/>
                  <input type="hidden" name="cocoon-lock-subject" value="{@subject}"/>
                  <input type="submit" name="doDeleteLock" value="Delete"/>
                </form>
              </td>
            </tr>
          </xsl:for-each>
          <tr>
            <form action="" method="post">
              <input type="hidden" name="cocoon-source-uri" value="{@uri}"/>
              <td align="left">
                <input name="cocoon-lock-subject" type="text" size="20" maxlength="40"/>
              </td>
              <td align="left">
                <input name="cocoon-lock-type" type="text" size="15" maxlength="40"/>
              </td>
              <td align="left">
                <input name="cocoon-lock-expiration" type="text" size="15" maxlength="40"/>
              </td>
              <td align="left">
                <select name="cocoon-lock-inheritable">
                  <option>true</option>
                  <option>false</option>
                </select>
              </td>
              <td align="left">
                <select name="cocoon-lock-exclusive">
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
