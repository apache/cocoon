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

<!-- CVS $Id: directory2xsamples.xsl,v 1.1 2004/04/02 20:40:57 joerg Exp $ -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dir="http://apache.org/cocoon/directory/2.0"
                exclude-result-prefixes="dir">

  <xsl:template match="/dir:directory">
    <xsamples>
      <xsl:apply-templates select="*"/>
    </xsamples>
  </xsl:template>

  <xsl:template match="dir:directory">
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="dir:file">
    <xsl:if test="substring-before(@name, '.xsamples')">
      <sample name="cocoon-block-{substring-before(@name, '.xsamples')}"/>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
