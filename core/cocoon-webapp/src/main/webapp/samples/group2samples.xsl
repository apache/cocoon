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

<!--+
    | Convert the output of the directory generator into a samples file.
    |
    | $Id$
    +-->

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:dir="http://apache.org/cocoon/directory/2.0">

  <xsl:template match="/">
    <samples name="Cocoon Blocks">
      <xsl:apply-templates/>
    </samples>    
  </xsl:template>
  
  <xsl:template match="group">
    <xsl:copy>
      <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="sample">
    <sample href="../blocks/{../../../../@name}/" name="{@name}">
      <xsl:copy-of select="*|text()"/>
    </sample>
  </xsl:template>
  
  <xsl:template match="*|@*|node()" priority="-2">
     <xsl:apply-templates select="@*|node()"/>
  </xsl:template>
  
</xsl:stylesheet>
