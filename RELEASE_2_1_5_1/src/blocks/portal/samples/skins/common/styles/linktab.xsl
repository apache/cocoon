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
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<!-- Process a tab  -->
<xsl:template match="linktab-layout">
<!-- ~~~~~ Begin body table ~~~~~ -->
<table border="0" cellpadding="0" cellspacing="0" width="100%">
  <!-- ~~~~~ Begin tab row ~~~~~ -->
  <tr vAlign="top">
  <td width="20%" align="top">
  <br/>
  &#160;Select:<br/><br/>
     <xsl:for-each select="named-item">
     
      <xsl:choose>
        <xsl:when test="not(@selected)">
          &#160;&#160;&#160;<a href="{@parameter}"><xsl:value-of select="@name"/></a><br/><br/>
        </xsl:when>
        <xsl:otherwise>
          &#160;&#160;&#160;<b><xsl:value-of select="@name"/></b><br/><br/>
        </xsl:otherwise>
      </xsl:choose> 
      </xsl:for-each>
  </td>
				<td width="80%" align="top">
					<xsl:apply-templates select="named-item"/>
				</td>
  </tr>
</table>
</xsl:template>

<xsl:template match="named-item">
  <xsl:apply-templates />
</xsl:template>

<!-- Copy all and apply templates -->

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()" />
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
