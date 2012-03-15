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
<!-- $Id: page2fo.xsl 712183 2008-11-07 16:24:12Z reinhard $ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:template match="/">
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

      <fo:layout-master-set>
        <fo:simple-page-master master-name="page" page-height="29.7cm" page-width="21cm" margin-top="1cm"
          margin-bottom="2cm" margin-left="2.5cm" margin-right="2.5cm">
          <fo:region-body margin-top="3cm" />
          <fo:region-before extent="3cm" />
          <fo:region-after extent="1.5cm" />
        </fo:simple-page-master>

        <fo:page-sequence-master master-name="all">
          <fo:repeatable-page-master-alternatives>
            <fo:conditional-page-master-reference master-reference="page" page-position="first" />
          </fo:repeatable-page-master-alternatives>
        </fo:page-sequence-master>
      </fo:layout-master-set>

      <fo:page-sequence master-reference="all">
        <fo:static-content flow-name="xsl-region-after">
          <fo:block text-align="center" font-size="10pt" font-family="serif" line-height="14pt">
            page <fo:page-number />
          </fo:block>
        </fo:static-content>

        <fo:flow flow-name="xsl-region-body">
          <xsl:apply-templates />
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>

  <xsl:template match="title">
    <fo:block font-size="36pt" space-before.optimum="24pt" text-align="center">
      <xsl:apply-templates />
    </fo:block>
  </xsl:template>

  <xsl:template match="para">
    <fo:block font-size="12pt" space-before.optimum="12pt" text-align="center">
      <xsl:apply-templates />
    </fo:block>
  </xsl:template>
</xsl:stylesheet>
