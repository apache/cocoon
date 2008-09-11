<?xml version="1.0"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!--

  This stylesheet is used to implement some of the YSlow recommendations
  namely :
    css at the start
    javascript at the end
    
  also:
    filters out duplicate dojo.require statements
    Remove (those lovely) namespace declarations
  
  it seems to speed up browser rendering 


  NOTE: introduced in 2.1.12
 
  @version $Id$
  
-->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:bu="http://apache.org/cocoon/browser-update/1.0"
  xmlns:fi="http://apache.org/cocoon/forms/1.0#instance"
  xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
  xmlns:jx="http://apache.org/cocoon/templates/jx/1.0"
  xmlns:ft="http://apache.org/cocoon/forms/1.0#template"
  exclude-result-prefixes="bu fi ft i18n jx"
 >

<!-- 

  NB. This XSLT must only be applied to full-page views, never to Ajax (BrowserUpdateHandler) responses
  During an Ajax response new Widgets may be introduced at any time, so the inline dojo.require stetements are much required 

-->

  <!-- using keys because the tags we are looking for are scattered throughout the document and this is supposed to be more efficient than // -->
  <xsl:key name="styles" match="style" use="@type"/><!-- all styles, keyed by @type, so they can be collected -->
  <xsl:key name="scripts" match="script[@type='text/javascript']" use="@type"/><!-- all scripts (excluding Template dijit.Declarations), keyed by @type, so they can be collected -->
  <xsl:key name="scriptsrc" match="script[not(@src)]" use="text()"/><!-- scripts keyed by their source (weird but true) used for outputting unique dojo.require statements etc. (in xslt, please only use one require statement per script tag) -->
  <!--<xsl:key name="widgets" match="*[@id and @dojoType]" use="'dojowidget'"/> removed because the searchIds enhancement is not working -->

  <xsl:template match="head">
    <xsl:copy>
      <xsl:apply-templates select="link" mode="copy-special"/>
      <xsl:apply-templates select="key('styles', 'text/css')" mode="copy-special"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="body">
    <xsl:copy>
      <xsl:apply-templates select="@*|*"/>
      
      <!-- Parsing Widgets via a list of IDs was recommended for performance enhancements, but does not seem to work anymore -->
      <!--<script type="text/javascript">
        if (typeof djConfig === "undefined") djConfig = {};
        djConfig.parseWidgets = false;
        djConfig.searchIds = [<xsl:for-each select="key('widgets','dojowidget')">'<xsl:value-of select="@id"/>',</xsl:for-each>]
      </script>-->
      
<!-- output scripts that load files -->
<xsl:apply-templates select="key('scripts', 'text/javascript')[@src]" mode="copy-special"/>
      
      <!--
      
        TODO: 
              I'd prefer if user scripts came after the require scripts output by the xslt
                    
              cope with widgets delared in the template 
                move */@dojoType="dijit.Declaration" to end?
                or leave their scripts in place ?
              TODO: test this
      -->
      
      
      
<!-- output unique scripts in one script tag -->
<script type="text/javascript"><xsl:comment>
<xsl:text>
</xsl:text>
<xsl:for-each select="key('scripts', 'text/javascript')[not(@src)]"><!-- for each source-bearing script -->
<xsl:variable name="s" select="key('scriptsrc', ./text())"/><!-- the scripts with this unique source -->
<xsl:if test="generate-id(.) = generate-id($s)"><!-- only get the first of each unique script -->
<xsl:value-of select="normalize-space($s/text())"/><xsl:text><!-- very aggresive: compress each in-page <script> into one line, you'd better not have any '//' comments in your scripts (use /* comment */ style) -->
</xsl:text>
</xsl:if>
</xsl:for-each>
//</xsl:comment></script>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="script[@type='text/javascript']|style|link"/><!-- don't output twice -->

  <!-- special treatment for djConfig, keep it in head (TODO: not used?) -->
  <xsl:template match="script[@id='djConfig']">
    <script type="text/javascript"><xsl:comment>
<xsl:text>
</xsl:text>
<xsl:copy-of select="normalize-space(text())"/><xsl:text><!-- very aggresive: compress each in-page <script> into one line, you'd better not have any '//' comments in your scripts (use /* comment */ style) -->
</xsl:text>
//</xsl:comment></script>
  </xsl:template>

  <xsl:template match="style" mode="copy-special">
    <xsl:element name="{local-name()}"><!-- strip namespace declarations -->
      <xsl:copy-of select="@*"/>
      <xsl:copy-of select="normalize-space(text())"/><!-- very aggresive: compress each in-page <style> into one line -->
    </xsl:element>
  </xsl:template>

  <!-- strip namespaces -->
  <xsl:template match="*" mode="copy-special"><!-- TODO: Do I need this one? -->
    <xsl:element name="{local-name()}"><!-- strip namespace declarations -->
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
  <xsl:template match="*">
    <xsl:element name="{local-name()}"><!-- strip namespace declarations -->
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>  
  </xsl:template>
  
  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>


</xsl:stylesheet>
