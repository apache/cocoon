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

<!-- CVS $Id: dynamic-xsp2xsp.xsl,v 1.2 2004/03/10 10:32:56 cziegeler Exp $ -->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsp="http://apache.org/xsp"
                xmlns:xsp-request="http://apache.org/xsp/request/2.0">

  <xsl:template match="*[namespace-uri() = 'urn:xsp']">
    <xsl:element name="xsp:{local-name()}">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name(.)}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>

      <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="*[namespace-uri() = 'urn:xsp-request']">
    <xsl:element name="xsp-request:{local-name()}" namespace="http://apache.org/xsp/request/2.0">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name(.)}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>

      <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
