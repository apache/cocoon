<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:variable name="content" select="/aggregate/content/document/body"/>
  <xsl:variable name="content-header" select="/aggregate/content/document/header"/>
  <xsl:variable name="comments" select="/aggregate/comments"/>
  <xsl:template match="/">
    <document>
      <header>
        <title>
          <xsl:value-of select="$content-header/title"/>
        </title>
      </header>
      <body>
        <xsl:copy-of select="$content/*"/>
        <xsl:apply-templates select="$comments"/>
      </body>
    </document>
  </xsl:template>
  <xsl:template match="comments">
    <section id="Comments">
      <title>Comments</title>
      <div class="commentsarea">
        <div class="commenttaskbar">
          add your comments
        </div>
        <xsl:apply-templates select="comment"/>
      </div>
    </section>
  </xsl:template>
  <xsl:template match="comment">
    <div class="comment">
      <div class="commentheader">
        <xsl:value-of select="@subject"/> by <xsl:value-of select="@name"/>
      </div>
      <div class="commentbody">
        <xsl:copy-of select="*"/>
      </div>
    </div>
  </xsl:template>
</xsl:stylesheet>
