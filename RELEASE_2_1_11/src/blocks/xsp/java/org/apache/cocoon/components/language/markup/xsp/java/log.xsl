<?xml version="1.0"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!--+
    | XSP Logger logicsheet for the Java language
    |
    | @author <a href="mailto:bloritsch@apache.org>Berin Loritsch</a>
    | @version $Id$
    +-->
<xsl:stylesheet version="1.0"
                xmlns:xsp="http://apache.org/xsp"
                xmlns:log="http://apache.org/xsp/log/2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="xsp:page">
    <xsp:page>
      <xsl:apply-templates select="@*"/>
      <xsp:logic>
        org.apache.avalon.framework.logger.Logger _log_defaultLogger;
      </xsp:logic>
      <xsl:apply-templates/>
    </xsp:page>
  </xsl:template>

  <xsl:template match="log:logger">
    <xsl:variable name="name">
      <xsl:choose>
        <xsl:when test="@name">"<xsl:value-of select="@name"/>"</xsl:when>
        <xsl:when test="name">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="log:name"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>null</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="level">
      <xsl:choose>
        <xsl:when test="@level">"<xsl:value-of select="@level"/>"</xsl:when>
        <xsl:when test="level">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="log:level"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>"DEBUG"</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsp:logic>
      if (_log_defaultLogger == null) {
        _log_defaultLogger = getLogger();
      }
      try {
        String category = <xsl:value-of select="$name"/>;
        if (category != null) {
          enableLogging(_log_defaultLogger.getChildLogger(category));
        } else {
          enableLogging(_log_defaultLogger);
        }
      } catch (Exception e) {
        getLogger().error("Could not create logger for \"" +
                           <xsl:value-of select="$name"/> + "\".", e);
      }
    </xsp:logic>
  </xsl:template>

  <xsl:template match="log:debug">
    <xsp:logic>
      if (getLogger().isDebugEnabled())
        getLogger().debug(<xsl:call-template name="get-log-message"/>);
    </xsp:logic>
  </xsl:template>

  <xsl:template match="log:info">
    <xsp:logic>
      if (getLogger().isInfoEnabled())
        getLogger().info(<xsl:call-template name="get-log-message"/>);
    </xsp:logic>
  </xsl:template>

  <xsl:template match="log:warn">
    <xsp:logic>
      if (getLogger().isWarnEnabled())
        getLogger().warn(<xsl:call-template name="get-log-message"/>);
    </xsp:logic>
  </xsl:template>

  <xsl:template match="log:error">
    <xsp:logic>
      if (getLogger().isErrorEnabled())
        getLogger().error(<xsl:call-template name="get-log-message"/>);
    </xsp:logic>
  </xsl:template>

  <xsl:template match="log:fatal-error">
    <xsp:logic>
      getLogger().fatalError(<xsl:call-template name="get-log-message"/>);
    </xsp:logic>
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

  <xsl:template name="get-log-message">
    <xsl:for-each select="./child::node()">
      <xsl:choose>
        <xsl:when test="xsp:expr"><xsl:apply-templates select="node()"/></xsl:when>
        <xsl:otherwise>"<xsl:value-of select="."/>"</xsl:otherwise>
      </xsl:choose>
      <xsl:if test="not(position() = last())"> + </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="@*|*|text()|processing-instruction()">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
