<?xml version="1.0" encoding="utf-8"?>

<!--
    Convert the output of the XSP caching sample to iText,
    in order to test caching of the whole pipeline, up to PDF

    @author tcurdt@apache.org
-->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    >

    <xsl:param name="pages" select="'10'"/>

    <xsl:template match="/">
      <itext creationdate="Fri May 23 9:30:00 CEST 2003" producer="tcurdt@cocoon.org">
        
       <xsl:call-template name="repeatPages">
         <xsl:with-param name="nPages" select="$pages"/>
       </xsl:call-template>

       <paragraph font="unknown" size="12.0" align="Default">
         End of test document
       </paragraph>
      </itext>
    </xsl:template>

    <!-- generate a lot of pages to make FOP generation slower -->
    <xsl:template name="repeatPages">
        <xsl:param name="nPages"/>

        <paragraph font="unknown" size="12.0" align="Default">
          <newpage/>
          Dummy page, used to slow down FOP generation to test caching...
          <xsl:value-of select="$nPages"/> pages to go.
        </paragraph>

        <xsl:if test="$nPages &gt; 1">
            <xsl:call-template name="repeatPages">
                <xsl:with-param name="nPages" select="$nPages - 1"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>


    <!-- minimal HTML scraping of input -->
    <xsl:template match="*[starts-with(name(),'h')]|p">
        <paragraph font="unknown" size="12.0" align="Default">
            <xsl:apply-templates/>
        </paragraph>
    </xsl:template>

    <!-- minimal HTML scraping of input -->
    <xsl:template match="br">
        <newline/>
    </xsl:template>

    <!-- minimal HTML scraping of input -->
    <xsl:template match="b">
       <chunk size="20.0" fontstyle="bold" red="255" green="0" blue="0">

            <xsl:apply-templates/>
       </chunk>
    </xsl:template>
    
</xsl:stylesheet>