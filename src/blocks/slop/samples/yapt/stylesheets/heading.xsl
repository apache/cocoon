<?xml version="1.0"?>

<!--
    Templates for transforming presentation heading to HTML
    $Id: heading.xsl,v 1.1 2003/09/26 14:42:36 bdelacretaz Exp $
-->
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

    <!-- presentation info: display field name + value -->
    <xsl:template mode="heading" match="*">
        <div class="headingField">
            <div class="fieldName">
                <xsl:value-of select="concat(name(),':')"/>
            </div>
            <div class="fieldValue">
                <xsl:value-of select="."/>
            </div>
        </div>
    </xsl:template>

    <!-- presentation info: comment lines -->
    <xsl:template mode="heading" match="line">
        <p class="commentLine">
            <xsl:value-of select="."/>
        </p>
    </xsl:template>

    <!-- presentation info: title -->
    <xsl:template mode="heading" match="presentation">
        <h1><xsl:value-of select="."/></h1>
    </xsl:template>

    <!-- omit some presentation fields -->
    <xsl:template mode="heading" match="image-directory"/>

</xsl:stylesheet>
