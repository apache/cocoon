<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:req="http://apache.org/cocoon/request/2.0"
                xmlns:source="http://apache.org/cocoon/source/1.0">

<xsl:param name="file"></xsl:param>

<xsl:template match="request/parameters">
<page>
  <source:write create="true">
    <source:source><xsl:value-of select="$file"/></source:source>
    <source:path>page</source:path>
    <source:fragment>
      <title><xsl:value-of select="title"/></title>
      <content>
        <xsl:for-each select="content/para">
        <para>
        <xsl:value-of select="normalize-space(.)"/>
        </para>
        </xsl:for-each>
      </content>
    </source:fragment>
  </source:write>

  <source:write create="true">
    <source:source><xsl:value-of select="$file"/>.meta</source:source>
    <source:path>metapage</source:path>
    <source:fragment>
      <author><xsl:value-of select="author"/></author>
      <category><xsl:value-of select="category"/></category>
      <state><xsl:value-of select="state"/></state>
    </source:fragment>
  </source:write>
</page>
</xsl:template>

</xsl:stylesheet>
