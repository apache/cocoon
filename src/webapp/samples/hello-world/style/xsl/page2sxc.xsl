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

<!--+
    | This stylesheets transforms hello world XML page to ZIP serializer's format.
    | After the serializer, result is an OpenOffice Calc document (sxc).
    |
    | @author <a href="mailto:vgritsenko@apache.org>Vadim Gritsenko</a>
    | @version CVS $Id: page2sxc.xsl,v 1.2 2004/03/10 10:18:52 cziegeler Exp $
    +-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:zip="http://apache.org/cocoon/zip-archive/1.0"
                xmlns:text="http://openoffice.org/2000/text"
                xmlns:table="http://openoffice.org/2000/table">

  <xsl:template match="page">
    <zip:archive>
      <!--
        <!DOCTYPE office:document-content PUBLIC "-//OpenOffice.org//DTD OfficeDocument 1.0//EN" "office.dtd">
      -->
      <zip:entry name="content.xml" serializer="xml">
        <office:document-content xmlns:office="http://openoffice.org/2000/office"
                                 xmlns:style="http://openoffice.org/2000/style"
                                 xmlns:text="http://openoffice.org/2000/text"
                                 xmlns:table="http://openoffice.org/2000/table"
                                 xmlns:draw="http://openoffice.org/2000/drawing"
                                 xmlns:fo="http://www.w3.org/1999/XSL/Format"
                                 xmlns:xlink="http://www.w3.org/1999/xlink"
                                 xmlns:number="http://openoffice.org/2000/datastyle"
                                 xmlns:svg="http://www.w3.org/2000/svg"
                                 xmlns:chart="http://openoffice.org/2000/chart"
                                 xmlns:dr3d="http://openoffice.org/2000/dr3d"
                                 xmlns:math="http://www.w3.org/1998/Math/MathML"
                                 xmlns:form="http://openoffice.org/2000/form"
                                 xmlns:script="http://openoffice.org/2000/script"
                                 office:class="spreadsheet"
                                 office:version="1.0">
          <office:script/>
          <office:font-decls>
            <style:font-decl style:name="Arial Unicode MS" fo:font-family="&apos;Arial Unicode MS&apos;" style:font-pitch="variable"/>
            <style:font-decl style:name="HG Mincho Light J" fo:font-family="&apos;HG Mincho Light J&apos;" style:font-pitch="variable"/>
            <style:font-decl style:name="Albany" fo:font-family="Albany" style:font-family-generic="swiss" style:font-pitch="variable"/>
          </office:font-decls>
          <office:automatic-styles>
            <style:style style:name="co1" style:family="table-column">
              <style:properties fo:break-before="auto" style:column-width="0.8925inch"/>
            </style:style>
            <style:style style:name="ro1" style:family="table-row">
              <style:properties fo:break-before="auto"/>
            </style:style>
            <style:style style:name="ta1" style:family="table" style:master-page-name="Default">
              <style:properties table:display="true"/>
            </style:style>
          </office:automatic-styles>
          <office:body>
            <table:table table:style-name="ta1"> <!-- table:name="Hello" -->
              <xsl:attribute namespace="http://openoffice.org/2000/table" name="table:name"><xsl:value-of select="title"/></xsl:attribute>
              <table:table-column table:style-name="co1" table:default-cell-style-name="Default"/>
              <table:table-row table:style-name="ro1">
                <table:table-cell table:style-name="Heading">
                  <text:p><xsl:value-of select="title"/></text:p>
                </table:table-cell>
              </table:table-row>
              <xsl:apply-templates/>
            </table:table>
          </office:body>
        </office:document-content>
      </zip:entry>

      <!--
        <!DOCTYPE office:document-meta PUBLIC "-//OpenOffice.org//DTD OfficeDocument 1.0//EN" "office.dtd">
      -->
      <zip:entry name="meta.xml" serializer="xml">
        <office:document-meta xmlns:office="http://openoffice.org/2000/office"
                              xmlns:xlink="http://www.w3.org/1999/xlink"
                              xmlns:dc="http://purl.org/dc/elements/1.1/"
                              xmlns:meta="http://openoffice.org/2000/meta"
                              office:version="1.0">
          <office:meta>
            <meta:generator>OpenOffice.org 1.0.3 (Win32)</meta:generator>
            <dc:title><xsl:value-of select="title"/></dc:title>
            <dc:subject>Cocoon Hello World Sample Document</dc:subject>
            <meta:creation-date>2003-05-08T08:15:40</meta:creation-date>
            <dc:date>2003-05-08T08:19:17</dc:date>
            <dc:language>en-US</dc:language>
            <meta:editing-cycles>4</meta:editing-cycles>
            <meta:editing-duration>PT3M39S</meta:editing-duration>
            <meta:user-defined meta:name="Info 1"/>
            <meta:user-defined meta:name="Info 2"/>
            <meta:user-defined meta:name="Info 3"/>
            <meta:user-defined meta:name="Info 4"/>
            <meta:document-statistic meta:table-count="1" meta:cell-count="2"/>
          </office:meta>
        </office:document-meta>
      </zip:entry>

      <!--
        <!DOCTYPE office:document-settings PUBLIC "-//OpenOffice.org//DTD OfficeDocument 1.0//EN" "office.dtd">
      -->
      <zip:entry name="settings.xml" serializer="xml">
        <office:document-settings xmlns:office="http://openoffice.org/2000/office"
                                  xmlns:xlink="http://www.w3.org/1999/xlink"
                                  xmlns:config="http://openoffice.org/2001/config"
                                  office:version="1.0">
          <office:settings>
            <config:config-item-set config:name="view-settings">
              <config:config-item config:name="VisibleAreaTop" config:type="int">0</config:config-item>
              <config:config-item config:name="VisibleAreaLeft" config:type="int">0</config:config-item>
              <config:config-item config:name="VisibleAreaWidth" config:type="int">2258</config:config-item>
              <config:config-item config:name="VisibleAreaHeight" config:type="int">1156</config:config-item>
              <config:config-item-map-indexed config:name="Views">
                <config:config-item-map-entry>
                  <config:config-item config:name="ViewId" config:type="string">View1</config:config-item>
                  <config:config-item-map-named config:name="Tables">
                    <config:config-item-map-entry> <!-- config:name="Hello" -->
                      <xsl:attribute namespace="http://openoffice.org/2001/config" name="config:name"><xsl:value-of select="title"/></xsl:attribute>
                      <config:config-item config:name="CursorPositionX" config:type="int">0</config:config-item>
                      <config:config-item config:name="CursorPositionY" config:type="int">1</config:config-item>
                      <config:config-item config:name="HorizontalSplitMode" config:type="short">0</config:config-item>
                      <config:config-item config:name="VerticalSplitMode" config:type="short">0</config:config-item>
                      <config:config-item config:name="HorizontalSplitPosition" config:type="int">0</config:config-item>
                      <config:config-item config:name="VerticalSplitPosition" config:type="int">0</config:config-item>
                      <config:config-item config:name="ActiveSplitRange" config:type="short">2</config:config-item>
                      <config:config-item config:name="PositionLeft" config:type="int">0</config:config-item>
                      <config:config-item config:name="PositionRight" config:type="int">0</config:config-item>
                      <config:config-item config:name="PositionTop" config:type="int">0</config:config-item>
                      <config:config-item config:name="PositionBottom" config:type="int">0</config:config-item>
                    </config:config-item-map-entry>
                  </config:config-item-map-named>
                  <config:config-item config:name="ActiveTable" config:type="string"><xsl:value-of select="title"/></config:config-item>
                  <config:config-item config:name="HorizontalScrollbarWidth" config:type="int">270</config:config-item>
                  <config:config-item config:name="ZoomType" config:type="short">0</config:config-item>
                  <config:config-item config:name="ZoomValue" config:type="int">100</config:config-item>
                  <config:config-item config:name="PageViewZoomValue" config:type="int">60</config:config-item>
                  <config:config-item config:name="ShowPageBreakPreview" config:type="boolean">false</config:config-item>
                  <config:config-item config:name="ShowZeroValues" config:type="boolean">true</config:config-item>
                  <config:config-item config:name="ShowNotes" config:type="boolean">true</config:config-item>
                  <config:config-item config:name="ShowGrid" config:type="boolean">true</config:config-item>
                  <config:config-item config:name="GridColor" config:type="long">12632256</config:config-item>
                  <config:config-item config:name="ShowPageBreaks" config:type="boolean">true</config:config-item>
                  <config:config-item config:name="HasColumnRowHeaders" config:type="boolean">true</config:config-item>
                  <config:config-item config:name="HasSheetTabs" config:type="boolean">true</config:config-item>
                  <config:config-item config:name="IsOutlineSymbolsSet" config:type="boolean">true</config:config-item>
                  <config:config-item config:name="IsSnapToRaster" config:type="boolean">false</config:config-item>
                  <config:config-item config:name="RasterIsVisible" config:type="boolean">false</config:config-item>
                  <config:config-item config:name="RasterResolutionX" config:type="int">1270</config:config-item>
                  <config:config-item config:name="RasterResolutionY" config:type="int">1270</config:config-item>
                  <config:config-item config:name="RasterSubdivisionX" config:type="int">1</config:config-item>
                  <config:config-item config:name="RasterSubdivisionY" config:type="int">1</config:config-item>
                  <config:config-item config:name="IsRasterAxisSynchronized" config:type="boolean">true</config:config-item>
                </config:config-item-map-entry>
              </config:config-item-map-indexed>
            </config:config-item-set>
            <config:config-item-set config:name="configuration-settings">
              <config:config-item config:name="ShowZeroValues" config:type="boolean">true</config:config-item>
              <config:config-item config:name="ShowNotes" config:type="boolean">true</config:config-item>
              <config:config-item config:name="ShowGrid" config:type="boolean">true</config:config-item>
              <config:config-item config:name="GridColor" config:type="long">12632256</config:config-item>
              <config:config-item config:name="ShowPageBreaks" config:type="boolean">true</config:config-item>
              <config:config-item config:name="LinkUpdateMode" config:type="short">3</config:config-item>
              <config:config-item config:name="HasColumnRowHeaders" config:type="boolean">true</config:config-item>
              <config:config-item config:name="HasSheetTabs" config:type="boolean">true</config:config-item>
              <config:config-item config:name="IsOutlineSymbolsSet" config:type="boolean">true</config:config-item>
              <config:config-item config:name="IsSnapToRaster" config:type="boolean">false</config:config-item>
              <config:config-item config:name="RasterIsVisible" config:type="boolean">false</config:config-item>
              <config:config-item config:name="RasterResolutionX" config:type="int">1270</config:config-item>
              <config:config-item config:name="RasterResolutionY" config:type="int">1270</config:config-item>
              <config:config-item config:name="RasterSubdivisionX" config:type="int">1</config:config-item>
              <config:config-item config:name="RasterSubdivisionY" config:type="int">1</config:config-item>
              <config:config-item config:name="IsRasterAxisSynchronized" config:type="boolean">true</config:config-item>
              <config:config-item config:name="AutoCalculate" config:type="boolean">true</config:config-item>
              <config:config-item config:name="PrinterName" config:type="string">\\vgritsenkopc2\HP LaserJet 5L</config:config-item>
              <config:config-item config:name="PrinterSetup" config:type="base64Binary">kgP+/1xcdmdyaXRzZW5rb3BjMlxIUCBMYXNlckpldCA1TAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAd2luc3Bvb2wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAWAAEA2AIAAAAAAAAFAAhSAAAEdAAAM1ROVwEACABcXHZncml0c2Vua29wYzJcSFAgTGFzZXJKZXQgNUwAAAEEAAWcADQCQ++ABQEAAQCaCzQIZAABAA8AWAIBAAEAWAIDAAEAQTQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAQAAAAIAAAABAAAA/////wAAAAAAAAAAAAAAAAAAAABESU5VIgAAADQCAACjWpOwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</config:config-item>
              <config:config-item config:name="ApplyUserData" config:type="boolean">true</config:config-item>
              <config:config-item config:name="CharacterCompressionType" config:type="short">0</config:config-item>
              <config:config-item config:name="IsKernAsianPunctuation" config:type="boolean">false</config:config-item>
              <config:config-item config:name="SaveVersionOnClose" config:type="boolean">false</config:config-item>
            </config:config-item-set>
          </office:settings>
        </office:document-settings>
      </zip:entry>

      <!--
        <!DOCTYPE office:document-styles PUBLIC "-//OpenOffice.org//DTD OfficeDocument 1.0//EN" "office.dtd">
      -->
      <zip:entry name="styles.xml" serializer="xml">
        <office:document-styles xmlns:office="http://openoffice.org/2000/office"
                                xmlns:style="http://openoffice.org/2000/style"
                                xmlns:text="http://openoffice.org/2000/text"
                                xmlns:table="http://openoffice.org/2000/table"
                                xmlns:draw="http://openoffice.org/2000/drawing"
                                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                                xmlns:xlink="http://www.w3.org/1999/xlink"
                                xmlns:number="http://openoffice.org/2000/datastyle"
                                xmlns:svg="http://www.w3.org/2000/svg"
                                xmlns:chart="http://openoffice.org/2000/chart"
                                xmlns:dr3d="http://openoffice.org/2000/dr3d"
                                xmlns:math="http://www.w3.org/1998/Math/MathML"
                                xmlns:form="http://openoffice.org/2000/form"
                                xmlns:script="http://openoffice.org/2000/script"
                                office:version="1.0">
          <office:font-decls>
            <style:font-decl style:name="Arial Unicode MS" fo:font-family="&apos;Arial Unicode MS&apos;" style:font-pitch="variable"/>
            <style:font-decl style:name="HG Mincho Light J" fo:font-family="&apos;HG Mincho Light J&apos;" style:font-pitch="variable"/>
            <style:font-decl style:name="Albany" fo:font-family="Albany" style:font-family-generic="swiss" style:font-pitch="variable"/>
          </office:font-decls>
          <office:styles>
            <style:default-style style:family="table-cell">
              <style:properties style:decimal-places="2" style:font-name="Albany" fo:language="en" fo:country="US" style:font-name-asian="HG Mincho Light J" style:language-asian="none" style:country-asian="none" style:font-name-complex="Arial Unicode MS" style:language-complex="none" style:country-complex="none" style:tab-stop-distance="0.5inch"/>
            </style:default-style>
            <number:number-style style:name="N0" style:family="data-style">
              <number:number number:min-integer-digits="1"/>
            </number:number-style>
            <number:currency-style style:name="N104P0" style:family="data-style" style:volatile="true">
              <number:currency-symbol number:language="en" number:country="US">$</number:currency-symbol>
              <number:number number:decimal-places="2" number:min-integer-digits="1" number:grouping="true"/>
            </number:currency-style>
            <number:currency-style style:name="N104" style:family="data-style">
              <style:properties fo:color="#ff0000"/>
              <number:text>-</number:text>
              <number:currency-symbol number:language="en" number:country="US">$</number:currency-symbol>
              <number:number number:decimal-places="2" number:min-integer-digits="1" number:grouping="true"/>
              <style:map style:condition="value()&gt;=0" style:apply-style-name="N104P0"/>
            </number:currency-style>
            <style:style style:name="Default" style:family="table-cell"/>
            <style:style style:name="Result" style:family="table-cell" style:parent-style-name="Default">
              <style:properties fo:font-style="italic" style:text-underline="single" style:text-underline-color="font-color" fo:font-weight="bold"/>
            </style:style>
            <style:style style:name="Result2" style:family="table-cell" style:parent-style-name="Result" style:data-style-name="N104"/>
            <style:style style:name="Heading" style:family="table-cell" style:parent-style-name="Default">
              <style:properties fo:text-align="center" style:text-align-source="fix" fo:font-size="16pt" fo:font-style="italic" fo:font-weight="bold"/>
            </style:style>
            <style:style style:name="Heading1" style:family="table-cell" style:parent-style-name="Heading">
              <style:properties fo:direction="ltr" style:rotation-angle="90"/>
            </style:style>
          </office:styles>
          <office:automatic-styles>
            <style:page-master style:name="pm1">
              <style:header-style>
                <style:properties fo:min-height="0.2957inch" fo:margin-left="0inch" fo:margin-right="0inch" fo:margin-bottom="0.0984inch"/>
              </style:header-style>
              <style:footer-style>
                <style:properties fo:min-height="0.2957inch" fo:margin-left="0inch" fo:margin-right="0inch" fo:margin-top="0.0984inch"/>
              </style:footer-style>
            </style:page-master>
            <style:page-master style:name="pm2">
              <style:header-style>
                <style:properties fo:min-height="0.2957inch" fo:margin-left="0inch" fo:margin-right="0inch" fo:margin-bottom="0.0984inch" fo:border="0.0346inch solid #000000" fo:padding="0.0071inch" fo:background-color="#c0c0c0">
                  <style:background-image/>
                </style:properties>
              </style:header-style>
              <style:footer-style>
                <style:properties fo:min-height="0.2957inch" fo:margin-left="0inch" fo:margin-right="0inch" fo:margin-top="0.0984inch" fo:border="0.0346inch solid #000000" fo:padding="0.0071inch" fo:background-color="#c0c0c0">
                  <style:background-image/>
                </style:properties>
              </style:footer-style>
            </style:page-master>
          </office:automatic-styles>
          <office:master-styles>
            <style:master-page style:name="Default" style:page-master-name="pm1">
              <style:header><text:p><text:sheet-name>???</text:sheet-name></text:p></style:header>
              <style:header-left style:display="false"/>
              <style:footer><text:p>Page <text:page-number>1</text:page-number></text:p></style:footer>
              <style:footer-left style:display="false"/>
            </style:master-page>
            <style:master-page style:name="Report" style:page-master-name="pm2">
              <style:header>
                <style:region-left><text:p><text:sheet-name>???</text:sheet-name> (<text:title>???</text:title>)</text:p></style:region-left><style:region-right><text:p><text:date style:data-style-name="N2" text:date-value="2003-05-08">05/08/2003</text:date>, <text:time>08:19:17</text:time></text:p></style:region-right>
              </style:header>
              <style:header-left style:display="false"/>
              <style:footer>
                <text:p>Page <text:page-number>1</text:page-number> / <text:page-count>99</text:page-count></text:p>
              </style:footer>
              <style:footer-left style:display="false"/>
            </style:master-page>
          </office:master-styles>
        </office:document-styles>
      </zip:entry>

      <!--
        <!DOCTYPE manifest:manifest PUBLIC "-//OpenOffice.org//DTD Manifest 1.0//EN" "Manifest.dtd">
      -->
      <zip:entry name="META-INF/manifest.xml" serializer="xml">
        <manifest:manifest xmlns:manifest="http://openoffice.org/2001/manifest">
          <manifest:file-entry manifest:media-type="application/vnd.sun.xml.calc" manifest:full-path="/"/>
          <manifest:file-entry manifest:media-type="" manifest:full-path="Pictures/"/>
          <manifest:file-entry manifest:media-type="text/xml" manifest:full-path="content.xml"/>
          <manifest:file-entry manifest:media-type="text/xml" manifest:full-path="styles.xml"/>
          <manifest:file-entry manifest:media-type="text/xml" manifest:full-path="meta.xml"/>
          <manifest:file-entry manifest:media-type="text/xml" manifest:full-path="settings.xml"/>
        </manifest:manifest>
      </zip:entry>
    </zip:archive>
  </xsl:template>

  <xsl:template match="title">
  </xsl:template>

  <xsl:template match="content">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="para">
    <table:table-row table:style-name="ro1">
      <table:table-cell><text:p><xsl:apply-templates/></text:p></table:table-cell>
    </table:table-row>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-2"><xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy></xsl:template>
  <xsl:template match="text()" priority="-1"><xsl:value-of select="."/></xsl:template>

</xsl:stylesheet>
