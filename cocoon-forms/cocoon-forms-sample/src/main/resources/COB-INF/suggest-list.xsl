<?xml version="1.0"?>
<!--
  Copyright 2006 The Apache Software Foundation or its licensors,
  as applicable.

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
<xsl:stylesheet
  version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fi="http://apache.org/cocoon/forms/1.0#instance">
  <xsl:template match="/">
    <ul>
      <xsl:apply-templates/>
    </ul>
  </xsl:template>
  
  <xsl:template match="fi:item">
    <li>
      <xsl:value-of select="@value"/>
      <xsl:if test="fi:label and (fi:label != @value)">
        <span class="informal"> (<xsl:copy-of select="fi:label/node()"/>)</span>
      </xsl:if>
    </li>
  </xsl:template>
</xsl:stylesheet>
