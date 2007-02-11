<?xml version="1.0"?>
<!--
  Copyright 1999-2004 The Apache Software Foundation

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:st="http://chaperon.sourceforge.net/schema/syntaxtree/2.0"
                xmlns="http://www.w3.org/1999/xhtml">

 <xsl:output indent="yes" method="html"/>

<!-- <xsl:template match="/">
  <xsl:choose>
   <xsl:when test="st:wiki">
    <html>
     <head>
      <title>Wiki example</title>
     </head>
     <body>
      <xsl:apply-templates select="st:wiki/st:paragraphs/st:paragraph"/>
     </body>
    </html>   
   </xsl:when>
   <xsl:otherwise>
    <xsl:apply-templates/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>-->

 <xsl:template match="//wiki">
  <div style="background: #b9d3ee; border: thin; border-color: black; border-style: solid; padding-left: 0.8em; 
               padding-right: 0.8em; padding-top: 0px; padding-bottom: 0px; margin: 0.5ex 0px; clear: both;">
   <xsl:apply-templates select="paragraphs/paragraph"/>
  </div>
 </xsl:template>

 <xsl:template match="st:paragraph" >
  <xsl:apply-templates select="st:bulletedlist|st:numberedlist1|st:numberedlist2|st:numberedlist3|st:headitem|st:footnote|st:textitem|st:LINE"/>
 </xsl:template>

 <xsl:template match="st:textitem" >
  <p>
   <xsl:apply-templates select="st:firstblock|st:textblock"/>
  </p>
 </xsl:template>

 <xsl:template match="st:textblock" >
  <xsl:apply-templates select="st:LINK|st:boldblock|st:italicblock|st:underlineblock|st:TEXT|st:note"/>
 </xsl:template>

 <xsl:template match="st:firstblock" >
  <xsl:apply-templates select="st:LINK|st:boldblock|st:italicblock|st:underlineblock|st:TEXT"/>
 </xsl:template>

 <xsl:template match="st:LINE" >
  <hr/>
 </xsl:template>

 <xsl:template match="st:bulletedlist" >
  <ul>
   <xsl:apply-templates select="st:bulletedlistitem"/>
  </ul>
 </xsl:template>

 <xsl:template match="st:bulletedlistitem" >
  <li>
   <xsl:apply-templates select="st:textblock"/>
  </li>
 </xsl:template>

 <xsl:template match="st:numberedlist1" >
  <ol>
   <xsl:apply-templates select="st:numberedlistitem1|st:numberedlist2"/>
  </ol>
 </xsl:template>

 <xsl:template match="st:numberedlistitem1" >
  <li>
   <xsl:apply-templates select="st:textblock"/>
  </li>
 </xsl:template>

 <xsl:template match="st:numberedlist2" >
  <ol>
   <xsl:apply-templates select="st:numberedlistitem2|st:numberedlist3"/>
  </ol>
 </xsl:template>
    
 <xsl:template match="st:numberedlistitem2" >
  <li>
   <xsl:apply-templates select="st:textblock"/>
  </li>
 </xsl:template>

 <xsl:template match="st:numberedlist3" >
  <ol>
   <xsl:apply-templates select="st:numberedlistitem3"/>
  </ol>
 </xsl:template>
    
 <xsl:template match="st:numberedlistitem3" >
  <li>
   <xsl:apply-templates select="st:textblock"/>
  </li>
 </xsl:template>

 <xsl:template match="st:headitem" >
  <xsl:choose>
   <xsl:when test="string-length(st:HEAD)=2">
    <h2>
     <xsl:apply-templates select="st:textblock"/>
    </h2>
   </xsl:when>
   <xsl:when test="string-length(st:HEAD)=3">
    <h3>
     <xsl:apply-templates select="st:textblock"/>
    </h3>
   </xsl:when>
   <xsl:otherwise>
    <h1>
     <xsl:apply-templates select="st:textblock"/>
    </h1>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <xsl:template match="st:footnote" >
  <a name="{normalize-space(st:note/st:TEXT|st:note/st:LINK)}">
   [<xsl:apply-templates select="st:note/st:TEXT|st:note/st:LINK"/>]
   <xsl:apply-templates select="st:textblock"/>
  </a>
 </xsl:template>

 <xsl:template match="st:LINK" >
  <a href="{normalize-space(.)}">
   <xsl:value-of select="."/>
  </a>
 </xsl:template>

 <xsl:template match="st:boldblock" >
  <b>
   <xsl:value-of select="st:TEXT"/>
  </b>
 </xsl:template>

 <xsl:template match="st:italicblock" >
  <i>
   <xsl:value-of select="st:TEXT"/>
  </i>
 </xsl:template>

 <xsl:template match="st:underlineblock" >
  <u>
   <xsl:value-of select="st:TEXT"/>
  </u><xsl:text> </xsl:text>
 </xsl:template>

 <xsl:template match="st:note" >
  <a href="#{normalize-space(st:TEXT|st:LINK)}">
   [<xsl:apply-templates select="st:TEXT|st:LINK"/>]
  </a>
 </xsl:template>

 <xsl:template match="st:TEXT" >
  <xsl:value-of select="."/>
 </xsl:template>

 <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
  <xsl:copy>
   <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:copy>
 </xsl:template>

</xsl:stylesheet>
