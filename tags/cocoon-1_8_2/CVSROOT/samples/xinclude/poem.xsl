<?xml version="1.0"?>
<xsl:stylesheet version="1.0" 
                id="poem_style"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!--
     This file is provided purely to make the "poem.xml" file a 
     complete xslt example, which is not, in fact, the point of this 
     exercise.
  -->

  <xsl:template match="poem">
   <html>
    <head>
     <title>
      <xsl:value-of select="@title"/>
     </title>
    </head>
    <body bgcolor="#ffffff">
     <h1><xsl:value-of select="@title"/></h1>
     <xsl:apply-templates select="stanza"/>
    </body>
   </html>
  </xsl:template>

  <xsl:template match="stanza">
    <p>
      <xsl:apply-templates />
    </p>
  </xsl:template>

  <xsl:template match="line">
    <xsl:value-of select="." /><br></br>
  </xsl:template>

</xsl:stylesheet>
