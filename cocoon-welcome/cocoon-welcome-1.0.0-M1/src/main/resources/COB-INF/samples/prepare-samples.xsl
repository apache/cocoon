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
  - Convert the output of the directory generator into a samples file.
  -
  - $Id$
  -->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="contextPath"/>


  <xsl:template match="/">
    <samples name="Cocoon Blocks">
      <links>
        <link role="see-also" href="sitemap.xmap">Sitemap</link>
      </links>
      <xsl:apply-templates select="//group">
        <xsl:sort select="@priority" data-type="number" order="descending"/>
        <xsl:sort select="@name"/>
      </xsl:apply-templates>
    </samples>
  </xsl:template>

  <xsl:template match="group">
    <xsl:copy>
      <xsl:copy-of select="@name"/>
      <xsl:apply-templates>
        <xsl:sort select="@priority" data-type="number" order="descending"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="sample">
    <!--
      - Document structure is:
      - /collection[@name]/collection/resource/group[@name]/sample[@name]
      -->
    <sample href="{$contextPath}{@href}" name="{@name}">
      <xsl:copy-of select="*|text()"/>
    </sample>
  </xsl:template>

  <xsl:template match="note">
    <xsl:copy>
      <xsl:copy-of select="*|text()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
