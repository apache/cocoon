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

<!--
This stylesheet filters all references to the javadocs
and the samples.
-->
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    version="1.0">

  <xsl:template match="@src|@href|@background">
    <xsl:if test="not(contains(.,'apidocs')) and not(starts-with(., 'samples/'))">
      <xsl:copy>
        <xsl:apply-templates select="."/>
      </xsl:copy>
    </xsl:if>
  </xsl:template>

  <!-- This is a hack which makes the javascript images work -->
  <xsl:template match="img[@onLoad and starts-with(@src, 'graphics')]">
      <img src="{@src}"/>
      <img>
        <xsl:attribute name="src">
          <xsl:value-of select="substring-before(@src, '.')"/>_over.<xsl:value-of select="substring-after(@src, '.')"/>
        </xsl:attribute>
      </img>
  </xsl:template>

  <xsl:template match="img[@onLoad and starts-with(@src, 'images') and contains(@src, '-lo.gif')]">
      <img src="{@src}"/>
      <img>
        <xsl:attribute name="src"><xsl:value-of select="substring-before(@src, '-lo.gif')"/>-hi.gif</xsl:attribute>
      </img>
  </xsl:template>

  <xsl:template match="@*|node()">
   <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
  </xsl:template>

</xsl:stylesheet>