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
                exclude-result-prefixes="st">

 <xsl:output indent="yes" 
             method="html"
             doctype-public="-//APACHE//DTD Documentation V1.1//EN"
             doctype-system="document-v11.dtd"
             cdata-section-elements="source"/>

 <xsl:param name="name" select="''"/>
 <xsl:param name="spaceless-filenames" select="''"/>

 <xsl:template name="splitString">
    <xsl:param name="restOfString"/>
    
    <xsl:variable name="uppercase">(ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
    <xsl:variable name="currentLetter" select="substring($restOfString,1,1)"/>
    
    <xsl:choose>
        <xsl:when test="contains($restOfString, '(') or contains($restOfString,' ')">
            <xsl:value-of select="$restOfString"/>
        </xsl:when>
        <xsl:when test="string-length($restOfString) &gt;= 2">
        <!-- there's a possibility it needs to be split -->
        <xsl:choose>
            <xsl:when test="contains($uppercase,$currentLetter)">
                <xsl:variable name="followingLetter" select="substring($restOfString,2,1)"/>
                <xsl:if test="not(contains($uppercase,$followingLetter))">
                    <xsl:text> </xsl:text>
                </xsl:if>
                <xsl:value-of select="$currentLetter"/>
                
                <xsl:call-template name="splitString">
                    <xsl:with-param name="restOfString" select="substring($restOfString,2)"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <!-- current letter is lower-case - just spit it out -->
                <xsl:value-of select="$currentLetter"/>
                <xsl:call-template name="splitString">
                    <xsl:with-param name="restOfString" select="substring($restOfString,2)"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
        <!-- end of string - just write the remainder -->
        <xsl:value-of select="$restOfString"/>
        </xsl:otherwise>
    </xsl:choose>
 </xsl:template>



 <xsl:template match="st:output">
   <document>  
    <header>  
     <title>
      <xsl:choose>
       <xsl:when test="$name">
        <xsl:call-template name="splitString">
         <xsl:with-param name="restOfString" select="$name"/>
        </xsl:call-template>
       </xsl:when>
       <xsl:otherwise>
        <xsl:value-of select="st:document/st:section/st:title/st:textsequence"/>
       </xsl:otherwise>
      </xsl:choose>
     </title>  
     </header>  
    <body>
  <xsl:apply-templates select="st:document/st:paragraphs/st:paragraph/*" mode="paragraph"/>  
  <xsl:apply-templates select="st:document/st:section"/>
    </body>  
   </document>
 </xsl:template>

 <xsl:template match="st:section">
  <section>
   <title><xsl:value-of select="st:title/st:textsequence"/></title>
   <xsl:apply-templates select="st:paragraphs/st:paragraph/*|st:paragraphs/st:subsection" mode="paragraph"/>
  </section>
 </xsl:template>

 <xsl:template match="st:subsection" mode="paragraph">
  <section>
   <title><xsl:value-of select="st:subtitle/st:textsequence"/></title>
   <xsl:apply-templates select="st:subparagraphs/st:paragraph/*|st:subparagraphs/st:subsubsection" mode="paragraph"/>
  </section>
 </xsl:template>

 <xsl:template match="st:subsubsection" mode="paragraph">
  <section>
   <title><xsl:value-of select="st:subsubtitle/st:textsequence"/></title>
   <xsl:apply-templates select="st:subsubparagraphs/st:paragraph/*" mode="paragraph"/>
  </section>
 </xsl:template>

 <xsl:template match="st:source" mode="paragraph">
  <source>
   <xsl:value-of select="substring(.,4,string-length(.)-6)"/>
  </source>
 </xsl:template>

 <xsl:template match="st:textsequence" mode="paragraph">
  <p>
   <xsl:apply-templates select="st:textblock/*|st:break"/>
  </p>
 </xsl:template>

 <xsl:template match="st:line" mode="paragraph">
  <p>--------------------------------------------------------------------------------</p>
 </xsl:template>

 <xsl:template match="st:table" mode="paragraph"> 
  <table>
   <xsl:apply-templates select="st:tablehead|st:tablerows/st:tablecolumns"/>
  </table>
 </xsl:template>

 <xsl:template match="st:tablehead"> 
  <tr>
   <xsl:apply-templates select="st:tabletitle"/>
  </tr>
 </xsl:template>

 <xsl:template match="st:tabletitle">
  <th>
   <xsl:apply-templates select="st:textblock/*"/>
  </th>
 </xsl:template>

 <xsl:template match="st:tablecolumns">
  <tr>
   <xsl:apply-templates select="st:tablecolumn"/>
  </tr>
 </xsl:template>

 <xsl:template match="st:tablecolumn">
  <td>
   <xsl:apply-templates select="st:textblock/*"/>
  </td>
 </xsl:template>

 <xsl:template match="st:text">
  <xsl:value-of select="."/><xsl:text> </xsl:text>
 </xsl:template>

 <xsl:template match="st:break">
  <br/>
 </xsl:template>

 <xsl:template match="st:link">
  <xsl:choose>
   <xsl:when test="contains(.,'|')">

    <xsl:variable name="href" select="substring-before(substring-after(.,'|'),']')"/>
    <xsl:variable name="text" select="substring-after(substring-before(.,'|'),'[')"/>

    <xsl:choose>
     <xsl:when test="string(number($href)) != 'NaN'">
      <link href="#{$href}">
       <xsl:value-of select="$text"/>
      </link>
     </xsl:when>
     <xsl:when test="contains($href,'.png') or contains($href,'.jpg') or contains($href,'.gif')">
      <img src="{$href}" alt="{$text}"/>
     </xsl:when>
     <xsl:when test="contains($href,':') or contains($href,'.')">
      <link href="{$href}">
       <xsl:value-of select="$text"/>
      </link>
     </xsl:when>
     <xsl:otherwise>
      <link>
       <xsl:attribute name="href">
      <xsl:choose>
       <xsl:when test="$spaceless-filenames">
          <xsl:value-of select="concat(translate($href,' ',''),'.html')"/>
       </xsl:when>
       <xsl:otherwise>
          <xsl:value-of select="concat('view.do?page=',$href)"/>
       </xsl:otherwise>
      </xsl:choose>
       </xsl:attribute>
       <xsl:value-of select="$text"/>
      </link>
     </xsl:otherwise>
    </xsl:choose>

   </xsl:when>
   <xsl:otherwise>
    <xsl:variable name="href" select="substring(.,2,string-length(.)-2)"/>
    
    <xsl:choose>
     <xsl:when test="string(number($href)) != 'NaN'">
      <link href="#{$href}">
       [<xsl:value-of select="$href"/>]
      </link>
     </xsl:when>
     <xsl:when test="contains($href,'.png') or contains($href,'.jpg') or contains($href,'.gif')">
      <img src="{$href}" alt="{$href}"/>
     </xsl:when>
     <xsl:when test="contains($href,':') or contains($href,'.')">
      <link href="{$href}">
       <xsl:value-of select="$href"/>
      </link>
     </xsl:when>
     <xsl:otherwise>
      <link>
       <xsl:attribute name="href">
      <xsl:choose>
       <xsl:when test="$spaceless-filenames">
          <xsl:value-of select="concat(translate($href,' ',''),'.html')"/>
       </xsl:when>
       <xsl:otherwise>
          <xsl:value-of select="concat('view.do?page=',$href)"/>
       </xsl:otherwise>
      </xsl:choose>
       </xsl:attribute>
       <xsl:value-of select="$href"/>
      </link>
     </xsl:otherwise>
    </xsl:choose>

   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <xsl:template match="st:anchor" >
  <p>
  <xsl:choose>
   <xsl:when test="contains(.,'|')">
    <anchor id="{substring-before(substring-after(.,'|#'),']')}">
     <xsl:value-of select="substring-after(substring-before(.,'|'),'[')"/>
    </anchor>
   </xsl:when>
   <xsl:otherwise>
    <em><xsl:value-of select="substring(.,3,string-length(.)-3)"/>:</em>
    <anchor id="{substring(.,3,string-length(.)-3)}"/>
   </xsl:otherwise>
  </xsl:choose>
  </p>
 </xsl:template>

 <xsl:template match="st:emblock">
  <em><xsl:value-of select="st:text"/></em><xsl:text> </xsl:text>
 </xsl:template>

 <xsl:template match="st:strongblock">
  <strong><xsl:value-of select="st:text"/></strong><xsl:text> </xsl:text>
 </xsl:template>

 <xsl:template match="st:bulletedlist1" mode="paragraph">
  <ul>
   <xsl:apply-templates select="st:bulletedlistitem1|st:bulletedlist2"/>
  </ul>
 </xsl:template>
 
 <xsl:template match="st:bulletedlistitem1" >
   <li>
    <xsl:apply-templates select="st:textsequence/st:textblock/*"/>
   </li>
 </xsl:template>
 
 <xsl:template match="st:bulletedlist2" >
  <ul>
   <xsl:apply-templates select="st:bulletedlistitem2|st:bulletedlist3"/>
  </ul>
 </xsl:template>

 <xsl:template match="st:bulletedlistitem2" >
  <li>
   <xsl:apply-templates select="st:textsequence/st:textblock/*"/>
  </li>
 </xsl:template>

 <xsl:template match="st:bulletedlist3" >
  <ul>
   <xsl:apply-templates select="st:bulletedlistitem3"/>
  </ul>
 </xsl:template>
    
 <xsl:template match="st:bulletedlistitem3" >
  <li>
   <xsl:apply-templates select="st:textsequence/st:textblock/*"/>
  </li>
 </xsl:template>


 <xsl:template match="st:numberedlist1" mode="paragraph">
  <ol>
   <xsl:apply-templates select="st:numberedlistitem1|st:numberedlist2"/>
  </ol>
 </xsl:template>

 <xsl:template match="st:numberedlistitem1" >
  <li>
   <xsl:apply-templates select="st:textsequence/st:textblock/*"/>
  </li>
 </xsl:template>

 <xsl:template match="st:numberedlist2" >
  <ol>
   <xsl:apply-templates select="st:numberedlistitem2|st:numberedlist3"/>
  </ol>
 </xsl:template>
    
 <xsl:template match="st:numberedlistitem2" >
  <li>
   <xsl:apply-templates select="st:textsequence/st:textblock/*"/>
  </li>
 </xsl:template>

 <xsl:template match="st:numberedlist3" >
  <ol>
   <xsl:apply-templates select="st:numberedlistitem3"/>
  </ol>
 </xsl:template>
    
 <xsl:template match="st:numberedlistitem3" >
  <li>
   <xsl:apply-templates select="st:textsequence/st:textblock/*"/>
  </li>
 </xsl:template>

 <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
  <xsl:copy>
   <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
  </xsl:copy>
 </xsl:template>

</xsl:stylesheet>
