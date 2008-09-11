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
dojo.provide("cocoon.forms._FieldMixin");
dojo.require("cocoon.forms.common");


/**
 * Common functions and properties for CForms Fields.
 * 
 * Mix this into field widgets to provide :
 *
 *   Status reporting to parent containers.
 *   eg. so a TabGroup may show the required or error status of it's children on the relevant tab.
 *   Useful when the parent container may not always show all of it's children.
 *
 *   Support for running the User's optional onChange handler in the expected context. 
 *
 *   Support for running the optional onSubmit function of subclasses.
 *
 * NOTE: introduced in 2.1.12
 *
 * @version $Id$
 */
dojo.declare("cocoon.forms._FieldMixin", null, {


  // Widget interface - make getDescendants() work
  buildRendering: function(){
    this.inherited(arguments);
    if(!this.containerNode) this.containerNode = this.domNode; 
  },

  // Widget interface
  postCreate: function() {
    this.inherited(arguments);
    // if there was a user @onChange attribute, it has overriden the Widget's onChange function
    if (this.srcNodeRef.getAttribute("onChange") !== "") {
      // make sure the onChange script runs with the field as it's context, not the Widget
      this.onChange = dojo.hitch(this.getValueNode(), this.onChange);
    }
  },

  // Widget interface
  startup: function(){
    this.inherited(arguments);
    // if this widget has an onSubmit function, wire it to the Form's published onSubmit event
    if (dojo.isFunction(this.onSubmit)) {
      this.onSubmitSubscription = dojo.subscribe(this.getFormWidget().getOnSubmitTopic(), this, this.onSubmit);
    }
    // send my status to my ancestors
    this.setAncestorStatus();
  },

  destroy: function() {
    this.inherited(arguments);
    if (this.onSubmitSubscription) dojo.unsubscribe(this.onSubmitSubscription);
  },

  // TextBox interface
  _onBlur: function(){
    this.inherited(arguments);
    // set ancestor status when finished editing
    this.setAncestorStatus();
  },

  // return the field node that contains the Widget value - overridden by 'MappedTextBox' type subclasses etc.
  getValueNode: function() {
    return this.focusNode;
  },

  // return this field's form Node
  getForm: function() {
    return this.getValueNode().form;
  },

  // return this field's form Widget
  getFormWidget: function() {
    return dijit.byId(this.getForm().id);
  },

  // walk up the dom tree, calling ancestor widgets that support setStatus
  setAncestorStatus: function() { 
    if (!this._cforms_AncestorStatusNodes) { // lazy initialise the nodes list, once
      this._cforms_AncestorStatusNodes = [];
      var node = this.domNode;
      if (node) {
        node = node.parentNode;
        while(node){ // collect relevant ancestor widgets
          var widget = node.id ? dijit.byId(node.id) : null;
          if(widget && dojo.isFunction(widget.setStatus)){
            this._cforms_AncestorStatusNodes.push(widget);
          }
          node = node.parentNode;
        }
      }
    }
    
    var status = 0; // no status
    if (this.required) status = 1; // field required
    if (this.state === "Error") status = 2; // field in error
    
    // call setStatus on ancestor widgets
    dojo.forEach(
      this._cforms_AncestorStatusNodes, 
      function(widget){ widget.setStatus(this.id, status); }, 
      this
    );
  }
});

/**
 * Common functions and properties for CForms Text Fields that perform content filtering.
 * Supports the values of @whitespace in CForms
 *
 * Mix this into field widgets that you want to support the 'trim' and 'textcase' filters.
 *
 * NOTE: introduced in 2.1.12
 */
dojo.declare("cocoon.forms._FilterFieldMixin", [cocoon.forms._FieldMixin], {

  textcase: "",  // upper|lower|proper - replaces @uppercase, @lowercase, @propercase from Dojo
  trim: "trim",  // trim|trim-start|trim-end|preserve - replaces @trim="true" from Dojo
  
  // TextBox interface
  filter: function(val){
    // Apply specified filters to the value
    if(val === null || val === undefined){ 
      return ""; 
    } else if (typeof val !== "string") { 
      return val; 
    }
    if (this.trim) {
      if ("trim" === this.trim) {
        val = dojo.trim(val);
      } else if ("trim-start" === this.trim) {
        var start = 0;
        while (start !== val.length && /\s/.test(val.charAt(start))) start++;
        val = val.substring(start);
      } else if ("trim-end" === this.trim) {
        var end = val.length;
        while (end !== 0 && /\s/.test(val.charAt(end - 1))) end--;
        val = val.substring(0, end);
      }				
    }
    if (this.textcase) {
      if ("upper" === this.textcase) {
        val = val.toUpperCase();
      } else if ("lower" === this.textcase) {
        val = val.toLowerCase();
      } else if ("proper" ===  this.textcase) {
        val = val.replace(/[^\s]+/g, function(word){
          return word.substring(0,1).toUpperCase() + word.substring(1);
        });
      } 
    }
    return val;
  }
});

/**
 * Common functions and properties for CForms Fields that perform client-side validation.
 *
 * Mix this into field widgets to :
 *   Keep track of errors being sent by CForms
 *   Display the CForms error-state on load
 *
 * NOTE: introduced in 2.1.12
 */
dojo.declare("cocoon.forms._ErrorAwareFieldMixin", [cocoon.forms._FilterFieldMixin], {

	tooltipPosition: ["above", "below"],

  // Widget interface - look for a cforms error on load
  postMixInProperties: function() {
    this.inherited(arguments);
    // in case there was no cforms error, make sure we are not reading the default error supplied by dojo i18n
    if(this.srcNodeRef.getAttribute("invalidMessage")) this._cforms_Error = true;
    this._cforms_statusMarker = cocoon.forms.defaults.statusMark; // get the marker character for the template
  },
  
  // Widget interface - display cforms errors on startup
  startup: function(){
    if (this._cforms_Error) {
      this.state = "Error";
      this._setStateClass();
      dijit.setWaiState(this.focusNode, "invalid", "true");
    }
    this.inherited(arguments);
  },
  
  // ValidatingTextBox interface - If there is a cforms error and the field cannot do client-side validation, leave the error on
  isValid: function() {
    if (this._cforms_Error && (this.constraints === undefined || this.constraints === {})) return false;
    return this.inherited(arguments);
  }
  
});

/**
 * Common functions and properties for CForms Fields that have formatted values, 
 * that must be understood by dojo to be able to do client-side validation.
 *
 * Mix this into field widgets to allow client-side validation of formats provided by cforms
 *
 * NOTE: introduced in 2.1.12
 */
dojo.declare("cocoon.forms._FormattingFieldMixin", [cocoon.forms._ErrorAwareFieldMixin], {

  // Widget interface
  postMixInProperties: function() {
    // get the original formatted value sent by cforms
    this.formattedValue = this.srcNodeRef.getAttribute("value");    
    this.inherited(arguments);
  },

  // Widget interface
  postCreate: function() {
    this.inherited(arguments);
    // let Dojo parse Cocoon's formatted value - so it may validate user edits etc.
    this.setDisplayedValue(this.formattedValue, false); 
    
    /*
    
      TODO: Still got problems with formatting on 'non-latin' number systems (ar_SA and hi_IN etc.)
            Dojo only seems to work with the digits 0-9, these two locales are output by icu4j using 
            those languages' native digits, which are not the same chars as 0-9.
            eg. Arabic-Indic Digits (U+0660...U+0669) and Devangari Digits (U+0966...U+096F)
            How can I get icu4j to only output 0-9, without hacking the locale?
              Say I always changed hi_IN to en, the grouping pattern would be wrong 
    
    */
    var display = this.getDisplayedValue();
    if (this.formattedValue !== "" && this.formattedValue !== display) { // parsing did not work
      console.warn("Validation disabled on : " + this.id + " : Incompatible formats between Dojo (" + display + ") and Java (" + this.formattedValue + ")");
      this.isValid = function(){return true;}; // turn off validation
      this.parse = function(value){return value;}; // turn off parsing      
      this.focusNode.value = this.formattedValue; // set the field anyway
      dojo.addClass(this.domNode.parentNode, "brokenValidation"); // mark as broken (ugh!)
    }
  },
  
  // _FieldMixin interface - return the field node that contains the value - NumberField is mapped, so it is not the same as the original input field
  getValueNode: function() {
    return this.valueNode;
  },
  
  // _FieldMixin interface - copy the display value to the submitted field, before the form submits
  onSubmit: function() {
    this.getValueNode().value = this.getDisplayedValue();
  }



});

/**
 * Common functions and properties for CForms Text Fields that size using @size.
 *
 * Mix this into intput-type field widgets to support the 'size' attribute.
 *
 * NOTE: introduced in 2.1.12
 */
dojo.declare("cocoon.forms._SizedFieldMixin", null, {

  size: 15, // default width for field from HTML, measured by the # of characters that should fit in the box

  // Widget interface - size field by @size
  buildRendering: function() {
    this.inherited(arguments);
    if (!isNaN(this.size)) {
      dojo.style(this.domNode, "width", (this.size * 0.85) + "em" ); // fudge factor to size field to @size, a bit hacky but it seems to do the job
    }
  }
});

/**
 * Common functions and properties for CForms Text Fields that have hints.
 *
 * Mix this into fields that do not extend dojo.form.ValidatorTextBox, but require field hints
 *
 * NOTE: introduced in 2.1.12
 */
dojo.declare("cocoon.forms._FieldHintMixin", null, {

	promptMessage: "",
	tooltipPosition: ["above", "below"],
	tooltipTimeout: 10000, // time for the hint to auto-hide (default 10 secs)

	onFocus: function(){
		this.inherited(arguments);
		if (this.getValue() === "") this.displayMessage(this.promptMessage);
		setTimeout(dojo.hitch(this, function(){this.displayMessage("");}), this.tooltipTimeout);
	},

	onBlur: function(){
		this.inherited(arguments);
		this.displayMessage("");
	},

  // currently displayed message
  _message: "",

  displayMessage: function(message){
    if(this._message == message){ return; }
    this._message = message;
    dijit.hideTooltip(this.domNode);
    if(message){
      dijit.showTooltip(message, this.domNode, this.tooltipPosition);
    }
  }

});

/**
 * Common functions and properties for CForms TextArea fields.
 *
 * Mix this into textarea-type field widgets to be able to access their content
 *
 * NOTE: introduced in 2.1.12
 */
dojo.declare("cocoon.forms._TextAreaMixin", null, {

	postMixInProperties: function(){
		this.inherited(arguments);
		if(this.srcNodeRef){
			this.value = this.srcNodeRef.value;
		}
	},
  
  //filter the field, before the form submits
  onSubmit: function() {
    this.setValue(this.getValue().replace(/\r/g,"")); // filter out returns
  }

});
