<?xml version="1.0" encoding="utf-8"?>

<!--
    Convert the output of the XSP caching sample to XSL-FO,
    in order to test caching of the whole pipeline, up to PDF

    @author bdelacretaz@codeconsult.ch
-->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    >

    <xsl:param name="pages" select="'10'"/>

    <xsl:attribute-set name="title">
        <xsl:attribute name="font-size">18pt</xsl:attribute>
        <xsl:attribute name="space-before">10pt</xsl:attribute>
        <xsl:attribute name="color">blue</xsl:attribute>
    </xsl:attribute-set>

    <xsl:template match="/">
        <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
            <fo:layout-master-set>

                <!-- layout for all pages -->
                <fo:simple-page-master master-name="main"
                    page-height="29.7cm"
                    page-width="21cm"
                    margin-top="1cm"
                    margin-bottom="2cm"
                    margin-left="2.5cm"
                    margin-right="2.5cm">
                    <fo:region-body margin-top="3cm"/>
                    <fo:region-before extent="3cm"/>
                    <fo:region-after extent="1.5cm"/>
                </fo:simple-page-master>

            </fo:layout-master-set>

            <!-- content -->
            <fo:page-sequence master-reference="main">

                <fo:flow flow-name="xsl-region-body">
                    <fo:block space-before="6pt">
                        <fo:block font-size="24pt">Cocoon FOP caching test</fo:block>
                        <xsl:call-template name="explain"/>
                        <xsl:element name="fo:block" use-attribute-sets="title">
                            Original output from the XSP page
                        </xsl:element>
                        <fo:block color="grey">
                            <xsl:apply-templates/>
                        </fo:block>
                        <xsl:element name="fo:block" use-attribute-sets="title">
                            Dummy pages
                        </xsl:element>
                        <fo:block>
                            Several dummy pages follow, as set by the
                            <fo:inline font-style="italic">pages</fo:inline> URL parameter.
                        </fo:block>
                        <xsl:call-template name="repeatPages">
                            <xsl:with-param name="nPages" select="$pages"/>
                        </xsl:call-template>
                        <fo:block id="lastBlock">
                            End of test document
                        </fo:block>
                    </fo:block>
                </fo:flow>
            </fo:page-sequence>
        </fo:root>
    </xsl:template>

    <!-- generate a lot of pages to make FOP generation slower -->
    <xsl:template name="repeatPages">
        <xsl:param name="nPages"/>
        <fo:block break-before="page">
            Dummy page, used to slow down FOP generation to test caching...
            <xsl:value-of select="$nPages"/> pages to go.
        </fo:block>
        <fo:block>
            <!-- slow down FOP for this test, forcing it to keep all pages in memory -->
            The last page is number <fo:page-number-citation ref-id="lastBlock"/>.
        </fo:block>
        <xsl:if test="$nPages &gt; 1">
            <xsl:call-template name="repeatPages">
                <xsl:with-param name="nPages" select="$nPages - 1"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- explain this sample -->
    <xsl:template name="explain">
        <xsl:element name="fo:block" use-attribute-sets="title">
            What's this?
        </xsl:element>
        <fo:block>
            This sample takes the output of the XSP cacheable sample and allows you
            to test caching all the way up to PDF generation.
        </fo:block>
        <fo:block>
            Note that I haven't been able to get PDF caching to work yet, I'm checking
            this sample in to have a reference for testing. Currently I see (from the timestamps
            written in the generated XML) that the XSP output is indeed cached, but the sitemap
            log shows that the FOP conversion runs for every request.
        </fo:block>

        <xsl:element name="fo:block" use-attribute-sets="title">
            How to test the cache
        </xsl:element>
        <fo:block>
            Call this page like
            <fo:inline font-style="italic">cache-test-keyA-xyz-1500.pdf</fo:inline>,
            and use the information shown in
            <fo:inline color="red">red</fo:inline> under
            <fo:inline font-style="italic">original output</fo:inline>
            below to check that the cache is working.
        </fo:block>
        <fo:block>
            In the above filename, <fo:inline font-style="italic">keyA</fo:inline> is the key used
            to store the content generated by the XSP page in cache, and <fo:inline font-style="italic">1500
            </fo:inline>is the number of pages to generate in the output PDF.
        </fo:block>
        <fo:block>
            The sitemap log (or whatever log the FOPSerializer is configured to write to) can also
            be used to tell if FOP is converting the data or if its being served from the Cocoon cache.
        </fo:block>
        <fo:block>
            Different values of
            <fo:inline font-style="italic">pageKey</fo:inline> should cause different versions of the document to be cached.
        </fo:block>
        <fo:block>
            Increase the
            <fo:inline font-style="italic">pages</fo:inline> parameter to have FOP take longer to generate the document if needed.
        </fo:block>
    </xsl:template>

    <!-- minimal HTML scraping of input -->
    <xsl:template match="*[starts-with(name(),'h')]|p">
        <fo:block>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <!-- minimal HTML scraping of input -->
    <xsl:template match="br">
        <fo:block>&#160;</fo:block>
    </xsl:template>

    <!-- minimal HTML scraping of input -->
    <xsl:template match="b">
        <fo:inline color="red" font-weight="bold" font-size="14pt">
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>


</xsl:stylesheet>
