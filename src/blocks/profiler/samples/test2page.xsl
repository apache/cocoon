<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

 <xsl:template match="hello-world">
  <page>
   <tab title="Overview" href="welcome"/>

   <row>
    <column title="{title}">
     <xsl:apply-templates select="some-text"/>
    </column>
   </row>
 
  </page>
 </xsl:template>

 <xsl:template match="some-text">
  <p>
   <xsl:apply-templates/>
  </p>
 </xsl:template>

</xsl:stylesheet>
