<?xml version="1.0"?>

<!--
    Select a single slide from the output of filter-slop-output.xsl
    $Id: select-slide.xsl,v 1.1 2003/09/26 14:42:36 bdelacretaz Exp $
-->
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

    <!-- which slide to select -->
    <xsl:param name="slideId"/>

    <!-- By default copy everything -->
    <xsl:template match="*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- add position attributes for next/previous navigation -->
    <xsl:template match="slide-ref">
        <xsl:copy>
            <xsl:copy-of select="@*"/>

            <xsl:attribute name="offset-from-current">
                <xsl:value-of select="@slide-id - $slideId"/>
            </xsl:attribute>

            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <!-- omit slides, except for the selected one -->
    <xsl:template match="slide[not(@slide-id = $slideId)]"/>

</xsl:stylesheet>
