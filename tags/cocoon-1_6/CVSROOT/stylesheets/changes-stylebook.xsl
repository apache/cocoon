<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

 <xsl:import href="./document-stylebook.xsl"/>

 <xsl:template match="changes">
  <s1 title="Changes">
   <s2 title="Main Developers">
    <ul>
     <xsl:for-each select="devs/person">
      <li>
       <anchor name="{@id}"/>
       <jump href="mailto:{@email}">
        <xsl:value-of select="@name"/>
       </jump>
      </li>
     </xsl:for-each> 
    </ul>
   </s2>
   <xsl:apply-templates/>
  </s1>
 </xsl:template>

 <xsl:template match="release">
  <s2 title="Cocoon {@version} ({@date})">
   <ul>
    <xsl:apply-templates/>
   </ul>
  </s2>
 </xsl:template>
 
 <xsl:template match="action">
  <li>
   <img src="images/{@type}.jpg" alt="{@type}"/>
   <xsl:apply-templates/>
   <xsl:text>(</xsl:text>
   <jump href="#{@dev}">
    <xsl:value-of select="@dev"/>
   </jump>
   <xsl:text>)</xsl:text>
   
   <xsl:if test="@due-to">
    <xsl:text> Thanks to </xsl:text>
    <jump href="mailto:{@due-to-email}"><xsl:value-of select="@due-to"/></jump>
    <xsl:text>.</xsl:text>
   </xsl:if>
  </li>
 </xsl:template>

</xsl:stylesheet>
