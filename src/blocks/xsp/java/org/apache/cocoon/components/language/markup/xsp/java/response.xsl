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

<!-- $Id: response.xsl,v 1.2 2004/03/17 11:28:22 crossley Exp $-->
<!--
 * XSP Response logicsheet for the Java language
 *
 * @author <a href="mailto:ricardo@apache.org>Ricardo Rocha</a>
 * @version CVS $Revision: 1.2 $ $Date: 2004/03/17 11:28:22 $
-->

<xsl:stylesheet version="1.0"
                xmlns:xsp="http://apache.org/xsp"
                xmlns:xsp-response="http://apache.org/xsp/response/2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


  <xsl:template match="xsp-response:get-character-encoding">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>response.getCharacterEncoding()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-response:character-encoding>
          <xsp:expr>response.getCharacterEncoding()</xsp:expr>
        </xsp-response:character-encoding>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-response:get-locale">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'object'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr>String.valueOf(response.getLocale())</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'object'">
        <xsp:expr>response.getLocale()</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp:logic>
          XSPResponseHelper.getLocale(response, contentHandler);
        </xsp:logic>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-response:set-locale">
    <xsp:logic>
      response.setLocale(
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="."/>
        </xsl:call-template>);
    </xsp:logic>
  </xsl:template>

  <xsl:template match="xsp-response:add-cookie">
    <xsp:logic>
      response.addCookie(
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="."/>
        </xsl:call-template>);
    </xsp:logic>
  </xsl:template>

  <xsl:template match="xsp-response:add-date-header">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsl:variable name="date">
      <xsl:choose>
        <xsl:when test="@date">"<xsl:value-of select="@date"/>"</xsl:when>
        <xsl:when test="xsp-response:date">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="xsp-response:date"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>null</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="format">
      <xsl:choose>
        <xsl:when test="@format">"<xsl:value-of select="@format"/>"</xsl:when>
        <xsl:when test="xsp-response:format">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="xsp-response:format"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>null</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsp:logic>
      <xsl:choose>
        <xsl:when test="$format != 'null'">
          XSPResponseHelper.addDateHeader(response,
            String.valueOf(<xsl:copy-of select="$name"/>),
            (<xsl:copy-of select="$date"/>),
            (<xsl:copy-of select="$format"/>));
        </xsl:when>
        <xsl:otherwise>
          XSPResponseHelper.addDateHeader(response,
            String.valueOf(<xsl:copy-of select="$name"/>),
            (<xsl:copy-of select="$date"/>));
        </xsl:otherwise>
      </xsl:choose>
    </xsp:logic>
  </xsl:template>

  <xsl:template match="xsp-response:add-header">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsl:variable name="value">
      <xsl:choose>
        <xsl:when test="@value">"<xsl:value-of select="@value"/>"</xsl:when>
        <xsl:when test="xsp-response:value">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="xsp-response:value"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>null</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsp:logic>
      response.addHeader(
       String.valueOf(<xsl:copy-of select="$name"/>),
       String.valueOf(<xsl:copy-of select="$value"/>));
    </xsp:logic>
  </xsl:template>

  <xsl:template match="xsp-response:add-int-header">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsl:variable name="value">
      <xsl:choose>
        <xsl:when test="@value">"<xsl:value-of select="@value"/>"</xsl:when>
        <xsl:when test="xsp-response:value">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="xsp-response:value"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>null</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsp:logic>
      response.addIntHeader(
        String.valueOf(<xsl:copy-of select="$name"/>),
        Integer.parseInt(String.valueOf(<xsl:copy-of select="$value"/>)));
    </xsp:logic>
  </xsl:template>

  <xsl:template match="xsp-response:contains-header">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'boolean'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'boolean'">
        <xsp:expr>response.containsHeader(String.valueOf(<xsl:copy-of select="$name"/>))</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'string'">
        <xsp:expr>String.valueOf(response.containsHeader(
              String.valueOf(<xsl:copy-of select="$name"/>)))</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-response:contains-header>
          <xsp:expr>response.containsHeader(String.valueOf(<xsl:copy-of select="$name"/>))</xsp:expr>
        </xsp-response:contains-header>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-response:encode-url">
    <xsl:variable name="url">
      <xsl:choose>
        <xsl:when test="@url">"<xsl:value-of select="@url"/>"</xsl:when>
        <xsl:when test="xsp-response:url">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="xsp-response:url"/>
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
        <xsp:expr>response.encodeURL(String.valueOf(<xsl:copy-of select="$url"/>))</xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <xsp-response:encode-url>
          <xsp:expr>
            response.encodeURL(String.valueOf(<xsl:copy-of select="$url"/>))
          </xsp:expr>
        </xsp-response:encode-url>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-response:set-date-header">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsl:variable name="date">
      <xsl:choose>
        <xsl:when test="@date">"<xsl:value-of select="@date"/>"</xsl:when>
        <xsl:when test="xsp-response:date">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="xsp-response:date"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>null</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="format">
      <xsl:choose>
        <xsl:when test="@format">"<xsl:value-of select="@format"/>"</xsl:when>
        <xsl:when test="xsp-response:format">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="xsp-response:format"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>null</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsp:logic>
      <xsl:choose>
        <xsl:when test="$format">
          XSPResponseHelper.setDateHeader(response,
            String.valueOf(<xsl:copy-of select="$name"/>),
            String.valueOf(
              <xsl:call-template name="get-nested-content">
                <xsl:with-param name="content" select="$date"/>
              </xsl:call-template>
            ),
            String.valueOf(<xsl:copy-of select="$format"/>));
        </xsl:when>
        <xsl:otherwise>
          XSPResponseHelper.setDateHeader(response,
            String.valueOf(<xsl:copy-of select="$name"/>),
            <xsl:call-template name="get-nested-content">
              <xsl:with-param name="content" select="$date"/>
            </xsl:call-template>);
        </xsl:otherwise>
      </xsl:choose>
    </xsp:logic>
  </xsl:template>

  <xsl:template match="xsp-response:set-header">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsl:variable name="value">
      <xsl:choose>
        <xsl:when test="@value">"<xsl:value-of select="@value"/>"</xsl:when>
        <xsl:when test=".">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="."/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>""</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsp:logic>
      response.setHeader(
       String.valueOf(<xsl:copy-of select="$name"/>),
       String.valueOf(<xsl:copy-of select="$value"/>));
    </xsp:logic>
  </xsl:template>

  <xsl:template match="xsp-response:set-int-header">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsl:variable name="value">
      <xsl:choose>
        <xsl:when test="@value">"<xsl:value-of select="@value"/>"</xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="."/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsp:logic>
      response.setIntHeader(String.valueOf(<xsl:copy-of select="$name"/>),
       Integer.parseInt(String.valueOf(<xsl:copy-of select="$value"/>)));
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
      <xsl:when test="xsp-response:name">
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="xsp-response:name"/>
        </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>null</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="get-nested-content">
    <xsl:param name="content"/>
    <xsl:choose>
      <xsl:when test="$content/xsp:text">"<xsl:value-of select="$content"/>"</xsl:when>
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
