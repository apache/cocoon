<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

 <xsl:template match="/">
  <xsl:processing-instruction name="cocoon-format">type="text/html"</xsl:processing-instruction>

  <html>
   <head>
    <title>XML Structure</title>
    <style type="text/css"><xsl:comment>
        #main {
                border: none;
                font-size: 10px;
                line-height: 10px;
                font-family: sans-serif
                font-variant: normal;
                font-style: normal;
                font-stretch: normal;
                font-size-adjust: none
                padding: 0.5em 0.5em 0.5em 0.5em
        }
        
        IMG {
                padding: 0px 0px 0px 0px;
                margin: 0px 0px 0px 0px;
        }
       
        .level1 { color: black}
        .level2 { color: red }
        .level3 { color: green }
        .level4 { color: blue }
   </xsl:comment></style>
   </head>
   <body>
    <xsl:apply-templates/>
   </body>
  </html>
 </xsl:template>

 <xsl:template match="*">
  <xsl:variable name="level"><xsl:value-of select="count(ancestor-or-self::*)"/></xsl:variable>

  <xsl:choose>
   <xsl:when test="$level=1">
    <img src="images/root.gif"/>
   </xsl:when>

   <xsl:when test="$level!=1">
    <xsl:for-each select="ancestor::*">
     <xsl:choose>
      <xsl:when test="(count(ancestor::*)=0) or (count(following-sibling::*)=0)">
       <img src="images/space.gif" height="1" width="18"/>
      </xsl:when>
      <xsl:otherwise>
       <img src="images/branch.gif"/>
      </xsl:otherwise>
     </xsl:choose>
    </xsl:for-each>

    <xsl:choose>
     <xsl:when test="count(following-sibling::*)!=0">
      <img src="images/tree.gif"/>
     </xsl:when>
     <xsl:otherwise>
      <img src="images/leaf.gif"/>
     </xsl:otherwise>
    </xsl:choose>

   </xsl:when>
  </xsl:choose>

  <span class="level{$level}">
   <xsl:value-of select="name(.)"/>
  </span>
  <br/>
  <xsl:apply-templates/>
 </xsl:template>

 <xsl:template match="@*|comment()|processing-instruction()|text()"/>

</xsl:stylesheet>