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
    | CVS $Id: gump2samples.xsl,v 1.1 2004/04/01 19:05:42 vgritsenko Exp $
    +-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/">
    <samples name="Blocks Samples">
      <group name="Back">
        <sample href="./" name="Back">
          Back to the samples home page.
        </sample>
      </group>

      <group name="Stable Blocks">
        <xsl:apply-templates select="module/project[starts-with(@name, 'cocoon-block-')][@status = 'stable']">
          <xsl:sort select="@name"/>
        </xsl:apply-templates>
      </group>

      <xsl:variable name="unstable" select="module/project[starts-with(@name, 'cocoon-block-')][@status = 'unstable']"/>
      <xsl:if test="$unstable">
        <group name="Unstable Blocks">
          <note>
            Blocks below are subject to change without notice!
          </note>
          <xsl:apply-templates select="$unstable">
            <xsl:sort select="@name"/>
          </xsl:apply-templates>
        </group>
      </xsl:if>

      <xsl:variable name="deprecated" select="module/project[starts-with(@name, 'cocoon-block-')][@status = 'deprecated']"/>
      <xsl:if test="$deprecated">
        <group name="Deprecated Blocks">
          <note>
            Blocks below will be removed in the future!
          </note>
          <xsl:apply-templates select="$deprecated">
            <xsl:sort select="@name"/>
          </xsl:apply-templates>
        </group>
      </xsl:if>

      <xsl:variable name="excluded" select="module/project[starts-with(@name, 'cocoon-block-')][not(document(concat(substring-after(@name,'cocoon-block-'), '/', substring-after(@name,'cocoon-block-'),'.xsamples')))]"/>
      <xsl:if test="$excluded">
        <group name="Excluded Blocks">
          <note>
            Blocks below are either excluded from the build or has no samples.
          </note>
          <xsl:apply-templates select="$excluded" mode="excluded">
            <xsl:sort select="@name"/>
          </xsl:apply-templates>
        </group>
      </xsl:if>
    </samples>
  </xsl:template>

  <xsl:template match="project">
    <xsl:variable name="name" select="substring-after(@name,'cocoon-block-')"/>
    <xsl:variable name="sample" select="document(concat($name, '/', $name,'.xsamples'))/xsamples/group/sample"/>
    <xsl:choose>
      <xsl:when test="$sample">
        <xsl:apply-templates select="$sample"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- Output nothing for excluded blocks -->
      </xsl:otherwise>
    </xsl:choose>
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
