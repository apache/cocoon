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
    | Convert Gump descriptor with Cocoon blocks to the Blocks Samples page
    |
    | CVS $Id: gump2samples.xsl,v 1.4 2004/05/01 00:51:21 joerg Exp $
    +-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/root">
    <samples name="Blocks Samples">
      <group name="Back">
        <sample href=".." name="Back">
          Back to the samples home page.
        </sample>
      </group>
      <xsl:variable name="xsamples" select="xsamples/sample"/>
      <xsl:variable name="blocks" select="gump/module/project[starts-with(@name, 'cocoon-block-')]"/>
      <xsl:variable name="includedBlocks" select="$blocks[@name = $xsamples/@name]"/>
      <xsl:variable name="excludedBlocks" select="$blocks[not(@name = $xsamples/@name)]"/>
      <xsl:variable name="includedStableBlocks" select="$includedBlocks[@status = 'stable']"/>
      <xsl:variable name="includedUnstableBlocks" select="$includedBlocks[@status = 'unstable']"/>
      <xsl:variable name="includedDeprecatedBlocks" select="$includedBlocks[@status = 'deprecated']"/>

      <xsl:if test="$includedStableBlocks">
        <group name="Stable Blocks">
          <xsl:apply-templates select="$includedStableBlocks">
            <xsl:sort select="@name"/>
          </xsl:apply-templates>
        </group>
      </xsl:if>

      <xsl:if test="$includedUnstableBlocks">
        <group name="Unstable Blocks">
          <note>
            Blocks below are subject to change without notice!
          </note>
          <xsl:apply-templates select="$includedUnstableBlocks">
            <xsl:sort select="@name"/>
          </xsl:apply-templates>
        </group>
      </xsl:if>

      <xsl:if test="$includedDeprecatedBlocks">
        <group name="Deprecated Blocks">
          <note>
            Blocks below will be removed in the future!
          </note>
          <xsl:apply-templates select="$includedDeprecatedBlocks">
            <xsl:sort select="@name"/>
          </xsl:apply-templates>
        </group>
      </xsl:if>

      <xsl:if test="$excludedBlocks">
        <group name="Excluded Blocks">
          <note>
            Blocks below are either excluded from the build or have no samples.
          </note>
          <xsl:apply-templates select="$excludedBlocks" mode="excluded">
            <xsl:sort select="@name"/>
          </xsl:apply-templates>
        </group>
      </xsl:if>
    </samples>
  </xsl:template>

  <xsl:template match="project">
    <xsl:variable name="name" select="substring-after(@name,'cocoon-block-')"/>
    <xsl:variable name="sample" select="document(concat($name, '/', $name,'.xsamples'))/xsamples/group/sample"/>
    <xsl:if test="$sample">
      <xsl:apply-templates select="$sample"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="project" mode="excluded">
    <xsl:variable name="name" select="substring-after(@name,'cocoon-block-')"/>
    <sample name="{$name}">
      <strong>Note:</strong> Block has no samples or was excluded from the build.
    </sample>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
