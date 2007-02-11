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

<!--
    Convert the slop parser output to a collection of slides
    $Id: filter-slop-output.xsl,v 1.8 2004/03/06 02:26:04 antonio Exp $
-->
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:slop="http://apache.org/cocoon/slop/parser/1.0"
>

    <!-- prefix for presentation hints in slides -->
    <xsl:variable name="hintPrefix" select="'hint-'"/>

    <!-- key based on last preceding slide element, used to group slides content -->
    <xsl:key
        name="lastSlideKey"
        match="slop:empty-line[not(self::slop:slide)]" use="generate-id(preceding::slop:slide[1])"
    />

    <!--
        Extract the presentation heading and let slides extract themselves,
        once to generate the navigation (list of slides) and once to
        generate content
    -->
    <xsl:template match="/">
        <yapt-presentation>
            <heading>
                <!-- get everything up to the first slide -->
                <xsl:apply-templates
                    mode="head"
                    select="slop:parsed-text/*[not(preceding::slop:slide) and not(self::slop:slide)]"
                />
            </heading>
            <navigation>
                <!-- extract all slides, without content -->
                <xsl:apply-templates
                    mode="navigation"
                    select="slop:parsed-text/slop:slide"
                />
            </navigation>
            <content>
                <!-- extract slide elements, let them get their content -->
                <xsl:apply-templates
                    mode="content"
                    select="slop:parsed-text/slop:slide"
                />
            </content>
        </yapt-presentation>
    </xsl:template>

    <!-- head: by default copy everything, removing slop namespace -->
    <xsl:template match="*" mode="head">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

    <!-- head: eat empty lines and config stuff -->
    <xsl:template match="slop:empty-line" mode="head"/>

    <!-- navigation: output slide with unique id -->
    <xsl:template match="slop:slide" mode="navigation">
        <xsl:variable name="id">
            <xsl:number level="single" count="slop:slide"/>
        </xsl:variable>
        <slide-ref slide-id="{$id}" title="{.}"/>
    </xsl:template>

    <!-- content: output slide with unique id and content -->
    <xsl:template match="slop:slide" mode="content">
        <xsl:variable name="id">
            <xsl:number level="single" count="slop:slide"/>
        </xsl:variable>

        <slide slide-id="{$id}">
            <xsl:variable name="anchor" select="."/>
            <slide-head>
                <title><xsl:value-of select="."/></title>
            </slide-head>
            <slide-hints>
                <!-- collect presentation hints -->
                <xsl:apply-templates mode="hints" select="following-sibling::slop:*[starts-with(name(),$hintPrefix)][preceding::slop:slide[1] = $anchor]"/>
            </slide-hints>
            <slide-content>
                <!-- get content of this slide based on lastSlideKey -->
                 <xsl:apply-templates select="key('lastSlideKey',generate-id(.))" mode="paragraph"/>
            </slide-content>
        </slide>
    </xsl:template>

    <!-- presentation hints -->
    <xsl:template match="*[starts-with(name(),$hintPrefix)]" mode="hints">
        <xsl:element name="{substring-after(name(),$hintPrefix)}">
            <xsl:value-of select="."/>
        </xsl:element>
    </xsl:template>

    <!-- ignore hints in content -->
    <xsl:template match="*[starts-with(name(),$hintPrefix)]" mode="content"/>

    <!-- ignore multiple empty lines -->
    <xsl:template mode="paragraph" match="slop:empty-line[following-sibling::*[1][self::slop:empty-line]]"/>

    <!-- extract paragraph and code blocks -->
    <xsl:template mode="paragraph" match="slop:empty-line">
        <xsl:choose>
          <xsl:when test="following-sibling::*[1][self::slop:code]">
		        <pre>
		            <xsl:for-each select="following-sibling::*[1][not(self::slop:empty-line) and not(self::slop:slide)]">
		                <xsl:call-template name="code-grouper"/>
		            </xsl:for-each>
		        </pre>
          </xsl:when>
          <xsl:when test="following-sibling::*[1][self::slop:list]">
		        <ul>
		            <xsl:for-each select="following-sibling::*[1][not(self::slop:empty-line) and not(self::slop:slide)]">
		                <xsl:call-template name="list-grouper"/>
		            </xsl:for-each>
		        </ul>
          </xsl:when>
          <xsl:otherwise>
		        <p>
		            <xsl:for-each select="following-sibling::*[1][not(self::slop:empty-line) and not(self::slop:slide)]">
		                <xsl:call-template name="para-grouper"/>
		            </xsl:for-each>
		        </p>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- recursively collect elements until an empty line or the next slide is found -->
    <xsl:template name="para-grouper">
        <xsl:apply-templates mode="paragraph" select="."/>

        <xsl:for-each select="following-sibling::*[1][not(self::slop:empty-line) and not(self::slop:slide)]">
            <xsl:call-template name="para-grouper"/>
        </xsl:for-each>
    </xsl:template>

    <!-- ditto for code lines -->
    <xsl:template name="code-grouper">
        <xsl:apply-templates mode="code" select="."/>
        
        <xsl:for-each select="following-sibling::*[1][not(self::slop:empty-line) and not(self::slop:slide)]">
            <xsl:call-template name="code-grouper"/>
        </xsl:for-each>
    </xsl:template>

    <!-- ditto for list items -->
    <xsl:template name="list-grouper">
        <xsl:apply-templates mode="list" select="."/>
        
        <xsl:for-each select="following-sibling::*[1][not(self::slop:empty-line) and not(self::slop:slide)]">
            <xsl:call-template name="list-grouper"/>
        </xsl:for-each>
    </xsl:template>

    <!-- paragraph grouping mode, by default copy everything, removing slop namespace -->
    <xsl:template match="slop:*" mode="paragraph">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

    <!-- output code slop:lines as-is, with added carriage return -->
    <xsl:template match="slop:line" mode="list">
        <li>&#187; <xsl:copy-of select="text()"/></li>
    </xsl:template>

    <!-- output code slop:lines as-is, with added carriage return -->
    <xsl:template match="slop:line" mode="code">
        <xsl:copy-of select="text()"/><xsl:text>&#13;</xsl:text>
    </xsl:template>

    <!-- extract the text of lines -->
    <xsl:template match="slop:line" mode="paragraph">
        <xsl:value-of select="concat(.,' ')"/>
    </xsl:template>

    <!-- images are defined like "img_XX: filename" where XX is the CSS class -->
    <xsl:template mode="paragraph" match="slop:*[starts-with(name(),'img')]">
          <xsl:variable name="class" select="substring-after(name(),'img_')"/>
            <img src="{normalize-space(.)}" alt="{normalize-space(.)}" class="{$class}"/>
      </xsl:template>

</xsl:stylesheet>
