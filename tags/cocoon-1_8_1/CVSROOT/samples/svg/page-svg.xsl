<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:template match="page">
  <xsl:processing-instruction name="cocoon-format">type="image/svg-xml"</xsl:processing-instruction>
  <svg xml:space="preserve" width="360" height="160" xmlns:xlink="http://www.w3.org/2000/xlink/namespace/">
   <defs>
    <filter id="blur1"><feGaussianBlur stdDeviation="3"/></filter>
    <filter id="blur2"><feGaussianBlur stdDeviation="1"/></filter>
   </defs>
   <g title="this is a tooltip">
    <rect 
      style="fill:#0086B3;stroke:#000000;stroke-width:4;filter:url(#blur1);"
      x="30" y="30" rx="20" ry="20" width="300" height="100"/>
    <text style="fill:#FFFFFF;font-size:24;font-family:TrebuchetMS-Bold;filter:url(#blur2);" x="65" y="80">
     <xsl:value-of select="content/p[1]"/>
    </text>
    <text style="font-size:14;font-family:TrebuchetMS;" x="65" y="110">
     <xsl:value-of select="content/p[2]"/>
    </text>
   </g>
  </svg>
 </xsl:template>
</xsl:stylesheet>
