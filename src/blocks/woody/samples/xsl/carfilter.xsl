<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:wd="http://apache.org/cocoon/woody/definition/1.0">

  <xsl:param name="type"/>
  <xsl:param name="make"/>

  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="$type = 'makes'">
        <xsl:call-template name="makes-list"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="model-list"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="makes-list">
    <wd:selection-list>
      <xsl:for-each select="cars/make">
        <wd:item value="{@name}"/>
      </xsl:for-each>
    </wd:selection-list>
  </xsl:template>

  <xsl:template name="model-list">
    <wd:selection-list>
      <xsl:for-each select="cars/make[@name=$make]/model">
        <wd:item value="{@name}"/>
      </xsl:for-each>
    </wd:selection-list>
  </xsl:template>

</xsl:stylesheet>
