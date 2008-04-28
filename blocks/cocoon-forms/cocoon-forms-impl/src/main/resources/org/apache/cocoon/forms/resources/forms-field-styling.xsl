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

<!--+
    | This stylesheet is designed to be included by 'forms-samples-styling.xsl'.
    |
    | @version $Id$
    +-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fi="http://apache.org/cocoon/forms/1.0#instance"
                exclude-result-prefixes="fi">

  <!-- option to turn on console debugging for dojo on the browser, from a parameter in the sitemap -->
  <xsl:param name="dojo-debug">false</xsl:param>

  <!-- Allows to configure the dojo locale from a parameter in the sitemap. This should be the same as the form locale. -->
  <xsl:param name="dojo-locale">en</xsl:param>

  <!-- Create a variable with the normalized locale, dojo needs locale parts to be separated with a dash -->
  <xsl:variable name="dojoLocale">
    <xsl:choose>
      <xsl:when test="$dojo-locale != ''">
        <xsl:value-of select="translate($dojo-locale, '_', '-')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>en</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!--+
      | Setup the scripts for CForms
      |
      | CForms can run in two different modes, in each mode different form widgets get instantiated.
      | The @ajax attribute in your ft:form-template controls the mode.
      |
      | 1. non-ajax mode (@ajax="false", this is the default) :
      |     All form submits happen via full page loads.
      |     Form submission is handled by cocoon.forms.SimpleForm (dojoType="forms:SimpleForm")
      |     either directly (submit buttons) or via cocoon.forms.submitForm (scripts, onChange handlers etc.).
      |
      | 2. ajax-mode (@ajax="true") :
      |     All form submits happen via AJAX (XHR or IframeIO) resulting in partial page updates.
      |     Form submission is handled by cocoon.forms.AjaxForm (dojoType="forms:AjaxForm")
      |     either directly (buttons) or via cocoon.forms.submitForm (scripts).
      |
      | NOTES:
      |    Dojo is always loaded by this XSLT. You can use dojo widgets regardless of whether you want ajax-type behaviour.
      |    Since 2.1.11, cocoon widgets no longer need to be explicitly 'dojo.require'd in the page, they load automatically using a namespace manifest.
      |    You may use this same mechanism for your own namespace widgets.
      |    Because we now use lazy-loading, it is recommended that any initialisation code your templates or custom widgets require
      |    should be added as an OnLoadHandler, as this guarentees that all code is loaded without you having to register cocoon modules etc.
      |
      |    If you are overiding this xslt to avoid the use of dojo (untested, but cocoon.forms.common should still work)
      |    you should add a call to run CForms OnLoadHandlers into the body's @onload attribute
      |
      |      eg.
      |       <xsl:attribute name="onload">cocoon.forms.callOnLoadHandlers(); <xsl:value-of select="@onload"/></xsl:attribute>
      +-->
  <xsl:template match="head" mode="forms-field">
    <xsl:variable name="doubleQuote">&#34;</xsl:variable>
    <script type="text/javascript">
      var djConfig = {};
      <xsl:if test="$dojo-debug = 'true'">                                           <!-- turn on debugging, if requested -->
        <xsl:text> djConfig.isDebug = true; </xsl:text>
      </xsl:if>
      <xsl:value-of select="concat('djConfig.locale = ', $doubleQuote, $dojoLocale, $doubleQuote, ';')"/>
      var cocoon;
      if (!cocoon)
        cocoon = {};
      <xsl:value-of select="concat('cocoon.formsResourcesUri = ', $doubleQuote, $forms-resources, $doubleQuote, ';')"/>
    </script>
    <script src="{$dojo-resources}/dojo.js" type="text/javascript"/>           <!-- load dojo -->
    <script type="text/javascript">dojo.require("dojo.widget.*");</script>         <!-- require dojo.widget for auto-loading -->
    <xsl:if test="$dojo-debug = 'true'">                                           <!-- require console etc. for dojo debug, if requested -->
      <script type="text/javascript">dojo.require("dojo.debug.console"); dojo.require("dojo.widget.Tree");</script>
    </xsl:if>
    <script src="{$forms-resources}/js/forms-lib.js" type="text/javascript"/>  <!-- load legacy scripts -->
    <script type="text/javascript">                                            <!-- load forms library -->
    <xsl:value-of select="concat('dojo.registerModulePath(', $doubleQuote, 'cocoon.forms', $doubleQuote, ', ', $doubleQuote, $forms-resources, '/js', $doubleQuote, ');')"/>                        <!-- tell dojo how to find our forms module. NB: (since 2.1.11, replaces cocoon.js) -->
    dojo.require("cocoon.forms.common");                                           <!-- tell dojo we require the commons library -->
    dojo.require("cocoon.forms.manifest");                                         <!-- tell dojo we require cocoon.forms.manifest registering namespace handler -->
    dojo.addOnLoad(cocoon.forms.callOnLoadHandlers);                               <!-- ask dojo to run our onLoad handlers -->
    </script>
    <xsl:copy-of select="fi:init/node()"/>                                         <!-- copy optional initialisation from form template -->
    <xsl:if test="/*/fi:googlemap">                                                <!-- googlemap-key TODO: This looks broken to me (JQ) -->
      <script src="/*/fi:googlemap/fi:key" type="text/javascript"/>
    </xsl:if>
    <link rel="stylesheet" type="text/css" href="{$forms-resources}/css/forms.css"/>
  </xsl:template>

  <xsl:template match="fi:init"/>                                                  <!-- ignore, was handled above -->

  <xsl:template match="body" mode="forms-field">
    <xsl:copy-of select="@*"/>
  </xsl:template>

  <!--+
      |
      | Fields rendering.
      |
      | Each field gets one or more templates, one per each distinct field state.
      |
      +-->

  <!--+
      | Generic fi:field : produce an <input>
      +-->
  <xsl:template match="fi:field">
    <span id="{@id}">
      <xsl:if test="fi:captcha-image">
        <img src="captcha-{fi:captcha-image/@id}.jpg" style="vertical-align:middle" class="forms captcha"/>
        <xsl:text> </xsl:text>
      </xsl:if>
      <!--  @id:input is what labels point to -->
      <input name="{@id}" id="{@id}:input" value="{fi:value}" title="{fi:hint}" type="text">
        <xsl:if test="fi:captcha-image">
          <xsl:attribute name="autocomplete">off</xsl:attribute>
        </xsl:if>
        <xsl:apply-templates select="." mode="styling"/>
      </input>
      <xsl:apply-templates select="." mode="common"/>
    </span>
  </xsl:template>

  <!--+
      | fi:field with a selection list (not 'radio' style)
      | Rendering depends on the attributes of fi:styling :
      | - if @list-type is "listbox" : produce a list box with @listbox-size visible
      |   items (default 5)
      | - otherwise, produce a dropdown menu
      +-->
  <xsl:template match="fi:field[fi:selection-list]" priority="1">
    <xsl:variable name="value" select="fi:value"/>

    <!-- dropdown or listbox -->
    <span id="{@id}">
      <select title="{fi:hint}" id="{@id}:input" name="{@id}">
        <xsl:apply-templates select="." mode="styling"/>
        <xsl:for-each select="fi:selection-list/fi:item">
          <option value="{@value}">
            <xsl:if test="@value = $value">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            <xsl:copy-of select="fi:label/node()"/>
          </option>
        </xsl:for-each>
      </select>
      <xsl:apply-templates select="." mode="common"/>
    </span>
  </xsl:template>

  <!--+
      | fi:field with a selection list and @list-type 'radio' : produce
      | radio-buttons oriented according to @list-orientation
      | ("horizontal" or "vertical" - default)
      +-->
  <xsl:template match="fi:field[fi:selection-list][fi:styling/@list-type='radio']" priority="2">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="value" select="fi:value"/>
    <xsl:variable name="vertical" select="string(fi:styling/@list-orientation) != 'horizontal'"/>
    <xsl:choose>
      <xsl:when test="$vertical">
        <table id="{$id}" cellpadding="0" cellspacing="0" border="0" title="{fi:hint}" class="forms vertical-list">
          <xsl:for-each select="fi:selection-list/fi:item">
            <xsl:variable name="item-id" select="concat($id, ':', position())"/>
            <tr>
              <td>
                <input type="radio" id="{$item-id}" name="{$id}" value="{@value}">
                  <xsl:if test="@value = $value">
                    <xsl:attribute name="checked">checked</xsl:attribute>
                  </xsl:if>
                  <xsl:apply-templates select="../.." mode="styling"/>
                </input>
              </td>
              <td>
                <xsl:apply-templates select="." mode="label">
                  <xsl:with-param name="id" select="$item-id"/>
                </xsl:apply-templates>
              </td>
              <xsl:if test="position() = 1">
                <td rowspan="{count(../fi:item)}">
                  <xsl:apply-templates select="../.." mode="common"/>
                </td>
              </xsl:if>
            </tr>
          </xsl:for-each>
        </table>
      </xsl:when>
      <xsl:otherwise>
        <span id="{$id}" title="{fi:hint}" class="forms horizontal-list">
          <xsl:for-each select="fi:selection-list/fi:item">
            <xsl:variable name="item-id" select="concat($id, ':', position())"/>
            <input type="radio" id="{$item-id}" name="{$id}" value="{@value}">
              <xsl:if test="@value = $value">
                <xsl:attribute name="checked">checked</xsl:attribute>
              </xsl:if>
              <xsl:apply-templates select="../.." mode="styling"/>
            </input>
            <xsl:apply-templates select="." mode="label">
              <xsl:with-param name="id" select="$item-id"/>
            </xsl:apply-templates>
          </xsl:for-each>
          <xsl:apply-templates select="." mode="common"/>
        </span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--+
      | fi:field with @type 'textarea'
      +-->
  <xsl:template match="fi:field[fi:styling/@type='textarea']">
    <span id="{@id}">
      <textarea id="{@id}:input" name="{@id}" title="{fi:hint}">
        <xsl:apply-templates select="." mode="styling"/>
        <!-- remove carriage-returns (occurs on certain versions of IE and doubles linebreaks at each submit) -->
        <xsl:copy-of select="translate(fi:value/node(), '&#13;', '')"/>
      </textarea>
      <xsl:apply-templates select="." mode="common"/>
    </span>
  </xsl:template>

  <!--+
      | Hidden fi:field : produce input with type='hidden'
      +-->
  <xsl:template match="fi:field[fi:styling/@type='hidden']" priority="2">
    <input type="hidden" name="{@id}" id="{@id}" value="{fi:value}">
      <xsl:apply-templates select="." mode="styling"/>
    </input>
  </xsl:template>

  <!--+
      | fi:field in "output" state: display its value
      +-->
  <xsl:template match="fi:field[@state='output']" priority="3">
    <span id="{@id}"><xsl:apply-templates select="." mode="css"/><xsl:value-of select="fi:value/node()"/></span>
  </xsl:template>

  <!--+
      | fi:field in @state="output" with selections-list : produce the label
      +-->
  <xsl:template match="fi:field[@state='output' and fi:selection-list]" priority="3">
    <xsl:variable name="value" select="fi:value/node()"/>
    <span id="{@id}"><xsl:apply-templates select="." mode="css"/><xsl:copy-of select="fi:selection-list/fi:item[@value=$value]/fi:label/node()"/></span>
  </xsl:template>

  <!--+
      | DEPRECATED:
      | fi:field with @type 'output' used to be allowed but causes too much problems
      +-->
  <xsl:template match="fi:field[fi:styling/@type='output']" priority="10">
    <xsl:message terminate="yes">
      <xsl:text>&lt;fi:styling type="output"&gt; should no more be used as it resets field values. </xsl:text>
      <xsl:text>Please set the widget's state to WidgetState.OUTPUT instead.</xsl:text>
    </xsl:message>
  </xsl:template>

  <!--+
      | fi:output is rendered as text
      +-->
  <xsl:template match="fi:output">
    <span id="{@id}"><xsl:apply-templates select="." mode="css"/><xsl:copy-of select="fi:value/node()"/></span>
  </xsl:template>

  <!--+
      | fi:output with selections-list : produce the label
      +-->
  <xsl:template match="fi:output[fi:selection-list]" priority="3">
    <xsl:variable name="value" select="fi:value/node()"/>
    <span id="{@id}"><xsl:apply-templates select="." mode="css"/><xsl:copy-of select="fi:selection-list/fi:item[@value=$value]/fi:label/node()"/></span>
  </xsl:template>

  <!--+
      | fi:validation-message: produce span with message text
      +-->
  <xsl:template match="fi:validation-message">
    <span dojoType="forms:infopopup" style="display:none" class="forms-validation-message-popup"
          id="forms-validation-message-{../@id}" icon="validation-message.gif">
      <xsl:copy-of select="node()"/>
    </span>
  </xsl:template>

  <!--+
      | fi:booleanfield : produce a checkbox
      | A hidden booleanfield is not a checkbox, so 'value' contains
      | the value and not the checked attribute
      +-->
  <xsl:template match="fi:booleanfield">
    <span id="{@id}">
      <input id="{@id}:input" type="checkbox" value="{@true-value}" name="{@id}" title="{fi:hint}">
        <xsl:apply-templates select="." mode="styling"/>
        <xsl:choose>
          <xsl:when test="./fi:styling[@type='hidden']">
            <xsl:if test="fi:value = 'false'">
              <xsl:attribute name="value">false</xsl:attribute>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:if test="fi:value != 'false'">
              <xsl:attribute name="checked">checked</xsl:attribute>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </input>
      <xsl:apply-templates select="." mode="common"/>
    </span>
  </xsl:template>

  <!--+
      | fi:booleanfield with @state 'output': rendered as an inactive checkbox (this doesn't
      | use text but avoids i18n problems related to hardcoding 'yes'/'no' or 'true'/'false'
      +-->
  <xsl:template match="fi:booleanfield[@state='output']" priority="3">
    <input id="{@id}" type="checkbox" title="{fi:hint}" disabled="disabled" value="{@true-value}" name="{@id}">
      <xsl:apply-templates select="." mode="css"/>
      <xsl:if test="fi:value != 'false'">
        <xsl:attribute name="checked">checked</xsl:attribute>
      </xsl:if>
    </input>
  </xsl:template>

  <!--+
      | fi:action
      +-->
  <xsl:template match="fi:action">
    <input id="{@id}" type="submit" name="{@id}" title="{fi:hint}">
      <xsl:attribute name="value"><xsl:value-of select="fi:label/node()"/></xsl:attribute>
      <xsl:apply-templates select="." mode="styling"/>
    </input>
  </xsl:template>

  <!--+
      | fi:action, link-style
      +-->
  <xsl:template match="fi:action[fi:styling/@type = 'link']" priority="1">
    <a id="{@id}" title="{fi:hint}" href="#" onclick="cocoon.forms.submitForm(this, '{@id}'); return false">
      <xsl:apply-templates select="." mode="styling"/>
      <xsl:copy-of select="fi:label/node()"/>
    </a>
  </xsl:template>

  <!--+
      | fi:continuation-id : produce a hidden "continuation-id" input
      +-->
  <xsl:template match="fi:continuation-id">
    <xsl:variable name="name">
      <xsl:value-of select="@name"/>
      <xsl:if test="not(@name)">continuation-id</xsl:if>
    </xsl:variable>
    <div style="display: none;">
      <input name="{$name}" type="hidden" value="{.}"/>
    </div>
  </xsl:template>

  <!--+
      | fi:multivaluefield : produce a multiple-selection list
      +-->
  <xsl:template match="fi:multivaluefield">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="values" select="fi:values/fi:value/text()"/>

    <span id="{@id}" title="{fi:hint}">
      <select id="{@id}:input" name="{$id}" multiple="multiple">
        <xsl:apply-templates select="." mode="styling"/>
        <xsl:for-each select="fi:selection-list/fi:item">
          <xsl:variable name="value" select="@value"/>
          <option value="{$value}">
            <xsl:if test="$values[. = $value]">
              <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            <xsl:copy-of select="fi:label/node()"/>
          </option>
        </xsl:for-each>
      </select>
      <xsl:apply-templates select="." mode="common"/>
    </span>
  </xsl:template>

  <!--+
      | fi:multivaluefield with checkbox list type : produce a list of checkboxes
      +-->
  <xsl:template match="fi:multivaluefield[fi:styling/@list-type='checkbox']">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="values" select="fi:values/fi:value/text()"/>
    <xsl:variable name="state" select="@state" />

    <span id="{@id}" title="{fi:hint}">
      <xsl:for-each select="fi:selection-list/fi:item">
        <xsl:variable name="value" select="@value"/>
        <xsl:variable name="item-id" select="concat($id, ':', position())"/>
        <input id="{$item-id}" type="checkbox" value="{@value}" name="{$id}">
          <xsl:apply-templates select="." mode="css"/>
          <xsl:if test="$state = 'disabled'">
            <xsl:attribute name="disabled">disabled</xsl:attribute>
          </xsl:if>
          <xsl:if test="$values[. = $value]">
            <xsl:attribute name="checked">checked</xsl:attribute>
          </xsl:if>
        </input>
        <xsl:apply-templates select="." mode="label">
          <xsl:with-param name="id" select="$item-id"/>
        </xsl:apply-templates>
        <br/>
      </xsl:for-each>
      <xsl:apply-templates select="." mode="common"/>
    </span>
  </xsl:template>

  <!--+
      | fi:multivaluefield in 'output' state
      +-->
  <xsl:template match="fi:multivaluefield[@state='output']" priority="3">
    <xsl:variable name="values" select="fi:values/fi:value/text()"/>
    <span id="{@id}">
      <xsl:apply-templates select="." mode="css"/>
      <xsl:for-each select="fi:selection-list/fi:item">
        <xsl:variable name="value" select="@value"/>
        <xsl:if test="$values[. = $value]">
          <xsl:value-of select="fi:label/node()"/>
        </xsl:if>
      </xsl:for-each>
    </span>
  </xsl:template>

  <!--+
      | fi:upload
      +-->
  <xsl:template match="fi:upload">
    <span id="{@id}" title="{fi:hint}">
      <xsl:choose>
        <xsl:when test="fi:value">
          <xsl:apply-templates select="." mode="css"/>
          <!-- Has a value (filename): display it with a change button -->
          <xsl:text>[</xsl:text>
          <xsl:value-of select="fi:value"/>
          <xsl:text>] </xsl:text>
          <input type="button" id="{@id}:input" name="{@id}" value="..."
                 onclick="cocoon.forms.submitForm(this)" class="forms upload-change-button"/>
        </xsl:when>
        <xsl:otherwise>
          <input type="file" id="{@id}:input" name="{@id}" title="{fi:hint}" accept="{@mime-types}">
            <xsl:apply-templates select="." mode="styling"/>
          </input>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="." mode="common"/>
    </span>
  </xsl:template>

  <!--+
      | fi:upload, output state
      +-->
  <xsl:template match="fi:upload[@state='output']" priority="3">
    <span id="{@id}"><xsl:apply-templates select="." mode="css"/><xsl:copy-of select="fi:value/node()"/></span>
  </xsl:template>

  <!--+
      | fi:imagemap
      +-->
  <xsl:template match="fi:imagemap">
    <input type ="image" name="{@id}" src="{@imageuri}" title="{fi:hint}" ismap="true">
      <xsl:apply-templates select="." mode="styling"/>
    </input>
  </xsl:template>

  <!--+
      | fi:repeater
      +-->
  <xsl:template match="fi:repeater">
    <input type="hidden" name="{@id}.size" value="{@size}"/>
    <input type="hidden" name="{@id}.page" value="{@page}"/>
    <table id="{@id}" border="1">
      <xsl:apply-templates select="." mode="css"/>
      <tr>
        <xsl:for-each select="fi:headings/fi:heading">
          <th><xsl:value-of select="."/></th>
        </xsl:for-each>
      </tr>
      <xsl:apply-templates select="fi:repeater-row"/>
    </table>
  </xsl:template>

  <!--+
      | fi:repeater-row
      +-->
  <xsl:template match="fi:repeater-row">
    <tr>
      <xsl:for-each select="*">
        <td><xsl:apply-templates select="."/></td>
      </xsl:for-each>
    </tr>
  </xsl:template>

  <!--+
      | fi:repeater-size
      +-->
  <xsl:template match="fi:repeater-size">
    <input type="hidden" name="{@id}.size" value="{@size}"/>
  </xsl:template>

  <!--+
      | fi:form-template|fi:form-generated
      |
      |      NB. If you are overiding this xslt to avoid the use of Dojo
      |      You should add a call to run CForms OnSubmitHandlers into the form's @onsubmit attribute
      |
      |      eg.
      |        <xsl:attribute name="onsubmit">cocoon.forms.callOnSubmitHandlers(this); <xsl:value-of select="@onsubmit"/></xsl:attribute>
      +-->
  <xsl:template match="fi:form-template|fi:form-generated">
    <xsl:variable name="id">
      <xsl:choose>
        <xsl:when test="@id != ''"><xsl:value-of select="@id"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="generate-id()"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <form>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute><!-- form/@id required since 2.1.11-->
      <xsl:if test="not(@dojoType)">
        <xsl:choose>
          <xsl:when test="@ajax = 'true'">
            <xsl:attribute name="dojoType">forms:AjaxForm</xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="dojoType">forms:SimpleForm</xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
      <xsl:apply-templates/>

      <!-- TODO: consider putting this in the xml stream from the generator? -->
      <xsl:if test="self::fi:form-generated">
        <input type="submit"/>
      </xsl:if>
    </form>
  </xsl:template>

  <!--+
      | fi:form
      +-->
  <xsl:template match="fi:form">
    <table border="1">
      <xsl:apply-templates select="." mode="css"/>
      <xsl:for-each select="fi:widgets/*">
        <tr>
          <xsl:choose>
            <xsl:when test="self::fi:repeater">
              <td colspan="2" class="forms repeater-cell">
                <xsl:apply-templates select="."/>
              </td>
            </xsl:when>
            <xsl:when test="self::fi:booleanfield">
              <td class="forms empty-cell">&#160;</td>
              <td class="forms booleanfield-cell">
                <xsl:apply-templates select="."/>
                <xsl:text> </xsl:text>
                <xsl:apply-templates select="." mode="label"/>
              </td>
            </xsl:when>
            <xsl:otherwise>
              <td class="forms label-cell">
                <xsl:apply-templates select="." mode="label"/>
              </td>
              <td class="forms input-cell">
                <xsl:apply-templates select="."/>
              </td>
            </xsl:otherwise>
          </xsl:choose>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

  <!--+
      | fi:aggregatefield : renders input
      +-->
  <xsl:template match="fi:aggregatefield">
    <span id="{@id}">
      <input id="{@id}:input" name="{@id}" value="{fi:value}" title="{fi:hint}">
        <xsl:apply-templates select="." mode="styling"/>
      </input>
      <xsl:apply-templates select="." mode="common"/>
    </span>
  </xsl:template>

  <xsl:template match="fi:messages">
    <div id="{@id}">
      <xsl:if test="fi:message">
        <xsl:apply-templates select="." mode="label"/>:
        <ul>
          <xsl:apply-templates select="." mode="css"/>
          <xsl:for-each select="fi:message">
            <li><xsl:apply-templates/></li>
          </xsl:for-each>
        </ul>
      </xsl:if>
    </div>
  </xsl:template>

  <!--+
      | fi:validation-errors : collect all fi:validation-message elements
      | present on the form and render as a list, with customizable header
      | and footer.
      +-->
  <xsl:template match="fi:validation-errors">
    <xsl:variable name="messages" select="ancestor::fi:form-template//fi:validation-message"/>
    <xsl:if test="$messages">
      <xsl:variable name="header">
        <xsl:choose>
          <xsl:when test="header">
            <xsl:copy-of select="header"/>
          </xsl:when>
          <xsl:otherwise>
            <p class="forms validation-errors-header">The following errors have been detected (marked with !):</p>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="footer">
        <xsl:choose>
          <xsl:when test="footer">
            <xsl:copy-of select="footer"/>
          </xsl:when>
          <xsl:otherwise>
            <p class="forms validation-errors-footer">Please, correct them and re-submit the form.</p>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <div class="forms forms-validation-errors">
        <xsl:copy-of select="$header"/>
        <ul>
          <xsl:apply-templates select="$messages" mode="validation-errors"/>
        </ul>
        <xsl:copy-of select="$footer"/>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template match="fi:validation-message" mode="validation-errors">
    <li class="forms forms-validation-error">
      <xsl:variable name="label">
        <xsl:apply-templates select=".." mode="label"/>
      </xsl:variable>
      <xsl:if test="$label">
        <xsl:copy-of select="$label"/><xsl:text>: </xsl:text>
      </xsl:if>
      <xsl:value-of select="."/>
    </li>
  </xsl:template>

  <xsl:template match="fi:union">
    <div id="{@id}">
      <xsl:apply-templates/>
    </div>
  </xsl:template>

  <xsl:template match="fi:repeater-template">
    <div id="{@id}">
      <xsl:apply-templates/>
    </div>
  </xsl:template>

  <!--+
      | fi:placeholder - used to represent invisible widgets so that AJAX updates
      | know where to insert the widget if it becomes visible
      +-->
  <xsl:template match="fi:placeholder">
    <span id="{@id}"/>
  </xsl:template>

  <!--+
      | fi:struct - has no visual representation by default
      | If the fi:struct contains an id and has only one child that is not in the fi: namespace,
      | then copy the id to the child. This is needed for ajax when grouping is just used to group
      | widgets.
      +-->
  <xsl:template match="fi:struct">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="fi:struct[@id and count(*) = 1 and not(fi:*)]">
    <xsl:apply-templates mode="copy-parent-id"/>
  </xsl:template>

  <!--+
      | fi:group - has no visual representation by default
      | If the fi:group contains an id and has only one child that is not in the fi: namespace,
      | then copy the id to the child. This is needed for ajax when grouping is just used to group
      | widgets.
      +-->
  <xsl:template match="fi:group">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="fi:group[@id and count(*) = 1 and not(fi:*)]">
    <xsl:apply-templates mode="copy-parent-id"/>
  </xsl:template>

  <xsl:template match="*" mode="copy-parent-id">
    <xsl:copy>
      <xsl:attribute name="id"><xsl:value-of select="../@id"/></xsl:attribute>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*" mode="css">
    <xsl:variable name="class">
      <xsl:text>forms </xsl:text>
      <xsl:value-of select="local-name()"/><xsl:text> </xsl:text>
      <xsl:value-of select="@state"/><xsl:text> </xsl:text>
      <xsl:value-of select="fi:styling/@class"/><xsl:text> </xsl:text>
      <xsl:if test="@required = 'true'"><xsl:text>required </xsl:text></xsl:if>
      <xsl:if test="count(fi:validation-error) != 0">with-errors</xsl:if>
    </xsl:variable>
    <xsl:attribute name="class"><xsl:value-of select="normalize-space($class)"/></xsl:attribute>
  </xsl:template>

  <!--+
      | fi:googlemap - generate div and hidden fields for value
      +-->
  <xsl:template match="fi:googlemap">

    <!-- we need a unique id without . as js variable-->
    <xsl:variable name="jsid" select="generate-id(@id)"/>
    <xsl:variable name="doubleQuote">&#34;</xsl:variable>

    <!-- the map-div and (optional), the geocoding input field -->
    <div>
      <xsl:apply-templates select="fi:value/fi:usermarker" mode="geo"/>
      <div id="{@id}">
        <xsl:copy-of select="fi:styling/@*"/>
      </div>
    </div>

    <!-- map creation -->
    <script type="text/javascript">
        <xsl:value-of select="concat('var map_', $jsid, ' = new GMap2(document.getElementById(', $doubleQuote, @id, $doubleQuote, '),[G_HYBRID_MAP]);')"/>
        <xsl:value-of select="concat('map_', $jsid, '.addControl(new GLargeMapControl());')"/>
        <xsl:value-of select="concat('map_', $jsid, '.addControl(new GScaleControl());')"/>
        <xsl:value-of select="concat('map_', $jsid, '.addControl(new GMapTypeControl());')"/>
        <xsl:value-of select="concat('map_', $jsid, '.setCenter(new GLatLng(', fi:value/@lat, ', ', fi:value/@lng, '), ', fi:value/@zoom, ');')"/>

        <xsl:value-of select="concat('GEvent.addListener(map_', $jsid, ', ', $doubleQuote, 'dragend', $doubleQuote, ', function()')"/> {
          <xsl:value-of select="concat('document.getElementById(', $doubleQuote, @id, '_lng', $doubleQuote, ').setAttribute(', $doubleQuote, 'value', $doubleQuote, ',map_', $jsid, '.getCenter().x);')"/>
          <xsl:value-of select="concat('document.getElementById(', $doubleQuote, @id, '_lat', $doubleQuote, ').setAttribute(', $doubleQuote, 'value', $doubleQuote, ',map_', $jsid, '.getCenter().y);')"/>
        });
        <xsl:value-of select="concat('GEvent.addListener(map_', $jsid, ', ', $doubleQuote, 'zoomend', $doubleQuote, ', function(oldLevel,newLevel)')"/> {
          <xsl:value-of select="concat('document.getElementById(', $doubleQuote, @id, '_zoom', $doubleQuote, ').value=newLevel;')"/>
        });

        <xsl:apply-templates select="fi:value/fi:markers/fi:marker"/>
        <xsl:apply-templates select="fi:value/fi:usermarker" mode="script"/>

    </script>

    <!-- hidden fields to store widget values -->
    <input name="{@id}_lng" id="{@id}_lng" value="{fi:value/@lng}" type="hidden"/>
    <input name="{@id}_lat" id="{@id}_lat" value="{fi:value/@lat}" type="hidden"/>
    <input name="{@id}_zoom" id="{@id}_zoom" value="{fi:value/@zoom}" type="hidden"/>
    <input name="{@id}_current" id="{@id}_current" value="{fi:value/@current}" type="hidden"/>
    <input name="{@id}_usermarker-lng" id="{@id}_usermarker-lng" value="{fi:value/fi:usermarker/@lng}" type="hidden"/>
    <input name="{@id}_usermarker-lat" id="{@id}_usermarker-lat" value="{fi:value/fi:usermarker/@lat}" type="hidden"/>
  </xsl:template>

  <!-- list of markers, the last selected is stored in hidden field "current" -->
  <xsl:template match="fi:value/fi:markers/fi:marker">

    <!-- we need a unique id without . as js variable-->
    <xsl:variable name="jsid" select="generate-id(../../../@id)"/>
    <xsl:variable name="doubleQuote">&#34;</xsl:variable>

    <xsl:value-of select="concat('var marker = new GMarker(new GLatLng(', @lat, ', ', @lng, '));')"/>
    GEvent.addListener(marker, "click", function() {
      <xsl:value-of select="concat('marker.openInfoWindowHtml(', $doubleQuote, fi:text, $doubleQuote, ');')"/>
      <xsl:value-of select="concat('document.getElementById(', $doubleQuote, ../../../@id, '_current', $doubleQuote, ').value=', position())"/>
    });
    <xsl:value-of select="concat('map_', $jsid, '.addOverlay(marker);')"/>
  </xsl:template>

  <!-- usermarker: user-click on map places this marker -->
  <xsl:template match="fi:value/fi:usermarker" mode="script">

    <!-- we need a unique id without . as js variable-->
    <xsl:variable name="jsid" select="generate-id(../../@id)"/>
    <xsl:variable name="doubleQuote">&#34;</xsl:variable>

    <xsl:value-of select="concat('var usermarker_', $jsid, ' = new GMarker(new GLatLng(', @lat, ', ', @lng, '));')"/>
    <xsl:value-of select="concat('map_', $jsid, '.addOverlay(usermarker_', $jsid, ');')"/>
    <xsl:value-of select="concat('GEvent.addListener(map_', $jsid, ', ', $doubleQuote, 'click', $doubleQuote, ', function(overlay,point)')"/> {
      <xsl:value-of select="concat('usermarker_', $jsid, '.setPoint(point);')"/>
      <xsl:value-of select="concat('document.getElementById(', $doubleQuote, ../../@id, '_usermarker-lng', $doubleQuote, ').value=point.x;')"/>
      <xsl:value-of select="concat('document.getElementById(', $doubleQuote, ../../@id, '_usermarker-lat', $doubleQuote, ').value=point.y;')"/>
    });
    <xsl:value-of select="concat('usermarker_', $jsid, '.showAddress = function showAddress(address)')"/> {
      var geocoder = new GClientGeocoder();
      geocoder.getLatLng(
      address,
      function(point) {
        if (!point) {
          alert(address + " not found");
        } else {
          <xsl:value-of select="concat('usermarker_', $jsid, '.setPoint(point);')"/>
          <xsl:value-of select="concat('map_', $jsid, '.setCenter(point);')"/>
          <xsl:value-of select="concat('document.getElementById(', $doubleQuote, ../../@id, '_usermarker-lng', $doubleQuote, ').value=point.x;')"/>
          <xsl:value-of select="concat('document.getElementById(', $doubleQuote, ../../@id, '_usermarker-lat', $doubleQuote, ').value=point.y;')"/>
          <xsl:value-of select="concat('document.getElementById(', $doubleQuote, ../../@id, '_lng', $doubleQuote, ').setAttribute(', $doubleQuote, 'value', $doubleQuote, ',map_', $jsid, '.getCenter().x);')"/>
          <xsl:value-of select="concat('document.getElementById(', $doubleQuote, ../../@id, '_lat', $doubleQuote, ').setAttribute(', $doubleQuote, 'value', $doubleQuote, ',map_', $jsid, '.getCenter().y);')"/>
        }
      });
    }
  </xsl:template>
  <xsl:template match="fi:value/fi:usermarker" mode="geo">
    <xsl:variable name="jsid" select="generate-id(../../@id)"/>
    <input name="{../../@id}_geo" id="{../../@id}_geo"/>
    <input name="{../../@id}_geo_go" id="{../../@id}_geo_go" value="Go!" onclick="usermarker_{$jsid}.showAddress(this.form['{../../@id}_geo'].value)" type="button"/>
  </xsl:template>


  <!--+
      |
      | Labels rendering.
      |
      +-->

  <!--+
      | Labels for form elements.
      +-->
  <xsl:template match="fi:*" mode="label">
    <xsl:param name="id"/>
    <xsl:variable name="resolvedId">
      <xsl:choose>
        <xsl:when test="$id != ''"><xsl:value-of select="$id"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="concat(@id, ':input')"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <label for="{$resolvedId}" title="{fi:hint}">
      <xsl:apply-templates select="." mode="css"/>
      <xsl:copy-of select="fi:label/node()"/>
    </label>
  </xsl:template>

  <!--+
      | Labels for pure outputs must not contain <label/> as there is no element to point to.
      +-->
  <xsl:template match="fi:output | fi:messages | fi:field[fi:selection-list][fi:styling/@list-type='radio']" mode="label">
    <span><xsl:apply-templates select="." mode="css"/><xsl:copy-of select="fi:label/node()"/></span>
  </xsl:template>


  <!--+
      |
      | Common templates and styling.
      |
      +-->

  <!--+
      | Common stuff like fi:validation-message, @required.
      +-->
  <xsl:template match="fi:*" mode="common">
    <!-- validation message -->
    <xsl:apply-templates select="fi:validation-message"/>
    <!-- required mark -->
    <xsl:if test="@required='true'">
      <span class="forms forms-field-required {local-name()}"> * </span>
    </xsl:if>
  </xsl:template>

  <!--+
      | Handling the common styling. You may only add attributes to the output
      | in this template as later processing might add attributes too, for
      | example @checked or @selected
      +-->
  <xsl:template match="fi:*" mode="styling">
    <xsl:apply-templates select="." mode="css"/>
    <xsl:apply-templates select="fi:styling/@*" mode="styling"/>

    <!--  Auto submit on fields which are listening -->
    <xsl:if test="@listening = 'true' and not(fi:styling/@submit-on-change = 'false') and
                  not(fi:styling/@onchange) and not(fi:styling/@list-type = 'double-listbox')">
      <xsl:choose>
        <!-- IE does not react to a click with an onchange, as firefox does, so for radio and checkbox put an onclick handler instead -->
        <xsl:when test="local-name() = 'booleanfield' or fi:styling/@list-type = 'radio' or fi:styling/@list-type = 'checkbox'">
          <xsl:attribute name="onclick">cocoon.forms.submitForm(this)</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="onchange">cocoon.forms.submitForm(this)</xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>

    <xsl:if test="@state = 'disabled'">
      <xsl:attribute name="disabled">disabled</xsl:attribute>
    </xsl:if>

    <!--+
        | @listbox-size needs to be handled separately as even if it is not
        | specified some output (@size) must be generated.
        +-->
    <xsl:if test="self::fi:field[fi:selection-list][fi:styling/@list-type = 'listbox'] or
                  self::fi:multivaluefield[not(fi:styling/@list-type = 'checkbox')]">
      <xsl:variable name="size">
        <xsl:value-of select="fi:styling/@listbox-size"/>
        <xsl:if test="not(fi:styling/@listbox-size)">5</xsl:if>
      </xsl:variable>
      <xsl:attribute name="size">
        <xsl:value-of select="$size"/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template match="fi:styling/@*" mode="styling" priority="-1">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="fi:styling/@submit-on-change" mode="styling">
    <xsl:if test=". = 'true'">
      <xsl:attribute name="onchange">cocoon.forms.submitForm(this)</xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template match="fi:styling/@list-type | fi:styling/@list-orientation |
                       fi:styling/@listbox-size | fi:styling/@format | fi:styling/@layout | fi:styling/@class"
                mode="styling">
    <!--+
        | Ignore marker attributes so they don't go into the resuling HTML.
        +-->
  </xsl:template>

  <xsl:template match="fi:styling/@type" mode="styling">
    <!--+
        | Do we have a duplicate semantic usage of @type?
        | @type is only a marker for the stylesheet in general, but some of the
        | types must/should be in the HTML output too.
        +-->
    <xsl:variable name="validHTMLTypes"
                  select="' text hidden checkbox radio password image reset submit '"/>
    <xsl:if test="normalize-space(.) and
                  contains($validHTMLTypes, concat(' ', ., ' '))">
      <xsl:copy-of select="."/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="apos-replace">
    <xsl:param name="text"/>
    <xsl:variable name="pattern">'</xsl:variable>
    <xsl:choose>
      <xsl:when test="contains($text,$pattern)">
        <xsl:value-of select="substring-before($text,$pattern)"/>
        <xsl:text>\'</xsl:text>
        <xsl:call-template name="apos-replace">
          <xsl:with-param name="text"
                          select="substring-after($text,$pattern)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!--+
      | copy over everything which was not matched
      +-->
  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
