<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:st="http://chaperon.sourceforge.net/schema/syntaxtree/1.0"
                exclude-result-prefixes="st">

 <xsl:output indent="yes" 
             method="html"
             doctype-public="-//APACHE//DTD Documentation V1.1//EN"
             doctype-system="document-v11.dtd"
             cdata-section-elements="source"/>

 <xsl:template match="st:document">
  <document>
   <header>
    <title><xsl:value-of select="st:section/st:title/st:textsequence"/></title>
   </header>
   <body>
    <xsl:apply-templates select="st:paragraphs/st:paragraph/*" mode="paragraph"/>  
    <xsl:apply-templates select="st:section"/>
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
     <xsl:when test="contains($href,':') or contains($href,'.')">
      <link href="{$href}">
       <xsl:value-of select="$text"/>
      </link>
     </xsl:when>
     <xsl:otherwise>
      <link href="{$href}.html">
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
      <link href="{$href}.html">
       <xsl:value-of select="$href"/>
      </link>
     </xsl:otherwise>
    </xsl:choose>

   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <xsl:template match="st:anchor" >
  <xsl:choose>
   <xsl:when test="contains(.,'|')">
    <anchor name="{substring-before(substring-after(.,'|#'),']')}">
     <xsl:value-of select="substring-after(substring-before(.,'|'),'[')"/>
    </anchor>
   </xsl:when>
   <xsl:otherwise>
    <anchor name="{substring(.,3,string-length(.)-3)}"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <xsl:template match="st:emblock">
  <em><xsl:value-of select="st:text"/></em><xsl:text> </xsl:text>
 </xsl:template>

 <xsl:template match="st:strongblock">
  <strong><xsl:value-of select="st:text"/></strong><xsl:text> </xsl:text>
 </xsl:template>

 <xsl:template match="st:codeblock">
  <code><xsl:value-of select="st:text"/></code><xsl:text> </xsl:text>
 </xsl:template>

 <xsl:template match="st:bulletedlist" mode="paragraph">
  <ul>
   <xsl:apply-templates select="st:bulletedlistitem"/>
  </ul>
 </xsl:template>

 <xsl:template match="st:bulletedlistitem" >
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
