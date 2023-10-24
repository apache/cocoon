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
<!-- SVN $Id$ -->
<xsl:stylesheet version="1.0" 
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:dir="http://apache.org/cocoon/directory/2.0">

<xsl:param name="dirprefix"/>

<xsl:template match="/">
    <pictures>
        <xsl:for-each select="dir:directory/dir:file">
            <picture><xsl:value-of select="$dirprefix"/>/<xsl:value-of select="@name"/></picture>
        </xsl:for-each>
    </pictures>
</xsl:template>

</xsl:stylesheet>
