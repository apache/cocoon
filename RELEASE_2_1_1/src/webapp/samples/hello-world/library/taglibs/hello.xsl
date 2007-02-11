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
 * Hello World logicsheet for the Java language
 *
 * @author <a href="mailto:vgritsenko@apache.org>Vadim Gritsenko</a>
 * @version CVS $Revision: 1.1 $ $Date: 2003/03/09 00:11:07 $
-->

<xsl:stylesheet
  version="1.0"
  xmlns:xsp="http://apache.org/xsp"
  xmlns:xsp-hello="http://apache.org/xsp/hello/1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="xsp-hello:greeting">
    <xsl:variable name="name">
      <xsl:choose>
        <xsl:when test="@name">"<xsl:value-of select="@name"/>"</xsl:when>
        <xsl:when test="xsp-hello:name">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="xsp-hello:name"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="value">
      <xsl:choose>
        <xsl:when test="@value">"<xsl:value-of select="@value"/>"</xsl:when>
        <xsl:when test="xsp-hello:value">
          <xsl:call-template name="get-nested-content">
            <xsl:with-param name="content" select="xsp-hello:value"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>"Hello"</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsp:content>
    <xsp:expr><xsl:value-of select="$value"/></xsp:expr>, 
    <xsp:expr><xsl:value-of select="$name"/></xsp:expr>!
    </xsp:content>
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
