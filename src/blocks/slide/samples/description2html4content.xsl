<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:source="http://apache.org/cocoon/description/2.0" xmlns:dav="DAV:" xmlns:xi="http://www.w3.org/2001/XInclude" version="1.0">

  <xsl:output indent="yes"/>

  <xsl:param name="cocoon-source-principal">guest</xsl:param>

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
              <a href="/cocoon/samples/slide/content/{substring-after(@parent,'://')}">Back</a>
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
                <a href="/cocoon/samples/slide/content/{substring-after(@uri,'://')}">
                  <xsl:value-of select="@name"/>
                </a>
              </font>
            </td>
          </tr>
        </xsl:for-each>
      </table>
    </column>
    <column title="Content">
      <xsl:choose>
        <xsl:when test="@collection='true'">
          <table width="100%" cellspacing="0" cellpadding="5" align="center">
            <font size="+0" face="arial,helvetica,sanserif" color="#000000">
              <tr>
                <td align="left">
                  <b>Filename</b>
                </td>
                <td align="left">
                  <b>Type</b>
                </td>
                <td align="left">
                  <b>Size</b>
                </td>
                <td align="left">
                  <b>Last Modified</b>
                </td>
                <td align="right"/>
              </tr>
              <xsl:for-each select="source:children/source:source">
                <tr>
                  <td align="left">&#xA0;&#xA0;
                   <a href="/cocoon/samples/slide/content/{substring-after(@uri,'://')}"><xsl:value-of select="@name"/></a>
                  </td>
                  <td align="left">
                    <xsl:value-of select="@mime-type"/>
                  </td>
                  <td align="left">
                    <xsl:value-of select="@contentlength"/>
                  </td>
                  <td align="left">
                    <xsl:value-of select="source:properties/dav:getlastmodified"/>
                  </td>
                  <td align="right">
                    <form action="" method="post">
                      <input type="hidden" name="cocoon-source-uri" value="{@uri}"/>
                      <input type="submit" name="doDeleteSource" value="Delete"/>
                    </form>
                  </td>
                </tr>
              </xsl:for-each>
              <tr>
                <form action="" method="post" enctype="multipart/form-data">
                  <input type="hidden" name="cocoon-source-uri" value="{@uri}"/>
                  <td align="left"><input type="text" name="cocoon-source-name" size="15" maxlength="40"/>(optional)</td>
                  <td align="left" colspan="3">
                   File:
                   <input type="file" name="cocoon-upload-file" size="15" maxlength="40"/>
                  </td>
                  <td align="right">
                    <input type="submit" name="doUploadSource" value="Upload File"/>
                  </td>
                </form>
              </tr>
              <tr>
                <form action="" method="post">
                  <input type="hidden" name="cocoon-source-uri" value="{@uri}"/>
                  <td align="left" colspan="4">
                    <input type="text" name="cocoon-source-name" size="15" maxlength="40"/>
                  </td>
                  <td align="right">
                    <input type="submit" name="doCreateCollection" value="Create collection"/>
                  </td>
                </form>
              </tr>
            </font>
          </table>
        </xsl:when>
        <xsl:when test="@mime-type='image/gif'">
          <img src="/cocoon/samples/slide/view/{substring-after(@uri,'://')}"/>
        </xsl:when>
        <xsl:when test="@mime-type='image/jpeg'">
          <img src="/cocoon/samples/slide/view/{substring-after(@uri,'://')}"/>
        </xsl:when>
        <xsl:when test="@mime-type='text/plain'">
          <pre>
            <xi:include href="{@uri}?cocoon-source-principal={$cocoon-source-principal}" parse="text"/>
          </pre>
        </xsl:when>
        <xsl:when test="@mime-type='text/xml'">
          <pre>
            <xi:include href="{@uri}?cocoon-source-principal={$cocoon-source-principal}" parse="text"/>
          </pre>
        </xsl:when>
        <xsl:otherwise>
          <h3>Could not display content.</h3>
        </xsl:otherwise>
      </xsl:choose>
    </column>
  </xsl:template>
</xsl:stylesheet>
