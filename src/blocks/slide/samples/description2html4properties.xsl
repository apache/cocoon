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
              <a href="/cocoon/samples/slide/properties/{substring-after(@parent,'://')}">Back</a>
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
                <a href="/cocoon/samples/slide/properties/{substring-after(@uri,'://')}">
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
          <xsl:for-each select="source:properties/*[local-name()!='children' and
                                local-name()!='permissions' and local-name()!='locks' and
                                local-name()!='parent']">
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
                <xsl:if test="namespace-uri()!='DAV:' and ../@type='live'">
                  <form action="" method="post">
                    <input type="hidden" name="cocoon-source-uri" value="{../../@uri}"/>
                    <input type="hidden" name="cocoon-source-property-namespace" value="{namespace-uri()}"/>
                    <input type="hidden" name="cocoon-source-property-name" value="{local-name()}"/>
                    <input type="submit" name="doDeleteProperty" value="Delete"/>
                  </form>
                </xsl:if>
              </td>
            </tr>
          </xsl:for-each>
          <tr>
            <form action="" method="post">
              <input type="hidden" name="cocoon-source-uri" value="{@uri}"/>
              <td align="left">
                <input name="cocoon-source-property-namespace" type="text" size="15" maxlength="40"/>
              </td>
              <td align="left">
                <input name="cocoon-source-property-name" type="text" size="15" maxlength="40"/>
              </td>
              <td align="left">
                <input name="cocoon-source-property-value" type="text" size="15" maxlength="40"/>
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
