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
    Convert the output of the XSP caching sample to iText,
    in order to test caching of the whole pipeline, up to PDF

    @author tcurdt@apache.org
    CVS $Id: xsp-sample-to-itext.xsl,v 1.4 2004/04/22 12:26:00 vgritsenko Exp $
-->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:param name="pages" select="'10'"/>

    <xsl:template match="/">
      <itext creationdate="Fri May 23 9:30:00 CEST 2003" producer="tcurdt@cocoon.org">
        <xsl:call-template name="explain"/>
        <xsl:call-template name="repeatPages">
          <xsl:with-param name="nPages" select="$pages"/>
        </xsl:call-template>

       <paragraph font="unknown" size="12.0" align="Default">
         End of test document
       </paragraph>
      </itext>
    </xsl:template>

    <!-- generate a lot of pages to make FOP generation slower -->
    <xsl:template name="repeatPages">
        <xsl:param name="nPages"/>

        <paragraph font="unknown" size="12.0" align="Default">
          <newpage/>
          Dummy page, used to slow down iText generation to test caching...
          <xsl:value-of select="$nPages"/> pages to go.
        </paragraph>

        <xsl:if test="$nPages &gt; 1">
            <xsl:call-template name="repeatPages">
                <xsl:with-param name="nPages" select="$nPages - 1"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- explain this sample -->
    <xsl:template name="explain">
        <phrase leading="27.0" align="Default" font="Helvetica" size="18.0" fontstyle="normal" red="0" green="64" blue="64">
            What's this?
        </phrase>
        <newline/>
        <paragraph font="unknown" size="12.0" align="Default">
            This sample reuses the XSP cacheable sample page and allows you
            to test caching all the way up to PDF generation.
        </paragraph>
        <paragraph font="unknown" size="12.0" align="Default">
            Note that I was unable to get caching to work when using the cocoon:/
            protocol and the FileGenerator to access the output of the XSP sample.
            Using the XSP page directly with the serverpages generator works.
        </paragraph>
        <newline/><newline/>
        <phrase leading="27.0" align="Default" font="Helvetica" size="18.0" fontstyle="normal" red="0" green="64" blue="64">
            How to test the cache
        </phrase>
        <newline/>
        <paragraph font="unknown" size="12.0" align="Default">
            Call this page as described below and use the information shown in
            <chunk font="Helvetica" size="14.0" fontstyle="normal" red="255" green="0" blue="0">red</chunk> under
            <chunk font="Helvetica" size="14.0" fontstyle="normal" red="255" green="0" blue="0">original output</chunk>
            below to check that the cache is working.
        </paragraph>
        <paragraph font="unknown" size="12.0" align="Default">
            The sitemap log (or whatever log the FOPSerializer is configured to write to) can also
            be used to tell if FOP is converting the data or if its being served from the Cocoon cache.
        </paragraph>
        <paragraph font="unknown" size="12.0" align="Default">
            Different values of
            <chunk font="Helvetica" size="14.0" fontstyle="normal" red="255" green="0" blue="0">pageKey</chunk> should cause different versions of the document to be cached.
        </paragraph>
        <paragraph font="unknown" size="12.0" align="Default">
            The number at the end of the page name is the number of pages to generate in the output PDF.
        </paragraph>
    </xsl:template>

    <!-- minimal HTML scraping of input -->
    <xsl:template match="*[starts-with(name(),'h')]|p">
        <paragraph font="unknown" size="12.0" align="Default">
            <xsl:apply-templates/>
        </paragraph>
    </xsl:template>

    <!-- minimal HTML scraping of input -->
    <xsl:template match="br">
        <newline/>
    </xsl:template>

    <!-- minimal HTML scraping of input -->
    <xsl:template match="b">
       <chunk size="20.0" fontstyle="bold" red="255" green="0" blue="0">

            <xsl:apply-templates/>
       </chunk>
    </xsl:template>

</xsl:stylesheet>
