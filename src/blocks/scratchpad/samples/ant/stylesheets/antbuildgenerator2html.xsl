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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns="http://www.w3.org/1999/xhtml"
                version="1.0">
                
  <xsl:template match="/build">
    <html>
    <title>Ant Build</title>
    <style>
    pre { margin: 0 }
    .debug { color: green }
    .verbose { color: blue }
    .info { color: black }
    .warn { color: yellow }
    .error { color: red }
    </style>
    <body>
    <xsl:apply-templates/>
    </body>
    </html>
  </xsl:template>
  
  <xsl:template match="message">
    <pre class="{@priority}"><xsl:value-of select="."/></pre>
  </xsl:template>
  
  <xsl:template match="target">
    <div>
    <h3><xsl:value-of select="@name"/></h3>
    <xsl:apply-templates/>
    </div>
  </xsl:template>
  
  <xsl:template match="task">
    <div>
    <h4><xsl:value-of select="@name"/></h4>
    <xsl:apply-templates/>
    </div>
  </xsl:template>
  
</xsl:stylesheet>