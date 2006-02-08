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

<!-- generates blocks.properties from gump.xml -->
<xsl:output method="text"/>

<xsl:key name="status" match="project[starts-with(@name, 'cocoon-block-')]" use="@status"/>
<xsl:key name="dependency" match="project[starts-with(@name, 'cocoon-block-')]/depend[starts-with(@project, 'cocoon-block-')]" use="@project"/>

<desc:descs>

<desc:desc name="license">
#  Copyright 1999-2005 The Apache Software Foundation
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
</desc:desc>

<desc:desc name="do-not-edit">
#------------------------------------------------------------------------------#
# ***** DO  NOT edit blocks.properties yourself! ********                      #
# This file is generated from gump.xml - to keep it in sync when that file is  #
# modified, use the generate-blocks.properties build target.                   #
#------------------------------------------------------------------------------#
</desc:desc>

<desc:desc name="common">
#------------------------------------------------------------------------------#
#                             Cocoon Blocks                                    #
#------------------------------------------------------------------------------#

# Remove blocks from your cocoon distribution by setting the corresponding
# include property to true or false. The blocks are included by default, i.e. if
# no property was set.

# NOTE: Don't modify this file directly but make a copy named
# 'local.blocks.properties' and modify that. The build system will first load
# 'local.blocks.properties' and properties are immutable in Ant.

# For most cases it is enough that you exclude all blocks and include only those
# few you want, example:
# exclude.all.blocks=true
# include.block.forms=true
# include.block.template=true

# The opposite is also allowed:
# include.all.blocks=true
# exclude.block.scratchpad=true

# If there is a conflict on the same level of granularity:
# include.block.template=true vs. exclude.block.template=true, 
# include.all.blocks=true vs. exclude.all.blocks=true
# it is always resolved in favour of include.* properties. 

# NOTE: "[dependency]" indicates blocks that are required by other blocks.
# Disabling batik, for example, will result in a RuntimeException when using
# fop. Dependencies only needed for the block's samples are marked explicitely.
# This latter information was introduced only short time ago, so do not expect
# it to be complete.

# NOTE: (to Cocoon committers): blocks.properties is generated from gump.xml
# using "build generate-blocks.properties". Any changes to blocks definitions
# must be made in gump.xml, not here.

# All blocks -------------------------------------------------------------------

# Use this property to exclude all blocks at once
# exclude.all.blocks=true

# Use this property to include all blocks at once
# include.all.blocks=true

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
# in favour of other blocks and therefore are excluded by default from the build.
# For including one of them you have to set the exclude property into comment in
# blocks.properties.

</desc:desc>

</desc:descs>

<xsl:template match="/module">
    <xsl:value-of select="document('')/xsl:stylesheet/desc:descs/desc:desc[@name = 'license']"/>
    <xsl:value-of select="document('')/xsl:stylesheet/desc:descs/desc:desc[@name = 'do-not-edit']"/>
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
    <xsl:text>include.block.</xsl:text>
    <xsl:value-of select="substring-after(@name, 'cocoon-block-')"/>
    <xsl:text>=false&#10;</xsl:text>
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
    <xsl:if test="@type='samples'"> (for samples)</xsl:if>
    <xsl:choose>
        <xsl:when test="position() = last()">.&#10;</xsl:when>
        <xsl:otherwise>,</xsl:otherwise>
    </xsl:choose>
</xsl:template>

</xsl:stylesheet>
