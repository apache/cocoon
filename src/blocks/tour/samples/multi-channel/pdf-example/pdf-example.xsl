<?xml version="1.0" encoding="iso-8859-1"?>

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

<!-- process the insert-toc element -->

<xsl:stylesheet
    id="main"
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:request="http://apache.org/cocoon/request/2.0"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
>

    <!-- by default copy everything -->
    <xsl:template match="/">
        <fo:root>
            <fo:layout-master-set>

                <!-- layout for all pages -->
                <fo:simple-page-master master-name="main"
                    page-height="29.7cm"
                    page-width="21cm"
                    margin-top="1cm"
                    margin-bottom="2cm"
                    margin-left="2.5cm"
                    margin-right="2.5cm">
                    <fo:region-body margin-top="3cm"/>
                    <fo:region-before extent="3cm"/>
                    <fo:region-after extent="1.5cm"/>
                </fo:simple-page-master>

            </fo:layout-master-set>

            <!-- content -->
            <fo:page-sequence id="page-sequence" master-reference="main">

                <fo:flow flow-name="xsl-region-body">
                    <fo:block font-size="12pt" text-align="right">
                        Supersonic FOP example
                    </fo:block>
                    <fo:block font-size="18pt">
                        <xsl:apply-templates select="//request:parameter[@name='title']"/>
                    </fo:block>
                    <fo:block font-size="12pt">
                        <xsl:apply-templates select="//request:parameter[@name='text']"/>
                    </fo:block>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

    <xsl:template match="request:parameter">
        <xsl:value-of select="request:value"/>
    </xsl:template>

</xsl:stylesheet>
