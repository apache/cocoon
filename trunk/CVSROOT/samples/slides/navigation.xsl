<?xml version="1.0"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="section"/>
  <xsl:param name="slide"/>

  <xsl:template match="slides">

   <xsl:processing-instruction name="cocoon-process">type="xslt"</xsl:processing-instruction>
   <xsl:processing-instruction name="xml-stylesheet">href="<xsl:value-of select="@style"/>.xsl" type="text/xsl"</xsl:processing-instruction>

   <slide style="{@style}">
     <xsl:if test="not($section)">
       <title>
        <xsl:value-of select="title"/>
       </title>
       <navigation>
        <next href="?section=1"/>
       </navigation>
       <layout>
        <h1><a href="?section=1"><xsl:value-of select="title"/></a></h1>
        <h3>
         <xsl:for-each select="authors/person">
          <xsl:value-of select="name"/> (<xsl:value-of select="email"/>)
         </xsl:for-each>
        </h3>
        <p class="quote">
         <xsl:value-of select="overview"/>
        </p>
       </layout>
     </xsl:if>

     <xsl:if test="$section">
      <xsl:if test="not($slide)">
       <xsl:apply-templates select="//section[$section]"/>
      </xsl:if>
      <xsl:if test="$slide">
       <xsl:apply-templates select="//section[$section]/slide[$slide]"/>
      </xsl:if>
     </xsl:if>
   </slide>
  </xsl:template>

<!-- ================================ Section ============================== -->

  <xsl:template match="section">
    <title>
     <xsl:value-of select="title"/>
    </title>

    <navigation>
     <xsl:variable name="last">
      <xsl:value-of select="count(//section)"/>
     </xsl:variable>
     <xsl:variable name="previous-last">
      <xsl:value-of select="count(//section[$section - 1]/slide)"/>
     </xsl:variable>

     <home href=""/>

     <xsl:if test="$section!=1">
      <previous-section href="?section={$section - 1}"/>
      <previous href="?section={$section - 1}&amp;slide={$previous-last}"/>
     </xsl:if>

     <xsl:if test="$section!=$last">
      <next-section href="?section={$section + 1}"/>
     </xsl:if>

     <next href="?section={$section}&amp;slide=1"/>
    </navigation>

    <layout>
     <title><xsl:value-of select="title"/></title>
     <subtitle><xsl:value-of select="subtitle"/></subtitle>
    </layout>
  </xsl:template>

<!-- ================================ Slide ============================== -->

  <xsl:template match="slide">
    <title>
     <xsl:value-of select="title"/>
    </title>

    <navigation>
     <xsl:variable name="last-section">
      <xsl:value-of select="count(//section)"/>
     </xsl:variable>
     <xsl:variable name="last">
      <xsl:value-of select="count(//section[$section]/slide)"/>
     </xsl:variable>
     <xsl:variable name="previous-last">
      <xsl:value-of select="count(//section[$section - 1]/slide)"/>
     </xsl:variable>

     <home href="?section={$section}"/>

     <xsl:if test="$slide!=1">
      <previous href="?section={$section}&amp;slide={$slide - 1}"/>
     </xsl:if>
     <xsl:if test="($slide=1)">
      <previous href="?section={$section}"/>
     </xsl:if>

     <xsl:if test="$slide!=$last">
      <next href="?section={$section}&amp;slide={$slide + 1}"/>
     </xsl:if>
     <xsl:if test="($slide=$last) and ($section!=$last-section)">
      <next href="?section={$section + 1}"/>
     </xsl:if>

     <xsl:if test="$section!=1">
      <previous-section href="?section={$section - 1}"/>
     </xsl:if>
     <xsl:if test="$section!=$last-section">
      <next-section href="?section={$section + 1}"/>
     </xsl:if>
    </navigation>

    <layout>
     <xsl:apply-templates/>
    </layout>
  </xsl:template>
  
  <xsl:template match="*|@*|text()">
   <xsl:copy>
    <xsl:apply-templates select="*|@*|text()"/>
   </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>