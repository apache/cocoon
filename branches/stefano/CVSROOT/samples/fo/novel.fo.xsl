<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/XSL/Transform/1.0"
  xmlns:fo="http://www.w3.org/XSL/Format/1.0">

  <xsl:template match="novel">
    <xsl:pi name="cocoon-format">type="text/xslfo"</xsl:pi>
    <fo:root xmlns:fo="http://www.w3.org/XSL/Format/1.0">
      <fo:layout-master-set>
      <fo:simple-page-master
        page-master-name="right"
        margin-top="75pt"
        margin-bottom="25pt"
        margin-left="100pt"
        margin-right="50pt">
        <fo:region-body margin-bottom="50pt"/>
        <fo:region-after extent="25pt"/>
      </fo:simple-page-master>
      <fo:simple-page-master
        page-master-name="left"
        margin-top="75pt"
        margin-bottom="25pt"
        margin-left="50pt"
        margin-right="100pt">
        <fo:region-body margin-bottom="50pt"/>
        <fo:region-after extent="25pt"/>
      </fo:simple-page-master>
      </fo:layout-master-set>

      <fo:page-sequence>

        <fo:sequence-specification>
          <fo:sequence-specifier-alternating
            page-master-first="right"
            page-master-odd="right"
            page-master-even="left"/>
        </fo:sequence-specification>

        <fo:static-content flow-name="xsl-after">
          <fo:block text-align-last="centered" font-size="10pt"><fo:page-number/></fo:block>
        </fo:static-content>

        <fo:flow>
          <xsl:apply-templates/>
        </fo:flow>
      </fo:page-sequence>

    </fo:root>
  </xsl:template>

  <xsl:template match="front/title">
    <fo:block font-size="36pt" text-align-last="centered" space-before.optimum="24pt"><xsl:apply-templates/></fo:block>
  </xsl:template>

  <xsl:template match="author">
    <fo:block font-size="24pt" text-align-last="centered" space-before.optimum="24pt"><xsl:apply-templates/></fo:block>
  </xsl:template>

  <xsl:template match="revision-list">
  </xsl:template>

  <xsl:template match="chapter">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="chapter/title">
    <fo:block font-size="24pt" text-align-last="centered" space-before.optimum="24pt"><xsl:apply-templates/></fo:block>
  </xsl:template>

  <xsl:template match="paragraph">
    <fo:block font-size="12pt" space-before.optimum="12pt" text-align="justified"><xsl:apply-templates/></fo:block>
  </xsl:template>
</xsl:stylesheet>
