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
                xmlns:req="http://apache.org/cocoon/request/2.0"
                xmlns:source="http://apache.org/cocoon/source/1.0">

<xsl:param name="file"></xsl:param>

<xsl:template match="request/parameters">
<page>
  <source:write create="true">
    <xsl:if test="filename">
      <source:source><xsl:value-of select="$file"/><xsl:value-of select="filename"/></source:source>
    </xsl:if>
    <xsl:if test="not(filename)">
      <source:source><xsl:value-of select="$file"/></source:source>
    </xsl:if>
    <source:path>page</source:path>
    <source:fragment>
      <title><xsl:value-of select="title"/></title>
      <content>
        <xsl:for-each select="content/para">
        <para>
        <xsl:value-of select="normalize-space(.)"/>
        </para>
        </xsl:for-each>
      </content>
    </source:fragment>
  </source:write>

  <source:write create="true">
    <xsl:if test="filename">
      <source:source><xsl:value-of select="$file"/><xsl:value-of select="filename"/>.meta</source:source>
    </xsl:if>
    <xsl:if test="not(filename)">
      <source:source><xsl:value-of select="$file"/>.meta</source:source>
    </xsl:if>
    <source:source><xsl:value-of select="$file"/>.meta</source:source>
    <source:path>metapage</source:path>
    <source:fragment>
      <author><xsl:value-of select="author"/></author>
      <category><xsl:value-of select="category"/></category>
      <state><xsl:value-of select="state"/></state>
    </source:fragment>
  </source:write>
</page>
</xsl:template>

</xsl:stylesheet>
