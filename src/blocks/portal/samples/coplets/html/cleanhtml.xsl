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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- 
This stylesheet simply removes the surrounding html and body tag

$Id: cleanhtml.xsl,v 1.2 2004/03/06 02:26:04 antonio Exp $ 

-->
<xsl:template match="/" xmlns:xhtml="http://www.w3.org/1999/xhtml">
  <xsl:apply-templates select="xhtml:html/xhtml:body"/>
  <xsl:apply-templates select="html/body"/>
</xsl:template>

<xsl:template match="xhtml:body" xmlns:xhtml="http://www.w3.org/1999/xhtml">
  <div>
    <xsl:copy-of select="*"/>
  </div>
</xsl:template>

<xsl:template match="body">
  <div>
    <xsl:copy-of select="*"/>
  </div>
</xsl:template>

</xsl:stylesheet>
