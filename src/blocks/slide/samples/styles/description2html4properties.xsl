<?xml version="1.0"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:collection="http://apache.org/cocoon/collection/1.0" 
  xmlns:dav="DAV:" 
  version="1.0">

  <xsl:output indent="yes"/>
  <xsl:param name="path" />
  <xsl:param name="namespace">cocoon</xsl:param>
  <xsl:param name="principal">guest</xsl:param>

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
          <xsl:apply-templates select="collection:collection|collection:resource"/>
        </row>
      </body>
    </document>
  </xsl:template>

  <xsl:template match="collection:collection|collection:resource">
    <column title="Navigation">
      <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2" width="100%" align="center">
        <tr>
          <td width="100%" bgcolor="#ffffff" align="left">
            <br/>
          </td>
        </tr>
        <xsl:for-each select="collection:collection|collection:resource">
          <tr>
            <td width="100%" bgcolor="#ffffff" align="left">
              <font size="+0" face="arial,helvetica,sanserif" color="#000000">
                <a href="../properties/{$path}/{@name}">
                  <xsl:value-of select="@name"/>
                </a>
              </font>
            </td>
          </tr>
        </xsl:for-each>
      </table>
    </column>

    <column title="Properties">
      <table bgcolor="#ffffff" border="0" cellspacing="0" cellpadding="2" width="100%" align="center">
        <font size="+0" face="arial,helvetica,sanserif" color="#000000">
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
          <xsl:for-each select="collection:properties/child::node()">
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
                  <form action="../removeproperty.do" method="post">
                    <input type="hidden" name="resourcePath" value="/{$path}"/>
                    <input type="hidden" name="namespace" value="{namespace-uri()}"/>
                    <input type="hidden" name="name" value="{local-name()}"/>
                    <input type="submit" name="doDeleteProperty" value="Delete"/>
                  </form>
                </xsl:if>
              </td>
            </tr>
          </xsl:for-each>
          <tr>
            <form action="../addproperty.do" method="post">
              <input type="hidden" name="resourcePath" value="/{$path}"/>
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
    </column>
  </xsl:template>
</xsl:stylesheet>
