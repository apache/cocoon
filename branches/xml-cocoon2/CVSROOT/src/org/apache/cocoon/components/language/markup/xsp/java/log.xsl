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
 * @author <a href="mailto:bloritsch@apache.org>Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-11-13 18:17:28 $
-->

<!-- XSP Response logicsheet for the Java language -->
<xsl:stylesheet
  version="1.0"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:log="http://apache.org/xsp/log"

  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

  <xsl:template match="log:logger">
    <xsl:variable name="name">
      <xsl:choose>
        <xsl:when test="@name">"<xsl:value-of select="@name"/>"</xsl:when>
        <xsl:when test="name">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="log:name"/>
          </xsl:call-template>
        </xsl:when>
	<xsl:otherwise>""</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="url">
      <xsl:choose>
        <xsl:when test="@url">"<xsl:value-of select="@url"/>"</xsl:when>
        <xsl:when test="url">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="log:url"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>""</xsl:otherwise>
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
      if (log == null) {
          try {
            log = LogKit.createLogger(<xsl:value-of select="$name"/>,
                                      <xsl:value-of select="$url"/>,
                                      <xsl:value-of select="$level"/>);
          } catch (Exception e) {
            cocoonLogger.error("Could not create logger for \"" +
                               <xsl:value-of select="$name"/> + "\".", e);
          }
      }
    </xsp:logic>
  </xsl:template>

  <xsl:template match="log:debug">
    <xsp:logic>
      log.debug("<xsl:value-of select="."/>");
    </xsp:logic>
  </xsl:template>

  <xsl:template match="log:info">
    <xsp:logic>
      log.info("<xsl:value-of select="."/>");
    </xsp:logic>
  </xsl:template>

  <xsl:template match="log:warn">
    <xsp:logic>
      log.warn("<xsl:value-of select="."/>");
    </xsp:logic>
  </xsl:template>

  <xsl:template match="log:error">
    <xsp:logic>
      log.error("<xsl:value-of select="."/>");
    </xsp:logic>
  </xsl:template>

  <xsl:template match="log:fatal-error">
    <xsp:logic>
      log.fatalError("<xsl:value-of select="."/>");
    </xsp:logic>
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
