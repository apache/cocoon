<?xml version="1.0" encoding="utf-8"?>
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
    Convert the output of the XSP caching sample to XSL-FO,
    in order to test caching of the whole pipeline, up to PDF

    @author bdelacretaz@codeconsult.ch
    CVS $Id: xsp-sample-to-fo.xsl,v 1.5 2004/04/22 12:26:00 vgritsenko Exp $
-->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format">

    <xsl:param name="pages" select="'10'"/>

    <xsl:attribute-set name="title">
        <xsl:attribute name="font-size">18pt</xsl:attribute>
        <xsl:attribute name="space-before">10pt</xsl:attribute>
        <xsl:attribute name="color">blue</xsl:attribute>
    </xsl:attribute-set>

    <xsl:template match="/">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
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
            <fo:page-sequence master-reference="main">

                <fo:flow flow-name="xsl-region-body">
                    <fo:block space-before="6pt">
                        <fo:block font-size="24pt">Cocoon FOP caching test</fo:block>
                        <xsl:call-template name="explain"/>

                        <fo:block border="solid grey 1px" padding="1em">
                            <xsl:element name="fo:block" use-attribute-sets="title">
                                Original output from the XSP page
                            </xsl:element>
                            <fo:block font-style="italic">
                                The test links below are not processed in the PDF version of this sample.
                            </fo:block>
                            <xsl:apply-templates/>
                        </fo:block>

                        <xsl:element name="fo:block" use-attribute-sets="title">
                            Dummy pages
                        </xsl:element>
                        <fo:block>
                            Several dummy pages follow, as set by the
                            <fo:inline font-style="italic">pages</fo:inline> URL parameter.
                        </fo:block>
                        <xsl:call-template name="repeatPages">
                            <xsl:with-param name="nPages" select="$pages"/>
                        </xsl:call-template>

                        <fo:block id="lastBlock">
                            End of test document
                        </fo:block>
                    </fo:block>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

    <!-- generate a lot of pages to make FOP generation slower -->
    <xsl:template name="repeatPages">
        <xsl:param name="nPages"/>
        <fo:block break-before="page">
            Dummy page, used to slow down FOP generation to test caching...
            <xsl:value-of select="$nPages"/> pages to go.
        </fo:block>
        <fo:block>
            <!-- slow down FOP for this test, forcing it to keep all pages in memory -->
            The last page is number <fo:page-number-citation ref-id="lastBlock"/>.
        </fo:block>
        <xsl:if test="$nPages &gt; 1">
            <xsl:call-template name="repeatPages">
                <xsl:with-param name="nPages" select="$nPages - 1"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- explain this sample -->
    <xsl:template name="explain">
        <xsl:element name="fo:block" use-attribute-sets="title">
            What's this?
        </xsl:element>
        <fo:block>
            This sample reuses the XSP cacheable sample page and allows you
            to test caching all the way up to PDF generation.
        </fo:block>
        <fo:block>
            Note that I was unable to get caching to work when using the cocoon:/
            protocol and the FileGenerator to access the output of the XSP sample.
            Using the XSP page directly with the serverpages generator works.
        </fo:block>

        <xsl:element name="fo:block" use-attribute-sets="title">
            How to test the cache
        </xsl:element>
        <fo:block>
            Call this page as described below and use the information shown in
            <fo:inline color="red">red</fo:inline> under
            <fo:inline font-style="italic">original output</fo:inline>
            below to check that the cache is working.
        </fo:block>
        <fo:block>
            The sitemap log (or whatever log the FOPSerializer is configured to write to) can also
            be used to tell if FOP is converting the data or if its being served from the Cocoon cache.
        </fo:block>
        <fo:block>
            Different values of
            <fo:inline font-style="italic">pageKey</fo:inline> should cause different versions of the document to be cached.
        </fo:block>
        <fo:block>
            The number at the end of the page name is the number of pages to generate in the output PDF.
        </fo:block>
    </xsl:template>

    <!-- minimal HTML scraping of input -->
    <xsl:template match="*[starts-with(name(),'h')]|p">
        <fo:block>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <!-- minimal HTML scraping of input -->
    <xsl:template match="br">
        <fo:block>&#160;</fo:block>
    </xsl:template>

    <!-- minimal HTML scraping of input -->
    <xsl:template match="b">
        <fo:inline color="red" font-weight="bold" font-size="14pt">
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

</xsl:stylesheet>
