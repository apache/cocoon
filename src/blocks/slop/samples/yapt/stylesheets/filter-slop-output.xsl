<?xml version="1.0"?>

<!--
    Convert the slop parser output to a collection of slides
    $Id: filter-slop-output.xsl,v 1.1 2003/09/26 14:42:36 bdelacretaz Exp $
-->
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:slop="http://apache.org/cocoon/slop/parser/1.0"
>

    <!-- prefix for presentation hints in slides -->
    <xsl:variable name="hintPrefix" select="'hint-'"/>

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
                <!-- recursively group paragraphs of this slide, separated by empty lines -->
                <!-- TODO can be made more efficient using keys -->
                <xsl:for-each select="following-sibling::slop:empty-line[preceding::slop:slide[1] = $anchor]">
                    <xsl:apply-templates select="." mode="paragraph"/>
                </xsl:for-each>
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

    <!-- empty line separates paragraphs -->
    <xsl:template mode="paragraph" match="slop:empty-line">
        <p>
            <xsl:for-each select="following-sibling::*[1][not(self::slop:empty-line) and not(self::slop:slide)]">
                <xsl:call-template name="para-grouper"/>
            </xsl:for-each>
        </p>
    </xsl:template>

    <!-- recursively collect elements until an empty line or the next slide is found -->
    <xsl:template name="para-grouper">
        <xsl:apply-templates mode="paragraph" select="."/>

        <xsl:for-each select="following-sibling::*[1][not(self::slop:empty-line) and not(self::slop:slide)]">
            <xsl:call-template name="para-grouper"/>
        </xsl:for-each>
    </xsl:template>

    <!-- paragraph grouping mode, by default copy everything, removing slop namespace -->
    <xsl:template match="slop:*" mode="paragraph">
        <xsl:element name="{name()}">
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

    <!-- extract the text of lines -->
    <xsl:template match="slop:line" mode="paragraph">
        <xsl:value-of select="concat(.,' ')"/>
    </xsl:template>

    <!-- images are defined like "img_XX: filename" where XX is the CSS class -->
    <xsl:template mode="paragraph" match="slop:*[starts-with(name(),'img')]">
          <xsl:variable name="class" select="substring-after(name(),'img_')"/>
            <img src="{.}" alt="{.}" class="{$class}"/>
      </xsl:template>

</xsl:stylesheet>
