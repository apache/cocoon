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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:desc="description">

<xsl:output method="text"/>

<xsl:key name="status" match="project[starts-with(@name, 'cocoon-block-')]" use="@status"/>
<xsl:key name="dependency" match="project[starts-with(@name, 'cocoon-block-')]/depend[starts-with(@project, 'cocoon-block-')]" use="@project"/>

<desc:descs>

<desc:desc name="common">
#------------------------------------------------------------------------------#
#                             Cocoon Blocks                                    #
#------------------------------------------------------------------------------#

# Remove blocks from your cocoon distribution by uncommenting the
# corresponding exclude property.

# NOTE: Don't modify this file directly but make a copy named
# 'local.blocks.properties' and modify that. The build system will override
# these properties with the ones in the 'local.blocks.properties' file.

# NOTE: "[dependency]" indicates blocks that are required by other blocks.
# Disabling batik, for example, will result in a RuntimeException when using
# fop. On the other hand some dependencies come only from the block samples.

</desc:desc>

<desc:desc name="stable">
# Stable blocks ----------------------------------------------------------------

# Stable blocks are those that can be considered ready for production and
# will contain components and API that will remain stable and where
# developers are committed to back compatibility. In short, stuff that you
# can depend on.

</desc:desc>

<desc:desc name="unstable">
# Unstable blocks --------------------------------------------------------------

# Unstable blocks are currently under development and do not guarantee that the
# contracts they expose (API, xml schema, properties, behavior) will remain
# constant in time. Developers are not committed to back-compatibility just yet.
# This doesn't necessarily mean the blocks implementation is unstable or
# the code can't be trusted for production, but use with care and watch
# its development as things might change over time before they are marked
# stable.

</desc:desc>

<desc:desc name="deprecated">
# Deprecated blocks ------------------------------------------------------------

# Although some of these blocks may have been stable, they are now deprecated
# in favour of other blocks and therefore are excluded by default from the build

</desc:desc>

</desc:descs>

<xsl:template match="/module">
    <xsl:value-of select="document('')/xsl:stylesheet/desc:descs/desc:desc[@name = 'common']"/>
    <xsl:apply-templates
        select="project[starts-with(@name, 'cocoon-block-')]
                       [count(. | key('status', @status)[1]) = 1]"
        mode="group"/>
</xsl:template>

<xsl:template match="project" mode="group">
    <xsl:value-of select="document('')/xsl:stylesheet/desc:descs/desc:desc[@name = current()/@status]"/>
    <!-- unfortunately key('status', @status) does not work with sorting because of a bug in Xalan: 24583 -->
    <xsl:apply-templates select="../project[starts-with(@name, 'cocoon-block-')][@status = current()/@status]">
        <xsl:sort select="@name"/>
    </xsl:apply-templates>
</xsl:template>

<xsl:template match="project">
    <xsl:call-template name="dependency">
        <xsl:with-param name="elements" select="depend[starts-with(@project, 'cocoon-block-')]"/>
        <xsl:with-param name="text" select="'depends on'"/>
    </xsl:call-template>

    <xsl:call-template name="dependency">
        <xsl:with-param name="elements" select="key('dependency', @name)/.."/>
        <xsl:with-param name="text" select="'is needed by'"/>
    </xsl:call-template>

    <!-- TODO: make this configurable externally (dependent on @status or @name) -->
    <xsl:if test="not(@status='deprecated' or @exclude='true')">#</xsl:if>
    <xsl:text>exclude.block.</xsl:text>
    <xsl:value-of select="substring-after(@name, 'cocoon-block-')"/>
    <xsl:text>=true&#10;</xsl:text>
</xsl:template>

<xsl:template name="dependency">
    <xsl:param name="elements" select="/.."/>
    <xsl:param name="text" select="''"/>
    <xsl:if test="$elements">
        <xsl:text>#-----[dependency]: "</xsl:text>
        <xsl:value-of select="substring-after(@name, 'cocoon-block-')"/>
        <xsl:text>" </xsl:text>
        <xsl:value-of select="$text"/>
        <xsl:apply-templates select="$elements" mode="dependency">
            <xsl:sort select="@name | @project"/>
        </xsl:apply-templates>
    </xsl:if>
</xsl:template>

<xsl:template match="project | depend" mode="dependency">
    <xsl:text> "</xsl:text>
    <xsl:value-of select="substring-after(concat(@name, @project), 'cocoon-block-')"/>
    <xsl:text>"</xsl:text>
    <xsl:choose>
        <xsl:when test="position() = last()">.&#10;</xsl:when>
        <xsl:otherwise>,</xsl:otherwise>
    </xsl:choose>
</xsl:template>

</xsl:stylesheet>
