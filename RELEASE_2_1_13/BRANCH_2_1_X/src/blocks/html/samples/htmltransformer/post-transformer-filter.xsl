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
  Filter the output of the HTMLTransformer
  
  CVS $Id: samples.xml 158439 2005-03-21 10:17:23Z cziegeler $
-->

<xsl:transform
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

  <xsl:param name="title" select="'HTMLTransformer output'"/>

  <!-- convert to an html document -->
  <xsl:template match="/">
    <html>
      <head>
        <style type="text/css">
          .note { font-size: 80%; font-style=italic; }
          .parsedHtml { margin: 1em; border: solid gray 1px; }
        </style>
        <title><xsl:value-of select="$title"/></title>
      </head>
      <body>
        <h1><xsl:value-of select="$title"/></h1>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <!--
    The HTMLTransformer produces html elements inside the document,
    keep the contents of their body only
  -->
  <xsl:template match="*/html">
    <div class="parsedHtml">
      <p class="note">This was parsed by the HTMLTransformer:</p>
      <xsl:apply-templates select="body/node()"/>
    </div>
  </xsl:template>

  <!-- by default copy everything -->
  <xsl:template match="*" priority="-1">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

</xsl:transform>