<?xml version="1.0" encoding="UTF-8"?>
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
    | After the serializer, result is an OpenOffice Draw document (sxd).
    |
    | @author <a href="mailto:vgritsenko@apache.org>Vadim Gritsenko</a>
    | @version CVS $Id: page2sxd.xsl,v 1.2 2004/03/10 10:18:52 cziegeler Exp $
    +-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:zip="http://apache.org/cocoon/zip-archive/1.0"
                xmlns:text="http://openoffice.org/2000/text">

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
                                 xmlns:presentation="http://openoffice.org/2000/presentation"
                                 xmlns:svg="http://www.w3.org/2000/svg"
                                 xmlns:chart="http://openoffice.org/2000/chart"
                                 xmlns:dr3d="http://openoffice.org/2000/dr3d"
                                 xmlns:math="http://www.w3.org/1998/Math/MathML"
                                 xmlns:form="http://openoffice.org/2000/form"
                                 xmlns:script="http://openoffice.org/2000/script"
                                 office:class="drawing"
                                 office:version="1.0">
          <office:script/>
          <office:automatic-styles>
            <style:style style:name="dp1" style:family="drawing-page"/>
            <style:style style:name="gr1" style:family="graphics" style:parent-style-name="title">
              <style:properties draw:textarea-horizontal-align="left" draw:auto-grow-width="true" draw:auto-grow-height="true" fo:min-height="0cm" fo:min-width="0cm"/>
            </style:style>
            <style:style style:name="gr2" style:family="graphics" style:parent-style-name="standard">
              <style:properties draw:stroke="none" svg:stroke-color="#000000" draw:fill="none" draw:fill-color="#ffffff" draw:textarea-horizontal-align="left" draw:auto-grow-width="true" draw:auto-grow-height="true" fo:min-height="0cm" fo:min-width="0cm"/>
            </style:style>
            <style:style style:name="P1" style:family="paragraph">
              <style:properties fo:margin-left="0cm" fo:margin-right="0cm" fo:text-indent="0cm"/>
            </style:style>
            <text:list-style style:name="L1">
              <text:list-level-style-bullet text:level="1" text:bullet-char="â—?">
                <style:properties fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
              </text:list-level-style-bullet>
              <text:list-level-style-bullet text:level="2" text:bullet-char="â—?">
                <style:properties text:space-before="0.6cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
              </text:list-level-style-bullet>
              <text:list-level-style-bullet text:level="3" text:bullet-char="â—?">
                <style:properties text:space-before="1.2cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
              </text:list-level-style-bullet>
              <text:list-level-style-bullet text:level="4" text:bullet-char="â—?">
                <style:properties text:space-before="1.8cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
              </text:list-level-style-bullet>
              <text:list-level-style-bullet text:level="5" text:bullet-char="â—?">
                <style:properties text:space-before="2.4cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
              </text:list-level-style-bullet>
              <text:list-level-style-bullet text:level="6" text:bullet-char="â—?">
                <style:properties text:space-before="3cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
              </text:list-level-style-bullet>
              <text:list-level-style-bullet text:level="7" text:bullet-char="â—?">
                <style:properties text:space-before="3.6cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
              </text:list-level-style-bullet>
              <text:list-level-style-bullet text:level="8" text:bullet-char="â—?">
                <style:properties text:space-before="4.2cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
              </text:list-level-style-bullet>
              <text:list-level-style-bullet text:level="9" text:bullet-char="â—?">
                <style:properties text:space-before="4.8cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
              </text:list-level-style-bullet>
              <text:list-level-style-bullet text:level="10" text:bullet-char="â—?">
                <style:properties text:space-before="5.4cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
              </text:list-level-style-bullet>
            </text:list-style>
          </office:automatic-styles>
          <office:body>
            <draw:page draw:style-name="dp1" draw:master-page-name="Default"> <!-- draw:name="Hello" -->
              <xsl:attribute namespace="http://openoffice.org/2000/drawing" name="draw:name"><xsl:value-of select="title"/></xsl:attribute>
              <draw:text-box draw:style-name="gr1" draw:text-style-name="P1" draw:layer="layout" svg:width="3.445cm" svg:height="1.717cm" svg:x="8.944cm" svg:y="1.422cm">
                <text:p text:style-name="P1"><xsl:value-of select="title"/></text:p>
              </draw:text-box>
              <draw:text-box draw:style-name="gr2" draw:layer="layout" svg:width="10.142cm" svg:height="0.955cm" svg:x="5.546cm" svg:y="4.019cm">
                <xsl:apply-templates/>
              </draw:text-box>
            </draw:page>
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
                              xmlns:presentation="http://openoffice.org/2000/presentation"
                              office:version="1.0">
          <office:meta>
            <meta:generator>OpenOffice.org 1.0.3 (Win32)</meta:generator>
            <dc:title><xsl:value-of select="title"/></dc:title>
            <dc:subject>Cocoon Hello World Sample Document</dc:subject>
            <meta:creation-date>2003-05-08T08:21:16</meta:creation-date>
            <dc:date>2003-05-08T08:24:29</dc:date>
            <dc:language>en-US</dc:language>
            <meta:editing-cycles>2</meta:editing-cycles>
            <meta:editing-duration>PT3M16S</meta:editing-duration>
            <meta:user-defined meta:name="Info 1"/>
            <meta:user-defined meta:name="Info 2"/>
            <meta:user-defined meta:name="Info 3"/>
            <meta:user-defined meta:name="Info 4"/>
            <meta:document-statistic meta:object-count="6"/>
          </office:meta>
        </office:document-meta>
      </zip:entry>

      <!--
        <!DOCTYPE office:document-settings PUBLIC "-//OpenOffice.org//DTD OfficeDocument 1.0//EN" "office.dtd">
      -->
      <zip:entry name="settings.xml" serializer="xml">
        <office:document-settings xmlns:office="http://openoffice.org/2000/office"
                                  xmlns:xlink="http://www.w3.org/1999/xlink"
                                  xmlns:presentation="http://openoffice.org/2000/presentation"
                                  xmlns:config="http://openoffice.org/2001/config"
                                  office:version="1.0">
          <office:settings>
            <config:config-item-set config:name="view-settings">
              <config:config-item config:name="VisibleAreaTop" config:type="int">-407</config:config-item>
              <config:config-item config:name="VisibleAreaLeft" config:type="int">-13433</config:config-item>
              <config:config-item config:name="VisibleAreaWidth" config:type="int">49253</config:config-item>
              <config:config-item config:name="VisibleAreaHeight" config:type="int">28850</config:config-item>
              <config:config-item-map-indexed config:name="Views">
                <config:config-item-map-entry>
                  <config:config-item config:name="ViewId" config:type="string">view1</config:config-item>
                  <config:config-item config:name="GridIsVisible" config:type="boolean">false</config:config-item>
                  <config:config-item config:name="GridIsFront" config:type="boolean">true</config:config-item>
                  <config:config-item config:name="IsSnapToGrid" config:type="boolean">false</config:config-item>
                  <config:config-item config:name="IsSnapToPageMargins" config:type="boolean">true</config:config-item>
                  <config:config-item config:name="IsSnapToSnapLines" config:type="boolean">true</config:config-item>
                  <config:config-item config:name="IsSnapToObjectFrame" config:type="boolean">false</config:config-item>
                  <config:config-item config:name="IsSnapToObjectPoints" config:type="boolean">false</config:config-item>
                  <config:config-item config:name="IsPlusHandlesAlwaysVisible" config:type="boolean">false</config:config-item>
                  <config:config-item config:name="IsFrameDragSingles" config:type="boolean">true</config:config-item>
                  <config:config-item config:name="EliminatePolyPointLimitAngle" config:type="int">1500</config:config-item>
                  <config:config-item config:name="IsEliminatePolyPoints" config:type="boolean">false</config:config-item>
                  <config:config-item config:name="VisibleLayers" config:type="base64Binary">//////////////////////////////////////////8=</config:config-item>
                  <config:config-item config:name="PrintableLayers" config:type="base64Binary">//////////////////////////////////////////8=</config:config-item>
                  <config:config-item config:name="LockedLayers" config:type="base64Binary"/>
                  <config:config-item config:name="NoAttribs" config:type="boolean">false</config:config-item>
                  <config:config-item config:name="NoColors" config:type="boolean">true</config:config-item>
                  <config:config-item config:name="RulerIsVisible" config:type="boolean">true</config:config-item>
                  <config:config-item config:name="PageKind" config:type="short">0</config:config-item>
                  <config:config-item config:name="SelectedPage" config:type="short">0</config:config-item>
                  <config:config-item config:name="IsLayerMode" config:type="boolean">false</config:config-item>
                  <config:config-item config:name="IsBigHandles" config:type="boolean">false</config:config-item>
                  <config:config-item config:name="IsDoubleClickTextEdit" config:type="boolean">true</config:config-item>
                  <config:config-item config:name="IsClickChangeRotation" config:type="boolean">false</config:config-item>
                  <config:config-item config:name="SlidesPerRow" config:type="short">4</config:config-item>
                  <config:config-item config:name="DrawMode" config:type="int">0</config:config-item>
                  <config:config-item config:name="PreviewDrawMode" config:type="int">0</config:config-item>
                  <config:config-item config:name="IsShowPreviewInPageMode" config:type="boolean">false</config:config-item>
                  <config:config-item config:name="IsShowPreviewInMasterPageMode" config:type="boolean">true</config:config-item>
                  <config:config-item config:name="SetShowPreviewInOutlineMode" config:type="boolean">true</config:config-item>
                  <config:config-item config:name="EditModeStandard" config:type="int">0</config:config-item>
                  <config:config-item config:name="EditModeNotes" config:type="int">0</config:config-item>
                  <config:config-item config:name="EditModeHandout" config:type="int">1</config:config-item>
                  <config:config-item config:name="VisibleAreaTop" config:type="int">-407</config:config-item>
                  <config:config-item config:name="VisibleAreaLeft" config:type="int">-13433</config:config-item>
                  <config:config-item config:name="VisibleAreaWidth" config:type="int">49254</config:config-item>
                  <config:config-item config:name="VisibleAreaHeight" config:type="int">28851</config:config-item>
                  <config:config-item config:name="GridCoarseWidth" config:type="int">1270</config:config-item>
                  <config:config-item config:name="GridCoarseHeight" config:type="int">1270</config:config-item>
                  <config:config-item config:name="GridFineWidth" config:type="int">635</config:config-item>
                  <config:config-item config:name="GridFineHeight" config:type="int">635</config:config-item>
                  <config:config-item config:name="GridSnapWidth" config:type="int">1000</config:config-item>
                  <config:config-item config:name="GridSnapHeight" config:type="int">1000</config:config-item>
                  <config:config-item config:name="GridSnapWidthXNumerator" config:type="int">635</config:config-item>
                  <config:config-item config:name="GridSnapWidthXDenominator" config:type="int">1</config:config-item>
                  <config:config-item config:name="GridSnapWidthYNumerator" config:type="int">635</config:config-item>
                  <config:config-item config:name="GridSnapWidthYDenominator" config:type="int">1</config:config-item>
                  <config:config-item config:name="IsAngleSnapEnabled" config:type="boolean">false</config:config-item>
                  <config:config-item config:name="SnapAngle" config:type="int">1500</config:config-item>
                  <config:config-item config:name="ZoomOnPage" config:type="boolean">true</config:config-item>
                </config:config-item-map-entry>
              </config:config-item-map-indexed>
            </config:config-item-set>
            <config:config-item-set config:name="configuration-settings">
              <config:config-item config:name="ApplyUserData" config:type="boolean">true</config:config-item>
              <config:config-item config:name="BitmapTableURL" config:type="string">file:///C:/Program%20Files/OpenOffice.org1.0.3/user/config/standard.sob</config:config-item>
              <config:config-item config:name="CharacterCompressionType" config:type="short">0</config:config-item>
              <config:config-item config:name="ColorTableURL" config:type="string">file:///C:/Program%20Files/OpenOffice.org1.0.3/user/config/standard.soc</config:config-item>
              <config:config-item config:name="DashTableURL" config:type="string">file:///C:/Program%20Files/OpenOffice.org1.0.3/user/config/standard.sod</config:config-item>
              <config:config-item config:name="DefaultTabStop" config:type="int">1270</config:config-item>
              <config:config-item-map-indexed config:name="ForbiddenCharacters">
                <config:config-item-map-entry>
                  <config:config-item config:name="Language" config:type="string">en</config:config-item>
                  <config:config-item config:name="Country" config:type="string">US</config:config-item>
                  <config:config-item config:name="Variant" config:type="string"/>
                  <config:config-item config:name="BeginLine" config:type="string"/>
                  <config:config-item config:name="EndLine" config:type="string"/>
                </config:config-item-map-entry>
              </config:config-item-map-indexed>
              <config:config-item config:name="GradientTableURL" config:type="string">file:///C:/Program%20Files/OpenOffice.org1.0.3/user/config/standard.sog</config:config-item>
              <config:config-item config:name="HatchTableURL" config:type="string">file:///C:/Program%20Files/OpenOffice.org1.0.3/user/config/standard.soh</config:config-item>
              <config:config-item config:name="IsKernAsianPunctuation" config:type="boolean">false</config:config-item>
              <config:config-item config:name="IsPrintBooklet" config:type="boolean">false</config:config-item>
              <config:config-item config:name="IsPrintBookletBack" config:type="boolean">true</config:config-item>
              <config:config-item config:name="IsPrintBookletFront" config:type="boolean">true</config:config-item>
              <config:config-item config:name="IsPrintDate" config:type="boolean">false</config:config-item>
              <config:config-item config:name="IsPrintFitPage" config:type="boolean">false</config:config-item>
              <config:config-item config:name="IsPrintHiddenPages" config:type="boolean">true</config:config-item>
              <config:config-item config:name="IsPrintPageName" config:type="boolean">false</config:config-item>
              <config:config-item config:name="IsPrintTilePage" config:type="boolean">false</config:config-item>
              <config:config-item config:name="IsPrintTime" config:type="boolean">false</config:config-item>
              <config:config-item config:name="LineEndTableURL" config:type="string">file:///C:/Program%20Files/OpenOffice.org1.0.3/user/config/standard.soe</config:config-item>
              <config:config-item config:name="MeasureUnit" config:type="short">7</config:config-item>
              <config:config-item config:name="PageNumberFormat" config:type="int">4</config:config-item>
              <config:config-item config:name="ParagraphSummation" config:type="boolean">false</config:config-item>
              <config:config-item config:name="PrintQuality" config:type="int">0</config:config-item>
              <config:config-item config:name="PrinterName" config:type="string">\\vgritsenkopc2\HP LaserJet 5L</config:config-item>
              <config:config-item config:name="PrinterSetup" config:type="base64Binary">kgP+/1xcdmdyaXRzZW5rb3BjMlxIUCBMYXNlckpldCA1TAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAd2luc3Bvb2wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAWAAEA2AIAAAAAAAAFAAhSAAAEdAAAM1ROVwEACABcXHZncml0c2Vua29wYzJcSFAgTGFzZXJKZXQgNUwAAAEEAAWcADQCQ++ABQEAAQCaCzQIZAABAA8AWAIBAAEAWAIDAAEAQTQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEAAAAAAAAAAQAAAAIAAAABAAAA/////wAAAAAAAAAAAAAAAAAAAABESU5VIgAAADQCAACjWpOwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</config:config-item>
              <config:config-item config:name="ScaleDenominator" config:type="int">1</config:config-item>
              <config:config-item config:name="ScaleNumerator" config:type="int">1</config:config-item>
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
                                xmlns:presentation="http://openoffice.org/2000/presentation"
                                xmlns:svg="http://www.w3.org/2000/svg"
                                xmlns:chart="http://openoffice.org/2000/chart"
                                xmlns:dr3d="http://openoffice.org/2000/dr3d"
                                xmlns:math="http://www.w3.org/1998/Math/MathML"
                                xmlns:form="http://openoffice.org/2000/form"
                                xmlns:script="http://openoffice.org/2000/script"
                                office:version="1.0">
          <office:styles>
            <draw:marker draw:name="Arrow" svg:viewBox="0 0 20 30" svg:d="m10 0-10 30h20z"/>
            <style:default-style style:family="graphics">
              <style:properties fo:color="#000000" fo:font-family="Thorndale" style:font-family-generic="roman" style:font-pitch="variable" fo:font-size="24pt" fo:language="en" fo:country="US" style:font-family-asian="&apos;HG Mincho Light J&apos;" style:font-pitch-asian="variable" style:font-size-asian="24pt" style:language-asian="none" style:country-asian="none" style:font-family-complex="&apos;Arial Unicode MS&apos;" style:font-pitch-complex="variable" style:font-size-complex="24pt" style:language-complex="none" style:country-complex="none" style:text-autospace="ideograph-alpha" style:punctuation-wrap="simple" style:line-break="strict">
                <style:tab-stops/>
              </style:properties>
            </style:default-style>
            <style:style style:name="standard" style:family="graphics">
              <style:properties draw:stroke="solid" svg:stroke-width="0cm" svg:stroke-color="#000000" draw:marker-start-width="0.3cm" draw:marker-start-center="false" draw:marker-end-width="0.3cm" draw:marker-end-center="false" draw:fill="solid" draw:fill-color="#00b8ff" draw:shadow="hidden" draw:shadow-offset-x="0.3cm" draw:shadow-offset-y="0.3cm" draw:shadow-color="#808080" fo:margin-left="0cm" fo:margin-right="0cm" fo:margin-top="0cm" fo:margin-bottom="0cm" fo:color="#000000" style:text-outline="false" style:text-crossing-out="none" fo:font-family="Thorndale" style:font-family-generic="roman" style:font-pitch="variable" fo:font-size="24pt" fo:font-style="normal" fo:text-shadow="none" style:text-underline="none" fo:font-weight="normal" style:font-family-asian="&apos;HG Mincho Light J&apos;" style:font-pitch-asian="variable" style:font-size-asian="24pt" style:font-style-asian="normal" style:font-weight-asian="normal" style:font-family-complex="&apos;Arial Unicode MS&apos;" style:font-pitch-complex="variable" style:font-size-complex="24pt" style:font-style-complex="normal" style:font-weight-complex="normal" style:text-emphasize="none" style:font-relief="none" fo:line-height="100%" fo:text-align="start" text:enable-numbering="false" fo:text-indent="0cm">
                <text:list-style>
                  <text:list-level-style-bullet text:level="1" text:bullet-char="â—?">
                    <style:properties fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="2" text:bullet-char="â—?">
                    <style:properties text:space-before="0.6cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="3" text:bullet-char="â—?">
                    <style:properties text:space-before="1.2cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="4" text:bullet-char="â—?">
                    <style:properties text:space-before="1.8cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="5" text:bullet-char="â—?">
                    <style:properties text:space-before="2.4cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="6" text:bullet-char="â—?">
                    <style:properties text:space-before="3cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="7" text:bullet-char="â—?">
                    <style:properties text:space-before="3.6cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="8" text:bullet-char="â—?">
                    <style:properties text:space-before="4.2cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="9" text:bullet-char="â—?">
                    <style:properties text:space-before="4.8cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="10" text:bullet-char="â—?">
                    <style:properties text:space-before="5.4cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                </text:list-style>
              </style:properties>
            </style:style>
            <style:style style:name="objectwitharrow" style:family="graphics" style:parent-style-name="standard">
              <style:properties draw:stroke="solid" svg:stroke-width="0.15cm" svg:stroke-color="#000000" draw:marker-start="Arrow" draw:marker-start-width="0.7cm" draw:marker-start-center="true" draw:marker-end-width="0.3cm"/>
            </style:style>
            <style:style style:name="objectwithshadow" style:family="graphics" style:parent-style-name="standard">
              <style:properties draw:shadow="visible" draw:shadow-offset-x="0.3cm" draw:shadow-offset-y="0.3cm" draw:shadow-color="#808080"/>
            </style:style>
            <style:style style:name="objectwithoutfill" style:family="graphics" style:parent-style-name="standard">
              <style:properties draw:fill="none"/>
            </style:style>
            <style:style style:name="text" style:family="graphics" style:parent-style-name="standard">
              <style:properties draw:stroke="none" draw:fill="none"/>
            </style:style>
            <style:style style:name="textbody" style:family="graphics" style:parent-style-name="standard">
              <style:properties draw:stroke="none" draw:fill="none" fo:font-size="16pt"/>
            </style:style>
            <style:style style:name="textbodyjustfied" style:family="graphics" style:parent-style-name="standard">
              <style:properties draw:stroke="none" draw:fill="none" fo:text-align="justify"/>
            </style:style>
            <style:style style:name="textbodyindent" style:family="graphics" style:parent-style-name="standard">
              <style:properties draw:stroke="none" draw:fill="none" fo:margin-left="0cm" fo:margin-right="0cm" fo:text-indent="0.6cm">
                <text:list-style>
                  <text:list-level-style-bullet text:level="1" text:bullet-char="â—?">
                    <style:properties text:space-before="0.6cm" text:min-label-width="-0.6cm" text:min-label-distance="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="2" text:bullet-char="â—?">
                    <style:properties text:space-before="0.6cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="3" text:bullet-char="â—?">
                    <style:properties text:space-before="1.2cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="4" text:bullet-char="â—?">
                    <style:properties text:space-before="1.8cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="5" text:bullet-char="â—?">
                    <style:properties text:space-before="2.4cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="6" text:bullet-char="â—?">
                    <style:properties text:space-before="3cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="7" text:bullet-char="â—?">
                    <style:properties text:space-before="3.6cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="8" text:bullet-char="â—?">
                    <style:properties text:space-before="4.2cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="9" text:bullet-char="â—?">
                    <style:properties text:space-before="4.8cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="10" text:bullet-char="â—?">
                    <style:properties text:space-before="5.4cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                </text:list-style>
              </style:properties>
            </style:style>
            <style:style style:name="title" style:family="graphics" style:parent-style-name="standard">
              <style:properties draw:stroke="none" draw:fill="none" fo:font-size="44pt"/>
            </style:style>
            <style:style style:name="title1" style:family="graphics" style:parent-style-name="standard">
              <style:properties draw:stroke="none" draw:fill="solid" draw:fill-color="#008080" draw:shadow="visible" draw:shadow-offset-x="0.2cm" draw:shadow-offset-y="0.2cm" draw:shadow-color="#808080" fo:font-size="24pt" fo:text-align="center"/>
            </style:style>
            <style:style style:name="title2" style:family="graphics" style:parent-style-name="standard">
              <style:properties svg:stroke-width="0.05cm" draw:fill-color="#ffcc99" draw:shadow="visible" draw:shadow-offset-x="0.2cm" draw:shadow-offset-y="0.2cm" draw:shadow-color="#808080" fo:margin-left="0.2cm" fo:margin-right="0.2cm" fo:margin-top="0.1cm" fo:margin-bottom="0.1cm" fo:font-size="36pt" fo:text-align="center" fo:text-indent="0cm">
                <text:list-style>
                  <text:list-level-style-bullet text:level="1" text:bullet-char="â—?">
                    <style:properties text:space-before="0.2cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="2" text:bullet-char="â—?">
                    <style:properties text:space-before="0.6cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="3" text:bullet-char="â—?">
                    <style:properties text:space-before="1.2cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="4" text:bullet-char="â—?">
                    <style:properties text:space-before="1.8cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="5" text:bullet-char="â—?">
                    <style:properties text:space-before="2.4cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="6" text:bullet-char="â—?">
                    <style:properties text:space-before="3cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="7" text:bullet-char="â—?">
                    <style:properties text:space-before="3.6cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="8" text:bullet-char="â—?">
                    <style:properties text:space-before="4.2cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="9" text:bullet-char="â—?">
                    <style:properties text:space-before="4.8cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                  <text:list-level-style-bullet text:level="10" text:bullet-char="â—?">
                    <style:properties text:space-before="5.4cm" text:min-label-width="0.6cm" fo:font-family="StarSymbol" fo:color="#000000" fo:font-size="45%"/>
                  </text:list-level-style-bullet>
                </text:list-style>
              </style:properties>
            </style:style>
            <style:style style:name="headline" style:family="graphics" style:parent-style-name="standard">
              <style:properties draw:stroke="none" draw:fill="none" fo:margin-top="0.42cm" fo:margin-bottom="0.21cm" fo:font-size="24pt"/>
            </style:style>
            <style:style style:name="headline1" style:family="graphics" style:parent-style-name="standard">
              <style:properties draw:stroke="none" draw:fill="none" fo:margin-top="0.42cm" fo:margin-bottom="0.21cm" fo:font-size="18pt" fo:font-weight="bold"/>
            </style:style>
            <style:style style:name="headline2" style:family="graphics" style:parent-style-name="standard">
              <style:properties draw:stroke="none" draw:fill="none" fo:margin-top="0.42cm" fo:margin-bottom="0.21cm" fo:font-size="14pt" fo:font-style="italic" fo:font-weight="bold"/>
            </style:style>
            <style:style style:name="measure" style:family="graphics" style:parent-style-name="standard">
              <style:properties draw:stroke="solid" draw:marker-start="Arrow" draw:marker-start-width="0.2cm" draw:marker-end="Arrow" draw:marker-end-width="0.2cm" draw:fill="none" fo:font-size="12pt"/>
            </style:style>
          </office:styles>
          <office:automatic-styles>
            <style:page-master style:name="PM0">
              <style:properties fo:margin-top="2cm" fo:margin-bottom="2cm" fo:margin-left="2cm" fo:margin-right="2cm" fo:page-width="27.94cm" fo:page-height="21.59cm" style:print-orientation="landscape"/>
            </style:page-master>
            <style:page-master style:name="PM1">
              <style:properties fo:margin-top="0.423cm" fo:margin-bottom="0.454cm" fo:margin-left="0.635cm" fo:margin-right="0.665cm" fo:page-width="21.59cm" fo:page-height="27.94cm" style:print-orientation="portrait"/>
            </style:page-master>
            <style:style style:name="dp1" style:family="drawing-page">
              <style:properties draw:background-size="border" draw:fill="none"/>
            </style:style>
          </office:automatic-styles>
          <office:master-styles>
            <draw:layer-set>
              <draw:layer draw:name="layout"/>
              <draw:layer draw:name="background"/>
              <draw:layer draw:name="backgroundobjects"/>
              <draw:layer draw:name="controls"/>
              <draw:layer draw:name="measurelines"/>
            </draw:layer-set>
            <style:master-page style:name="Default" style:page-master-name="PM1" draw:style-name="dp1"/>
          </office:master-styles>
        </office:document-styles>
      </zip:entry>

      <!--
        <!DOCTYPE manifest:manifest PUBLIC "-//OpenOffice.org//DTD Manifest 1.0//EN" "Manifest.dtd">
      -->
      <zip:entry name="META-INF/manifest.xml" serializer="xml">
        <manifest:manifest xmlns:manifest="http://openoffice.org/2001/manifest">
          <manifest:file-entry manifest:media-type="application/vnd.sun.xml.draw" manifest:full-path="/"/>
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
    <text:p text:style-name="P1"><xsl:apply-templates/></text:p>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-2"><xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy></xsl:template>
  <xsl:template match="text()" priority="-1"><xsl:value-of select="."/></xsl:template>

</xsl:stylesheet>
