<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:n="http://www.betaversion.org/linotype/news/1.0"
                              xmlns:h="http://www.w3.org/1999/xhtml">
  
  <xsl:import href="news2rss.xslt"/>

  <xsl:param name="home"/>
  
  <xsl:template match="/">
   <rss version="0.91">
    <channel>
     <title>Stefano's Linotype</title>
     <link>http://www.betaversion.org/~stefano/linotype/</link>
     <description>Stefano Mazzocchi's Weblog</description>
     <language>en</language>
     <xsl:apply-templates select="list/element"/>
    </channel>
   </rss>
  </xsl:template>
    
  <xsl:template match="n:news">
   <xsl:param name="id"/>
   <item>
    <title><xsl:value-of select="n:title"/></title>
    <link><xsl:value-of select="$home"/>/<xsl:value-of select="$id"/>/</link>
    <description>
     <xsl:apply-templates select="h:body/node()" mode="encode">
      <xsl:with-param name="id" select="$id"/>
     </xsl:apply-templates>
    </description>
   </item>
  </xsl:template>
         
</xsl:stylesheet>
