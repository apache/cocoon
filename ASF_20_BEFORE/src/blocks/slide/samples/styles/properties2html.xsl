<?xml version="1.0"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:col="http://apache.org/cocoon/collection/1.0" 
  xmlns:DAV="DAV:"
  version="1.0">

  <xsl:import href="layout.xsl" />
  <xsl:output indent="yes"/>
  
  <xsl:param name="base"/>
  <xsl:param name="path"/>
  
  <xsl:param name="type">properties</xsl:param>

  <xsl:template name="middle">
    <column title="Properties">
      <xsl:apply-templates select="/document/col:resource|/document/col:collection" />
    </column>
  </xsl:template>

  <xsl:template match="col:resource|col:collection">
      <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2" width="100%" align="center">
        <font size="+1" face="arial,helvetica,sanserif" color="#000000">
          <tr>
            <td align="left">
              <b>Namespace</b>
            </td>
            <td align="left">
              <b>Name</b>
            </td>
            <td align="left">
              <b>Value</b>
            </td>
            <td align="right"/>
          </tr>
          <xsl:for-each select="col:properties/child::node()">
            <tr>
              <td align="left">
                <xsl:value-of select="namespace-uri(.)"/>
              </td>
              <td align="left">
                <xsl:value-of select="local-name(.)"/>
              </td>
              <td align="left">
                <xsl:value-of select="."/>
              </td>
              <td align="right">
                <xsl:if test="namespace-uri()!='DAV:'">
                  <form action="{$base}/removeproperty" method="post">
                    <input type="hidden" name="resourcePath" value="{$path}"/>
                    <input type="hidden" name="namespace" value="{namespace-uri()}"/>
                    <input type="hidden" name="name" value="{local-name()}"/>
                    <input type="submit" name="doDeleteProperty" value="Delete"/>
                  </form>
                </xsl:if>
              </td>
            </tr>
          </xsl:for-each>
          <tr>
            <form action="{$base}/addproperty" method="post">
              <input type="hidden" name="resourcePath" value="{$path}"/>
              <td align="left">
                <input name="namespace" type="text" size="15" maxlength="40"/>
              </td>
              <td align="left">
                <input name="name" type="text" size="15" maxlength="40"/>
              </td>
              <td align="left">
                <input name="value" type="text" size="15" maxlength="40"/>
              </td>
              <td align="right">
                <input type="submit" name="doAddProperty" value="Add/Modify"/>
              </td>
            </form>
          </tr>
        </font>
      </table>
  </xsl:template>
</xsl:stylesheet>
