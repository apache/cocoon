<?xml version="1.0"?>

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
  xmlns:xsp="http://apache.org/DTD/XSP/Layer1"
  xmlns:request="http://apache.org/DTD/XSP/request"
>
  <!-- *** ServletRequest Templates *** -->

  <!-- Import Global XSP Templates -->
  <!-- <xsl:import href="base-library.xsl"/> -->
  <!-- Default copy-over's -->
  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
  </xsl:template>

  <!-- *** Utility Templates *** -->
  <!-- Retrieve "name" parameter as either attribute or element -->
  <xsl:template name="value-for-name">
    <xsl:choose>
      <!-- As attribute (String constant) -->
      <xsl:when test="@name">"<xsl:value-of select="@name"/>"</xsl:when>
      <!-- As nested (presumably dynamic) element -->
      <xsl:when test="name">
        <!-- Recursively evaluate nested expression -->
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="name"/>
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- Return nested element content as expression or constant -->
  <xsl:template name="get-nested-content">
    <xsl:choose>
      <!-- Nested element -->
      <xsl:when test="$content/*">
        <xsl:apply-templates select="$content/*"/>
      </xsl:when>
      <!-- Plain Text -->
      <xsl:otherwise>"<xsl:value-of select="normalize($content)"/>"</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Ensure attribute "as" has a value -->
  <xsl:template name="value-for-as">
    <xsl:choose>
      <xsl:when test="@as"><xsl:value-of select="@as"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$default"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- request.getAttribute -->
  <xsl:template match="request:get-attribute">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <!-- Ensure attribute "as" has a value -->
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
          XSPRequestLibrary.getAttribute(
            request,
            String.valueOf(<xsl:copy-of select="$name"/>)
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- request.getAttributeNames -->
  <xsl:template match="request:get-attribute-names">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getCharacterEncoding -->
  <xsl:template match="request:get-character-encoding">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getContentLength -->
  <xsl:template match="request:get-content-length">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getContentType -->
  <xsl:template match="request:get-content-type">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getLocale -->
  <xsl:template match="request:get-locale">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getLocales -->
  <xsl:template match="request:get-locales">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getParameter -->
  <xsl:template match="request:get-parameter">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <!-- Ensure attribute "as" has a value -->
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
            document
          )
        </xsl:when>
        <xsl:when test="$as = 'string'">
          request.getParameter(String.valueOf(<xsl:copy-of select="$name"/>))
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <!-- request.getParameterNames -->
  <xsl:template match="request:get-parameter-names">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getParameterValues -->
  <xsl:template match="request:get-parameter-values">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getProtocol -->
  <xsl:template match="request:get-protocol">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getRemoteAddr -->
  <xsl:template match="request:get-remote-addr">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getRemoteHost -->
  <xsl:template match="request:get-remote-host">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getScheme -->
  <xsl:template match="request:get-scheme">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getServerName -->
  <xsl:template match="request:get-server-name">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getServerPort -->
  <xsl:template match="request:get-server-port">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.isSecure -->
  <xsl:template match="request:is-secure">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.removeAttribute -->
  <xsl:template match="request:remove-attribute">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsp:logic>
      request.removeAttribute(
        String.valueOf(<xsl:copy-of select="$name"/>)
      );
    </xsp:logic>
  </xsl:template>

  <!-- request.setAttribute -->
  <xsl:template match="request:set-attribute">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <!-- Recursively evaluate nested attribute value -->
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

  <!-- request.getMethod -->
  <xsl:template match="request:get-method">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getPathInfo -->
  <xsl:template match="request:get-path-info">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getPathTranslated -->
  <xsl:template match="request:get-path-translated">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getQueryString -->
  <xsl:template match="request:get-query-string">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getRemoteUser -->
  <xsl:template match="request:get-remote-user">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getRequestedSessionId -->
  <xsl:template match="request:get-requested-session-id">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getRequestURI -->
  <xsl:template match="request:get-request-uri">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getServletPath -->
  <xsl:template match="request:get-servlet-path">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getUserPrincipal -->
  <xsl:template match="request:get-user-principal">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- *** HttpServletRequest *** -->

  <!-- request.getAuthType -->
  <xsl:template match="request:get-auth-type">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getContextPath -->
  <xsl:template match="request:get-context-path">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getCookies -->
  <xsl:template match="request:get-cookies">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getDateHeader -->
  <xsl:template match="request:get-date-header">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <!-- Ensure attribute "as" has a value -->
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'long'"/>
      </xsl:call-template>
    </xsl:variable>

    <!-- Get "format" parameter as either attribute or nested element -->
    <xsl:variable name="format">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@format">"<xsl:value-of select="@format"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="format">
          <!-- Recursively evaluate nested expression -->
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

  <!-- request.getHeader -->
  <xsl:template match="request:get-header">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getHeaderNames -->
  <xsl:template match="request:get-header-names">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getHeaders -->
  <xsl:template match="request:get-headers">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.getIntHeader -->
  <xsl:template match="request:get-int-header">
    <!-- Get "name" parameter as either attribute or nested element -->
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.isRequestedSessionIdFromCookie -->
  <xsl:template match="request:is-requested-session-id-from-cookie">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.isRequestedSessionIdFromURL -->
  <xsl:template match="request:is-requested-session-id-from-url">
    <!-- Ensure attribute "as" has a value -->
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


  <!-- request.isRequestedSessionIdValid -->
  <xsl:template match="request:is-requested-session-id-valid">
    <!-- Ensure attribute "as" has a value -->
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

  <!-- request.isUserInRole -->
  <xsl:template match="request:is-user-in-role">
    <!-- Get "role" parameter as either attribute or nested element -->
    <xsl:variable name="role">
      <xsl:choose>
        <!-- As attribute (String constant) -->
        <xsl:when test="@role">"<xsl:value-of select="@role"/>"</xsl:when>
        <!-- As nested (presumably dynamic) element -->
        <xsl:when test="role">
          <!-- Recursively evaluate nested expression -->
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="role"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <!-- Ensure attribute "as" has a value -->
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
              String.valueOf(<xsl:copy-of select="$role"/>),
            )
          )
        </xsl:when>
        <xsl:when test="$as = 'boolean'">
          request.isUserInRole(
            String.valueOf(<xsl:copy-of select="$role"/>),
          )
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>
</xsl:stylesheet>
