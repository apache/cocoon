<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dir="http://apache.org/cocoon/directory/2.0">

  <xsl:template match="dir:directory">
    <samples name="ASCII Art Samples" xmlns:xlink="http://www.w3.org/1999/xlink">
      <group name="Main examples page.">
        <sample name="Back" href="..">to Cocoon examples main page</sample>
      </group>

      <group name="Available ascii art TXT files">
        <xsl:apply-templates select="dir:file" mode="txt"/>
      </group>

      <group name="ascii art in JPEG">
        <xsl:apply-templates select="dir:file" mode="jpg"/>
      </group>

      <group name="ascii art in PNG">
        <xsl:apply-templates select="dir:file" mode="png"/>
      </group>
    </samples>
  </xsl:template>

  <xsl:template match="dir:file" mode="txt">
    <sample name="{@name}" href="{@name}">as text</sample>
  </xsl:template>

  <xsl:template match="dir:file" mode="jpg">
    <sample name="{@name}" href="{@name}.jpg">as jpg</sample>
  </xsl:template>

  <xsl:template match="dir:file" mode="png">
    <sample name="{@name}" href="{@name}.png">as png</sample>
  </xsl:template>
</xsl:stylesheet>
