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
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:collection="http://apache.org/cocoon/collection/1.0"
  xmlns:D="DAV:">
  
  <xsl:param name="requestURI"></xsl:param>
  <xsl:variable name="adjustedRequestURI">
    <xsl:choose>
      <xsl:when test="substring($requestURI, string-length($requestURI),1)='/'"><xsl:value-of select="$requestURI"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$requestURI"/>/</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  
  <xsl:template match="/">
    <D:multistatus>
      <xsl:apply-templates />
    </D:multistatus>
  </xsl:template>
  
  <xsl:template match="/collection:collection">
    <xsl:call-template name="collection">
      <xsl:with-param name="href" select="$adjustedRequestURI" />
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="collection:collection">
    <xsl:param name="parent-href" />
    <xsl:call-template name="collection">
      <xsl:with-param name="href">
        <xsl:value-of select="$parent-href" /><xsl:value-of select="@name"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="/collection:resource">
    <xsl:call-template name="resource">
      <xsl:with-param name="href" select="$adjustedRequestURI" />
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="collection:resource">
    <xsl:param name="parent-href" />
    <xsl:call-template name="resource">
      <xsl:with-param name="href">
        <xsl:value-of select="$parent-href" /><xsl:value-of select="@name"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="collection">
    <xsl:param name="href" />
    <D:response>
      <D:href><xsl:value-of select="$href"/></D:href>
      <D:propstat>
        <D:prop>
          <D:displayname><xsl:value-of select="@name"/></D:displayname>
          <D:getlastmodified><xsl:value-of select="@date"/></D:getlastmodified>
          <D:creationdate />
          <D:resourcetype><D:collection/></D:resourcetype>
          <D:getcontenttype>httpd/unix-directory</D:getcontenttype>
          <D:contentlength>0</D:contentlength>
          <xsl:copy-of select="collection:properties/child::node()" />
        </D:prop>
        <D:status>HTTP/1.1 200 OK</D:status>
      </D:propstat>
    </D:response>
    <xsl:apply-templates>
      <xsl:with-param name="parent-href" select="$href" />
    </xsl:apply-templates>
  </xsl:template>
    
  <xsl:template name="resource">
    <xsl:param name="href" />
    <D:response>
      <D:href><xsl:value-of select="$href"/></D:href>
      <D:propstat>
        <D:prop>
          <D:displayname><xsl:value-of select="@name"/></D:displayname>
          <D:getlastmodified><xsl:value-of select="@date"/></D:getlastmodified>
          <D:creationdate />
          <D:getcontenttype><xsl:value-of select="@mimeType"/></D:getcontenttype>
          <D:getcontentlength><xsl:value-of select="@size" /></D:getcontentlength>
          <D:resourcetype />
          <xsl:copy-of select="collection:properties/child::node()" />
        </D:prop>
        <D:status>HTTP/1.1 200 OK</D:status>
      </D:propstat>
    </D:response>
  </xsl:template>

</xsl:stylesheet>
