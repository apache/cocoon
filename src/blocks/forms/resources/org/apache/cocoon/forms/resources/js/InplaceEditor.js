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
dojo.provide("cocoon.forms.InplaceEditor");

dojo.require("dijit.InlineEditBox");
dojo.require("cocoon.forms._FieldMixin");


/**
 * CForms InplaceEditor Widget.
 * A wrapper for an editor field that shows plain content until clicked on to edit
 *
 * NOTE: introduced in 2.1.12
 *
 * @version $Id$
 */
dojo.declare("cocoon.forms.InplaceEditor", [dijit.InlineEditBox, cocoon.forms._FieldMixin], {	
    
  //noValueIndicator: "edit this",

  required: false, // is this a required field
  valueNode: "",   // the id of the field holding my value
  editorId: "",    // the id of my editor
  autoSave: false, // changing default
    
  postMixInProperties: function() {
    this.inherited(arguments);
    if(this.editorParams.invalidMessage) this._cforms_Error = true;
    this._cforms_statusMarker = cocoon.forms.defaults.statusMark; // the error marker
  },
    
  // Widget interface
  postCreate: function() {
    this.inherited(arguments);
    // make a node for the status marker
    this.statusNode = dojo.doc.createElement('span');
    this.statusNode.innerHTML = this._cforms_statusMarker;
    dojo.addClass(this.statusNode, "dijitReset inPlaceValidation dijitValidationIconText");
    dojo.place(this.statusNode, this.domNode, "before");
  },

  // Widget interface
  startup: function(){
    if (this._cforms_Error) {
      this.state = "Error";
      dojo.addClass(this.getStatusWidget(), "dijitError cformsStatus" + this.required ? " cformsRequired" : "");
      dijit.setWaiState(this.domNode, "invalid", "true");
    } else if (this.required) {
      dojo.addClass(this.getStatusWidget(), "cformsStatus cformsRequired");
    } else {
      dojo.addClass(this.getStatusWidget(), "cformsStatus");
    }
    this.inherited(arguments);
  },
    
  // return the field node that contains the Widget value - overridden by 'MappedTextBox' type subclasses
  getValueNode: function() {
    return dojo.byId(this.valueNode);
  },
    
  //copy the display value to the submitted field, before the form submits
  onSubmit: function() {
    this.getValueNode().value = this.getValue();
  },

  // where to place classes to mark status
  getStatusWidget: function() { 
    return this.domNode.parentNode;
  },

  // InlineEditBox interface - copy my classes to the phantom node (required etc.)
  _edit: function(){
    // hide the statusNode
    dojo.style(this.statusNode, "display", "none");
    // copy css to new view
    var classes = this.domNode.className;
    // let the editor switch domNode
    this.inherited(arguments);
    dojo.addClass(this.domNode, classes);
  },
  
  // InlineEditBox interface - show my child editor's status on me
  _showText: function() {
    this.state = dijit.byId(this.editorId).state; // undefined
    this.inherited(arguments);
    // show the statusNode
    dojo.style(this.statusNode, "display", "inline-block");
    // set my state classes
    if (this.state === "Error") {
      dojo.addClass(this.getStatusWidget(), "dijitError");
    } else {
      dojo.removeClass(this.getStatusWidget(), "dijitError");
    }
  }
});
