<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <xsl:template match="novel">
    <xsl:processing-instruction name="cocoon-format">type="text/xslfo"</xsl:processing-instruction>
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
      <fo:simple-page-master
        master-name="right"
        margin-top="75pt"
        margin-bottom="25pt"
        margin-left="100pt"
        margin-right="50pt">
        <fo:region-body margin-bottom="50pt"/>
        <fo:region-after extent="25pt"/>
      </fo:simple-page-master>
      <fo:simple-page-master
        master-name="left"
        margin-top="75pt"
        margin-bottom="25pt"
        margin-left="50pt"
        margin-right="100pt">
        <fo:region-body margin-bottom="50pt"/>
        <fo:region-after extent="25pt"/>
      </fo:simple-page-master>
      <fo:page-sequence-master master-name="psmOddEven">
        <fo:repeatable-page-master-alternatives>
          <fo:conditional-page-master-reference master-name="right" page-position="first"/>
          <fo:conditional-page-master-reference master-name="right" odd-or-even="even"/>
          <fo:conditional-page-master-reference master-name="left" odd-or-even="odd"/>
          <!-- recommended fallback procedure -->
          <fo:conditional-page-master-reference master-name="right"/>
        </fo:repeatable-page-master-alternatives>
      </fo:page-sequence-master>
      </fo:layout-master-set>

      <fo:page-sequence master-name="psmOddEven">

        <fo:static-content flow-name="xsl-region-after">
          <fo:block text-align-last="center" font-size="10pt"><fo:page-number/></fo:block>
        </fo:static-content>

        <fo:flow flow-name="xsl-region-body">
          <xsl:apply-templates/>
        </fo:flow>
      </fo:page-sequence>

    </fo:root>
  </xsl:template>

  <xsl:template match="front/title">
    <fo:block font-size="36pt" text-align-last="center" space-before.optimum="24pt"><xsl:apply-templates/></fo:block>
  </xsl:template>

  <xsl:template match="author">
    <fo:block font-size="24pt" text-align-last="center" space-before.optimum="24pt"><xsl:apply-templates/></fo:block>
  </xsl:template>

  <xsl:template match="revision-list">
  </xsl:template>

  <xsl:template match="chapter">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="chapter/title">
    <fo:block font-size="24pt" text-align-last="center" space-before.optimum="24pt"><xsl:apply-templates/></fo:block>
  </xsl:template>

  <xsl:template match="paragraph">
    <fo:block font-size="12pt" space-before.optimum="12pt" text-align="justify"><xsl:apply-templates/></fo:block>
  </xsl:template>
</xsl:stylesheet>
