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

<!--
  This stylesheet is designed to be included by 'forms-samples-styling.xsl'.

  @version $Id$
-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fi="http://apache.org/cocoon/forms/1.0#instance"
                exclude-result-prefixes="fi">

  <xsl:template match="head" mode="forms-field">
    <script src="{$resources-uri}/js/forms-lib.js" type="text/javascript"/>
    <script src="{$resources-uri}/js/cforms.js" type="text/javascript"/>
    <link rel="stylesheet" type="text/css" href="{$resources-uri}/css/forms.css"/>
  </xsl:template>

  <xsl:template match="body" mode="forms-field">
    <xsl:copy-of select="@*"/>
    <xsl:attribute name="onload">forms_onload();<xsl:value-of select="@onload"/></xsl:attribute>
  </xsl:template>
  
  <xsl:template match="body" mode="forms-afterload">
    <script language="Javascript">
       forms_afterLoad();
    </script>   
  </xsl:template>

  <!--+
      | Generic fi:field : produce an <input>
      +-->
  <xsl:template match="fi:field">
    <span id="{@id}">
      <xsl:if test="fi:captcha-image">
        <img src="captcha-{fi:captcha-image/@id}.jpg" style="vertical-align:middle"/>
        <xsl:text> </xsl:text>
      </xsl:if>
      <!--  @id-input is what labels point to -->
      <input name="{@id}" id="{@id}-input" value="{fi:value}" title="{fi:hint}" type="text">
        <xsl:apply-templates select="." mode="styling"/>
      </input>
      <xsl:apply-templates select="." mode="common"/>
    </span>
  </xsl:template>

  <!--+
      | Field in "output" state: display its value
      +-->
  <xsl:template match="fi:field[@state='output']" priority="3">
    <span id="{@id}"><xsl:value-of select="fi:value/node()"/></span>
  </xsl:template>

  <!--+
      | Common stuff like fi:validation-message, @required.
      +-->
  <xsl:template match="fi:*" mode="common">
    <!-- validation message -->
    <xsl:apply-templates select="fi:validation-message"/>
    <!-- required mark -->
    <xsl:if test="@required='true'">
      <span class="forms-field-required"> * </span>
    </xsl:if>
  </xsl:template>

  <!--+
      | Handling the common styling. You may only add attributes to the output
      | in this template as later processing might add attributes too, for
      | example @checked or @selected
      +-->
  <xsl:template match="fi:*" mode="styling">
    <xsl:apply-templates select="fi:styling/@*" mode="styling"/>

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

  <xsl:template match="fi:styling/@*" mode="styling">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="fi:styling/@submit-on-change" mode="styling">
    <xsl:if test=". = 'true'">
      <xsl:attribute name="onchange">forms_submitForm(this)</xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template match="fi:styling/@list-type | fi:styling/@list-orientation |
                       fi:styling/@listbox-size | fi:styling/@format | fi:styling/@layout"
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
                  select="'text hidden checkbox radio password image reset submit'"/>
    <xsl:if test="normalize-space(.) and
                  contains(concat(' ', $validHTMLTypes, ' '), concat(' ', ., ' '))">
      <xsl:copy-of select="."/>
    </xsl:if>
  </xsl:template>

  <!--+
      |
      +-->
  <xsl:template match="fi:validation-message | fi:validation-error">
    <a href="#" class="forms-validation-message" onclick='alert("{normalize-space(.)}");return false;'>&#160;!&#160;</a>
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
        <table id="{$id}" cellpadding="0" cellspacing="0" border="0" title="{fi:hint}">
          <xsl:for-each select="fi:selection-list/fi:item">
            <tr>
              <td>
                <input type="radio" id="{generate-id()}" name="{$id}" value="{@value}">
                  <xsl:if test="@value = $value">
                    <xsl:attribute name="checked">checked</xsl:attribute>
                  </xsl:if>
                  <xsl:apply-templates select="../.." mode="styling"/>
                </input>
              </td>
              <td>
                <xsl:apply-templates select="." mode="label">
                  <xsl:with-param name="id" select="generate-id()"/>
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
        <span id="{$id}" title="{fi:hint}">
          <xsl:for-each select="fi:selection-list/fi:item">
            <input type="radio" id="{generate-id()}" name="{$id}" value="{@value}">
              <xsl:if test="@value = $value">
                <xsl:attribute name="checked">checked</xsl:attribute>
              </xsl:if>
              <xsl:apply-templates select="../.." mode="styling"/>
            </input>
            <xsl:apply-templates select="." mode="label">
              <xsl:with-param name="id" select="generate-id()"/>
            </xsl:apply-templates>
          </xsl:for-each>
          <xsl:apply-templates select="." mode="common"/>
        </span>
      </xsl:otherwise>
    </xsl:choose>
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
      <select title="{fi:hint}" id="{@id}-input" name="{@id}">
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
      | fi:field with a selection list and @type 'output'
      +-->
  <xsl:template match="fi:field[fi:selection-list][fi:styling/@type='output']" priority="3">
    <xsl:variable name="value" select="fi:value"/>
    <xsl:variable name="selected" select="fi:selection-list/fi:item[@value = $value]"/>
    <span id="{@id}">
      <xsl:choose>
        <xsl:when test="$selected/fi:label">
          <xsl:copy-of select="$selected/fi:label/node()"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$value"/>
        </xsl:otherwise>
      </xsl:choose>
    </span>
  </xsl:template>

  <!--+
      | fi:field with @type 'textarea'
      +-->
  <xsl:template match="fi:field[fi:styling/@type='textarea']">
    <span id="{@id}">
      <textarea id="{@id}-input" name="{@id}" title="{fi:hint}">
        <xsl:apply-templates select="." mode="styling"/>
        <!-- remove carriage-returns (occurs on certain versions of IE and doubles linebreaks at each submit) -->
        <xsl:copy-of select="translate(fi:value/node(), '&#13;', '')"/>
      </textarea>
      <xsl:apply-templates select="." mode="common"/>
    </span>
  </xsl:template>

  <!--+
      | fi:field with @type 'output' and fi:output are both rendered as text
      +-->
  <xsl:template match="fi:output | fi:field[fi:styling/@type='output']" priority="2">
    <span id="{@id}"><xsl:copy-of select="fi:value/node()"/></span>
  </xsl:template>

  <!--+
      | Labels for form elements.
      +-->
  <xsl:template match="fi:*" mode="label">
    <xsl:param name="id" select="@id"/>
    <label for="{$id}-input" title="{fi:hint}">
      <xsl:copy-of select="fi:label/node()"/>
    </label>
  </xsl:template>

  <!--+
      | Labels for pure outputs must not contain <label/> as there is no element to point to.
      +-->
  <xsl:template match="fi:output | fi:field[fi:styling/@type='output'] | fi:messages | fi:field[fi:selection-list][fi:styling/@list-type='radio']" mode="label">
    <xsl:copy-of select="fi:label/node()"/>
  </xsl:template>

  <!--+
      | fi:booleanfield : produce a checkbox
      | A hidden booleanfield is not a checkbox, so 'value' contains 
      | the value and not the checked attribute
      +-->
  <xsl:template match="fi:booleanfield">
    <span id="{@id}">
      <input id="{@id}-input" type="checkbox" value="{@true-value}" name="{@id}" title="{fi:hint}">
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
  <xsl:template match="fi:booleanfield[@state='output' or fi:styling/@type='output']" priority="3">
    <input id="{@id}" type="checkbox" title="{fi:hint}" disabled="disabled" value="{@true-value}">
    	  <xsl:if test="fi:value != 'true'">
    	    <xsl:attribute name="checked">checked</xsl:attribute>
    	  </xsl:if>
    </input>
  </xsl:template>

  <!--+
      | fi:action
      +-->
  <xsl:template match="fi:action">
    <input id="{@id}" type="submit" name="{@id}" title="{fi:hint}" onclick="forms_submitForm(this, '{@id}'); return false">
      <xsl:attribute name="value"><xsl:value-of select="fi:label/node()"/></xsl:attribute>
      <xsl:apply-templates select="." mode="styling"/>
    </input>
  </xsl:template>

  <!--+ 
      | fi:action, link-style 	 
      +--> 	 
  <xsl:template match="fi:action[fi:styling/@type = 'link']" priority="1"> 	 
    <a id="{@id}" title="{fi:hint}" href="#" onclick="forms_submitForm(this, '{@id}'); return false"> 	 
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
      | fi:multivaluefield : produce a list of checkboxes
      +-->
  <xsl:template match="fi:multivaluefield[fi:styling/@list-type='checkbox']">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="values" select="fi:values/fi:value/text()"/>

    <span id="{@id}" title="{fi:hint}">
      <xsl:for-each select="fi:selection-list/fi:item">
        <xsl:variable name="value" select="@value"/>
        <input id="{generate-id()}" type="checkbox" value="{@value}" name="{$id}">
          <xsl:if test="$values[. = $value]">
            <xsl:attribute name="checked">checked</xsl:attribute>
          </xsl:if>
        </input>
        <xsl:apply-templates select="." mode="label">
          <xsl:with-param name="id" select="generate-id()"/>
        </xsl:apply-templates>
        <br/>
      </xsl:for-each>
      <xsl:apply-templates select="." mode="common"/>
    </span>
  </xsl:template>

  <!--+
      | fi:multivaluefield : produce a multiple-selection list
      +-->
  <xsl:template match="fi:multivaluefield">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="values" select="fi:values/fi:value/text()"/>

    <span id="{@id}" title="{fi:hint}">
      <select id="{@id}-input" name="{$id}" multiple="multiple">
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
      | fi:multivaluefield in 'output' state
      +-->
  <xsl:template match="fi:multivaluefield[@state='output']" priority="3">
    <xsl:variable name="values" select="fi:values/fi:value/text()"/>
    <span id="{@id}">
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
          <!-- Has a value (filename): display it with a change button -->
            <xsl:text>[</xsl:text>
            <xsl:value-of select="fi:value"/>
            <xsl:text>] </xsl:text>
            <input type="button" id="{@id}-input" name="{@id}" value="..." onclick="forms_submitForm(this)"/>
        </xsl:when>
        <xsl:otherwise>
          <input type="file" id="{@id}-input" name="{@id}" title="{fi:hint}" accept="{@mime-types}">
            <xsl:apply-templates select="." mode="styling"/>
          </input>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="." mode="common"/>`
    </span>
  </xsl:template>

  <!--+
      | fi:upload, output state
      +-->
  <xsl:template match="fi:upload[@state='output']" priority="3">
      <span id="{@id}"><xsl:copy-of select="fi:value/node()"/></span>
  </xsl:template>

  <!--+
      | fi:repeater
      +-->
  <xsl:template match="fi:repeater">
    <table id="{@id}" border="1">
      <input type="hidden" name="{@id}.size" value="{@size}"/>
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
        <td>
          <xsl:apply-templates select="."/>
        </td>
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
      +-->
  <xsl:template match="fi:form-template|fi:form-generated">
    <form>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="onsubmit">forms_onsubmit(); <xsl:value-of select="@onsubmit"/></xsl:attribute>
      <!-- hidden field to store the submit id -->
      <div><input type="hidden" name="forms_submit_id"/></div>
      <xsl:if test="@ajax = 'true'">
        <script script="text/javascript">CForms.ajax = true;</script>
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
      <xsl:for-each select="fi:widgets/*">
        <tr>
          <xsl:choose>
            <xsl:when test="self::fi:repeater">
              <td colspan="2">
                <xsl:apply-templates select="."/>
              </td>
            </xsl:when>
            <xsl:when test="self::fi:booleanfield">
              <td>&#160;</td>
              <td>
                <xsl:apply-templates select="."/>
                <xsl:text> </xsl:text>
                <xsl:apply-templates select="." mode="label"/>
              </td>
            </xsl:when>
            <xsl:otherwise>
              <td>
                <xsl:apply-templates select="." mode="label"/>
              </td>
              <td>
                <xsl:apply-templates select="."/>
              </td>
            </xsl:otherwise>
          </xsl:choose>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="fi:aggregatefield">
    <span id="{@id}">
      <input id="{@id}-input" name="{@id}" value="{fi:value}" title="{fi:hint}">
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
          <xsl:for-each select="fi:message">
            <li><xsl:apply-templates/></li>
          </xsl:for-each>
        </ul>
      </xsl:if>
    </div>
  </xsl:template>

  <xsl:template match="fi:validation-errors">
    <xsl:variable name="header">
      <xsl:choose>
        <xsl:when test="header">
          <xsl:copy-of select="header"/>
        </xsl:when>
        <xsl:otherwise>
          <p class="forms-validation-errors">The following errors have been detected (marked with !):</p>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="footer">
      <xsl:choose>
        <xsl:when test="footer">
          <xsl:copy-of select="footer"/>
        </xsl:when>
        <xsl:otherwise>
          <p class="forms-validation-errors">Please, correct them and re-submit the form.</p>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="messages" select="ancestor::fi:form-template//fi:validation-message"/>
    <xsl:if test="$messages">
      <div class="forms-validation-errors">
        <xsl:copy-of select="$header"/>
        <ul>
          <xsl:for-each select="$messages">
            <li class="forms-validation-error">
              <xsl:variable name="label">
                <xsl:apply-templates select=".." mode="label"/>
              </xsl:variable>
              <xsl:if test="$label">
                <xsl:copy-of select="$label"/><xsl:text>: </xsl:text>
              </xsl:if>
              <xsl:value-of select="."/>
            </li>
          </xsl:for-each>
        </ul>
        <xsl:copy-of select="$footer"/>
      </div>
    </xsl:if>
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
      +-->
  <xsl:template match="fi:struct">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="@*|node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
