<?xml version="1.0"?>

<xsl:stylesheet
  version="1.0"
  xmlns:xsp="http://xml.apache.org/xsp"

  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
  <xsl:template match="test-dynamic-tag">
    <b><i>
      <xsp:logic> {
        this.contentHandler.startElement("", "p", "p", xspAttr);
        this.characters("I'm a logicsheet-generated text!");
        this.contentHandler.endElement("", "p", "p");
      } </xsp:logic>
    </i></b>
  </xsl:template>

  <xsl:template match="@*|*|text()|processing-instruction()">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
