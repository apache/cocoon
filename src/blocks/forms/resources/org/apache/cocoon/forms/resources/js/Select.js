/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
dojo.provide("cocoon.forms.Select");
dojo.require("dijit.form._FormWidget");
dojo.require("cocoon.forms._FieldMixin");

/*


Design of *Select etc.

Issues:
  dijit.form.ComboBox only sends Labels, not Values, but has 'other-field' functionality
  dijit.form.FilteringSelect does send Values, but has no 'other-field' functionality
    I will have to defualt on FilteringSelect
    Is there a usecase for ComboBox?
  
  dojo.data.ItemFileReadStore only reads the suggestion list once, then filters locally
  dojox.data.QueryReadStore does query the server for each keystroke
    add optional fi:styling dynamic="true|false" to choose between the two (which default?)
    
    
Widgets:

fi:field with selection-list
  cocoon.forms.Select 
    extends cocoon.forms._ErrorAwareFieldMixin, cocoon.forms._FieldHintMixin
  templateString adds status to a native select + ComboBox CSS classes

fi:field with with selection-list and fi:styling/@list-type="listbox"
  cocoon.forms.Select 
  templateString adds status to a native select + ComboBox CSS classes
  supports fi:styling/@size, default:5
  
fi:field with selection-list and fi:styling/@type="suggest"  
  cocoon.forms.FilteringSelect 
    extends dijit.form.FilteringSelect, cocoon.forms._ErrorAwareFieldMixin, cocoon.forms._SizedFieldMixin
  templateString adds status to extended Template
  supports fi:styling/@pageSize
  
fi:field without selection-list but with fi:styling/@type="suggest" and (optional) fi:styling/@dataUrl
  cocoon.forms.FilteringSelect
  templateString adds status to extended Template
  supports fi:styling/@pageSize (only on the client at first ....)
  xslt adds data.store 
    if fi:styling/@dynamic="true" use dojox.data.QueryReadStore, otherwise use default dojo.dataItemFileReadStore
    if fi:styling/@dataUrl use it, else assume(?) widget makes URL like CFormsSuggest (&filter=SearchString auto added to both)

fi:multivaluefield with selection-list
  cocoon.forms.MultiSelect
    extends cocoon.forms._ErrorAwareFieldMixin
  templateString adds status to two native selects (multiple="true"), with controls for move etc.
  supports fi:styling/@size, default:5


fi:multivaluefield without selection-list but with fi:styling/@type="suggest" and fi:styling/@url
  cocoon.forms.FilteringMultiSelect
  templateString adds status to a dijit.form.FilteringSelect and a native select (multiple="true"), with controls for move etc.
  supports fi:styling/@size, default:5
  xslt adds data.store 
    if fi:styling/@dynamic="true" use dojox.data.QueryReadStore, otherwise use default dojo.dataItemFileReadStore
    if fi:styling/@url use it, else widget makes URL like CFormsSuggest (&filter=SearchString auto added to both)

May need to extend the data.stores, @see dojox/data/tests/QueryReadStore.html

TODO: Variations on multiselect not well worked out-and-out
TODO: Variations for the use of fi:styling/@dataUrl :
      1) system path to Widget-based javascript suggestion-list handler
      2) system path to named System.Collections.registerSelectionList
      3) ¿¿ absolute or relative path to a custom suggestion-list handler ??

TODO: What about cforms datatypes?
      Cocoon sends nice formatted strings; test it works in json response; there is some data-type support in dojo.data ... is it useful?
      Do we need client-side validation on selection lists, where we have no 'other-value' functionality?
*/



dojo.declare("cocoon.forms.Select", [dijit.form._FormValueWidget, cocoon.forms._ErrorAwareFieldMixin, cocoon.forms._FieldHintMixin], {	
  
  templateString: "<div class=\"dijit dijitReset dijitInlineTable dijitLeft\" id=\"widget_${id}\"  tabIndex=\"-1\"\r\t><div xstyle=\"overflow:hidden;\"\r\t\t><div class=\"dijitReset dijitValidationIcon\"><br></div\r\t\t><div class=\"dijitReset dijitValidationIconText\">${_cforms_statusMarker}</div\r\t\t><div dojoAttachEvent=\"onmouseenter:_onMouse,onmouseleave:_onMouse,onmousedown:_onMouse\" class=\"dijitReset\"\r\t\t\t><select name=\"${name}\" class=\"dijitReset \" dojoAttachEvent=\"onchange:onChange\" dojoAttachPoint=\"focusNode\"\r\t\t></select></div\r\t></div\r></div>\r",

  size: 1,
  multiple: false,

  baseClass: "dijitComboBox",
	attributeMap: dojo.mixin(dojo.clone(dijit.form._FormValueWidget.prototype.attributeMap),
		{size:"focusNode", multiple:"focusNode"}),

// TODO: do we need setValue and getValue functions?

  // Widget interface - copy Options to template select, the auto-mechanism is breaking
  postCreate: function() {
    dojo.forEach(
      this.srcNodeRef.options, 
      function(o){ 
        this.focusNode[this.focusNode.options.length] = 
          new Option(o.text, o.value, o.defaultSelected, o.defaultSelected);
      }, 
      this
    );
    this.inherited(arguments);
  }

  
});