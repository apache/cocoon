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

<!-- CVS $Id: error2html-debug.xslt,v 1.2 2004/03/06 02:26:08 antonio Exp $ -->

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:error="http://apache.org/cocoon/error/2.1">

<xsl:param name="home" select="string('')"/>

<xsl:template match="error:notify">
 <html>
  <head>
   <title>
    <xsl:value-of select="error:title"/>
   </title>
   <link href="{$home}/styles/main.css" type="text/css" rel="stylesheet"/>
   <style>
        body { padding: 20px }
		p.message { padding: 10px 30px 10px 30px; font-weight: bold; font-size: 130%; border-width: 1px; border-style: dashed; border-color: #336699; }
		p.description { padding: 10px 30px 20px 30px; border-width: 0px 0px 1px 0px; border-style: solid; border-color: #336699;}
		p.topped { padding-top: 10px; border-width: 1px 0px 0px 0px; border-style: solid; border-color: #336699; }
		span.description { color: #336699; font-weight: bold; }
		span.switch { cursor: pointer; margin-left: 5px; text-decoration: underline; }
   </style>
 <script><![CDATA[
function toggle(id) {
    var element = document.getElementById(id);
    with (element.style) {
        if ( display == "none" ){
            display = ""
        } else{
            display = "none"
        }
    }
    var text = document.getElementById(id + "-switch").firstChild;
    if (text.nodeValue == "[show]") {
        text.nodeValue = "[hide]";
    } else {
        text.nodeValue = "[show]";
    }
}
]]></script>
  </head>
  <body>
   <h1><xsl:value-of select="error:title"/></h1>

   <p class="message">
    <xsl:call-template name="returns2br">
     <xsl:with-param name="string" select="error:message"/>
    </xsl:call-template>
   </p>

   <p class="description">
    <xsl:call-template name="returns2br">
     <xsl:with-param name="string" select="error:description"/>
    </xsl:call-template>
   </p>

   <xsl:apply-templates select="error:extra"/>

   <p class="topped">
    The <a href="http://cocoon.apache.org/">Apache Cocoon</a> Project
   </p>
  </body>
 </html>
</xsl:template>

<xsl:template match="error:extra">
 <xsl:choose>
  <xsl:when test="contains(@error:description,'stacktrace')">
   <p class="stacktrace">
    <span class="description"><xsl:value-of select="@error:description"/></span>
    <span class="switch" id="{@error:description}-switch" onclick="toggle('{@error:description}')">[show]</span>
    <pre id="{@error:description}" style="display: none">
     <xsl:call-template name="returns2br">
      <xsl:with-param name="string" select="."/>
     </xsl:call-template>
    </pre>
   </p>
  </xsl:when>
  <xsl:otherwise>
   <p class="extra">
    <span class="description"><xsl:value-of select="@error:description"/>:&#160;</span>
    <xsl:call-template name="returns2br">
     <xsl:with-param name="string" select="."/>
    </xsl:call-template>
   </p>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<xsl:template name="returns2br">
  <xsl:param name="string"/>
  <xsl:variable name="return" select="'&#xa;'"/>
  <xsl:choose>
    <xsl:when test="contains($string,$return)">
      <xsl:value-of select="substring-before($string,$return)"/>
      <br/>
      <xsl:call-template name="returns2br">
        <xsl:with-param name="string" select="substring-after($string,$return)"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$string"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
