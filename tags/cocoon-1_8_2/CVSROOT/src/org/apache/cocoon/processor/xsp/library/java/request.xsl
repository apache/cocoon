<?xml version="1.0"?>
<!--

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

-->

<!-- written by Ricardo Rocha "ricardo@apache.org" -->


<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp="http://www.apache.org/1999/XSP/Core"
  xmlns:request="http://www.apache.org/1999/XSP/Request"
  version="1.0"
>
  <!-- *** ServletRequest Templates *** -->

  <!-- Import Global XSP Templates -->
  <!-- <xsl:import href="base-library.xsl"/> -->

  <xsl:template match="request:get-attribute">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getAttribute(
            request,
            String.valueOf(<xsl:copy-of select="$name"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getAttribute(
            String.valueOf(<xsl:copy-of select="$name"/>)
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-attribute-names">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'array'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getAttributeNames(request, document)
        </xsl:when>
        <xsl:when test="$as = 'array'">
          XSPRequestLibrary.getAttributeNames(request)
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-character-encoding">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getCharacterEncoding(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getCharacterEncoding()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-content-length">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'int'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getContentLength(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(request.getContentLength())
        </xsl:when>
        <xsl:when test="$as = 'int'">
          request.getContentLength()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-content-type">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getContentType(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getContentType()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-locale">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'object'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getLocale(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getLocale(request).toString()
        </xsl:when>
        <xsl:when test="$as = 'object'">
          request.getLocale(request)
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-locales">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'array'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getLocales(request, document)
        </xsl:when>
        <xsl:when test="$as = 'array'">
          XSPRequestLibrary.getLocales(request)
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-parameter">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="default">
      <xsl:choose>
        <xsl:when test="@default">"<xsl:value-of select="@default"/>"</xsl:when>
        <xsl:when test="default">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="default"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>null</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>


    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getParameter(
            request,
	    String.valueOf(<xsl:copy-of select="$name"/>),
            <xsl:copy-of select="$default"/>,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          XSPRequestLibrary.getParameter(
            request,
	    String.valueOf(<xsl:copy-of select="$name"/>),
            <xsl:copy-of select="$default"/>
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-parameter-names">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'array'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getParameterNames(request, document)
        </xsl:when>
        <xsl:when test="$as = 'array'">
          XSPRequestLibrary.getParameterNames(request)
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-parameter-values">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'array'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getParameterValues(
            request,
            String.valueOf(<xsl:copy-of select="$name"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'array'">
          request.getParameterValues(
            String.valueOf(<xsl:copy-of select="$name"/>)
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-protocol">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getProtocol(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getProtocol()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-remote-addr">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getRemoteAddr(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getRemoteAddr()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-remote-host">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getRemoteHost(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getRemoteHost()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-scheme">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getScheme(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getScheme()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-server-name">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getServerName(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getServerName()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-server-port">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'int'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getServerPort(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(request.getServerPort())
        </xsl:when>
        <xsl:when test="$as = 'int'">
          request.getServerPort()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:is-secure">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'boolean'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.isSecure(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(request.isSecure())
        </xsl:when>
        <xsl:when test="$as = 'boolean'">
          request.isSecure()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:remove-attribute">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsp:logic>
      request.removeAttribute(
        String.valueOf(<xsl:copy-of select="$name"/>)
      );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="request:set-attribute">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="content">
      <xsl:call-template name="get-nested-content">
        <xsl:with-param name="content">
          <content><xsl:apply-templates/></content>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>

    <xsp:logic>
      request.setAttribute(
        String.valueOf(<xsl:copy-of select="$name"/>),
        <xsl:copy-of select="$content"/>
      );
    </xsp:logic>
  </xsl:template>

  <xsl:template match="request:get-method">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getMethod(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getMethod()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-path-info">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getPathInfo(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getPathInfo()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-path-translated">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getPathTranslated(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getPathTranslated()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-query-string">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getQueryString(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getQueryString()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-remote-user">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getRemoteUser(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getRemoteUser()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-requested-session-id">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getRequestedSessionId(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getRequestedSessionId()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-request-uri">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getRequestURI(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getRequestURI()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-servlet-path">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getServletPath(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getServletPath()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-user-principal">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'object'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getUserPrincipal(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(request.getUserPrincipal())
        </xsl:when>
        <xsl:when test="$as = 'object'">
          request.getUserPrincipal()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-auth-type">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getAuthType(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getAuthType()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-context-path">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getContextPath(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getContextPath()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-cookies">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'array'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getCookies(request, document)
        </xsl:when>
        <xsl:when test="$as = 'array'">
          request.getCookies()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-date-header">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'long'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="format">
      <xsl:choose>
        <xsl:when test="@format">"<xsl:value-of select="@format"/>"</xsl:when>
        <xsl:when test="format">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="format"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>null</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getDateHeader(
            request,
            String.valueOf(<xsl:copy-of select="$name"/>),
            <xsl:copy-of select="$format"/>,
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          XSPUtil.formatDate (
            new Date(
              request.getDateHeader(
                String.valueOf(
                  String.valueOf(<xsl:copy-of select="$name"/>)
                )
              )
            ),
            <xsl:copy-of select="$format"/>
          )
        </xsl:when>
        <xsl:when test="$as = 'date'">
          new Date(
            request.getDateHeader(
              String.valueOf(<xsl:copy-of select="$name"/>)
            )
          )
        </xsl:when>
        <xsl:when test="$as = 'long'">
          request.getDateHeader(
            String.valueOf(<xsl:copy-of select="$name"/>)
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-header">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getHeader(
            request,
            String.valueOf(<xsl:copy-of select="$name"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getHeader(String.valueOf(<xsl:copy-of select="$name"/>))
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-header-names">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'array'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getHeaderNames(request, document)
        </xsl:when>
        <xsl:when test="$as = 'array'">
          XSPRequestLibrary.getHeaderNames(request)
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-headers">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'array'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getHeaders(
            request,
            String.valueOf(<xsl:copy-of select="$name"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'array'">
          XSPRequestLibrary.getHeaders(
            request,
            String.valueOf(<xsl:copy-of select="$name"/>),
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:get-int-header">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'int'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.getIntHeader(
            request,
            String.valueOf(<xsl:copy-of select="$name"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.ValueOf(
              request.getIntHeader(
                String.valueOf(<xsl:copy-of select="$name"/>)
            )
          )
        </xsl:when>
        <xsl:when test="$as = 'int'">
          request.getIntHeader(
            String.valueOf(<xsl:copy-of select="$name"/>)
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:is-requested-session-id-from-cookie">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'boolean'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.isRequestedSessionIdFromCookie(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(request.isRequestedSessionIdFromCookie())
        </xsl:when>
        <xsl:when test="$as = 'boolean'">
          request.isRequestedSessionIdFromCookie()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:is-requested-session-id-from-url">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'boolean'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.isRequestedSessionIdFromURL(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(request.isRequestedSessionIdFromURL())
        </xsl:when>
        <xsl:when test="$as = 'boolean'">
          request.isRequestedSessionIdFromURL()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>


  <xsl:template match="request:is-requested-session-id-valid">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'boolean'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.isRequestedSessionIdValid(request, document)
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(request.isRequestedSessionIdValid())
        </xsl:when>
        <xsl:when test="$as = 'boolean'">
          request.isRequestedSessionIdValid()
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="request:is-user-in-role">
    <xsl:variable name="role">
      <xsl:choose>
        <xsl:when test="@role">"<xsl:value-of select="@role"/>"</xsl:when>
        <xsl:when test="role">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="role"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'boolean'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'node'">
          XSPRequestLibrary.isUserInRole(
            request,
            String.valueOf(<xsl:copy-of select="$role"/>),
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          String.valueOf(
            request.isUserInRole(
              String.valueOf(<xsl:copy-of select="$role"/>)
            )
          )
        </xsl:when>
        <xsl:when test="$as = 'boolean'">
          request.isUserInRole(
            String.valueOf(<xsl:copy-of select="$role"/>)
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template name="value-for-name">
    <xsl:choose>
      <xsl:when test="@name">"<xsl:value-of select="@name"/>"</xsl:when>
      <xsl:when test="name">
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="name"/>
        </xsl:call-template>
      </xsl:when>
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

  <xsl:template name="value-for-as">
    <xsl:param name="default"/>
    <xsl:choose>
      <xsl:when test="@as"><xsl:value-of select="@as"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$default"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@*|*|text()|processing-instruction()">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
