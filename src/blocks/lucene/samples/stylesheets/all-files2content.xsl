<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:cinclude="http://apache.org/cocoon/include/1.0"
>

  <xsl:template match="files">
    <includes>
      <path><xsl:value-of select="path"/></path>
      <xsl:apply-templates/>
    </includes>
  </xsl:template >

  <xsl:template match="file">
    <file>
      <name><xsl:value-of select="path"/></name>
      <include>
      <cinclude:includexml ignoreErrors="true">
        <cinclude:src><xsl:value-of select="absolutePath"/></cinclude:src>
      </cinclude:includexml>
      </include>
    </file>
  </xsl:template >

  <xsl:template match="@*|node()" priority="-1"></xsl:template>
  <xsl:template match="text()" priority="-1"></xsl:template>
</xsl:stylesheet> 