<?xml version="1.0"?>
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

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:dir="http://apache.org/cocoon/directory/2.0"
 exclude-result-prefixes="dir">
 
  <xsl:param name="prefix"/>
  <xsl:param name="count"/>
  
  <xsl:template match="/">
   <list>
    <xsl:for-each select="dir:directory/dir:directory[not(starts-with(@name,'template')) and not(starts-with(@name,'CVS'))]">
     <xsl:if test="position() &lt;= number($count)">
      <xsl:apply-templates select="."/>
     </xsl:if>
    </xsl:for-each>
   </list>
  </xsl:template>
    
  <xsl:template match="dir:directory">
    <element id="{@name}">
     <include:include src="cocoon:/{$prefix}/{@name}.xml" xmlns:include="http://apache.org/cocoon/include/1.0"/>
    </element>
  </xsl:template>

</xsl:stylesheet>
