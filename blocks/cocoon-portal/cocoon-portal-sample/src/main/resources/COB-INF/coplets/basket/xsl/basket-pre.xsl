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
<!-- $Id$ 

-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="itemCount"/>
<xsl:param name="maxSize"/>

<xsl:template match="basket-content">
  <h1>Your Basket</h1>
  <p>You have <xsl:value-of select="item-count"/> items in your basket (Allowed: <xsl:value-of select="$itemCount"/>)
     / <xsl:value-of select="item-size"/> Kb (Allowed: <xsl:value-of select="$maxSize"/> Kb).</p>
  <xsl:apply-templates select="items"/>
</xsl:template>

<xsl:template match="items">
    <form method="POST" action="coplets/basket/processBasket.process">
  <table>
    <tr>
      <th>&#160;</th>
      <th>Title</th>
      <th>Size</th>
      <th>Storage</th>
      <th>Replay</th>
      <th>Frequency</th>
      <th>Action</th>
    </tr>
    <xsl:for-each select="item" xmlns:basket="http://apache.org/cocoon/portal/basket/1.0">
   
      <tr>
        <td><input type="checkbox" name="c{id}" value="{id}"/>&#160;</td>
        <td><a href="{show-url}"><xsl:value-of select="title"/></a></td>
        <td><xsl:value-of select="size"/></td>
        <td><xsl:value-of select="store"/><input type="hidden" value="{store}" name="s{id}"/></td>
        <td>
          <input type="checkbox" name="r{id}" value="{id}">
            <xsl:if test="attributes/attribute[@name='action-replay']/@value != ''">
              <xsl:attribute name="checked">checked</xsl:attribute>
            </xsl:if>
          </input>
           &#160;
        </td>
        <xsl:variable name="freq">
          <xsl:choose>
            <xsl:when test="attributes/attribute[@name='action-freq']/@value != ''">
              <xsl:value-of select="attributes/attribute[@name='action-freq']/@value"/>
            </xsl:when>
            <xsl:otherwise>1</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <td>Days: <input type="text" name="f{id}" value="{$freq}" size="3"/></td>
        <td>
          <select name="a{id}" size="1">
            <xsl:choose>
              <xsl:when test="store = 'basket' and //configuration/basket">
                <option selected="selected" value="briefcase">Move to Briefcase</option>
              </xsl:when>
              <xsl:when test="store = 'briefcase' and //configuration/briefcase">
                <option selected="selected" value="basket">Move to Basket</option>
              </xsl:when>
            </xsl:choose>
            <option value="delete">delete</option>
            <basket:show-actions storage="{store}" checked="{attributes/attribute[@name='action-name']/@value}"/>
          </select>
        </td>
      </tr>
    </xsl:for-each>
  </table>
  <input type="submit" value="Change" name="Change"/>
  </form>
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
        <td><a href="{show-url}"><xsl:value-of select="id"/></a></td>
        <td><xsl:value-of select="size"/></td>
        <td><a href="{remove-url}">Clean Basket</a></td>
      </tr>
    </xsl:for-each>
  </table>
</xsl:template>

</xsl:stylesheet>
