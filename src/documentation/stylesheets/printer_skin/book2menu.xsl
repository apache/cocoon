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
                version="1.0">

  <xsl:param name="resource"/>

  <xsl:template match="book">
    <menu>
      <xsl:apply-templates/>
    </menu>
  </xsl:template>

  <xsl:template match="project">
  </xsl:template>

  <xsl:template match="menu[position()=1]">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="menu">
     <tr>
       <td align="left" valign="top">
         ----------
         <br/>
         <b><xsl:value-of select="@label"/></b>
       </td>
     </tr>
     <xsl:apply-templates/>
  </xsl:template>


  <xsl:template match="menu-item">
    <xsl:if test="not(@type) or @type!='hidden'">
     <tr>
      <td align="left" valign="top">
       <xsl:choose>
         <xsl:when test="starts-with(@href, $resource)">
          <xsl:value-of select="@label"/>
         </xsl:when>
         <xsl:otherwise>
          <a href="{@href}"><xsl:value-of select="@label"/></a>
        </xsl:otherwise>
       </xsl:choose>
      </td>
     </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template match="external">
    <xsl:if test="not(@type) or @type!='hidden'">
     <tr>
	<td align="left" valign="top">
          <a href="{@href}"><xsl:value-of select="@label"/></a>
	</td>
     </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template match="node()|@*" priority="-1"/>
</xsl:stylesheet>

