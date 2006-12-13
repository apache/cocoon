<?xml version="1.0"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

<!-- CVS $Id$ -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/2000/svg">

 <xsl:template match="page">
  <svg width="500" height="160">
   <defs>
    <filter id="blur1"><feGaussianBlur stdDeviation="3"/></filter>
    <filter id="blur2"><feGaussianBlur stdDeviation="1"/></filter>
   </defs>
   
   <g title="this is a tooltip">
    <rect 
      style="fill:#0086B3;stroke:#000000;stroke-width:4;filter:url(#blur1);"
      x="30" y="30" rx="20" ry="20" width="450" height="80"/>
    <xsl:apply-templates/>
   </g>
  </svg>
 </xsl:template>
 
 <xsl:template match="para">
  <text style="fill:#FFFFFF;font-size:24;font-family:TrebuchetMS-Bold;filter:url(#blur2);" x="65" y="80">
   <xsl:apply-templates/>
  </text>
 </xsl:template>

</xsl:stylesheet>
