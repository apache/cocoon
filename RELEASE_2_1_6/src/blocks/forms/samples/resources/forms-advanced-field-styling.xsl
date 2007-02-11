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
      | It extends the 'forms-field-styling.xsl' with additional stylings.
      | The very specific advanced stylings as the calendar or htmlarea (both
      | also need additional JS files) are separated out of this file.
      +-->

  <xsl:import href="forms-field-styling.xsl"/>
  <xsl:include href="forms-calendar-styling.xsl"/>
  <xsl:include href="forms-htmlarea-styling.xsl"/>
  <!-- Location of the resources directory, where JS libs and icons are stored -->
  <xsl:param name="resources-uri">resources</xsl:param>

  <xsl:template match="head" mode="forms-field">
    <xsl:apply-imports/>
    <script src="{$resources-uri}/mattkruse-lib/AnchorPosition.js" type="text/javascript"/>
    <script src="{$resources-uri}/mattkruse-lib/PopupWindow.js" type="text/javascript"/>
    <script src="{$resources-uri}/mattkruse-lib/OptionTransfer.js" type="text/javascript"/>
    <script src="{$resources-uri}/mattkruse-lib/selectbox.js" type="text/javascript"/>
    <xsl:apply-templates select="." mode="forms-calendar"/>
    <xsl:apply-templates select="." mode="forms-htmlarea"/>
  </xsl:template>

  <xsl:template match="body" mode="forms-field">
    <xsl:apply-imports/>
    <xsl:apply-templates select="." mode="forms-calendar"/>
    <xsl:apply-templates select="." mode="forms-htmlarea"/>
  </xsl:template>

  <!--+ This template should not be necessary as this stylesheet "inherits"
      | all templates from 'forms-field-styling.xsl', but without it, it does
      | not work for me (using Xalan 2.5.1). It's like adding all methods of
      | a superclass in a subclass and calling everywhere only the super
      | implementation.
      +-->
  <xsl:template match="*">
    <xsl:apply-imports/>
  </xsl:template>

  <!--+
      | Add fi:help to the common stuff.
      +-->
  <xsl:template match="fi:*" mode="common">
    <xsl:apply-imports/>
    <xsl:apply-templates select="fi:help"/>
  </xsl:template>

  <!--+
      | 
      +-->
  <xsl:template match="fi:help">
    <xsl:variable name="id" select="generate-id()"/>
    <div class="forms-help" id="help{$id}" style="visibility:hidden; position:absolute;">
    	<span style="float:right"><a href="#" onClick="helpWin{$id}.hidePopup();return false;"><img align="top" alt="close" src="{$resources-uri}/close.gif" height="6" width="6"/></a></span>
      <xsl:apply-templates select="node()"/>
    </div>
    <script type="text/javascript">
      var helpWin<xsl:value-of select="$id"/> = forms_createPopupWindow('help<xsl:value-of select="$id"/>');
    </script>
    <a id="{$id}" name="{$id}" href="#" onclick="helpWin{$id}.showPopup('{$id}');return false;">
      <!-- TODO: i18n key for helppopup -->
      <img src="{$resources-uri}/help.gif" alt="helppopup"/>
    </a>
  </xsl:template>

  <!--+
      | fi:multivaluefield with list-type='double-listbox' styling
      +-->
  <xsl:template match="fi:multivaluefield[fi:styling/@list-type='double-listbox']">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="values" select="fi:values/fi:value/text()"/>

    <div class="forms-doubleList" title="{fi:hint}">
      <table>
        <xsl:if test="fi:styling/fi:available-label|fi:styling/fi:selected-label">
          <tr>
            <th>
              <xsl:copy-of select="fi:styling/fi:available-label/node()"/>
            </th>
            <th> </th>
            <th>
              <xsl:copy-of select="fi:styling/fi:selected-label/node()"/>
            </th>
          </tr>
        </xsl:if>
        <tr>
          <td>
            <!-- select for the unselected values -->
            <select id="{@id}.unselected" name="{@id}.unselected" multiple="multiple"
                    ondblclick="opt{generate-id()}.forms_transferRight()">
              <xsl:apply-templates select="." mode="styling"/>
              <xsl:for-each select="fi:selection-list/fi:item">
                <xsl:variable name="value" select="@value"/>
                <xsl:if test="not($values[. = $value])">
                  <option value="{$value}">
                    <xsl:copy-of select="fi:label/node()"/>
                  </option>
                </xsl:if>
              </xsl:for-each>
            </select>
          </td>
          <td>
            <!-- command buttons -->
            <!-- strangely, IE adds an extra blank line if there only a button on a line. So we surround it with nbsp -->
            <xsl:text>&#160;</xsl:text>
            <input type="button" value="&gt;" onclick="opt{generate-id()}.forms_transferRight()">
              <xsl:if test="@state='disabled'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
              </xsl:if>
            </input>
            <xsl:text>&#160;</xsl:text>
            <br/>
            <xsl:text>&#160;</xsl:text>
            <input type="button" value="&gt;&gt;" onclick="opt{generate-id()}.forms_transferAllRight()">
              <xsl:if test="@state='disabled'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
              </xsl:if>
            </input>
            <xsl:text>&#160;</xsl:text>
            <br/>
            <xsl:text>&#160;</xsl:text>
            <input type="button" value="&lt;" onclick="opt{generate-id()}.forms_transferLeft()">
              <xsl:if test="@state='disabled'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
              </xsl:if>
            </input>
            <xsl:text>&#160;</xsl:text>
            <br/>
            <xsl:text>&#160;</xsl:text>
            <input type="button" value="&lt;&lt;" onclick="opt{generate-id()}.forms_transferAllLeft()">
              <xsl:if test="@state='disabled'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
              </xsl:if>
            </input>
            <xsl:text>&#160;</xsl:text>
            <br/>
            <xsl:apply-templates select="." mode="common"/>
          </td>
          <td>
            <!-- select for the selected values -->
            <select id="{@id}" name="{@id}" multiple="multiple"
                    ondblclick="opt{generate-id()}.forms_transferLeft()" >
              <xsl:apply-templates select="." mode="styling"/>
              <xsl:for-each select="fi:selection-list/fi:item">
                <xsl:variable name="value" select="@value"/>
                <xsl:if test="$values[. = $value]">
                  <option value="{$value}">
                    <xsl:copy-of select="fi:label/node()"/>
                  </option>
                </xsl:if>
              </xsl:for-each>
            </select>
          </td>
        </tr>
      </table>
      <script type="text/javascript">
        var opt<xsl:value-of select="generate-id()"/> = forms_createOptionTransfer('<xsl:value-of select="@id"/>', <xsl:value-of select="fi:styling/@submit-on-change = 'true'"/>);
      </script>
    </div>
  </xsl:template>

  <xsl:template match="fi:multivaluefield/fi:styling[@list-type='double-listbox']/@submit-on-change" mode="styling"/>

</xsl:stylesheet>
