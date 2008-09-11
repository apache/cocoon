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
dojo.provide("cocoon.forms.FilteringSelect");
dojo.require("dijit.form.FilteringSelect");
dojo.require("cocoon.forms._FieldMixin");

// @see cocoon.forms.Select for design notes

// TODO: Required status not appearing on parent objects (Tabs etc.)

dojo.declare("cocoon.forms.FilteringSelect", [dijit.form.FilteringSelect, cocoon.forms._ErrorAwareFieldMixin, cocoon.forms._FieldHintMixin], {	
  
  templateString: "<div class=\"dijit dijitReset dijitInlineTable dijitLeft\"\r\tid=\"widget_${id}\"\r\tdojoAttachEvent=\"onmouseenter:_onMouse,onmouseleave:_onMouse,onmousedown:_onMouse\" dojoAttachPoint=\"comboNode\" waiRole=\"combobox\" tabIndex=\"-1\"\r\t><div style=\"overflow:hidden;\"\r\t\t><div class='dijitReset dijitRight dijitButtonNode dijitArrowButton dijitDownArrowButton'\r\t\t\tdojoAttachPoint=\"downArrowNode\" waiRole=\"presentation\"\r\t\t\tdojoAttachEvent=\"onmousedown:_onArrowMouseDown,onmouseup:_onMouse,onmouseenter:_onMouse,onmouseleave:_onMouse\"\r\t\t\t><div class=\"dijitArrowButtonInner\">&thinsp;</div\r\t\t\t><div class=\"dijitArrowButtonChar\">&#9660;</div\r\t\t></div\r\t\t><div class=\"dijitReset dijitValidationIcon\"><br></div\r\t\t><div class=\"dijitReset dijitValidationIconText\">${_cforms_statusMarker}</div\r\t\t><div class=\"dijitReset dijitInputField\"\r\t\t\t><input type=\"text\" autocomplete=\"off\" name=\"${name}\" class='dijitReset'\r\t\t\tdojoAttachEvent=\"onkeypress:_onKeyPress, onfocus:_update, compositionend,onkeyup\"\r\t\t\tdojoAttachPoint=\"textbox,focusNode\" waiRole=\"textbox\" waiState=\"haspopup-true,autocomplete-list\"\r\t\t/></div\r\t></div\r></div>\r",

  suggestion: "", // pre-supplied label for round-tripping
  
  baseClass: "dijitComboBox",

  postMixInProperties: function() {
    if (this.srcNodeRef.getAttribute("store")) {
      this.searchDelay = 200; // slow down a bit
    }
    this.inherited(arguments);
  },
  
  buildRendering: function() {
    this.inherited(arguments);
    if (this.value && this.suggestion) this._setValue(this.value, this.suggestion, false); 
  },



/* 

    TODO: this is doing a reverse lookup using the label when you blur the field
    this is bad as labels seldom need to be unique (unlike key values)
    
    It should not need to do the reverse-lookup, it has just retrieved the data it needs
    NB. The latest QueryReadStore samples do not do this, is there a problem with my code or data?


  setDisplayedValue:function(label, priorityChange){
    // this function in the superclass appears to be completely broken.
    // for one, it attempts to do a reverse lookup for the value, FROM THE LABEL !!!!!!!!!!!!
    
    // TODO: if I nop this function, after setting the label, the value is unchanged (wrong)
    
  }
    
*/
  
});