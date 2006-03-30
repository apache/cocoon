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

<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:st="http://chaperon.sourceforge.net/schema/syntaxtree/2.0"
    xmlns:text="http://chaperon.sourceforge.net/schema/text/1.0"
    exclude-result-prefixes="st">

 <xsl:output encoding="ASCII"/>

 <xsl:template match="st:string">
  <text:text><xsl:value-of select="substring(.,2,string-length(.)-2)"/></text:text>
 </xsl:template>

  <xsl:template match="@*|*|text()|processing-instruction()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
