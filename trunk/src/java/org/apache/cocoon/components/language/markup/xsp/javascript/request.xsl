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
<!--
 * @author <a href="mailto:ricardo@apache.org>Ricardo Rocha</a>
 * @author <a href="mailto:vgritsenko@apache.org>Vadim Gritsenko</a>
 * @version CVS $Revision: 1.2 $ $Date: 2004/03/10 09:46:23 $
-->

<!-- XSP Request logicsheet for the Javascript language -->
<xsl:stylesheet version="1.0"
                xmlns:xsp="http://apache.org/xsp"
                xmlns:xsp-request="http://apache.org/xsp/request/2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="xsp-request:get-uri">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>(request.getRequestURI())</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:uri>
          <xsp:expr>request.getRequestURI()</xsp:expr>
        </xsp-request:uri>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-sitemap-uri">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>(request.getSitemapURI())</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:sitemap-uri>
          <xsp:expr>request.getSitemapURI()</xsp:expr>
        </xsp-request:sitemap-uri>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-scheme">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>(request.getScheme())</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:scheme>
          <xsp:expr>request.getScheme()</xsp:expr>
        </xsp-request:scheme>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-character-encoding">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>(request.getCharacterEncoding())</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:character-encoding>
          <xsp:expr>request.getCharacterEncoding()</xsp:expr>
        </xsp-request:character-encoding>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-content-length">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'int'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'int'">
        <xsp:expr>(request.getContentLength())</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'string'">
        <xsp:expr>String(request.getContentLength())</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:content-length>
          <xsp:expr>request.getContentLength()</xsp:expr>
        </xsp-request:content-length>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-content-type">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>request.getContentType()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:content-type>
          <xsp:expr>request.getContentType()</xsp:expr>
        </xsp-request:content-type>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-locale">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'object'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'object'">
        <xsp:expr>(request.getLocale())</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'string'">
        <xsp:expr>String(request.getLocale())</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp:logic>
          page.getLocale();
        </xsp:logic>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-locales">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'xml'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'array'">
        <xsp:expr>page.getLocalesAsArray()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp:logic>
          page.getLocalesAsXML();
        </xsp:logic>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-parameter">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsl:variable name="default">
      <xsl:choose>
        <xsl:when test="@default">"<xsl:value-of select="@default"/>"</xsl:when>
        <xsl:when test="xsp-request:default">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="xsp-request:default"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>null</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="form-encoding">
      <xsl:call-template name="value-for-form-encoding"/>
    </xsl:variable>
    <xsl:variable name="container-encoding">
      <xsl:call-template name="value-for-container-encoding"/>
    </xsl:variable>
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>
          (page.getParameter(<xsl:copy-of select="$name"/>,
          <xsl:copy-of select="$default"/>,
          <xsl:copy-of select="$form-encoding"/>,
          <xsl:copy-of select="$container-encoding"/>))
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <!-- <xsp-request:parameter name="..."> -->
        <xsp:logic>
          page.getParameterAsXML(<xsl:copy-of select="$name"/>, <xsl:copy-of select="$default"/>,
          <xsl:copy-of select="$form-encoding"/>,
          <xsl:copy-of select="$container-encoding"/>);
        </xsp:logic>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-parameter-values">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'xml'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsl:variable name="form-encoding">
      <xsl:call-template name="value-for-form-encoding"/>
    </xsl:variable>
    <xsl:variable name="container-encoding">
      <xsl:call-template name="value-for-container-encoding"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'array'">
        <xsp:expr>
          (page.getParameterValues(<xsl:copy-of select="$name"/>,
          <xsl:copy-of select="$form-encoding"/>,
          <xsl:copy-of select="$container-encoding"/>))
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <!-- <xsp-request:parameter-values name="..."> -->
        <xsp:logic>
          page.getParameterValuesAsXML(<xsl:copy-of select="$name"/>,
          <xsl:copy-of select="$form-encoding"/>,
          <xsl:copy-of select="$container-encoding"/>);
        </xsp:logic>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-parameter-names">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'xml'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'array'">
        <xsp:expr>
          (page.getParameterNames())
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp:logic>
          page.getParameterNamesAsXML();
        </xsp:logic>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-header">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>request.getHeader(<xsl:copy-of select="$name"/>)</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:header>
          <xsp:attribute name="name">
            <xsp:expr><xsl:copy-of select="$name"/></xsp:expr>
          </xsp:attribute>
          <xsp:expr>request.getHeader(<xsl:copy-of select="$name"/>)</xsp:expr>
        </xsp-request:header>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-int-header">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'int'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'int'">
        <xsp:expr>request.getIntHeader(<xsl:copy-of select="$name"/>)</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'string'">
        <xsp:expr>String(request.getIntHeader(<xsl:copy-of select="$name"/>))</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:int-header>
          <xsp:attribute name="name">
            <xsp:expr><xsl:copy-of select="$name"/></xsp:expr>
          </xsp:attribute>
          <xsp:expr>request.getIntHeader(<xsl:copy-of select="$name"/>)</xsp:expr>
        </xsp-request:int-header>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-date-header">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsl:variable name="format">
      <xsl:call-template name="value-for-format"/>
    </xsl:variable>
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'object'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'object'">
        <xsp:expr>page.getDateHeader(<xsl:copy-of select="$name"/>)</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'long'">
        <xsp:expr>request.getDateHeader(<xsl:copy-of select="$name"/>)</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'string'">
        <xsp:expr>page.getDateHeader(<xsl:copy-of select="$name"/>, <xsl:copy-of select="$format"/>))</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:date-header>
          <xsp:attribute name="name">
            <xsp:expr><xsl:copy-of select="$name"/></xsp:expr>
          </xsp:attribute>
          <xsp:expr>page.getDateHeader(<xsl:copy-of select="$name"/>, <xsl:copy-of select="$format"/>))</xsp:expr>
        </xsp-request:date-header>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-header-names">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'xml'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'array'">
        <xsp:expr>(page.getHeaderNames())</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp:logic>
          page.getHeaderNamesAsXML();
        </xsp:logic>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-headers">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'xml'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'array'">
        <xsp:expr>
          (page.getHeaders(<xsl:copy-of select="$name"/>))
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp:logic>
          page.getHeadersAsXML(<xsl:copy-of select="$name"/>);
        </xsp:logic>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-attribute">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'object'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'object'">
        <xsp:expr>request.getAttribute(<xsl:copy-of select="$name"/>)</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'string'">
        <xsp:expr>
          String(request.getAttribute(<xsl:copy-of select="$name"/>))
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:attribute>
          <xsp:attribute name="name">
            <xsp:expr><xsl:copy-of select="$name"/></xsp:expr>
          </xsp:attribute>
          <xsp:expr>request.getAttribute(<xsl:copy-of select="$name"/>)</xsp:expr>
        </xsp-request:attribute>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:set-attribute">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsl:variable name="content">
      <xsl:call-template name="get-nested-content">
        <xsl:with-param name="content" select="*|text()"/>
      </xsl:call-template>
    </xsl:variable>
    <xsp:logic>
      request.setAttribute(String(<xsl:copy-of select="$name"/>),
                           <xsl:copy-of select="$content"/>);
    </xsp:logic>
  </xsl:template>

  <xsl:template match="xsp-request:get-attribute-names">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'xml'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'array'">
        <xsp:expr>
          (page.getAttributeNames())
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp:logic>
          page.getAttributeNames(this.contentHandler);
        </xsp:logic>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:remove-attribute">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsp:logic>
      request.removeAttribute(<xsl:copy-of select="$name"/>);
    </xsp:logic>
  </xsl:template>

  <xsl:template match="xsp-request:get-requested-url">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>page.getRequestedURL()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:requested-url>
          <xsp:expr>page.getRequestedURL()</xsp:expr>
        </xsp-request:requested-url>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-remote-address">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>request.getRemoteAddr()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:remote-address>
          <xsp:expr>request.getRemoteAddr()</xsp:expr>
        </xsp-request:remote-address>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-remote-user">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>request.getRemoteUser()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:remote-user>
          <xsp:expr>request.getRemoteUser()</xsp:expr>
        </xsp-request:remote-user>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-context-path">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>request.getContextPath()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:context-path>
          <xsp:expr>request.getContextPath()</xsp:expr>
        </xsp-request:context-path>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-path-info">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>request.getPathInfo()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:path-info>
          <xsp:expr>request.getPathInfo()</xsp:expr>
        </xsp-request:path-info>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-server-name">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>request.getServerName()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:server-name>
          <xsp:expr>request.getServerName()</xsp:expr>
        </xsp-request:server-name>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-server-port">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'int'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'int'">
        <xsp:expr>request.getServerPort()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'string'">
        <xsp:expr>String(request.getServerPort())</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:server-port>
          <xsp:expr>request.getServerPort()</xsp:expr>
        </xsp-request:server-port>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-method">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>request.getMethod()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:method>
          <xsp:expr>request.getMethod()</xsp:expr>
        </xsp-request:method>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-query-string">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>request.getQueryString()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:query-string>
          <xsp:expr>request.getQueryString()</xsp:expr>
        </xsp-request:query-string>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-protocol">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>request.getProtocol()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:protocol>
          <xsp:expr>request.getProtocol()</xsp:expr>
        </xsp-request:protocol>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-remote-host">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>request.getRemoteHost()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:remote-host>
          <xsp:expr>request.getRemoteHost()</xsp:expr>
        </xsp-request:remote-host>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:is-secure">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'boolean'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'boolean'">
        <xsp:expr>request.isSecure()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'string'">
        <xsp:expr>String(request.isSecure())</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:secure>
          <xsp:expr>request.isSecure()</xsp:expr>
        </xsp-request:secure>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-path-translated">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>request.getPathTranslated()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:path-translated>
          <xsp:expr>request.getPathTranslated()</xsp:expr>
        </xsp-request:path-translated>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-servlet-path">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>request.getServletPath()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:servlet-path>
          <xsp:expr>request.getServletPath()</xsp:expr>
        </xsp-request:servlet-path>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-user-principal">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'object'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>String(request.getUserPrincipal())</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'object'">
        <xsp:expr>request.getUserPrincipal()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:user-principal>
          <xsp:expr>request.getUserPrincipal()</xsp:expr>
        </xsp-request:user-principal>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-auth-type">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>request.getAuthType()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:auth-type>
          <xsp:expr>request.getAuthType()</xsp:expr>
        </xsp-request:auth-type>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:is-user-in-role">
    <xsl:variable name="role">
      <xsl:call-template name="value-for-role"/>
    </xsl:variable>
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'boolean'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'boolean'">
        <xsp:expr>request.isUserInRole(<xsl:copy-of select="$role"/>)</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'string'">
        <xsp:expr>String(request.isUserInRole(<xsl:copy-of select="$role"/>))</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:user-in-role>
          <xsp:attribute name="role">
            <xsp:expr><xsl:copy-of select="$role"/></xsp:expr>
          </xsp:attribute>
          <xsp:expr>request.isUserInRole(<xsl:copy-of select="$role"/>)</xsp:expr>
        </xsp-request:user-in-role>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-requested-session-id">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>request.getRequestedSessionId()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-request:requested-session-id>
          <xsp:expr>request.getRequestedSessionId()</xsp:expr>
        </xsp-request:requested-session-id>
      </xsl:when>
    </xsl:choose>
  </xsl:template>


  <!-- ============== Session Related ============== -->

  <xsl:template match="xsp-request:get-session-attribute">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="default">
      <xsl:choose>
        <xsl:when test="@default">"<xsl:value-of select="@default"/>"</xsl:when>
        <xsl:when test="xsp-request:default">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="xsp-request:default"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>null</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>
          (page.getSessionAttribute(<xsl:copy-of select="$name"/>, <xsl:copy-of select="$default"/>))
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <!-- <xsp-request:session-attribute name="..."> -->
        <xsp:logic>
          page.getSessionAttribute(this.contentHandler,
          <xsl:copy-of select="$name"/>, <xsl:copy-of select="$default"/>);
        </xsp:logic>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-session-id">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>(request.getSession().getId())</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <!-- <xsp-request:session-id> -->
        <xsp-request:session-id>
          <xsp:expr>(request.getSession().getId())</xsp:expr>
        </xsp-request:session-id>
      </xsl:when>
    </xsl:choose>
  </xsl:template>


  <!-- Supporting templates -->

  <xsl:template name="value-for-as">
    <xsl:param name="default"/>
    <xsl:choose>
      <xsl:when test="@as"><xsl:value-of select="@as"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$default"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="value-for-name">
    <xsl:choose>
      <xsl:when test="@name">"<xsl:value-of select="@name"/>"</xsl:when>
      <xsl:when test="xsp-request:name">
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="xsp-request:name"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>null</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="value-for-role">
    <xsl:choose>
      <xsl:when test="@role">"<xsl:value-of select="@role"/>"</xsl:when>
      <xsl:when test="xsp-request:role">
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="xsp-request:role"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>null</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="value-for-format">
    <xsl:choose>
      <xsl:when test="@format">"<xsl:value-of select="@format"/>"</xsl:when>
      <xsl:when test="xsp-request:format">
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="xsp-request:format"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>null</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="value-for-form-encoding">
    <xsl:choose>
      <xsl:when test="@form-encoding">"<xsl:value-of
          select="@form-encoding"/>"</xsl:when>
      <xsl:when test="xsp-request:form-encoding">
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="xsp-request:form-encoding"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>null</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="value-for-container-encoding">
    <xsl:choose>
      <xsl:when test="@container-encoding">"<xsl:value-of
          select="@container-encoding"/>"</xsl:when>
      <xsl:when test="xsp-request:container-encoding">
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content"
            select="xsp-request:container-encoding"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>null</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="get-nested-content">
    <xsl:param name="content"/>
    <xsl:choose>
      <xsl:when test="$content/*">
        <xsl:apply-templates select="$content/*"/>
      </xsl:when>
      <xsl:otherwise>"<xsl:value-of select="$content"/>"</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@*|*|text()|processing-instruction()">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="xsp-request:name"/>
  <xsl:template match="xsp-request:default"/>
  <xsl:template match="xsp-request:form-encoding"/>
  <xsl:template match="xsp-request:container-encoding"/>
</xsl:stylesheet>
