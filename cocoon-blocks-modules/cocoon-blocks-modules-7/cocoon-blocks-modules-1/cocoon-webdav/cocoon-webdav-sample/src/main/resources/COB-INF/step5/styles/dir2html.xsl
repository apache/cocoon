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
                xmlns:collection="http://apache.org/cocoon/collection/1.0">

<xsl:template match="/collection:collection">
  <html>
    <body>
    Files:
      <ul>
        <xsl:apply-templates select="collection:resource"/>
      </ul>
      <a href="new">new file</a>
      <br /><br /><br /><br />
    Folders:
      <ul>
        <xsl:apply-templates select="collection:collection"/>
      </ul>
    </body>
  </html>
</xsl:template>

<xsl:template match="collection:resource">
  <li><a href="{@name}"><xsl:value-of select="@name"/></a></li>
</xsl:template>

<xsl:template match="collection:collection">
  <li><a href="{@name}/"><xsl:value-of select="@name"/></a></li>
</xsl:template>

</xsl:stylesheet>
