<?xml version="1.0" encoding="ISO-8859-1"?>
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

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dir="http://apache.org/cocoon/directory/2.0">

  <xsl:template match="dir:directory">
    <samples name="ASCII Art Samples" xmlns:xlink="http://www.w3.org/1999/xlink">
      <group name="Main examples page.">
        <sample name="Back" href="..">to Cocoon examples main page</sample>
      </group>

      <group name="Available ascii art TXT files">
        <xsl:apply-templates select="dir:file" mode="txt"/>
      </group>

      <group name="ascii art in JPEG">
        <xsl:apply-templates select="dir:file" mode="jpg"/>
      </group>

      <group name="ascii art in PNG">
        <xsl:apply-templates select="dir:file" mode="png"/>
      </group>
    </samples>
  </xsl:template>

  <xsl:template match="dir:file" mode="txt">
    <sample name="{@name}" href="{@name}">as text</sample>
  </xsl:template>

  <xsl:template match="dir:file" mode="jpg">
    <sample name="{@name}" href="{@name}.jpg">as jpg</sample>
  </xsl:template>

  <xsl:template match="dir:file" mode="png">
    <sample name="{@name}" href="{@name}.png">as png</sample>
  </xsl:template>
</xsl:stylesheet>
