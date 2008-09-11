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
dojo.provide("cocoon.forms.Repeater");
dojo.require("dijit._Widget");
dojo.require("dojo.dnd.Manager");

/**
 * CForms Repeater Widget.
 * A versatile repeater, configured from the CForms model and template
 * A range of different behaviours are exhibited depending on the configuration
 * Includes drag and drop but this is only loaded if needed
 *
 * uses dojo.dnd.Container if you only need row highlighing
 * uses dojo.dnd.Selector if you need rows to be selectable
 * uses dojo.dnd.Target or dojo.dnd.Source if you need drag and drop behaviour
 *
 * The types that may be dragged onto a Repeater is ultimately enforced by the CForms model
 * along with what kind of copy versus move behaviour is allowed
 *
 * TODO: To support file fields in Repeaters (usecase: queueing an unknown quantity of uploads), 
 *       we need a way to optionally stop a full field submit when repeater and row-actions are called.
 *       Can we add a row without replacing the Repeater (???)
 *       Currently BU only does replace, but could do more (???)
 * TODO: Disallow drags of rows containing inValid fields, to another Repeater (???)
 *
 * NOTE: introduced in 2.1.12, replacing: CFormsRepeater and CFormsDragAndDropRepeater
 *
 * @version $Id$
 */
dojo.declare("cocoon.forms.Repeater", dijit._Widget, {

  // Properties that can be set from the widget's attributes
  orderable   : false,   // use orderable to turn internal dnd behaviour on and off
  handleClass : "",      // the user-specified css class marking row drag handles (if empty, use the whole row)
  select      : "",      // the name of the optional select control
  singular    : false,   // should it be possible to select/dnd more than one row at a time?
  horizontal  : false,   // should we try to render the rows horizontally?
  skipForm    : true,    // do not dnd on form fields
  acceptType  : [],      // the type(s) I accept - used to allow dragging in
  rowType     : "",      // the type(s) I contain - used to allow dragging out
  dndAction   : "",      // custom dnd handler in form model
  dndAllow    : ["any"], // control whether to allow copy and/or move based on meta key
  id          : "",      // the cforms id of the widget

  /** 
   * Widget interface
   * Initialise instance variables
   */ 
  constructor: function() {
    this.subscriptions = []; // keep track of my subscriptions for disposal
  },
  
  /** 
   * Widget interface
   * Prepare some configuration parameters
   */ 
  postMixInProperties: function() {
    this.inherited(arguments); // call my super class
    this.dndAction = this.dndAction ? this.dndAction : this.id; // the action to call on the server, default: this repeater's id
    if (this.dndAllow.length === 0) this.dndAllow = ["any"];
    this.ignoreMeta = this.dndAllow[0] !== "any";
    this.ignoreMetaTo = this.dndAllow[1] ? this.dndAllow[1] !== "any" : this.ignoreMeta;
    this.copyOrMove = this.dndAllow[0] === "copy";
    this.copyOrMoveTo = this.dndAllow[1] ? this.dndAllow[1] === "copy" : this.copyOrMove;
  },
  
  /** 
   * Widget interface
   * Choose what type of container to make depending on input parameters
   * Go through finding all dragable children, prepare them for dnd constructor
   */ 
  buildRendering: function(){   
    this.inherited(arguments); // call my super class
    if(!this.containerNode) this.containerNode = this.domNode; // make getDescendants() work for proper disposal of child widgets
    var row, widgetNode, rows = [], firstRow = dojo.byId(this.id + ".0");
    if (firstRow) { // there is row markup in this Repeater
      widgetNode = firstRow.parentNode; // get the parent node to add the dnd container to
      for (var i = 0; row = dojo.byId(this.id + "." + i); i++) rows.push(row); // build nodelist of RepeaterRows
    } else { // there are no Rows to find the parent Widget from
      var widgetNode = this.domNode.firstChild; // find the first child element of this domNode
      while(widgetNode && widgetNode.nodeType !== 1 /*ELEMENT_NODE*/) widgetNode = widgetNode.nextSibling;
      if (!widgetNode) widgetNode = this.domNode; // resort to using this domNode div
    }
    var params = { // parameters to construct our dnd container
      withHandles: this.handleClass !== "" && (this.orderable || this.rowType !== ""), // do we have select/drag handles?
      skipForm: this.skipForm, // should the user be able to drag having clicked in a form field?
      accept: this.acceptType.length > 0 ? this.acceptType : ["id_" + this.id], // the type(s) you can drop on me
      horizontal: this.horizontal, // should I be drawn horizontally (divs only)
      singular: this.singular, // should the user be able to drag more than one row at a time?
      isSource: true, // don't let this be overriden by the user
      creator: dojo.hitch(this, this.creator) // cforms needs a custom row creator
    };
    dojo.forEach(rows, function(node) { // decorate the RepeaterRows
      dojo.addClass(node, "dojoDndItem"); // used by the DND Constructor to find rows
      var type = this.rowType ? this.rowType : "id_" + this.id; // default: 'id_[widget ID]'
      node.setAttribute("dndType", type); // mark the rows with their type for the DND constructor
      dojo.addClass(node, type.split(/\s*,\s*/).join(' ')); // add type(s) as CSS classes
      if (params.withHandles) {
        dojo.forEach(dojo.query("." + this.handleClass, row), function(handle) { // mark the handles for the DND constructor
          dojo.addClass(handle, "dojoDndHandle"); // you could put more than one handle on a row
        });
      }
    }, this); // the context for dojo.forEach
    // decide which type of dojo.dnd Object we need to instantiate
    if (!this.orderable && this.select === "" && this.acceptType.length === 0 && this.rowType === "") {
      dojo.require("dojo.dnd.Container"); // lazy load
      this.container = new dojo.dnd.Container(widgetNode, params); // a container that just highlights rows as you mouse over them
    } else if (!this.orderable && this.select !== "" && this.acceptType.length === 0 && this.rowType === "") {
      dojo.require("dojo.dnd.Selector"); // lazy load
      this.container = new dojo.dnd.Selector(widgetNode, params); // a container that allows rows to be selected by clicking on them
    } else if (!this.orderable && this.acceptType.length > 0 && this.rowType === "") {
      dojo.require("dojo.dnd.Source"); // lazy load
      this.subscriptions.push(dojo.subscribe("/dnd/drop", this, "onDndDrop")); // we must be setup before construction (so we get called first)
      this.container = new dojo.dnd.Target(widgetNode, params); // a container you can drop rows onto
    } else {
      dojo.require("dojo.dnd.Source"); // lazy load
      this.subscriptions.push(dojo.subscribe("/dnd/drop", this, "onDndDrop")); // we must be setup before construction (so we get called first)
      this.container = new dojo.dnd.Source(widgetNode, params); // a container you can dnd rows to and from
    }
    if (this.container.checkAcceptance) { // monkey-patch dojo.dnd.Source to handle extra cforms features
      this.container._cforms_originalCheckAcceptance = this.container.checkAcceptance; // preserve the old function
      this.container.checkAcceptance = dojo.hitch(this, this.checkAcceptance); // give the container our checkAcceptance function
      this.container.copyState = dojo.hitch(this, this.copyState); // give the container our copyState function
    }
  },
  
  /** 
   * Widget interface
   * Wire up the rows to their select controls
   */ 
  startup: function() {
    this.inherited(arguments); // call my super class
    if (this.select !== "") { // set selection state on pre-selected Items
      var suffix = "." + this.select + ":input";
      dojo.forEach(this.container.getAllNodes(), function(row) {
        var widget = dijit.byId(row.id + suffix); // get the select widget
        if (widget) {
          if (widget.checked) this.selectRow(row); // set selection state of row if select is checked
          widget.onChange = dojo.hitch(this, function(newValue) {newValue ? this.selectRow(row) : this.deSelectRow(row); return true;});
        }
      }, this);
      // setup listeners for synchronising select events with the select fields
      this.connect(this.container, "_addItemClass", "checkRowSelect");
      this.connect(this.container, "_removeItemClass", "uncheckRowSelect");
    }
  },
  
  /** 
   * Widget interface
   * Clean up on unload
   */ 
  destroy: function() {
    this.inherited(arguments);
    dojo.forEach(this.connections, dojo.disconnect); // disconnect event listeners
    dojo.forEach(this.subscriptions, dojo.unsubscribe); // unsubscribe from event subscriptions 
    if (this.container) this.container.destroy(); // destroy my dnd container and it's child widgets
    this.container = null;
  },
  
  /** 
   * dojo.dnd.Source interface
   * Subscription Event Handler for '/dnd/drop'
   * Row(s) are being dropped on me, it has already been checked that this is allowed
   * Send request parameters to the server to say what has changed
   * NB. This must be called before the before the handler in the Container
   *     This event is called on every Repeater in the form, in the order they subscribe
   */ 
  onDndDrop: function(source, nodes, copy) {
    if(this.container.containerState !== "Over") return; // only the container being dropped on should run onDndDrop
    var beforeIdx = 0, fromIdxs = [], i = 0, params = {};
    if (this.container.targetAnchor) { // get the index of the row in the target to drop before
      beforeIdx = this.container.targetAnchor.id.substring(this.container.targetAnchor.id.lastIndexOf(".") +1);
      if (isNaN(beforeIdx)) beforeIdx = 0;
    }
    if (!this.container.before) beforeIdx++; // dojo has 'before' and 'after' states, cforms only uses 'before'
    for (var key in source.selection) { // get the indexes of all of the rows being dragged
      var idx = key.substring(key.lastIndexOf(".") +1);
      if (!isNaN(idx)) fromIdxs[i++] = idx; // there will not be a numerical index on dummy rows in empty drop targets
    }
    if (fromIdxs.length < 1) return; // no change
    params["dndTarget.id"] = this.id; // the ID of the target repeater (needed by fd:action handlers)
    params[this.id + ".before"] = beforeIdx; // items should be inserted before this index
    params[this.id + ".action"] = copy ? "copy" : "move"; // the command to copy or move the rows
    if (this.container === source) { // internal operation (legacy parameters)
      params[this.id + ".from"] = fromIdxs; // the index(es) of the row(s) to move or copy
    } else { // between two Repeaters (legacy parameters)
      params[this.id + ".sourceRepeaterId"] = dijit.getEnclosingWidget(nodes[0]).id; // the ID of the source repeater
      params[this.id + ".sourceRepeaterIndex"] = fromIdxs; // the index(es) of the row(s) to move or copy
    }
    cocoon.forms.submitForm(this.domNode, this.dndAction, params); // submit the form
    if (!copy) { // if this is a move, destroy the child widgets
      dojo.forEach(nodes, function(item){
        var list= dojo.query('[widgetId]', item);
		    dojo.forEach(list.map(dijit.byNode), function(widget){ widget.destroy(); });
      }, this);
    }
    
  },
  
  /**
   * dojo.dnd.Container interface
   * Custom Row Creator, called by dojo.dnd.Container._normalizedCreator
   * @param node String the innerHTML of the row being dragged (which is not great, as it shows the contents of help etc.)
   * @param hint boolean if true, then an avatar is being made
   * 
   * The problem being solved here is this :
   * The built-in creator deep-clones rows from the Source to the Target Repeater.
   * When the Target Repeater is replaced via BrowserUpdate, we have to recursively destroy it and it's child widgets.
   * This also destroys the widgets in the Source Row that these were cloned from.
   * So instead, as the Target is about to be replaced via XHR, just put a placeholder in there.
   */
  creator: function(node, hint) {
    var containerType = this.container.parent.tagName.toLowerCase();
    if (containerType === "tbody" || containerType === "table") {
      var n = dojo.dnd._createTrTd("&#160;");
      dojo.addClass(n, "dijitTreeExpandoLoading"); // animated loading icon
      return {node: n}; // put a placeholder row in the Target table
    } else { // the Target is a div etc.
      var n = dojo.dnd._createNode(dojo.dnd._defaultCreatorNodes[containerType])("&#160;");
      dojo.addClass(n, "dijitTreeExpandoLoading");
      return {node: n};// put an appropriately wrapped placeholder in the Target
    }
  },
  
  /**
   * dojo.dnd.Source interface
   * Check the acceptance of a row dragged over this container
   * This is monkey-patched into this.container to add support for @orderable
   * @orderable controls dnd behaviour within a single repeater
   */
  checkAcceptance: function(source, nodes) {
    if (this.container === source) {
      if (!this.orderable) return false; 
    } else {
      if (!this.container._cforms_originalCheckAcceptance(source, nodes)) return false;
    }
    return true;
  },
  
  /**
   * dojo.dnd.Source interface
   * Returns true, if we may copy items, false to move
   * This is monkey-patched into this.container to add support for @dndAllow
   * This is called every time the mouse moves while dragging
   */
  copyState: function(keyPressed) { // adding support for @dndAllow
    if (this.container.containerState === "Over") { // are we over this Source?        
      if (this.ignoreMeta) return this.copyOrMove;
    } else { // we may be dragging between containers
      if (this.ignoreMetaTo) return this.copyOrMoveTo;
    }
    return keyPressed;
  },
  
  /**
   * Event Handlers to keep the select Widget and row selection state in sync
   *
   * NB. There is no proper API for this in dojo.dnd
   * TODO: This code is not entirely Saneª
   */
  
  /**
    * onChange handler for row select control
    * Deselect the row
    */
  deSelectRow:function(row) {
    if (this.container.selection[row.id]) { 
      if (this.container.anchor === row) {
        this.container._removeAnchor();
      } else {
        dojo.removeClass(row, "dojoDndItemSelected");
      }
      delete this.container.selection[row.id];
    }
  },
  
  /**
    * Listener for container._removeItemClass 
    * Update select when row de-selected
    */
  uncheckRowSelect: function(row, type) {
    if (type === "Selected" || type === "Anchor") { 
      var widget = dijit.byId(row.id + "." + this.select + ":input");
      if (widget && widget.checked === true) {
        widget.setValue(false); 
      }
    }
  },

  /**
    * onChange handler for row select control
    * Select the row
    */
  selectRow: function(row) {
    if (!this.container.selection[row.id]) { 
      if (!this.container.anchor){
        this.container.anchor = row;
        dojo.removeClass(row, "dojoDndItemSelected");
        dojo.addClass(row, "dojoDndItemAnchor");
      } else if (this.container.anchor !== row){
        dojo.removeClass(row, "dojoDndItemAnchor");
        dojo.addClass(row, "dojoDndItemSelected");
      }
      this.container.selection[row.id] = 1;
    }
  },
  
  /**
    * Listener for container._addItemClass 
    * Update select when row selected
    */
  checkRowSelect: function(row, type) { 
    if (type === "Selected" || type === "Anchor") {
      // find the row's select
      var widget = dijit.byId(row.id + "." + this.select + ":input");
      if (widget && widget.checked === false) {
        widget.setValue(true); 
       }
    }
  }

});

/**
 * CForms Repeater DND Avatar Widget.
 * Extend the construct function, so that the built-in row creator is used for making our Avatar
 * Not the custom creator we supplied.
 * Due to issues with the DND api, a custom creator cannot make a decent avatar out of a row containing widgets
 *
 * NOTE: introduced in 2.1.12
 */
dojo.declare("cocoon.forms.Avatar", dojo.dnd.Avatar, {
  construct: function() {
    var creator = this.manager.source.creator;
    this.manager.source.creator = null; // temporarily hide our creator, so the built-in one is used for Avatars
    this.inherited(arguments);
    this.manager.source.creator = creator;
  }
});

dojo.extend(dojo.dnd.Manager, {
  makeAvatar: function() {
    return new cocoon.forms.Avatar(this);
  }
});