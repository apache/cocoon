<?xml version="1.0" encoding="ISO-8859-1"?>
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

<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:cinclude="http://apache.org/cocoon/include/1.0"
>

  <xsl:template match="files">
    <includes>
      <path><xsl:value-of select="path"/></path>
      <xsl:apply-templates/>
    </includes>
  </xsl:template >

  <xsl:template match="file">
    <file>
      <name><xsl:value-of select="path"/></name>
      <include>
      <cinclude:includexml ignoreErrors="true">
        <cinclude:src><xsl:value-of select="absolutePath"/></cinclude:src>
      </cinclude:includexml>
      </include>
    </file>
  </xsl:template >

  <xsl:template match="@*|node()" priority="-1"></xsl:template>
  <xsl:template match="text()" priority="-1"></xsl:template>
</xsl:stylesheet> 