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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:variable name="notes" select="//NOTE_ON"/>
  <xsl:variable name="medianPitch" select="floor(sum($notes/@PITCH) div count($notes))"/>

  <xsl:template match="NOTE_ON">
    <NOTE_ON PITCH="{$medianPitch * 2 - @PITCH}">
      <xsl:copy-of select="@REGISTER | @VELOCITY"/>
      <xsl:apply-templates select="node()"/>
    </NOTE_ON>
  </xsl:template>

  <xsl:template match="node()">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>

