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
<!-- SVN $Id:$ -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="storage"/>
<xsl:param name="type"/>

<xsl:template match="/">
  <result>
    <xsl:if test="$storage='folder'">
      <!-- Copy the global values for the folder -->
      <attribute>
        <xsl:copy-of select="basket-quota/storage/storage[@value='folder']/*"/>
      </attribute>
    </xsl:if>
    <xsl:if test="$storage!='folder'">
      <!-- If type is not specified use global values -->
      <xsl:if test="$type=''">
        <attribute>
          <xsl:copy-of select="basket-quota/storage/storage[@value=$storage]/*"/>
        </attribute>
      </xsl:if>
      <xsl:if test="$type!=''">
        <xsl:copy-of select="basket-quota/storage/storage[@value=$storage]/attribute[@name='type']/attribute[@value=$type]"/>
      </xsl:if>
    </xsl:if>
  </result>
</xsl:template>

</xsl:stylesheet>

