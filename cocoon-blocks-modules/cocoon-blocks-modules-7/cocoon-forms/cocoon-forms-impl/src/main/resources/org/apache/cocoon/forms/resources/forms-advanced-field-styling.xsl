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

  <xsl:import href="resource://org/apache/cocoon/forms/resources/forms-field-styling.xsl"/>
  <xsl:include href="resource://org/apache/cocoon/forms/resources/forms-calendar-styling.xsl"/>
  <xsl:include href="resource://org/apache/cocoon/forms/resources/forms-htmlarea-styling.xsl"/>
  <!--  https://issues.apache.org/jira/browse/COCOON-2141
  <xsl:include href="resource://org/apache/cocoon/forms/resources/forms-xinha-styling.xsl"/ -->
  
  <xsl:param name="forms-resources"/>

  <xsl:template match="head" mode="forms-field">
    <xsl:apply-imports/>
    <script src="{$forms-resources}/mattkruse-lib/AnchorPosition.js" type="text/javascript"/>
    <script src="{$forms-resources}/mattkruse-lib/PopupWindow.js" type="text/javascript"/>
    <script src="{$forms-resources}/mattkruse-lib/OptionTransfer.js" type="text/javascript"/>
    <script src="{$forms-resources}/mattkruse-lib/selectbox.js" type="text/javascript"/>
    <xsl:apply-templates select="." mode="forms-htmlarea"/>
    <!-- https://issues.apache.org/jira/browse/COCOON-2141
    <xsl:apply-templates select="." mode="forms-xinha"/ -->
  </xsl:template>

  <xsl:template match="body" mode="forms-field">
    <xsl:apply-imports/>
    <xsl:apply-templates select="." mode="forms-htmlarea"/>
    <!-- https://issues.apache.org/jira/browse/COCOON-2141
    <xsl:apply-templates select="." mode="forms-xinha"/ -->
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
    <xsl:variable name="id" select="concat(../@id, ':help')"/>
    <span id="{$id}" dojoType="forms:infopopup" style="display:none" class="forms-help-popup" icon="help.gif">
      <xsl:copy-of select="node()"/>
    </span>
  </xsl:template>

  <!--+
      | fi:multivaluefield with list-type='double-listbox' styling
      +-->
  <xsl:template match="fi:multivaluefield[fi:styling/@list-type='double-listbox']">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="values" select="fi:values/fi:value/text()"/>
    <xsl:variable name="browser-variable">
      <xsl:value-of select="concat(translate($id, '.', '_'), '_jsWidget')"/>
    </xsl:variable>
    <xsl:variable name="singleQuote">&#39;</xsl:variable>

    <script type="text/javascript">
      <xsl:value-of select="concat('var ', $browser-variable, ';')"/>
    </script>
    <div id="{@id}" class="forms-doubleList forms doubleList" title="{fi:hint}">
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
                    ondblclick="{$browser-variable}.forms_transferRight()">
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
            <input type="button" value="&gt;" onclick="{$browser-variable}.forms_transferRight()">
              <xsl:if test="@state='disabled'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
              </xsl:if>
            </input>
            <xsl:text>&#160;</xsl:text>
            <br/>
            <xsl:text>&#160;</xsl:text>
            <input type="button" value="&gt;&gt;" onclick="{$browser-variable}.forms_transferAllRight()">
              <xsl:if test="@state='disabled'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
              </xsl:if>
            </input>
            <xsl:text>&#160;</xsl:text>
            <br/>
            <xsl:text>&#160;</xsl:text>
            <input type="button" value="&lt;" onclick="{$browser-variable}.forms_transferLeft()">
              <xsl:if test="@state='disabled'">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
              </xsl:if>
            </input>
            <xsl:text>&#160;</xsl:text>
            <br/>
            <xsl:text>&#160;</xsl:text>
            <input type="button" value="&lt;&lt;" onclick="{$browser-variable}.forms_transferAllLeft()">
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
            <select id="{@id}:input" name="{@id}" multiple="multiple"
                    ondblclick="{$browser-variable}.forms_transferLeft()" >
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
        <xsl:value-of select="concat($browser-variable, ' = forms_createOptionTransfer(', $singleQuote, @id, $singleQuote, ',', @listening = 'true' and not(fi:styling/@submit-on-change = 'false'), ');')" />
      </script>
    </div>
  </xsl:template>

  <xsl:template match="fi:multivaluefield/fi:styling[@list-type='double-listbox']/@submit-on-change" mode="styling"/>

   <!--+
      | fi:multivaluefield with list-type='dojo-double-listbox' styling
      +-->
  <xsl:template
      match="fi:multivaluefield[fi:styling/@list-type='dojo-double-listbox']">
      <xsl:variable name="id" select="@id"/>
      <xsl:variable name="values" select="fi:values/fi:value/text()"/>
      <div id="{@id}" class="forms-doubleList forms doubleList"
          title="{fi:hint}">
          <table>
              <tr>
                  <td>
                      <div dojoType="forms:MultiValueDoubleList" styleClass="multivalue-widget" id="{$id}:widget" cformsIdPrefix="{$id}"
                          availableListLabel="{fi:styling/fi:available-label}" selectedListLabel="{fi:styling/fi:selected-label}"
                          size="{fi:styling/@size}">
                          <!-- Data is supplied to the widget using this table-->
                          <table>
                              <tbody>
                                  <!-- select for the available values-->
                                  <xsl:for-each select="fi:selection-list/fi:item">
                                      <xsl:variable name="value" select="@value"/>
                                      <xsl:if test="not($values[. = $value])">
                                          <tr>
                                              <td>
                                                  <xsl:value-of select="$value"/>
                                              </td>
                                              <td>
                                                  <xsl:copy-of select="fi:label/node()"/>
                                              </td>
                                          </tr>
                                      </xsl:if>
                                  </xsl:for-each>
                              </tbody>
                          </table>
                          <table>
                              <tbody>
                                  <!-- select for the selected values -->
                                  <xsl:for-each select="fi:selection-list/fi:item">
                                      <xsl:variable name="value" select="@value"/>
                                      <xsl:if test="$values[. = $value]">
                                          <tr>
                                              <td>
                                                  <xsl:value-of select="$value"/>
                                              </td>
                                              <td>
                                                  <xsl:copy-of select="fi:label/node()"/>
                                              </td>
                                          </tr>
                                      </xsl:if>
                                  </xsl:for-each>
                              </tbody>
                          </table>
                      </div>
                  </td>
                  <td>
                      <xsl:apply-templates select="." mode="common"/>
                  </td>
              </tr>
          </table>
      </div>
      <xsl:apply-templates select="." mode="label-ajax-request"/>
  </xsl:template>

  <!--+
      | fi:multivaluefield without a selection list
      +-->
  <xsl:template match="fi:multivaluefield[not(fi:selection-list)]">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="values" select="fi:values/fi:value/text()"/>

    <div id="{$id}">
      <table>
        <tr>
          <td>
            <div dojoType="forms:MultiValueEditor" id="{$id}:widget" cformsIdPrefix="{$id}">
              <!-- Data is supplied to the widget using this table -->
              <table>
                <tbody>
                  <xsl:for-each select="$values">
                    <tr>
                      <td><xsl:value-of select="."/></td>
                    </tr>
                  </xsl:for-each>
                </tbody>
              </table>
            </div>
          </td>
          <td>
            <xsl:apply-templates select="." mode="common"/>
          </td>
        </tr>
      </table>
    </div>
  </xsl:template>

  <!--+
      | fi:multivaluefield with a selection list and suggestions support
      +-->
  <xsl:template match="fi:multivaluefield[fi:styling/@type='MultiValueEditorWithSuggestion']">
    <!-- <xsl:variable name="values" select="fi:values/fi:value/text()"/>
    <xsl:variable name="popupUri" select="fi:styling/@popup-uri"/>
    <xsl:variable name="popupLinkText" select="fi:styling/@popup-link-text"/>
    <xsl:variable name="dataUrl" select="fi:styling/@dataUrl"/> 
    <xsl:variable name="popupSize" select="fi:styling/@popup-size"/>-->

    <div id="{@id}" dojoType="forms:MultiValueEditorWithSuggestion" styleClass="multivalue-widget" dataUrl="{fi:styling/@dataUrl}"
        popupUri="{fi:styling/@popup-uri}" popupLinkText="{fi:styling/@popup-link-text}" popupSize="{fi:styling/@popup-size}">
      <table>
        <tbody>
          <xsl:for-each select="fi:selection-list/fi:item">
            <xsl:variable name="value" select="@value"/>
            <tr>
              <td><xsl:value-of select="$value"/></td>
              <td><xsl:copy-of select="fi:label/node()"/></td>
            </tr>
          </xsl:for-each>
        </tbody>
      </table>
    </div>
  </xsl:template>

  <!--+
      | Field with in-place editing
      | Reacts to 2 different types:
      | - 'inplace' for a single line input
      | - 'inplace-area' for a textarea
      +-->
  <xsl:template match="fi:field[fi:styling[@type='inplace' or @type='inplace-area'] and @state='active']">
    <span id="{@id}">
      <span dojoType="InlineEditBox" onSave="dojo.byId('{@id}:input').value = arguments[0]">
        <xsl:attribute name="onSave">
          <xsl:text>dojo.byId('</xsl:text>
          <xsl:value-of select="@id"/>
          <xsl:text>:input').value = arguments[0];</xsl:text>
          <xsl:if test="(@listening = 'true' and not(fi:styling/@submit-on-change = 'false')) or fi:styling/@submit-on-change = 'true'">
            <xsl:text>cocoon.forms.submitForm(dojo.byId('</xsl:text>
            <xsl:value-of select="@id"/>
            <xsl:text>:input'))</xsl:text>
          </xsl:if>
        </xsl:attribute>
        <xsl:if test="fi:styling/@type='inplace-area'">
          <xsl:attribute name="mode">
            <xsl:text>textarea</xsl:text>
          </xsl:attribute>
        </xsl:if>
        <xsl:choose>
          <xsl:when test="fi:value">
            <xsl:value-of select="fi:value"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="value">
              <xsl:text>[</xsl:text>
              <xsl:value-of select="fi:hint"/>
              <xsl:text>]</xsl:text>
            </xsl:attribute>
            <xsl:text> </xsl:text> <!-- some dumb text, otherwise IE bugs... -->
          </xsl:otherwise>
        </xsl:choose>
      </span>
      <xsl:apply-templates select="." mode="common"/>
      <input id="{@id}:input" type="hidden" name="{@id}" value="{fi:value}"/>
    </span>
  </xsl:template>

  <!--+
      | Field with a suggestion list
      +-->
  <xsl:template match="fi:field[fi:styling/@type='suggest' and @state='active']">
    <span id="{@id}">
      <input name="{@id}" id="{@id}:input" value="{fi:value}" dojoType="forms:CFormsSuggest">
        <xsl:apply-templates select="." mode="styling"/>
        <xsl:if test="fi:suggestion">
          <xsl:attribute name="suggestion"><xsl:value-of select="fi:suggestion"/></xsl:attribute>
        </xsl:if>
      </input>
      <xsl:apply-templates select="." mode="common"/>
    </span>
  </xsl:template>

</xsl:stylesheet>
