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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                              xmlns:sql="http://apache.org/cocoon/SQL/2.0">


  <xsl:import href="../../../common/style/xsl/html/simple-page2html.xsl"/>

  <xsl:template match="sql:rowset">
   <xsl:choose>
    <xsl:when test="ancestor::sql:rowset">
     <tr>
      <td>
       <table border="1">
        <xsl:apply-templates/>
       </table>
      </td>
     </tr>
    </xsl:when>
    <xsl:otherwise>
     <table border="1">
      <xsl:apply-templates/>
     </table>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:template>

  <xsl:template match="sql:row">
   <tr>
    <xsl:apply-templates/>
   </tr>
  </xsl:template>

  <xsl:template match="sql:name">
   <td><xsl:value-of select="."/></td>  
  </xsl:template>

  <xsl:template match="sql:id">
   <!-- ignore -->
  </xsl:template>

</xsl:stylesheet>
