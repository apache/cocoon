<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- default copy-over's -->

 <xsl:template match="*|/">
  <xsl:apply-templates/>
 </xsl:template>

 <xsl:template match="text()|@*">
  <xsl:value-of select="."/>
 </xsl:template>

<!-- document section -->

 <xsl:template match="document">
  <s1 title="{header/title}">
   <xsl:apply-templates/>
  </s1>
 </xsl:template>

<!-- header section -->

 <xsl:template match="header">
  <!-- ignore -->
 </xsl:template>

<!-- body section -->

 <xsl:template match="body">
  <xsl:apply-templates/>
 </xsl:template>

 <xsl:template match="s1">
  <s2 title="{@title}">
   <xsl:apply-templates/>
  </s2>
 </xsl:template>

 <xsl:template match="s2">
  <s3 title="{@title}">
   <xsl:apply-templates/>
  </s3>
 </xsl:template>

 <xsl:template match="s3|s4">
  <s4 title="{@title}">
   <xsl:apply-templates/>
  </s4>
 </xsl:template>

<!-- footer section -->

 <xsl:template match="footer">
  <!-- ignore -->
 </xsl:template>

<!-- links -->

 <xsl:template match="link|jump|fork">
  <jump href="{@href}">
   <xsl:apply-templates/>
  </jump>
 </xsl:template>

 <xsl:template match="connect">
  <xsl:apply-templates/>
 </xsl:template>

 <xsl:template match="anchor">
  <anchor name="{@name}"/>
 </xsl:template>

<!-- paragraphs -->

 <xsl:template match="p|note|source">
  <xsl:copy>
   <xsl:apply-templates/>
  </xsl:copy>
 </xsl:template>
  
 <xsl:template match="fixme">
  <!-- ignore on documentation -->
 </xsl:template>

<!-- lists -->

 <xsl:template match="ul|ol|li">
  <xsl:copy>
   <xsl:apply-templates/>
  </xsl:copy>
 </xsl:template>

 <xsl:template match="sl|dl">
  <ul>
   <xsl:apply-templates/>
  </ul>
 </xsl:template>

 <xsl:template match="dt">
  <li>
   <em><xsl:value-of select="."/></em>
   <xsl:text> - </xsl:text>
   <xsl:value-of select="following::dd"/>   
  </li>
 </xsl:template>
 
 <xsl:template match="dd">
  <!-- ignore -->
 </xsl:template>

<!-- table (just copy over) -->

 <xsl:template match="table|tr">
  <xsl:copy>
   <xsl:apply-templates/>
  </xsl:copy>
 </xsl:template>

 <xsl:template match="caption|colgroup|thead|tfoot|tbody">
  <!-- ignore -->
 </xsl:template>
 
 <xsl:template match="th">
  <th colspan="{@colspan}" rowspan="{@rowspan}">
   <xsl:apply-templates/>
  </th>
 </xsl:template>

 <xsl:template match="td">
  <td colspan="{@colspan}" rowspan="{@rowspan}">
   <xsl:apply-templates/>
  </td>
 </xsl:template>

<!-- specials -->

 <xsl:template match="br">
  <xsl:copy>
   <xsl:apply-templates/>
  </xsl:copy>
 </xsl:template>

 <xsl:template match="img-block">
  <p>
   <img src="{@src}" alt="{@alt}"/>
  </p>
 </xsl:template>
 
 <xsl:template match="img">
  <img src="{@src}" alt="{@alt}"/>
 </xsl:template>

<!-- text markup -->

 <xsl:template match="strong">
  <em>
   <xsl:apply-templates/>
  </em>
 </xsl:template>

 <xsl:template match="em">
  <ref>
   <xsl:apply-templates/>
  </ref>
 </xsl:template>

 <xsl:template match="code">
  <xsl:copy>
   <xsl:apply-templates/>
  </xsl:copy>
 </xsl:template>
 
 <xsl:template match="sub|sup">
  <xsl:apply-templates/>
 </xsl:template>

</xsl:stylesheet>
