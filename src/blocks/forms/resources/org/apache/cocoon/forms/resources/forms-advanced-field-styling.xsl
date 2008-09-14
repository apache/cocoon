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
                xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
                exclude-result-prefixes="fi">
  <!--+
      | This stylesheet is designed to be included by 'forms-samples-styling.xsl'.
      | It extends the 'forms-field-styling.xsl' with additional stylings.
      | The very specific advanced stylings as the calendar or htmlarea (both
      | also need additional JS files) are separated out of this file.
      |
      | @version $Id$
      +-->

  <!-- TODO: the forms-advanced-styling.xsl and forms-field-styling.xsl pair need a big cleanup -->
  <!-- (JQ) IMO all references to Dojo should be moved here, forms-field-styling.xsl should work as a basic non-javascript cforms renderer -->

  <xsl:import href="forms-field-styling.xsl"/>
  <xsl:include href="forms-calendar-styling.xsl"/>
  <!--<xsl:include href="forms-htmlarea-styling.xsl"/> TODO: Should we find a way to unobtrusively include htmlarea? (i.e. not include unused scripts.) Does htmlarea even work with Dojo? -->

  <!-- TODO: Should these dojo setup, style and script blocks go into a separate xslt? -->
  <xsl:param name="dojo-use-cdn">false</xsl:param><!-- NB. By default, CForms uses a local repository for Dojo, if you  need to make a custom build, see src/blocks/ajax/dojo -->
  <xsl:param name="dojo-cdn">http://ajax.googleapis.com/ajax/libs/dojo/1.1.1</xsl:param><!-- there are other existing CDNs with Dojo 1.1.1, or you could setup your own -->

  <!-- Location of the resources directories, where JS libs etc. are stored -->
  <xsl:param name="context-path"></xsl:param><!-- for 2.1.X this should be the only path param you should have to send from the sitemap and then only if you have a non-empty servlet contextPath, should never be used in 2.2  -->
  <!-- NB. for 2.2 these four paths MUST be overridden by the sitemap -->
  <xsl:param name="ajax-resources"><xsl:value-of select="$context-path"/>/_cocoon/resources/ajax</xsl:param>        <!-- Url prefix for ajax block resources -->
  <xsl:param name="dojo-resources"><xsl:value-of select="$context-path"/>/_cocoon/resources/dojotoolkit</xsl:param> <!-- Url prefix for dojo resources -->
  <xsl:param name="forms-resources"><xsl:value-of select="$context-path"/>/_cocoon/resources/forms</xsl:param>      <!-- Url prefix for forms block resources -->
  <xsl:param name="forms-system"><xsl:value-of select="$context-path"/>/_cocoon/system/forms</xsl:param>            <!-- Url prefix for forms block system pipelines -->
  
  <!-- set the default dojo css theme, other choices are currently 'nihilo' and 'soria' -->
  <xsl:param name="dojo-theme-default">tundra</xsl:param>
  
  <!-- A convenient way for samples to have a url that switches the theme. If your app does not want this, don't send this param from your sitemap -->
  <xsl:param name="dojo-theme-param"></xsl:param>
  
  <!-- the determine dojo css theme to use -->
  <xsl:variable name="dojo-theme">
    <xsl:choose>
        <xsl:when test="$dojo-theme-param = ''"><xsl:value-of select="$dojo-theme-default"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$dojo-theme-param"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  
  <!-- option to turn on console debugging for dojo on the browser and loading of uncompressed resources, from a parameter in the sitemap -->
  <xsl:param name="dojo-debug">false</xsl:param>
  <xsl:variable name="dojo-debug-js"><xsl:if test="$dojo-debug='true'">.uncompressed.js</xsl:if></xsl:variable><!-- load the uncompressed version for debug mode -->
  <xsl:variable name="dojo-debug-css"><xsl:if test="$dojo-debug='true'">.commented.css</xsl:if></xsl:variable>
  
  <!-- Configure the dojo locale from a parameter in the sitemap (required?). -->
  <xsl:param name="dojo-locale"></xsl:param> 
  
  <!-- Create a variable with the normalized locale, dojo needs locale parts to be separated with a dash -->
  <xsl:variable name="dojoLocale">
    <xsl:choose>
      <xsl:when test="$dojo-locale != ''">
        <xsl:value-of select="translate($dojo-locale, '_', '-')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>en</xsl:text><!-- I am not sure this is the correct thing to do, but it is difficult to test -->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  
  <!-- create a variable for the dojo configuration -->
  <xsl:variable name="djConfig">
    isDebug: <xsl:value-of select="$dojo-debug"/>,
    parseOnLoad: true,
    modulePaths: {
      'cocoon.forms': '<xsl:value-of select="$forms-resources"/>/js',
      'cocoon.ajax' : '<xsl:value-of select="$ajax-resources"/>/js'
    },
    extraLocale: 'en', <!-- need to force validated plain numbers to 'en', dojo does not know about cocoon's plain number format -->
    locale: '<xsl:value-of select="$dojoLocale"/>'<xsl:if test="$dojo-use-cdn = 'true' and $dojo-cdn != ''">,
    dojoBlankHtmlUrl: '<xsl:value-of select="$dojo-resources"/>/dojo/resources/blank.html',
    dojoIframeHistoryUrl: '<xsl:value-of select="$dojo-resources"/>/dojo/resources/iframe_history.html', <!-- not currently used by cforms itself -->
    baseUrl: './', 
    xdWaitSeconds: 10</xsl:if>
  </xsl:variable>

  <xsl:template match="head" mode="forms-field">    
    <xsl:choose>
      <xsl:when test="$dojo-use-cdn = 'true' and $dojo-cdn != ''">
        <link rel="stylesheet" type="text/css" href="{$dojo-cdn}/dijit/themes/dijit.css"/><!-- Google CDN does not keep *.commented.css -->
        <link rel="stylesheet" type="text/css" href="{$dojo-cdn}/dijit/themes/{$dojo-theme}/{$dojo-theme}.css"/>
        <link rel="stylesheet" type="text/css" href="{$dojo-cdn}/dijit/themes/{$dojo-theme}/{$dojo-theme}_rtl.css"/>
        <script type="text/javascript" djConfig="{normalize-space($djConfig)}" src="{$dojo-cdn}/dojo/dojo.xd.js{$dojo-debug-js}"/>
        <script type="text/javascript" src="{$dojo-cdn}/dijit/dijit.xd.js{$dojo-debug-js}"/>
      </xsl:when>
      <xsl:otherwise>
        <link rel="stylesheet" type="text/css" href="{$dojo-resources}/dijit/themes/dijit.css{$dojo-debug-css}"/>
        <link rel="stylesheet" type="text/css" href="{$dojo-resources}/dijit/themes/{$dojo-theme}/{$dojo-theme}.css{$dojo-debug-css}"/>
        <link rel="stylesheet" type="text/css" href="{$dojo-resources}/dijit/themes/{$dojo-theme}/{$dojo-theme}_rtl.css{$dojo-debug-css}"/>
        <script type="text/javascript" djConfig="{normalize-space($djConfig)}" src="{$dojo-resources}/dojo/dojo.js{$dojo-debug-js}"/>
        <script type="text/javascript" src="{$dojo-resources}/dijit/dijit.js{$dojo-debug-js}"/>
      </xsl:otherwise>
    </xsl:choose>
    <link rel="stylesheet" type="text/css" href="{$forms-resources}/css/forms.css"/>
    <script type="text/javascript">
      dojo.require("dojo.parser");                                    <!-- dojo parser is required to convert tags with @dojoType into widgets -->
      dojo.require("cocoon.forms.common");                            <!-- tell dojo we require the forms commons library -->
      dojo.addOnLoad(function(){cocoon.forms.callOnLoadHandlers();}); <!-- ask dojo to run our onLoad handlers -->
    </script>
    <xsl:apply-imports/>
    <!--<xsl:apply-templates select="." mode="forms-htmlarea"/>-->
  </xsl:template>

  <xsl:template match="body" mode="forms-field">
    <xsl:apply-imports/>
    <!--<xsl:apply-templates select="." mode="forms-htmlarea"/>-->
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
      | Generic fi:field : produce an <input> (advanced version)
      | Adds support for live data validation on the client, based on fi:datatype
      | (Browser validation may be turned off with fi:styling/@browserValidation="false")
      +-->
      
      <!-- TODO: Document new options -->
      <!-- TODO: fi:styling/@regExp should not be a user tag, it should come from the validator (eventually?) -->
      
  <xsl:template match="fi:field[not(fi:styling/@type='inplace-area') and (@state='active' or @state='disabled')] | 
                       fi:aggregatefield[not(fi:styling/@type='inplace-area') and (@state='active' or @state='disabled')]">
    <!-- isValidating --><xsl:variable name="v" select="@required = 'true' or fi:validation-message or fi:styling/@regExp != '' or fi:styling/@browserValidation != 'false'"/>
    <!-- isFormatted  --><xsl:variable name="f" select="fi:datatype/fi:convertor"/>
    <!-- isCurrency   --><xsl:variable name="c" select="fi:datatype/fi:convertor/@variant = 'currency'"/>
    <!-- data type    --><xsl:variable name="t" select="fi:datatype/@type"/>
    <xsl:variable name="editor"><!-- user overide for which field type to use (only use <input> based editors please) -->
      <xsl:choose>
        <xsl:when test="fi:styling/@editor != ''"><xsl:value-of select="fi:styling/@editor"/></xsl:when>
        <xsl:when test="$c">cocoon.forms.CurrencyField</xsl:when>
        <xsl:when test="$v and ($t='integer' or $t='long' or $t='decimal' or $t='float' or $t='double')">cocoon.forms.NumberField</xsl:when>
        <xsl:when test="$v">cocoon.forms.ValidatingTextField</xsl:when>
        <xsl:otherwise>cocoon.forms.TextField</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="require"><!-- required base-class for the editors -->
      <xsl:choose>
        <xsl:when test="fi:styling/@editor != ''"><xsl:value-of select="$editor"/></xsl:when>
        <xsl:when test="$c"><xsl:value-of select="$editor"/></xsl:when>
        <xsl:when test="$t='string' and $v"><xsl:value-of select="$editor"/></xsl:when>
        <xsl:when test="$t='string' or not($v)">cocoon.forms.TextField</xsl:when>
        <xsl:otherwise>cocoon.forms.NumberField</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="valueType"><!-- which *numeric* datatype are we sending -->
      <xsl:choose>
        <xsl:when test="$t!='string' and $f">l10n-<xsl:value-of select="fi:datatype/fi:convertor/@variant"/></xsl:when>
        <xsl:when test="$t!='string'"><xsl:value-of select="$t"/></xsl:when><!-- don't bother sending a valueType for String -->
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="constraints"><!-- contraints for currencies -->
      <xsl:if test="$c">currency:'<xsl:value-of select="fi:datatype/fi:convertor/@currency"/>',</xsl:if>
      <xsl:if test="fi:datatype/fi:convertor/@symbol">symbol:'<xsl:value-of select="fi:datatype/fi:convertor/@symbol"/>',</xsl:if>
      <xsl:if test="fi:datatype/fi:convertor/@pattern">pattern:'<xsl:value-of select="fi:datatype/fi:convertor/@pattern"/>'</xsl:if>
    </xsl:variable>
    <span id="{@id}:bu" class="dijitReset dijitInline dijitInlineTable dijitLeft forms forms-field">
      <xsl:if test="fi:captcha-image">
        <div class="dijitInline forms-captcha">
          <img src="captcha-{fi:captcha-image/@id}.jpg" style="vertical-align:middle" class="forms-captcha"/>
          <xsl:text> </xsl:text>
        </div>
      </xsl:if>
      <span>
        <xsl:choose>
          <xsl:when test="fi:styling[@type='inplace']">
            <xsl:attribute name="class">dijitInline forms-field-input forms-inplace-editor</xsl:attribute>
            <xsl:variable name="editorId" select="generate-id()"/>
            <xsl:variable name="editorParams"><!-- params for inplace editor's editor -->
              id:'<xsl:value-of select="$editorId"/>',
              <xsl:apply-templates select="." mode="subeditor-styling"/>
              <xsl:if test="$valueType != ''">valueType:'<xsl:value-of select="$valueType"/>',</xsl:if>
              <xsl:if test="$constraints != ''">constraints:{<xsl:value-of select="$constraints"/>}</xsl:if>
            </xsl:variable>
            <script type="text/javascript">dojo.require("cocoon.forms.InplaceEditor");</script>
            <script type="text/javascript">dojo.require("<xsl:value-of select="$require"/>");</script>
            <span id="{@id}" value="{fi:value}" dojoType="cocoon.forms.InplaceEditor" editor="{$editor}" editorId="{$editorId}" valueNode="{@id}:input">
              <xsl:if test="@autoSave"><xsl:attribute name="autoSave"><xsl:value-of select="@autoSave"/></xsl:attribute></xsl:if>
              <xsl:attribute name="editorParams">{<xsl:value-of select="normalize-space($editorParams)"/>}</xsl:attribute>
              <xsl:if test="fi:hint"><xsl:attribute name="noValueIndicator">[<xsl:value-of select="fi:hint/text()"/>]</xsl:attribute></xsl:if>
              <xsl:apply-templates select="." mode="styling"/>
            </span>
            <input name="{@id}" id="{@id}:input" type="hidden"/>
          </xsl:when>
          <xsl:otherwise><!-- the field is not inside an InplaceEditor -->
            <xsl:attribute name="class">dijitInline forms-field-input</xsl:attribute>
            <script type="text/javascript">dojo.require("<xsl:value-of select="$require"/>");</script>
            <input name="{@id}" id="{@id}:input" dojoType="{$editor}" value="{fi:value}" type="text">
              <xsl:if test="@whitespace and $t = 'string'"><xsl:attribute name="trim"><xsl:value-of select="@whitespace"/></xsl:attribute></xsl:if>
              <xsl:if test="$valueType !=''"><xsl:attribute name="valueType"><xsl:value-of select="$valueType"/></xsl:attribute></xsl:if>
              <xsl:if test="$constraints !=''"><xsl:attribute name="constraints">{<xsl:value-of select="$constraints"/>}</xsl:attribute></xsl:if>
              <xsl:if test="fi:captcha-image"><xsl:attribute name="autocomplete">off</xsl:attribute></xsl:if>
              <xsl:if test="fi:hint"><xsl:attribute name="promptMessage"><xsl:value-of select='translate(fi:hint/text(),"&#x27;","`")'/></xsl:attribute></xsl:if>
              <xsl:if test="fi:validation-message"><xsl:attribute name="invalidMessage"><xsl:value-of select='translate(fi:validation-message/text(),"&#x27;","`")'/></xsl:attribute></xsl:if>
              <xsl:apply-templates select="." mode="styling"/>
            </input>
          </xsl:otherwise>
        </xsl:choose>
      </span>
      <span class="dijitReset dijitInline forms-help-button"><xsl:apply-templates select="." mode="common"/></span>
    </span>
  </xsl:template>

  <!-- special styling mode for making properties instead of attributes -->
  <xsl:template match="fi:*" mode="subeditor-styling">
    <xsl:if test="fi:hint">promptMessage:'<xsl:value-of select='translate(fi:hint/text(),"&#x27;","`")'/>',</xsl:if>
    <xsl:if test="fi:validation-message">invalidMessage:'<xsl:value-of select='translate(fi:validation-message/text(),"&#x27;","`")'/>'</xsl:if>
    <xsl:if test="@whitespace">trim:'<xsl:value-of select="@whitespace"/>',</xsl:if>
    <xsl:if test="@required = 'true'">required:'true',</xsl:if>
    <xsl:apply-templates select="fi:styling/@*[not(local-name()='type')][not(local-name()='submit-on-change')][not(local-name()='onChange')]" mode="subeditor-styling"/>
  </xsl:template>

  <xsl:template match="fi:styling/@*" mode="subeditor-styling" priority="-1">
    '<xsl:value-of select="local-name()"/>':'<xsl:value-of select="."/>',
  </xsl:template>

  <xsl:template match="fi:styling/@editorParams" mode="subeditor-styling">
    <xsl:if test="string-length(.) &gt; 2 and starts-with(.,'{')">
      <xsl:value-of select="substring(., 2, string-length(.) -1)"/>,
    </xsl:if>
  </xsl:template>


  <!--+
      | fi:field with @type 'textarea'  (advanced version)
      +-->
  <xsl:template match="fi:field[@state = 'active'][fi:styling/@type='textarea' or fi:styling/@type='htmlarea' or fi:styling/@type='inplace-area']">
    <!-- isValidating --><xsl:variable name="v" select="@required = 'true' or fi:validation-message or fi:styling/@browserValidation != 'false'"/>
    <!-- styling type --><xsl:variable name="t" select="fi:styling/@type"/>
    <xsl:variable name="editor"><!-- which field type to use -->
      <xsl:choose>
        <xsl:when test="fi:styling/@editor != ''"><xsl:value-of select="fi:styling/@editor"/></xsl:when>
        <xsl:when test="$t = 'htmlarea'">cocoon.forms.RichTextArea</xsl:when><!-- or should we change this to 'richarea' ? -->
        <xsl:when test="$v">cocoon.forms.ValidatingTextArea</xsl:when>
        <xsl:otherwise>cocoon.forms.TextArea</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <span id="{@id}:bu" class="dijitReset dijitInlineTable forms-field-textarea ">
      <div>
        <xsl:choose>
          <xsl:when test="fi:styling[@type='inplace-area']">
            <xsl:attribute name="class">dijitInline forms-field-input forms-inplace-editor forms-inplace-textarea</xsl:attribute>
            <xsl:variable name="editorId" select="generate-id()"/>
            <xsl:variable name="editorParams"><!-- params for inplace editor's editor -->
              id:'<xsl:value-of select="$editorId"/>',
              <xsl:apply-templates select="." mode="subeditor-styling"/>
            </xsl:variable>
            <script type="text/javascript">dojo.require("cocoon.forms.InplaceEditor");</script>
            <script type="text/javascript">dojo.require("<xsl:value-of select="$editor"/>");</script>
            <span id="{@id}" dojoType="cocoon.forms.InplaceEditor" editor="{$editor}" editorId="{$editorId}" valueNode="{@id}:save" renderAsHtml="true">
              <xsl:if test="@autoSave"><xsl:attribute name="autoSave"><xsl:value-of select="@autoSave"/></xsl:attribute></xsl:if>
              <xsl:attribute name="editorParams">{<xsl:value-of select="normalize-space($editorParams)"/>}</xsl:attribute>
              <xsl:if test="fi:hint"><xsl:attribute name="noValueIndicator">[<xsl:value-of select="fi:hint/text()"/>]</xsl:attribute></xsl:if>
              <xsl:apply-templates select="." mode="styling"/>
              <xsl:copy-of select="translate(fi:value/node(), '&#13;', '')"/>
            </span>
            <input name="{@id}" id="{@id}:save" type="hidden"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="class">dijitInline forms-field-input forms-field-textarea</xsl:attribute>
            <script type="text/javascript">dojo.require("<xsl:value-of select="$editor"/>");</script>
            <textarea name="{@id}" id="{@id}:input" dojoType="{$editor}">
              <xsl:if test="@whitespace and $t = 'string'"><xsl:attribute name="trim"><xsl:value-of select="@whitespace"/></xsl:attribute></xsl:if>
              <xsl:if test="fi:hint"><xsl:attribute name="promptMessage"><xsl:value-of select='translate(fi:hint,"&#x27;","`")'/></xsl:attribute></xsl:if>
              <xsl:if test="fi:validation-message"><xsl:attribute name="invalidMessage"><xsl:value-of select='translate(fi:validation-message/text(),"&#x27;","`")'/></xsl:attribute></xsl:if>
              <xsl:apply-templates select="." mode="styling"/>
              <xsl:copy-of select="translate(fi:value/node(), '&#13;', '')"/>
            </textarea>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:if test="$editor = 'cocoon.forms.RichTextArea'"><input id="{@id}:save" name="{@id}" type="hidden"/></xsl:if>
      </div><div class="dijitInline forms-help-button"><xsl:apply-templates select="." mode="common"/></div>
    </span>
  </xsl:template>

  <!--+
      | Add fi:help (etc.) to the common stuff.
      +-->
  <xsl:template match="fi:*" mode="common">
    <!--<xsl:apply-imports/>-->

    <xsl:apply-templates select="fi:help"/>
  </xsl:template>

  <!--+
      |
      +-->
  <xsl:template match="fi:help">
    <script type="text/javascript">dojo.require("dijit.form.Button");</script>
    <script type="text/javascript">dojo.require("dijit.Dialog");</script>
    <div dojoType="dijit.form.DropDownButton">
        <span><i18n:text i18n:catalogue="forms">general.field-help-symbol</i18n:text></span>
        <div dojoType="dijit.TooltipDialog"><xsl:copy-of select="node()"/></div>
    </div>
  </xsl:template>

  <!--+
      | Dojoize regular submit buttons
      |
      | TODO: This currently supplies an invalid submit ID, so commented out, is there a fix?
  <xsl:template match="input[@type='submit']">
    <script type="text/javascript">dojo.require("dijit.form.Button");</script>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:attribute name="dojoType">dijit.form.Button</xsl:attribute>
      <xsl:attribute name="label">
        <xsl:if test="not(@value) or @value=''">Submit</xsl:if><xsl:value-of select="@value"/>
      </xsl:attribute>
    </xsl:copy>
  </xsl:template>
      +-->

  <!--+
      | TODO: Not used
      +-->
  <xsl:template match="fi:hint">
    <xsl:param name="id"/>
    <xsl:attribute name="title"></xsl:attribute>
    <script type="text/javascript">dojo.require("dijit.Tooltip");</script>
    <span dojoType="dijit.Tooltip" connectId="{$id}" position="above,below" class="forms-help-popup">
        <xsl:copy-of select="node()"/>
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
    <span id="{@id}:bu" class="dijitReset dijitInlineTable forms-field-select">
      <span class="dijitInline">
        <script type="text/javascript">dojo.require("cocoon.forms.Select");</script>
        <select id="{@id}:input" name="{@id}" dojoType="cocoon.forms.Select">
          <xsl:apply-templates select="." mode="styling"/>
          <xsl:if test="fi:hint"><xsl:attribute name="promptMessage"><xsl:value-of select='translate(fi:hint,"&#x27;","`")'/></xsl:attribute></xsl:if>
          <xsl:if test="fi:validation-message"><xsl:attribute name="invalidMessage"><xsl:value-of select='translate(fi:validation-message/text(),"&#x27;","`")'/></xsl:attribute></xsl:if>
          <xsl:for-each select="fi:selection-list/fi:item">
            <option value="{@value}">
              <xsl:if test="@value = $value">
                <xsl:attribute name="selected">selected</xsl:attribute>
              </xsl:if>
              <xsl:copy-of select="fi:label/node()"/>
            </option>
          </xsl:for-each>
        </select>
      </span>
      <span class="dijitInline forms-help-button"><xsl:apply-templates select="." mode="common"/></span>
    </span>
  </xsl:template>

  <!--+
      | Select Field that makes suggestions as you type
      | Rendering depends on several different factors:
      |   1) There is a selection-list and fi:styling/@type='suggest', the content of the selection list are used for the suggestions filtered on the client
      |   2) There is no selection-list but there is a suggestion-list that has filter handlers in the Model (legacy CFormsSuggest pattern)
      |   3) There is no selection-list or suggestion-list but there is a fi:styling/@dataUrl that refers to a pipeline to provide suggestions (legacy *EditorWithSuggestions pattern)
      |
      | Because the base class dijit.forms.FilteringSelect is able to perform filtering itself,
      | this widget will normally only retrieve the suggestions once, on load.
      | If your list of possible suggestions is too big, you can use fi:styling/@dynamic to force a load for each keystroke,
      | You can add paging by adding fi:styling/@pageSize. NB. TODO: no server-side support, paging currently happens on the client (except for dynamic)
      | You can toggle auto-completion with fi:styling/@autoComplete (default: true)
      +-->
  <xsl:template match="fi:field[(fi:styling/@type='suggest' or fi:styling/@dataUrl != '') and @state='active']" priority="1">
    <xsl:variable name="value" select="fi:value"/>
    <xsl:variable name="isStore" select="not(fi:selection-list) or fi:styling/@dataUrl"/>    
    <!-- <xsl:variable name="queryExpr">
      <xsl:choose>
        <xsl:when test="fi:styling/@match = 'contains'">*${0}*</xsl:when>
        <xsl:when test="fi:styling/@match = 'word'">\b${0}\w</xsl:when>
      </xsl:choose>
    </xsl:variable>      -->
    <xsl:if test="$isStore"><xsl:apply-templates select="." mode="suggestion-store"/></xsl:if><!-- Add a data.store? -->
    <span id="{@id}:bu" class="dijitReset dijitInline dijitInlineTable dijitLeft forms forms-field">
      <span class="dijitInline forms-field-input">
        <script type="text/javascript">dojo.require("cocoon.forms.FilteringSelect");</script>
        <select name="{@id}" id="{@id}:input" value="{fi:value}" dojoType="cocoon.forms.FilteringSelect">
          <xsl:apply-templates select="." mode="styling"/>
          <xsl:if test="fi:hint"><xsl:attribute name="promptMessage"><xsl:value-of select='translate(fi:hint,"&#x27;","`")'/></xsl:attribute></xsl:if>
          <xsl:if test="fi:validation-message"><xsl:attribute name="invalidMessage"><xsl:value-of select='translate(fi:validation-message/text(),"&#x27;","`")'/></xsl:attribute></xsl:if>
          <xsl:if test="$isStore">
            <xsl:attribute name="store"><xsl:value-of select="concat('_cforms_store_',generate-id(.))"/></xsl:attribute>
            <xsl:attribute name="searchAttr">label</xsl:attribute>
          </xsl:if>
          <xsl:if test="fi:suggestion"><xsl:attribute name="suggestion"><xsl:value-of select="fi:suggestion"/></xsl:attribute></xsl:if><!-- needed for populating select on round trip ?? -->
          <!--<xsl:if test="$queryExpr != ''"><xsl:attribute name="queryExpr"><xsl:value-of select="$queryExpr"/></xsl:attribute></xsl:if>-->
          <xsl:for-each select="fi:selection-list/fi:item">
            <option value="{@value}">
              <xsl:if test="@value = $value">
                <xsl:attribute name="selected">selected</xsl:attribute>
              </xsl:if>
              <xsl:copy-of select="fi:label/node()"/>
            </option>
          </xsl:for-each>
        </select>
      </span>
      <span class="dijitInline forms-help-button"><xsl:apply-templates select="." mode="common"/></span>
    </span>
  </xsl:template>

  <!--+
      | A dojo.data ReadStore for retrieving suggestion lists dynamically
      | Works with either a fd:suggestion-list/@type="javascript" in the Model
      |   - in which case the fd:suggestion-list must contain an implementation
      |     and there must be pipeline at "_cocoon/forms/suggest" (relative) 
      |     that uses the SuggestionListGenerator to return the list (see samples)
      | Or fi:styling/@dataUrl in the Template
      |   - in which case there must be a pipeline at that url to return the list
      |
      | TODO: implement 'start' and 'count' request params in the samples
      +-->
  <xsl:template match="fi:field" mode="suggestion-store">
    <xsl:variable name="isWidget" select="not(fi:styling/@dataUrl)"/>
    <xsl:variable name="class">
      <xsl:choose>
        <xsl:when test="not(fi:styling/@dynamic) or fi:styling/@dynamic = 'true'">cocoon.forms.SuggestionReadStore</xsl:when>
        <xsl:otherwise>dojo.data.ItemFileReadStore</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <script type="text/javascript">dojo.require("<xsl:value-of select="$class"/>");</script>
    <div jsId="_cforms_store_{generate-id(.)}" dojoType="{$class}">
      <xsl:if test="fi:styling/@pageSize"><xsl:attribute name="pageSize"><xsl:value-of select="fi:styling/@pageSize"/></xsl:attribute></xsl:if>
      <xsl:choose>
        <xsl:when test="$isWidget">
          <!-- TODO: maybe a candidate for a system pipeline? -->
          <!-- Reads from a widget, via a pipeline:  you must have a pipeline that serves this url 
               - the widget adds: continuation-id, widget (name) and filter (search) request parameters 
               - there must be a suggestion-list providing this data, in your Model -->
          <xsl:attribute name="url"><xsl:value-of select="$forms-system"/>/suggestionlist</xsl:attribute>
          <xsl:attribute name="client"><xsl:value-of select="@id"/>:input</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <!-- Reads from a pipeline: you must have a pipeline that serves this url (the widget adds a filter request parameter) -->
          <xsl:attribute name="url"><xsl:value-of select="$forms-system"/>/suggestionlist/<xsl:value-of select="fi:styling/@dataUrl"/></xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>      
    </div>
  </xsl:template>

  <!--+
      | fi:multivaluefield with list-type='double-listbox' styling
      +-->
  <xsl:template match="fi:multivaluefield[fi:styling/@list-type='double-listbox']">
    <xsl:variable name="id" select="@id"/>
    <xsl:variable name="values" select="fi:values/fi:value/text()"/>
    <xsl:variable name="browser-variable"><xsl:value-of select="translate($id, '.', '_')"/>_jsWidget</xsl:variable>

    <script type="text/javascript">var <xsl:value-of select="$browser-variable"/>;</script>
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
      <script type="text/javascript"><xsl:value-of select="$browser-variable"/> = forms_createOptionTransfer('<xsl:value-of select="@id"/>', <xsl:value-of select="@listening = 'true' and not(fi:styling/@submit-on-change = 'false')"/>);</script>
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
    <xsl:apply-templates select="." mode="label-ajax-request"/>
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
    <xsl:apply-templates select="." mode="label-ajax-request"/>
  </xsl:template>


  <!--+
      | Field with in-place editing
      | Reacts to 2 different types:
      | - 'inplace' for a single line input
      | - 'inplace-area' for a textarea
      +-->
  <xsl:template match="fi:field[fi:styling[@type='inplace' or @type='inplace-area'] and @state='active']" mode="redundant">
    <span id="{@id}:bu" class="forms-inplace-editor">
      <script type="text/javascript">dojo.require("dijit.InlineEditBox");</script>
      <span dojoType="dijit.InlineEditBox" autoSave="false">
        <xsl:attribute name="onChange">
          <xsl:text>dojo.byId('</xsl:text>
          <xsl:value-of select="@id"/>
          <xsl:text>:input').value = arguments[0];</xsl:text>
          <xsl:if test="(@listening = 'true' and not(fi:styling/@submit-on-change = 'false')) or fi:styling/@submit-on-change = 'true'">
            <xsl:text>cocoon.forms.submitForm(dojo.byId('</xsl:text>
            <xsl:value-of select="@id"/>
            <xsl:text>:input'))</xsl:text>
          </xsl:if>
        </xsl:attribute>
        <!-- TODO: should this become a new fi:styling? -->
        <xsl:if test="@autoSave">
          <xsl:attribute name="autoSave"><xsl:value-of select="@autoSave"/></xsl:attribute>
        </xsl:if>
        <!-- TODO: many more editors are available now -->
        <xsl:if test="fi:styling/@type='inplace-area'">
          <xsl:attribute name="editor">
            <xsl:text>dijit.form.Textarea</xsl:text>
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
      <input id="{concat(@id,':input')}" type="hidden" name="{@id}" value="{fi:value}"/>
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
    <xsl:apply-templates select="." mode="label-ajax-request"/>
  </xsl:template>

</xsl:stylesheet>
