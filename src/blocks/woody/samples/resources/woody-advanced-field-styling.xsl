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
                xmlns:wi="http://apache.org/cocoon/woody/instance/1.0"
                exclude-result-prefixes="wi">
  <!--+
      | This stylesheet is designed to be included by 'woody-samples-styling.xsl'.
      | It extends the 'woody-field-styling.xsl' with additional stylings.
      | The very specific advanced stylings as the calendar or htmlarea (both
      | also need additional JS files) are separated out of this file.
      +-->

  <xsl:import href="woody-field-styling.xsl"/>
  <xsl:include href="woody-calendar-styling.xsl"/>
  <xsl:include href="woody-htmlarea-styling.xsl"/>
  <!-- Location of the resources directory, where JS libs and icons are stored -->
  <xsl:param name="resources-uri">resources</xsl:param>

  <xsl:template match="head" mode="woody-field">
    <xsl:apply-imports/>
    <script src="{$resources-uri}/mattkruse-lib/AnchorPosition.js" type="text/javascript"/>
    <script src="{$resources-uri}/mattkruse-lib/PopupWindow.js" type="text/javascript"/>
    <script src="{$resources-uri}/mattkruse-lib/OptionTransfer.js" type="text/javascript"/>
    <script src="{$resources-uri}/mattkruse-lib/selectbox.js" type="text/javascript"/>
    <xsl:apply-templates select="." mode="woody-calendar"/>
    <xsl:apply-templates select="." mode="woody-htmlarea"/>
  </xsl:template>

  <xsl:template match="body" mode="woody-field">
    <xsl:apply-imports/>
    <xsl:apply-templates select="." mode="woody-calendar"/>
    <xsl:apply-templates select="." mode="woody-htmlarea"/>
  </xsl:template>

  <!--+ This template should not be necessary as this stylesheet "inherits"
      | all templates from 'woody-field-styling.xsl', but without it, it does
      | not work for me (using Xalan 2.5.1). It's like adding all methods of
      | a superclass in a subclass and calling everywhere only the super
      | implementation.
      +-->
  <xsl:template match="*">
    <xsl:apply-imports/>
  </xsl:template>

  <!--+
      | Add wi:help to the common stuff.
      +-->
  <xsl:template match="wi:*" mode="common">
    <xsl:apply-imports/>
    <xsl:apply-templates select="wi:help"/>
  </xsl:template>

  <!--+
      | 
      +-->
  <xsl:template match="wi:help">
    <xsl:variable name="id" select="generate-id()"/>
    <div class="woody-help" id="help{$id}" style="visibility:hidden; position:absolute;">
      <xsl:apply-templates select="node()"/>
    </div>
    <script type="text/javascript">
      var helpWin<xsl:value-of select="$id"/> = woody_createPopupWindow('help<xsl:value-of select="$id"/>');
    </script>
    <a id="{$id}" href="#" onclick="helpWin{$id}.showPopup('{$id}');return false;"><img border="0" src="{$resources-uri}/help.gif"/></a>
  </xsl:template>

  <!--+
      | wi:multivaluefield with list-type='double-listbox' styling
      +-->
  <xsl:template match="wi:multivaluefield[wi:styling/@list-type='double-listbox']">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="values" select="wi:values/wi:value/text()"/>

    <span class="woody-doubleList" title="{wi:hint}">
      <table>
        <xsl:if test="wi:styling/wi:available-label|wi:styling/wi:selected-label">
          <tr>
            <th>
              <xsl:copy-of select="wi:styling/wi:available-label/node()"/>
            </th>
            <th> </th>
            <th>
              <xsl:copy-of select="wi:styling/wi:selected-label/node()"/>
            </th>
          </tr>
        </xsl:if>
        <tr>
          <td>
            <!-- select for the unselected values -->
            <select id="{@id}.unselected" name="{@id}.unselected" multiple="multiple"
                    ondblclick="opt{generate-id()}.transferRight()">
              <xsl:apply-templates select="." mode="styling"/>
              <xsl:for-each select="wi:selection-list/wi:item">
                <xsl:variable name="value" select="@value"/>
                <xsl:if test="not($values[. = $value])">
                  <option value="{$value}">
                    <xsl:copy-of select="wi:label/node()"/>
                  </option>
                </xsl:if>
              </xsl:for-each>
            </select>
          </td>
          <td>
            <!-- command buttons -->
            <!-- strangely, IE adds an extra blank line if there only a button on a line. So we surround it with nbsp -->
            <xsl:text>&#160;</xsl:text>
            <input type="button" value="&gt;" onclick="opt{generate-id()}.transferRight()"/>
            <xsl:text>&#160;</xsl:text>
            <br/>
            <xsl:text>&#160;</xsl:text>
            <input type="button" value="&gt;&gt;" onclick="opt{generate-id()}.transferAllRight()"/>
            <xsl:text>&#160;</xsl:text>
            <br/>
            <xsl:text>&#160;</xsl:text>
            <input type="button" value="&lt;" onclick="opt{generate-id()}.transferLeft()"/>
            <xsl:text>&#160;</xsl:text>
            <br/>
            <xsl:text>&#160;</xsl:text>
            <input type="button" value="&lt;&lt;" onclick="opt{generate-id()}.transferAllLeft()"/>
            <xsl:text>&#160;</xsl:text>
            <br/>
            <xsl:apply-templates select="." mode="common"/>
          </td>
          <td>
            <!-- select for the selected values -->
            <select id="{@id}" name="{@id}" multiple="multiple"
                    ondblclick="opt{generate-id()}.transferLeft()" >
              <xsl:apply-templates select="." mode="styling"/>
              <xsl:for-each select="wi:selection-list/wi:item">
                <xsl:variable name="value" select="@value"/>
                <xsl:if test="$values[. = $value]">
                  <option value="{$value}">
                    <xsl:copy-of select="wi:label/node()"/>
                  </option>
                </xsl:if>
              </xsl:for-each>
            </select>
          </td>
        </tr>
      </table>
      <script type="text/javascript">
        var opt<xsl:value-of select="generate-id()"/> = woody_createOptionTransfer('<xsl:value-of select="@id"/>');
      </script>
    </span>
  </xsl:template>

</xsl:stylesheet>
