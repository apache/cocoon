<?xml version="1.0"?>
<!--
 *****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * _________________________________________________________________________ *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************
-->

<!--
 * @author <a href="mailto:ricardo@apache.org>Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2001-02-13 14:00:15 $
-->

<!-- XSP Request logicsheet for the Java language -->

<xsl:stylesheet
  version="1.0"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:xsp-request="http://apache.org/xsp/request"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
  <xsl:template match="xsp-request:get-uri">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>
          (this.request.getRequestURI())
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
    <!-- <xsp-request:uri> -->
        <xsp:logic>
          XSPRequestHelper.getUri(request, this.contentHandler);
        </xsp:logic>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

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
        <xsl:when test="default">
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
          (XSPRequestHelper.getSessionAttribute(request, <xsl:copy-of select="$name"/>, <xsl:copy-of select="$default"/>))
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp:logic>
          XSPRequestHelper.getSessionAttribute(request, this.contentHandler, <xsl:copy-of select="$name"/>, <xsl:copy-of select="$default"/>);
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
        <xsl:when test="default">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="xsp-request:default"/>
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

    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>
          (XSPRequestHelper.getParameter(request, <xsl:copy-of select="$name"/>, <xsl:copy-of select="$default"/>))
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
    <!-- <xsp-request:uri> -->
        <xsp:logic>
          XSPRequestHelper.getParameter(request, this.contentHandler, <xsl:copy-of select="$name"/>, <xsl:copy-of select="$default"/>);
        </xsp:logic>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-parameter-values">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

     <xsp:logic>
        XSPRequestHelper.getParameterValues(request, this.contentHandler, <xsl:copy-of select="$name"/>);
     </xsp:logic>
  </xsl:template>

  <xsl:template match="xsp-request:get-parameter-names">
     <xsp:logic>
        XSPRequestHelper.getParameterNames(request, this.contentHandler);
     </xsp:logic>
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
        <xsp:expr>
          (XSPRequestHelper.getHeader(request, <xsl:copy-of select="$name"/>))
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
    <!-- <xsp-request:uri> -->
        <xsp:logic>
          XSPRequestHelper.getHeader(request, this.contentHandler, <xsl:copy-of select="$name"/>);
        </xsp:logic>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:get-header-names">
     <xsp:logic>
        XSPRequestHelper.getHeaderNames(request, this.contentHandler);
     </xsp:logic>
  </xsl:template>

  <xsl:template match="xsp-request:get-attribute">
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
        <xsp:expr>
          String.valueOf(request.getAttribute(<xsl:copy-of select="$name"/>))
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'object'">
        <xsp:expr>
          request.getAttribute(<xsl:copy-of select="$name"/>)
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp:element name="xsp-request:attribute">
          <xsp:expr>
            request.getAttribute(<xsl:copy-of select="$name"/>)
          </xsp:expr>
        </xsp:element>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-request:remove-attribute">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>

    <xsp:logic>
      request.removeAttribute(<xsl:value-of select="$name"/>);
    </xsp:logic>
  </xsl:template>

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
      <xsl:when test="name">
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="xsp-request:name"/>
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

  <xsl:template match="@*|*|text()|processing-instruction()">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
