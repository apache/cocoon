<?xml version="1.0" encoding="UTF-8"?>
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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:osm="http://osmosis.gr/osml/1.0">
  <xsl:template match="osm:list">
    <ul>
      <xsl:apply-templates/>
    </ul>
  </xsl:template>
  <xsl:template match="osm:listItem">
    <xsl:choose>
      <xsl:when test="@href">
        <li>
          <xsl:attribute name="class">
            <xsl:value-of select="@class"/>
          </xsl:attribute>
          <a>
            <xsl:attribute name="href">
              <xsl:value-of select="@href"/>
            </xsl:attribute>
            <xsl:value-of select="." disable-output-escaping="yes"/>
          </a>
        </li>
      </xsl:when>
      <xsl:otherwise>
        <li>
          <xsl:attribute name="class">
            <xsl:value-of select="@class"/>
          </xsl:attribute>
          <xsl:value-of select="." disable-output-escaping="yes"/>
        </li>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
