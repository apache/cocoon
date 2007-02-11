<?xml version="1.0" encoding="UTF-8"?>
<!--
  Copyright 2002-2004 The Apache Software Foundation

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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:n="http://www.betaversion.org/linotype/news/1.0">

  <xsl:param name="home"/>

  <xsl:template match="/">
   <html>
    <head>
     <title>News History</title>
     <link rel="stylesheet" type="text/css" href="{$home}/styles/main.css"/>
    </head>
    <body>
    
     <div id="path"> 
	  <a href="../">Home</a> &#187; <a href="./">Blog</a> &#187; History
	 </div>
	 
     <div id="page">
      <h1>News History</h1>
      <h2>The whole braindump</h2>
      <xsl:apply-templates select="//n:news"/>
     </div>

     <div id="sidebar">
      <a href="{$home}/"><img alt="Linotype" src="{$home}/images/linotype.jpg" width="156px" height="207px" /></a>
     </div>

     <div id="bottombar">
      <a href="http://cocoon.apache.org" title="Apache Cocoon"><img alt="Powered by Cocoon" src="{$home}/images/cocoon.jpg"/></a>
     </div>
     
    </body>
   </html>
  </xsl:template>

  <xsl:template match="n:news">
   <xsl:variable name="id" select="../@id"/>
   <div class="news">
    <h1><img src="{$home}/images/hand.jpg" alt=""/><a href="news/{$id}/"><xsl:value-of select="n:title"/></a></h1>
    <h2><xsl:value-of select="@creation-date"/> ~ <xsl:value-of select="@creation-time"/></h2>
   </div>
  </xsl:template>

</xsl:stylesheet>
