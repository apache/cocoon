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
 * XSP Session-fw logicsheet for the Java language
 *
 * @author <a href="mailto:antonio@apache.org>Antonio Gallardo</a>
 * @version CVS $Revision: 1.5 $ $Date: 2004/03/17 11:28:21 $
-->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:xsp-session-fw="http://apache.org/xsp/session-fw/1.0">

  <!-- *** Session-fw Templates *** -->
  <xsl:template match="xsp:page">
    <xsp:page>
      <xsl:apply-templates select="@*"/>

      <xsp:structure>
        <xsp:include>org.apache.cocoon.environment.Session</xsp:include>
        <xsp:include>org.apache.avalon.framework.component.ComponentManager</xsp:include>
        <xsp:include>org.apache.cocoon.components.language.markup.xsp.XSPSessionFwHelper</xsp:include>
        <xsp:include>org.w3c.dom.DocumentFragment</xsp:include>
      </xsp:structure>

      <xsl:variable name="create">
        <xsl:choose>
          <xsl:when test="@create-session='yes' or @create-session='true'">true</xsl:when>
          <xsl:when test="@create-session='no' or @create-session='false'">false</xsl:when>
          <xsl:otherwise>true</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsp:init-page>
        /* Session session = request.getSession(<xsl:value-of select="$create"/>); */
      </xsp:init-page>

      <xsl:apply-templates/>
    </xsp:page>
  </xsl:template>

  <xsl:template match="xsp-session-fw:getxml">
    <xsl:variable name="context">
        <xsl:call-template name="value-for-context"/>
    </xsl:variable>
    <xsl:variable name="path">
        <xsl:call-template name="value-for-path"/>
    </xsl:variable>
    <xsl:variable name="as">
        <xsl:call-template name="value-for-as">
            <xsl:with-param name="default" select="'string'"/>
        </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
        <xsl:when test="$as='string'">
            <xsp:expr>XSPSessionFwHelper.getXMLAsString(this.manager,
                String.valueOf(<xsl:copy-of select="$context"/>),
                String.valueOf(<xsl:copy-of select="$path"/>))</xsp:expr>
        </xsl:when>
        <xsl:when test="$as='xml'">
            <xsp-session-fw:xml>
                <xsp:expr>XSPSessionFwHelper.getXML(this.manager,
                    String.valueOf(<xsl:copy-of select="$context"/>),
                    String.valueOf(<xsl:copy-of select="$path"/>))</xsp:expr>
            </xsp-session-fw:xml>
        </xsl:when>
        <xsl:when test="$as='object'">
            <xsp:expr>XSPSessionFwHelper.getXML(this.manager,
                String.valueOf(<xsl:copy-of select="$context"/>),
                String.valueOf(<xsl:copy-of select="$path"/>))</xsp:expr>
        </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="value-for-context">
    <xsl:choose>
        <xsl:when test="@context">"<xsl:value-of select="@context"/>"</xsl:when>
        <xsl:otherwise>""</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="value-for-path">
    <xsl:choose>
        <xsl:when test="@path">"<xsl:value-of select="@path"/>"</xsl:when>
        <xsl:otherwise>""</xsl:otherwise>
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
