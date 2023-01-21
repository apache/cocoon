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
  - Transforms properties.xml to result page.
  -
  - $Id$
  -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="title">Input Module</xsl:param>
  <xsl:param name="description"/>

  <xsl:template match="/">
    <page>
      <title><xsl:value-of select="$title"/></title>
      <content>
        <p><xsl:value-of select="$description"/></p>
        <xsl:apply-templates/>
      </content>
    </page>
  </xsl:template>

  <xsl:template match="properties">
    <table class="table">
      <tr>
        <th>Expression</th>
        <th>Result</th>
      </tr>
      <xsl:apply-templates>
        <xsl:sort select="name"/>
      </xsl:apply-templates>
    </table>
  </xsl:template>

  <xsl:template match="property">
    <tr>
      <td><xsl:value-of select="name"/></td>
      <td><xsl:value-of select="value"/>&#160;</td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
