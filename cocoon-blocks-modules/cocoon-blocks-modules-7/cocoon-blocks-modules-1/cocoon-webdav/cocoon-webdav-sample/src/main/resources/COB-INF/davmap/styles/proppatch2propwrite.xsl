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
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:collection="http://apache.org/cocoon/collection/1.0" 
  xmlns:source="http://apache.org/cocoon/propwrite/1.0" 
  xmlns:D="DAV:">
  
  <xsl:param name="location" />
  
  <xsl:template match="/D:propertyupdate">
    <proppatch>
      <source:patch>
        <source:source><xsl:value-of select="$location" /></source:source>
        <xsl:apply-templates />
      </source:patch>
    </proppatch>
  </xsl:template>
  
  <xsl:template match="D:set/D:prop">
    <source:set>
      <xsl:copy-of select="child::node()" />
    </source:set>
  </xsl:template>
  
  <xsl:template match="D:remove/D:prop">
    <source:remove>
      <xsl:copy-of select="child::node()" />
    </source:remove>
  </xsl:template>  

</xsl:stylesheet>
