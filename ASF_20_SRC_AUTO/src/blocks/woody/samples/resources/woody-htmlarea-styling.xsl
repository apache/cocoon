<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:wi="http://apache.org/cocoon/woody/instance/1.0"
                exclude-result-prefixes="wi">
  <!--+
      | This stylesheet is designed to be included by 'woody-advanced-styling.xsl'.
      +-->

  <!-- Location of the resources directory, where JS libs and icons are stored -->
  <xsl:param name="resources-uri">resources</xsl:param>

  <xsl:template match="head" mode="woody-htmlarea">
    <script type="text/javascript">
      _editor_url = "<xsl:value-of select="concat($resources-uri, '/htmlarea/')"/>";
      _editor_lang = "en";
    </script>
    <script type="text/javascript" src="{$resources-uri}/htmlarea/htmlarea.js"></script>
  </xsl:template>

  <xsl:template match="body" mode="woody-htmlarea"/>

  <!--+
      | wi:field with @type 'htmlarea'
      +-->
  <xsl:template match="wi:field[wi:styling[@type='htmlarea']]">
    <textarea id="{@id}" name="{@id}" title="{wi:hint}">
      <xsl:apply-templates select="." mode="styling"/>
      <!-- remove carriage-returns (occurs on certain versions of IE and doubles linebreaks at each submit) -->
      <xsl:apply-templates select="wi:value/node()" mode="htmlarea-copy"/>
    </textarea>
    <xsl:apply-templates select="." mode="common"/>
    <script language="JavaScript">HTMLArea.replace('<xsl:value-of select="@id"/>');</script>
  </xsl:template>

  <xsl:template match="@*|*" mode="htmlarea-copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" mode="htmlarea-copy"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="text()" mode="htmlarea-copy">
    <xsl:copy-of select="translate(., '&#13;', '')"/>
  </xsl:template>

</xsl:stylesheet>
