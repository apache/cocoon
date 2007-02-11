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
 * XSP Session logicsheet for the Java language
 *
 * @author <a href="mailto:ricardo@apache.org>Ricardo Rocha</a>
 * @author ported by <a href="mailto:bloritsch@apache.org>Berin Loritsch</a>
 * @version CVS $Revision: 1.2 $ $Date: 2004/03/17 11:28:22 $
-->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:xsp-session="http://apache.org/xsp/session/2.0">

  <!-- *** ServletSession Templates *** -->
  <xsl:template match="xsp:page">
    <xsp:page>
      <xsl:apply-templates select="@*"/>

      <xsp:structure>
        <xsp:include>org.apache.cocoon.environment.Session</xsp:include>
      </xsp:structure>

      <xsl:variable name="create">
        <xsl:choose>
          <xsl:when test="@create-session='yes' or @create-session='true'">true</xsl:when>
          <xsl:when test="@create-session='no' or @create-session='false'">false</xsl:when>
          <xsl:otherwise>true</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsp:init-page>
        Session session = request.getSession(<xsl:value-of select="$create"/>);
      </xsp:init-page>

      <xsl:apply-templates/>
    </xsp:page>
  </xsl:template>

  <xsl:template match="xsp-session:get-session-id">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'string'">
        <xsp:expr> (session.getId()) </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'xml'">
        <!-- <xsp-session:session-id> -->
        <xsp-session:session-id>
          <xsp:expr>session.getId()</xsp:expr>
        </xsp-session:session-id>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-session:get-attribute">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'object'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="default">
      <xsl:choose>
        <xsl:when test="@default">"<xsl:value-of select="@default"/>"</xsl:when>
        <xsl:when test="xsp-session:default">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="xsp-session:default"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>null</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'xml'">
        <xsp-session:attribute>
          <xsp:expr>
            XSPSessionHelper.getSessionAttribute(session,
            String.valueOf(<xsl:copy-of select="$name"/>),
            <xsl:copy-of select="$default"/>)
          </xsp:expr>
        </xsp-session:attribute>
      </xsl:when>
      <xsl:when test="$as = 'object'">
        <xsp:expr>
          XSPSessionHelper.getSessionAttribute(session,
          String.valueOf(<xsl:copy-of select="$name"/>),
          <xsl:copy-of select="$default"/>)
        </xsp:expr>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-session:get-attribute-names">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'array'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'xml'">
        <xsp:logic>
          List v = XSPSessionHelper.getSessionAttributeNames(session);
        </xsp:logic>
        <xsp-session:attribute-names>
          <xsp:logic>
            for (int i = 0; i &lt; v.size(); i++) {
            <xsp-session:attribute-name>
              <xsp:expr>v.get(i)</xsp:expr>
            </xsp-session:attribute-name>
            }
          </xsp:logic>
        </xsp-session:attribute-names>
      </xsl:when>
      <xsl:when test="$as = 'array'">
        <xsp:expr>
          XSPSessionHelper.getSessionAttributeNames(session)
        </xsp:expr>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-session:get-creation-time">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'long'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'xml'">
        <xsp-session:creation-time>
          <xsp:expr>
            new Date(session.getCreationTime())
          </xsp:expr>
        </xsp-session:creation-time>
      </xsl:when>
      <xsl:when test="$as = 'string'">
        <xsp:expr>
          new Date(session.getCreationTime())
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'long'">
        <xsp:expr>
          session.getCreationTime()
        </xsp:expr>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-session:get-id">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'string'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'xml'">
        <xsp-session:id>
          <xsp:expr>session.getId()</xsp:expr>
        </xsp-session:id>
      </xsl:when>
      <xsl:when test="$as = 'string'">
        <xsp:expr>session.getId()</xsp:expr>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-session:get-last-accessed-time">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'long'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$as = 'xml'">
        <xsp-session:last-accessed-time>
          <xsp:expr>
            new Date(session.getLastAccessedTime())
          </xsp:expr>
        </xsp-session:last-accessed-time>
      </xsl:when>
      <xsl:when test="$as = 'string'">
        <xsp:expr>
          new Date(session.getLastAccessedTime())
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'long'">
        <xsp:expr>
          session.getLastAccessedTime()
        </xsp:expr>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-session:get-max-inactive-interval">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'int'"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$as = 'xml'">
        <xsp-session:max-inactive-interval>
          <xsp:expr>
            session.getMaxInactiveInterval()
          </xsp:expr>
        </xsp-session:max-inactive-interval>
      </xsl:when>
      <xsl:when test="$as = 'string'">
        <xsp:expr>
          String.valueOf(session.getMaxInactiveInterval())
        </xsp:expr>
      </xsl:when>
      <xsl:when test="$as = 'int'">
        <xsp:expr>
          session.getMaxInactiveInterval()
        </xsp:expr>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xsp-session:invalidate">
    <xsp:logic>
      session.invalidate();
    </xsp:logic>
  </xsl:template>

  <xsl:template match="xsp-session:is-new">
    <xsl:variable name="as">
      <xsl:call-template name="value-for-as">
        <xsl:with-param name="default" select="'boolean'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsp:expr>
      <xsl:choose>
        <xsl:when test="$as = 'xml'">
          <xsp-session:is-new>
            <xsp:expr>session.isNew()</xsp:expr>
          </xsp-session:is-new>
        </xsl:when>
        <xsl:when test="$as = 'string'">
          <xsp:expr>String.valueOf(session.isNew())</xsp:expr>
        </xsl:when>
        <xsl:when test="$as = 'boolean'">
          <xsp:expr>session.isNew()</xsp:expr>
        </xsl:when>
      </xsl:choose>
    </xsp:expr>
  </xsl:template>

  <xsl:template match="xsp-session:remove-attribute">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsp:logic>
      session.removeAttribute(String.valueOf(<xsl:copy-of select="$name"/>));
    </xsp:logic>
  </xsl:template>

  <xsl:template match="xsp-session:set-attribute">
    <xsl:variable name="name">
      <xsl:call-template name="value-for-name"/>
    </xsl:variable>
    <xsl:variable name="content">
      <xsl:call-template name="get-nested-content">
        <xsl:with-param name="content" select="."/>
      </xsl:call-template>
    </xsl:variable>
    <xsp:logic>
      session.setAttribute(String.valueOf(<xsl:copy-of select="$name"/>),
          <xsl:copy-of select="$content"/>);
    </xsp:logic>
  </xsl:template>

  <xsl:template match="xsp-session:set-max-inactive-interval">
    <xsl:variable name="interval">
      <xsl:choose>
        <xsl:when test="@interval">"<xsl:value-of select="@interval"/>"</xsl:when>
        <xsl:when test="xsp-session:interval">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="xsp-session:interval"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>0</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsp:logic>
      session.setMaxInactiveInterval(Integer.parseInt(String.valueOf(<xsl:copy-of select="$interval"/>)));
    </xsp:logic>
  </xsl:template>

  <!-- encode an URL with the session ID -->
  <xsl:template match="xsp-session:encode-url">
    <xsl:variable name="href">"<xsl:value-of select="@href"/>"</xsl:variable>
    <a>
      <xsp:attribute name="href">
        <xsp:expr>
          response.encodeURL(String.valueOf(<xsl:copy-of select="$href"/>))
        </xsp:expr>
      </xsp:attribute>
      <xsl:value-of select="."/>
    </a>
  </xsl:template>

  <!-- encode an URL with the session ID as a form-->
  <xsl:template match="xsp-session:form-encode-url">
    <xsl:variable name="action">"<xsl:value-of
        select="@action"/>"</xsl:variable>
    <xsl:variable name="method">"<xsl:value-of
        select="@method"/>"</xsl:variable>
    <xsl:variable name="onsubmit">"<xsl:value-of
        select="@onsubmit"/>"</xsl:variable>

    <form>
      <xsp:attribute name="action">
        <xsp:expr>
          response.encodeURL(String.valueOf(<xsl:copy-of select="$action"/>))
        </xsp:expr>
      </xsp:attribute>
      <xsp:attribute name="method">
        <xsp:expr><xsl:copy-of select="$method"/></xsp:expr>
      </xsp:attribute>
      <xsp:attribute name="onsubmit">
        <xsp:expr><xsl:copy-of select="$onsubmit"/></xsp:expr>
      </xsp:attribute>
      <xsl:apply-templates/>
    </form>
  </xsl:template>



  <xsl:template name="value-for-name">
    <xsl:choose>
      <xsl:when test="@name">"<xsl:value-of select="@name"/>"</xsl:when>
      <xsl:when test="xsp-session:name">
        <xsl:call-template name="get-nested-content">
          <xsl:with-param name="content" select="xsp-session:name"/>
        </xsl:call-template>
      </xsl:when>
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

  <xsl:template name="value-for-as">
    <xsl:param name="default"/>
    <xsl:choose>
      <xsl:when test="@as"><xsl:value-of select="@as"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$default"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="value-for-default">
    <xsl:choose>
      <xsl:when test="@default">"<xsl:value-of select="@default"/>"</xsl:when>
      <xsl:otherwise>""</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@*|*|text()|processing-instruction()">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
