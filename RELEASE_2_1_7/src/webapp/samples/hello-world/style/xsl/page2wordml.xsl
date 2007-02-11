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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:w="http://schemas.microsoft.com/office/word/2003/wordml" xmlns:wx="http://schemas.microsoft.com/office/word/2003/auxHint">

  <xsl:template match="page">
    <w:wordDocument>
      <w:styles>
        <w:style w:type="paragraph" w:styleId="h1"><w:name w:val="heading 1"/><wx:uiName wx:val="Heading 1"/><w:basedOn w:val="Standard"/><w:next w:val="Standard"/><w:rsid w:val="006217F2"/><w:pPr><w:pStyle w:val="h1"/><w:keepNext/><w:spacing w:before="240" w:after="60"/><w:outlineLvl w:val="0"/></w:pPr><w:rPr><w:rFonts w:ascii="Arial" w:h-ansi="Arial" w:cs="Arial"/><wx:font wx:val="Arial"/><w:b/><w:b-cs/><w:kern w:val="32"/><w:sz w:val="32"/><w:sz-cs w:val="32"/></w:rPr></w:style>
      </w:styles>
      <w:body>
        <wx:sect>
          <wx:sub-section>
            <xsl:apply-templates/>
          </wx:sub-section>
        </wx:sect>
      </w:body>
    </w:wordDocument>
  </xsl:template>

  <xsl:template match="title">
    <w:p>
      <w:pPr><w:pStyle w:val="h1"/></w:pPr>
      <w:r><w:t><xsl:apply-templates/></w:t></w:r>
    </w:p>
  </xsl:template>

  <xsl:template match="content">
   <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="para">
   <w:p>
      <w:r>
         <w:t><xsl:apply-templates/></w:t>
      </w:r>
   </w:p>
  </xsl:template>


  <xsl:template match="@*|node()" priority="-2"><xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy></xsl:template>
  <xsl:template match="text()" priority="-1"><xsl:value-of select="."/></xsl:template>

</xsl:stylesheet>
