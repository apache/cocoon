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
                xmlns:req="http://apache.org/cocoon/request/2.0">
<xsl:template match="/req:request">
  <request>
    <xsl:apply-templates select="req:requestParameters"/>
  </request>
</xsl:template>

<xsl:template match="req:requestParameters">
  <parameters>
    <xsl:apply-templates select="req:parameter[@name='title']"/>
    <xsl:apply-templates select="req:parameter[@name='para']"/>
  </parameters>
</xsl:template>

<xsl:template match="req:parameter[@name='title']">
  <title>
    <xsl:value-of select="req:value"/>
  </title>
</xsl:template>

<xsl:template match="req:parameter[@name='para']">
  <content>
    <xsl:for-each select="req:value">
      <para><xsl:value-of select="normalize-space(.)"/></para>
    </xsl:for-each>
  </content>
</xsl:template>

</xsl:stylesheet>
