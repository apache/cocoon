<?xml version="1.0"?>
<!--
  Copyright 1999-2005 The Apache Software Foundation

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
<!-- $Id$ 

-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:java="http://xml.apache.org/xalan/java">

<!-- The current picture to display -->
<xsl:param name="pic"/>
<xsl:param name="tpic"/>

<xsl:template match="pictures" xmlns:cl="http://apache.org/cocoon/portal/coplet/1.0">
    <xsl:choose>
        <xsl:when test="$pic=''">
            <p>Please choose a picture in the gallery.</p>
        </xsl:when>
        <xsl:otherwise>
            <img src="{$pic}"/>
            <p><cl:link path="temporaryAttributes/pictitle" value="{$pic}" coplet="GalleryViewer-1">Show file name</cl:link>
            <br/><xsl:value-of select="$tpic"/></p>
            <p>Date: <xsl:value-of select="java:java.util.Date.new()"/></p>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

</xsl:stylesheet>
