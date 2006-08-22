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

<!--
    Templates for transforming presentation heading to HTML
    $Id$
-->
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

    <!-- presentation info: display field name + value -->
    <xsl:template mode="heading" match="*">
        <div class="headingField">
            <div class="fieldName">
                <xsl:value-of select="concat(name(),':')"/>
            </div>
            <div class="fieldValue">
                <xsl:value-of select="."/>
            </div>
        </div>
    </xsl:template>

    <!-- presentation info: comment lines -->
    <xsl:template mode="heading" match="line">
        <p class="commentLine">
            <xsl:value-of select="."/>
        </p>
    </xsl:template>

    <!-- presentation info: title -->
    <xsl:template mode="heading" match="presentation">
        <h1><xsl:value-of select="."/></h1>
    </xsl:template>

    <!-- omit some presentation fields -->
    <xsl:template mode="heading" match="image-directory"/>

</xsl:stylesheet>
