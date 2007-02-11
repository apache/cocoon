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

<!-- CVS $Id: directory2html.xslt,v 1.4 2004/03/06 02:25:41 antonio Exp $ -->

<xsl:stylesheet version="1.0"
                xmlns:pom="http://maven.apache.org/POM/4.0.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/">
    <root>
      <xsl:apply-templates/>
    </root>
  </xsl:template>

  <xsl:template match="root">
    <xsl:for-each select="pom/pom:project/pom:dependencies/pom:dependency">
      <!--xsl:sort select="pom:groupId"/-->
      <xsl:sort select="pom:artifactId"/>
      <xsl:sort select="pom:version"/>
      <xsl:value-of select="pom:groupId"/><xsl:text>	</xsl:text>
      <xsl:value-of select="pom:artifactId"/><xsl:text>	</xsl:text>
      <xsl:value-of select="pom:version"/><xsl:text>	</xsl:text>
      <xsl:value-of select="../../../@file"/><xsl:text>
</xsl:text>
    </xsl:for-each>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="node()|@*" priority="-1">
    <xsl:apply-templates/>
  </xsl:template>

</xsl:stylesheet>
