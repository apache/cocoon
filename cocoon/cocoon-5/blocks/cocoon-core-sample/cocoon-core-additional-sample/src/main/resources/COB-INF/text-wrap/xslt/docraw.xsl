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
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- doc.xsl - text line wrapping
  This stylesheet does not do any line wrapping.
-->

  <xsl:template match="document">
    <html>
      <head>
        <title><xsl:value-of select="header/title"/></title>
      </head>
      <xsl:apply-templates/>
      </html>
  </xsl:template>

  <xsl:template match="body">
    <body>
      <h1><xsl:value-of select="header/title"/></h1>
      <p>Comment from stylesheet: No special handling was done for the
        &lt;source&gt; elements. Note the long right-left scrollbar.
      </p>
      <xsl:apply-templates/>
    </body>
  </xsl:template>

  <xsl:template match="section">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="source">
    <div style="padding:4pt; margin-bottom:8pt; border-width:1px; border-style:solid; border-color:#0086b2;">
      <pre class="code">
        <xsl:apply-templates/>
      </pre>
    </div>
  </xsl:template>

  <xsl:template match="title">
    <h2><xsl:apply-templates/></h2>
  </xsl:template>

  <xsl:template match="p">
    <p><xsl:apply-templates/></p>
  </xsl:template>

</xsl:stylesheet>
