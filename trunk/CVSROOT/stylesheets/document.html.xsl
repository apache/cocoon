<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/XSL/Transform">

<!--
    Documentation XSLT Stylesheet for Document -> HTML4.0 transformation
    
    NOTE: no stilying should be placed in this transformation sheet since 
    even if XSLT would allow this and separation would be guaranteed, 
    it's better to use CSS for pure HTML styling issues. Transformations
    must be used to apply the necessary structure manipulation but should
    bend to "copy-over" behavior when possible. This is a general design
    patter that should _always_ be used when transforming something to
    HTML4.0.
 
    $Id: document.html.xsl,v 1.1 1999-11-30 16:30:11 stefano Exp $
-->

<!-- default copy-over's -->

<xsl:template match="*|/">
  <xsl:apply-templates/>
 </xsl:template>

<xsl:template match="text()|@*">
  <xsl:value-of select="."/>
 </xsl:template>

<!-- document section -->

<!-- FIXME (SM): how do we make the CSS link "soft"? -->

<xsl:template match="document">
  <html>
   <link rel="stylesheet" type="text/css" href="stylesheets/document.css"/>
   <head>
    <title><xsl:value-of select="/document/header/title"/></title>
   </head>
   <body>
    <xsl:apply-templates/>
   </body>
  </html>
 </xsl:template>

<!-- header section -->

<xsl:template match="header">
  <div class="header">
   <h1><xsl:value-of select="/document/header/title"/></h1>
   <h3><xsl:value-of select="/document/header/subtitle"/></h3>
  
   <center>written by</center>
   <center>
    <xsl:for-each select="/document/header/authors/author">
     <a href="mailto:{email}"><xsl:value-of select="name"/></a>
    </xsl:for-each>
   </center>
  </div>
  
  <xsl:apply-templates/>
 </xsl:template>

<xsl:template match="title|subtitle|authors">
  <!-- ignore -->
 </xsl:template>

<!-- body section -->

<xsl:template match="body">
  <div class="body">
   <xsl:apply-templates/>
  </div>
 </xsl:template>

 <xsl:template match="s1">
  <div class="s1">  
   <h2>
    <xsl:number level="multiple" format="1.1"/>
    <xsl:text>. </xsl:text>
    <xsl:value-of select="@title"/>
   </h2>
   <xsl:apply-templates/>
  </div>
 </xsl:template>

<!-- footer section -->

<xsl:template match="footer">
  <div class="footer">
   <xsl:apply-templates/>
  </div>
 </xsl:template>

<xsl:template match="legal">
  <p class="legal"><xsl:apply-templates/></p>
 </xsl:template>

<!-- links -->

<!-- Note: these are treated the same because pre-XSLT expansion should
      have translated "soft" links in the <connect> element to "hard" links.
      After this expansion there is no difference between the two. -->

 <xsl:template match="link|connect">
  <a href="{@href}">
   <xsl:apply-templates/>
  </a>
 </xsl:template>

 <xsl:template match="jump">
  <a href="{@href}" target="_top">
   <xsl:apply-templates/>
  </a>
 </xsl:template>

 <xsl:template match="fork">
  <a href="{@href}" target="_new">
   <xsl:apply-templates/>
  </a>
 </xsl:template>

 <xsl:template match="anchor">
  <a name="{@id}"></a>
 </xsl:template>

<!-- paragraphs -->

 <xsl:template match="p">
  <xsl:choose>
   <xsl:when test="position() = 1">
    <p class="first">
     <xsl:apply-templates/>
    </p>
   </xsl:when>
   <xsl:otherwise>
     <xsl:copy>
      <xsl:apply-templates/>
     </xsl:copy>
   </xsl:otherwise>
  </xsl:choose> 
 </xsl:template>
  
 <xsl:template match="source">
  <p class="source">
   <pre><xsl:apply-templates/></pre>
  </p>
 </xsl:template>

 <xsl:template match="note">
  <p class="note">
   <strong><xsl:text>Note: </xsl:text></strong>
   <xsl:apply-templates/>
  </p>
 </xsl:template>

 <xsl:template match="fixme">
  <!-- ignore on documentation -->
 </xsl:template>

<!-- lists -->

 <xsl:template match="sl">
  <p class="list">
   <dl>
    <xsl:for-each select="li">
     <dd><xsl:apply-templates/></dd>
    </xsl:for-each>
   </dl>
  </p>
 </xsl:template>

 <xsl:template match="ul|ol|dl">
  <xsl:choose>
   <xsl:when test="../li|../dt">
    <xsl:copy>
     <xsl:apply-templates/>
    </xsl:copy>
   </xsl:when>
   <xsl:otherwise>
    <p class="list">
     <xsl:copy>
      <xsl:apply-templates/>
     </xsl:copy>
    </p>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <xsl:template match="dt">
  <xsl:copy>
   <strong><xsl:apply-templates/></strong>
  </xsl:copy>
 </xsl:template>
 
 <xsl:template match="dd|li">
  <xsl:copy>
   <xsl:apply-templates/>
  </xsl:copy>
 </xsl:template>

<!-- table (just copy over) -->

 <!-- FIXME (SM) find a way to copy the attributed too! -->
 <xsl:template match="table|caption|colgroup|thead|tfoot|tbody|tr|th|td">
  <xsl:copy>
   <xsl:apply-templates/>
  </xsl:copy>
 </xsl:template>

<!-- specials -->

 <xsl:template match="br">
  <xsl:copy>
   <xsl:apply-templates/>
  </xsl:copy>
 </xsl:template>

 <xsl:template match="img-block">
  <p align="center" class="img-block">
   <img src="{@src}"/>
  </p>
 </xsl:template>
 
 <xsl:template match="img">
  <img src="{@src}"/>
 </xsl:template>

<!-- text markup (just copy over) -->

 <xsl:template match="strong|em|code|sub|sup">
  <xsl:copy>
   <xsl:apply-templates/>
  </xsl:copy>
 </xsl:template>

</xsl:stylesheet>
