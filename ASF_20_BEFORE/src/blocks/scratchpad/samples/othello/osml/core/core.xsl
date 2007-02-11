<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:osm="http://osmosis.gr/osml/1.0">
  <xsl:output method="html" version="1.0" encoding="utf-8" indent="yes" omit-xml-declaration="no"/>
  <!-- if you create your custom xml - xsl element add here refernce to your xsl -->

  <!-- add elements with custom transformation instructions
    in this case some osm: elements are transformed to xhtml
   -->
  <xsl:include href="../plugins/list.xsl"/>
  <xsl:include href="../plugins/custombutton.xsl"/>
  <xsl:include href="../plugins/custom.xsl"/>

  <xsl:template match="osm:site">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- we dont need (for the momend special tag to include xhtml code.
        Everything else, that dont belong in any specific name space is xhtml. Those elements jare coped.
        But osm:xhtml is used just to group xhtml framgents.
       -->
  <xsl:template match="osm:xhtml">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="osm:block">
    <xsl:apply-templates/>
  </xsl:template>

  <!-- in case a osm:pageTitle element exist don't try to transform it
    fixme: this will be used to create each page title
   -->
  <xsl:template match="osm:pageTitle">
	
  </xsl:template>
  <xsl:template match="node()|@*" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
