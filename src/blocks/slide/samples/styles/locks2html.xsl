<?xml version="1.0"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:col="http://apache.org/cocoon/collection/1.0" 
  version="1.0">
  
  <xsl:import href="layout.xsl" />
  <xsl:output indent="yes"/>
  
  <xsl:param name="base"/>
  <xsl:param name="path"/>
  <xsl:param name="type">locks</xsl:param>
  
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
