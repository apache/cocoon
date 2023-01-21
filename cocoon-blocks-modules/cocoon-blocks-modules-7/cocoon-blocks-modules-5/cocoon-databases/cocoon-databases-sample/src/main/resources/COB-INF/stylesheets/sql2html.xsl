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

<!--
  - Transforms sql transformer output into html.
  - @version $Id$
  -->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:sql="http://apache.org/cocoon/SQL/2.0">

  <xsl:import href="servlet:style-default:/common/style/xsl/html/simple-page2html.xsl"/>

  <xsl:template match="sql:rowset">
    <xsl:choose>
      <xsl:when test="ancestor::sql:rowset">
        <tr>
          <td>
            <table border="1">
              <xsl:apply-templates/>
            </table>
          </td>
        </tr>
      </xsl:when>
      <xsl:otherwise>
        <table border="1">
          <xsl:apply-templates/>
        </table>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="sql:row">
    <tr>
      <xsl:apply-templates/>
    </tr>
  </xsl:template>

  <xsl:template match="sql:name">
    <td>
      <xsl:copy-of select="node()"/>
      <br/>
      <xsl:copy-of select="../sql:description/node()"/>
    </td>
  </xsl:template>

  <xsl:template match="sql:description"/>

  <xsl:template match="sql:returncode">
    <td>
      <xsl:copy-of select="node()"/>
      rows updated.
    </td>
  </xsl:template>

  <xsl:template match="sql:id">
    <!-- ignore -->
  </xsl:template>
</xsl:stylesheet>
