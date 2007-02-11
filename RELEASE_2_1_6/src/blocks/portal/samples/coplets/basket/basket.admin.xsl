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
<!-- $Id: basket.admin.xsl,v 1.2 2004/03/06 02:26:14 antonio Exp $ 

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="basket-content">
<h1>Basket Content</h1>
<p>There are <xsl:value-of select="item-count"/> items in the basket.</p>
<xsl:apply-templates select="items"/>
</xsl:template>
<xsl:template match="items">
<table>
<xsl:for-each select="item">
<tr>
<td>
<a href="{show-url}"><xsl:value-of select="id"/></a>
</td>
<td>
<xsl:value-of select="size"/>
</td>
<td>
<a href="{remove-url}">Remove Item</a>
</td>
</tr>
</xsl:for-each>
</table>
</xsl:template>

<xsl:template match="basket-admin">
<h1>Basket Administration</h1>
<xsl:apply-templates select="baskets"/>
<p><a href="{refresh-url}">Refresh list</a> - <a href="{clean-url}">Clean all baskets</a></p>
</xsl:template>

<xsl:template match="baskets">
<table>
<xsl:for-each select="basket">
<tr>
<td>
<a href="{show-url}"><xsl:value-of select="id"/></a>
</td>
<td>
<xsl:value-of select="size"/>
</td>
<td>
<a href="{remove-url}">Clean Basket</a>
</td>
</tr>
</xsl:for-each>
</table>
</xsl:template>

</xsl:stylesheet>
