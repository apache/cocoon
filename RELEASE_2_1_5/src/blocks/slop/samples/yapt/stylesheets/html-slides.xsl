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
    Generate the HTML for one or many slides
    $Id: html-slides.xsl,v 1.6 2004/03/06 02:26:04 antonio Exp $
-->
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
    <!-- heading templates -->
    <xsl:import href="heading.xsl"/>

    <!-- path to images -->
    <xsl:variable name="imagePath" select="concat('../../',normalize-space(/yapt-presentation/heading/image-directory),'/')"/>

    <!-- single or multi-slide display? -->
    <xsl:variable name="slideCount" select="count(/yapt-presentation/content/slide)"/>

    <!-- setup CSS stylesheets, javascript for "next slide" click and output content -->
    <xsl:template match="/">
        <html>
            <head>
                <link rel="stylesheet" type="text/css" href="css/yapt-style.css"/>
                <link rel="stylesheet" type="text/css" href="css/yapt-images.css"/>
            </head>
            <body>
                <xsl:if test="$slideCount &lt; 2">
                    <!-- single slide: click body to go to next slide -->
                    <xsl:variable name="nextId" select="//navigation/slide-ref[@offset-from-current=1]/@slide-id"/>
                    <xsl:if test="$nextId">
                        <xsl:attribute name="onClick">document.location='slide-<xsl:value-of select="$nextId"/>'</xsl:attribute>
                    </xsl:if>
                </xsl:if>

                <!-- indicate if multiple or single slides (boy is XSLT painful for this) -->
                <xsl:variable name="typeId">
                    <xsl:choose>
                        <xsl:when test="$slideCount &gt; 1">multipleSlides</xsl:when>
                        <xsl:otherwise>singleSlide</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <div id="content">
                    <div id="{$typeId}">
                        <xsl:apply-templates select="yapt-presentation"/>
                    </div>
                </div>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="yapt-presentation">

        <!-- if multiple slides, output presentation heading -->
        <xsl:if test="$slideCount &gt; 1">
            <div id="presHeading">
                <xsl:apply-templates select="heading/*" mode="heading"/>
            </div>
        </xsl:if>

        <!-- output slides -->
        <div id="slides">
            <xsl:apply-templates mode="content" select="content/slide"/>
        </div>

        <!-- for single slide, output navigation -->
        <xsl:if test="$slideCount &lt; 2">
            <div id="navigation">
                <xsl:apply-templates select="navigation" mode="navigation"/>
            </div>
        </xsl:if>

    </xsl:template>

    <!-- navigation: link to previous slide, table of contents and next slide -->
    <xsl:template match="navigation" mode="navigation">
        <xsl:variable name="prev" select="slide-ref[@offset-from-current = -1]/@slide-id"/>
        <xsl:variable name="next" select="slide-ref[@offset-from-current = 1]/@slide-id"/>

        <xsl:if test="$prev">
            <div class="navItem">
                <a href="{concat('slide-',$prev)}">previous</a>
            </div>
        </xsl:if>

        <div class="navItem">
            <a href="index">index</a>
        </div>

        <xsl:if test="$next">
            <div class="navItem">
                <a href="{concat('slide-',$next)}">next</a>
            </div>
        </xsl:if>

    </xsl:template>

    <!-- in content, float images according to their CSS classes -->
    <xsl:template mode="content" match="img">
        <div class="{@class}">
            <img src="{concat($imagePath,@src)}" alt="{@alt}" class="scaledImage"/>
        </div>
    </xsl:template>

    <!-- subtitle in content -->
    <xsl:template mode="content" match="subtitle">
        <h2><xsl:value-of select="."/></h2>
    </xsl:template>

    <!-- note in content -->
    <xsl:template mode="content" match="note">
        <p class="note">
            <xsl:value-of select="."/>
        </p>
    </xsl:template>

    <!-- slide in content -->
    <xsl:template mode="content" match="slide">
        <div class="singleSlide">
            <div class="singleSlideHeading">
                <xsl:apply-templates mode="content" select="slide-head/*"/>
            </div>

            <!-- use style hints if any (but not if generating multiple slides for printing) -->
            <xsl:variable name="hintClass">
                <xsl:choose>
                    <xsl:when test="$slideCount &gt; 1">slideHint</xsl:when>
                    <xsl:otherwise><xsl:value-of select="concat('slideHint',slide-hints/style)"/></xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <div class="{$hintClass}">
                <div class="singleSlideContent">
                    <xsl:apply-templates mode="content" select="slide-content/*"/>
                </div>
            </div>
        </div>
    </xsl:template>

    <!-- slide title in content -->
    <xsl:template mode="content" match="title">
        <h1>
            <xsl:value-of select="ancestor::slide/@slide-id"/>.
            <xsl:value-of select="."/>
        </h1>
    </xsl:template>

    <!-- copy other content elements -->
    <xsl:template mode="content" match="*" priority="-1">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates mode="content"/>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
