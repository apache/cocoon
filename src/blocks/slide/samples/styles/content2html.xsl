<?xml version="1.0"?>
<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:col="http://apache.org/cocoon/collection/1.0" 
  xmlns:xi="http://www.w3.org/2001/XInclude" 
  xmlns:DAV="DAV:"
  version="1.0">

  <xsl:import href="layout.xsl" />
  <xsl:output indent="yes"/>
  
  <xsl:param name="base"/>
  <xsl:param name="path"/>
  
  <xsl:param name="namespace"/>
  <xsl:param name="principal"/>

  <xsl:param name="type">content</xsl:param>
  
  <xsl:template name="middle">
    <column title="Content">
      <xsl:apply-templates select="/document/col:resource|/document/col:collection" />
    </column>
  </xsl:template>
  
  <xsl:template match="col:resource|col:collection">
    <xsl:choose>
      <xsl:when test="local-name() = 'collection'">
        <table width="100%" cellspacing="0" cellpadding="5" align="center">
          <font size="+1" face="arial,helvetica,sanserif" color="#000000">
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
            <xsl:for-each select="col:collection|col:resource">
              <tr>
                <td align="left">&#xA0;&#xA0;
                  <xsl:choose>
                    <xsl:when test="$path != ''">
                      <a href="{$base}/{$type}/{$path}/{@name}">
                        <xsl:value-of select="@name"/>
                      </a>
                    </xsl:when>
                    <xsl:otherwise>
                      <a href="{$base}/{$type}/{@name}">
                        <xsl:value-of select="@name"/>
                      </a>
                    </xsl:otherwise>
                  </xsl:choose>
                </td>
                <td align="left">
                  <xsl:value-of select="col:properties/DAV:getcontenttype"/>
                </td>
                <td align="left">
                  <xsl:value-of select="col:properties/DAV:getcontentlength"/>
                </td>
                <td align="left">
                  <xsl:value-of select="col:properties/DAV:modificationdate"/>
                </td>
                <td align="right">
                  <form action="{$base}/delete" method="post">
                    <input type="hidden" name="parentPath" value="{$path}" />
                    <input type="hidden" name="resourceName" value="{@name}"/>
                    <input type="submit" name="doDeleteSource" value="Delete"/>
                  </form>
                </td>
              </tr>
            </xsl:for-each>
            <tr>
              <form action="{$base}/upload" method="post" enctype="multipart/form-data">
                <input type="hidden" name="parentPath" value="{$path}"/>
                <td align="left"><input type="text" name="resourceName" size="15" maxlength="40"/></td>
                <td align="left" colspan="3">
                 File:
                 <input type="file" name="uploadFile" size="15" maxlength="40"/>
                </td>
                <td align="right">
                  <input type="submit" name="doUploadSource" value="Upload File"/>
                </td>
              </form>
            </tr>
            <tr>
              <form action="{$base}/mkcol" method="post">
                <input type="hidden" name="parentPath" value="{$path}"/>
                <td align="left" colspan="4">
                  <input type="text" name="collectionName" size="15" maxlength="40"/>
                </td>
                <td align="right">
                  <input type="submit" name="doCreateCollection" value="Create collection"/>
                </td>
              </form>
            </tr>
          </font>
        </table>
      </xsl:when>
      <xsl:when test="col:properties/DAV:getcontenttype='text/plain'">
        <pre>
          <xi:include href="slide://{$principal}@{$namespace}/{$path}" parse="text"/>
        </pre>
      </xsl:when>
      <xsl:when test="col:properties/DAV:getcontenttype='text/xml'">
        <pre>
          <xi:include href="slide://{$principal}@{$namespace}/{$path}" parse="text"/>
        </pre>
      </xsl:when>
      <xsl:otherwise>
        <h3>Could not display content.</h3>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>
