<?xml version="1.0"?>
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fi="http://apache.org/cocoon/forms/1.0#instance">
  <xsl:template match="/">
    <ul>
      <xsl:apply-templates/>
    </ul>
  </xsl:template>
  
  <xsl:template match="fi:item">
    <li>
      <xsl:value-of select="@value"/>
      <xsl:if test="fi:label and (fi:label != @value)">
        <span class="informal"> (<xsl:copy-of select="fi:label/node()"/>)</span>
      </xsl:if>
    </li>
  </xsl:template>
</xsl:stylesheet>
