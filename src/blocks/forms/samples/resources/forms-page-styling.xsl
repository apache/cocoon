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
                xmlns:fi="http://apache.org/cocoon/forms/1.0#instance"
                exclude-result-prefixes="fi">
  <!--+
      | This stylesheet is designed to be included by 'forms-samples-styling.xsl'.
      +-->
  <xsl:template match="head" mode="woody-page">
    <!--+ 'woody-page-styling.xsl' relies on 'woody-field-styling.xsl' for the
        | inclusion of the correct JS and CSS files. To fix it, we have to
        | separate the page specific parts into its own files.
        +-->
  </xsl:template>

  <xsl:template match="body" mode="woody-page"/>

  <!--
    fi:group : default is to enclose items in a div
  -->
  <xsl:template match="fi:group">
    <div title="{fi:hint}">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="group-layout" select="."/>
    </div>
  </xsl:template>

  <!--
    fi:group of type tabs
  -->
  <xsl:template match="fi:group[fi:styling/@type='tabs']">
    <!-- find the currently selected tab.
         Thoughts still needed here, such as autogenerating a field in the woodytransformer
         to hold this state.
    -->
    <xsl:variable name="active">
      <xsl:variable name="value" select="normalize-space(fi:state/fi:*/fi:value)"/>
      <xsl:choose>
        <xsl:when test="$value">
          <xsl:value-of select="$value"/>
        </xsl:when>
        <xsl:otherwise>0</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!-- copy the "state-widget" attribute for use in for-each -->
    <xsl:variable name="state-widget" select="fi:state/fi:*/@id"/>
    <xsl:variable name="id" select="generate-id()"/>

    <div id="{$id}" title="{fi:hint}">
      <!-- add an hidden input for the state -->
      <xsl:if test="$state-widget">
        <input type="hidden" name="{$state-widget}" value="{$active}"/>
      </xsl:if>
      <!-- div containing the tabs -->
      <div class="woody-tabArea">
        <xsl:for-each select="fi:items/fi:*">
          <xsl:variable name="pos" select="position() - 1"/>
          <span id="{$id}_tab_{$pos}" onclick="woody_showTab('{$id}', {$pos}, {last()}, '{$state-widget}')">
            <xsl:attribute name="class">
              <xsl:text>woody-tab</xsl:text>
              <xsl:if test="$active = $pos"> woody-activeTab</xsl:if>
            </xsl:attribute>
            <xsl:copy-of select="fi:label/node()"/>
            <xsl:if test="fi:items/*//fi:validation-message">
              <span class="woody-validation-message">&#160;!&#160;</span>
            </xsl:if>
          </span>
        </xsl:for-each>
      </div>
      <!-- a div for each of the items -->
      <xsl:for-each select="fi:items/fi:*">
        <xsl:variable name="pos" select="position() - 1"/>
        <div class="woody-tabContent" id="{$id}_items_{$pos}">
          <xsl:if test="$active != $pos">
            <xsl:attribute name="style">display:none</xsl:attribute>
          </xsl:if>
          <xsl:apply-templates select="."/>
        </div>
      </xsl:for-each>
    </div>
  </xsl:template>

  <!--
    fi:group of type choice : a popup is used instead of tabs
  -->
  <xsl:template match="fi:group[fi:styling/@type='choice']">
    <!-- find the currently selected tab.
         Thoughts still needed here, such as autogenerating a field in the woodytransformer
         to hold this state.
    -->
    <xsl:variable name="active">
      <xsl:variable name="value" select="normalize-space(fi:state/fi:*/fi:value)"/>
      <xsl:choose>
        <xsl:when test="$value">
          <xsl:value-of select="$value"/>
        </xsl:when>
        <xsl:otherwise>0</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!-- copy the "state-widget" attribute for use in for-each -->
    <xsl:variable name="state-widget" select="fi:state/fi:*/@id"/>
    <xsl:variable name="id" select="generate-id()"/>

    <fieldset id="{$id}">
      <legend title="{fi:hint}">
        <xsl:apply-templates select="fi:label/node()"/>
        <select name="{$state-widget}" onchange="woody_showTab('{$id}', this.selectedIndex, {count(fi:items/*)}, '{$state-widget}')">
          <xsl:for-each select="fi:items/fi:*">
            <xsl:variable name="pos" select="position() - 1"/>
            <option>
              <xsl:attribute name="value">
                <xsl:choose>
                  <xsl:when test="fi:value">
                    <xsl:value-of select="fi:value"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$pos"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:attribute>
              <xsl:if test="$active = $pos">
                <xsl:attribute name="selected">selected</xsl:attribute>
              </xsl:if>
              <xsl:copy-of select="fi:label/node()"/>
            </option>
          </xsl:for-each>
        </select>
        <xsl:if test="fi:items/*//fi:validation-message">
          <span class="woody-validation-message">&#160;!&#160;</span>
        </xsl:if>
      </legend>
      <!-- a div for each of the items -->
      <xsl:for-each select="fi:items/fi:*">
        <xsl:variable name="pos" select="position() - 1"/>
        <div id="{$id}_items_{$pos}">
          <xsl:if test="$active != $pos">
            <xsl:attribute name="style">display:none</xsl:attribute>
          </xsl:if>
          <xsl:apply-templates select="."/>
        </div>
      </xsl:for-each>
    </fieldset>
  </xsl:template>

  <!--
    fi:group of type fieldset : enclose items in a fieldset frame
  -->
  <xsl:template match="fi:group[fi:styling/@type='fieldset']">
    <fieldset>
      <xsl:apply-templates select="." mode="styling"/>
      <legend title="{fi:hint}"><xsl:copy-of select="fi:label/node()"/></legend>
      <xsl:apply-templates mode="group-layout" select="."/>
    </fieldset>
  </xsl:template>

  <!--
    Group items layout : default is no layout
  -->
  <xsl:template match="fi:group" mode="group-layout">
    <xsl:apply-templates select="fi:items/*"/>
  </xsl:template>

  <!--
    Column group items layout
  -->
  <xsl:template match="fi:group[fi:styling/@layout='column']" mode="group-layout">
    <table border="0" summary="{fi:hint}">
      <tbody>
        <xsl:apply-templates select="fi:items/*" mode="group-column-content"/>
      </tbody>
    </table>
  </xsl:template>

  <!--
    Default column layout : label above and input below
  -->
  <xsl:template match="fi:*" mode="group-column-content">
    <tr>
      <td><xsl:apply-templates select="." mode="label"/></td>
    </tr>
    <tr>
      <td><xsl:apply-templates select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="fi:action" mode="group-column-content">
    <tr>
      <td><xsl:apply-templates select="."/></td>
    </tr>
  </xsl:template>

  <!--
    Columns group items layout
  -->
  <xsl:template match="fi:group[fi:styling/@layout='columns']" mode="group-layout">
    <table border="0" summary="{fi:hint}">
      <tbody>
        <xsl:apply-templates select="fi:items/*" mode="group-columns-content"/>
      </tbody>
    </table>
  </xsl:template>

  <!--
    Default columns layout : label left and input right
  -->
  <xsl:template match="fi:*" mode="group-columns-content">
    <tr>
      <td><xsl:apply-templates select="." mode="label"/></td>
      <td><xsl:apply-templates select="."/></td>
    </tr>
  </xsl:template>

  <!--
    Row group items layout
  -->
  <xsl:template match="fi:group[fi:styling/@layout='row']" mode="group-layout">
    <table border="0" summary="{fi:hint}">
      <tbody>
        <tr>
          <xsl:apply-templates select="fi:items/*" mode="group-row-content"/>
        </tr>
      </tbody>
    </table>
  </xsl:template>

  <!--
    Default row layout : label left and input right
  -->
  <xsl:template match="fi:*" mode="group-row-content">
    <td><xsl:apply-templates select="." mode="label"/></td>
    <td><xsl:apply-templates select="."/></td>
  </xsl:template>

  <xsl:template match="fi:action" mode="group-row-content">
    <td><xsl:apply-templates select="."/></td>
  </xsl:template>
  <!--
    Rows group items layout
  -->
  <xsl:template match="fi:group[fi:styling/@layout='rows']" mode="group-layout">
    <table border="0" summary="{fi:hint}">
      <tbody>
        <tr>
          <xsl:apply-templates select="fi:items/*" mode="group-rows-labels"/>
        </tr>
        <tr>
          <xsl:apply-templates select="fi:items/*" mode="group-rows-content"/>
        </tr>
      </tbody>
    </table>
  </xsl:template>

  <!--
    Default rows layout : label above and input below
  -->
  <xsl:template match="fi:*" mode="group-rows-labels">
    <td><xsl:apply-templates select="." mode="label"/></td>
  </xsl:template>

  <xsl:template match="fi:action" mode="group-rows-labels">
    <td>&#160;</td>
  </xsl:template>

  <xsl:template match="fi:*" mode="group-rows-content">
    <td><xsl:apply-templates select="."/></td>
  </xsl:template>

  <!-- boolean field : checkbox and label on a single line -->
  <xsl:template match="fi:booleanfield" mode="group-columns-content">
    <tr>
      <td colspan="2">
        <xsl:apply-templates select="."/>
        <xsl:apply-templates select="." mode="label"/>
      </td>
    </tr>
  </xsl:template>

  <!-- action : on a single line -->
  <xsl:template match="fi:action" mode="group-columns-content">
    <tr>
      <td colspan="2"><xsl:apply-templates select="."/></td>
    </tr>
  </xsl:template>

  <!-- any other element : on a single line -->
  <xsl:template match="*" mode="group-columns-content">
    <tr>
      <td colspan="2"><xsl:apply-templates select="."/></td>
    </tr>
  </xsl:template>

  <!-- double-list multivaluefield : lists under the label -->
  <xsl:template match="fi:multivaluefield[fi:styling/@list-type='double-listbox']"
                mode="group-columns-content">
    <tr>
      <td colspan="2"><xsl:apply-templates select="." mode="label"/></td>
    </tr>
    <tr>
      <td colspan="2"><xsl:apply-templates select="."/></td>
    </tr>
  </xsl:template>

  <!-- nested group -->
  <xsl:template match="fi:group" mode="group-columns-content">
    <tr>
      <td colspan="2"><xsl:apply-templates select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="fi:*" mode="label">
    <label for="{@id}" title="{fi:hint}">
      <xsl:copy-of select="fi:label/node()"/>
    </label>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
