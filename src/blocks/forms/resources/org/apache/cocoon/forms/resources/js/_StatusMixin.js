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
dojo.provide("cocoon.forms._StatusMixin");

/**
 * Common functions and properties for CForms containers.
 *
 * Adds support to show the status of their child widgets
 * Useful for Groups that may hide their children (TabGroup, NavGroup, TitlePane etc.)
 *
 * NOTE: introduced in 2.1.12
 *
 * @version $Id$
 */
dojo.declare("cocoon.forms._StatusMixin", null, {

  /** 
   *   Support for displaying descendant-status on parent containers (tabs/buttons/titles etc.)
   *   A proxy for the setStatus Handler, called by each child widget as they build or are changed
   *
   *   NB. This function will generally be acquired via inheritance
   *   However not all widgets that inherit this, actually want to implement it themselves
   *   eg. TabGroup wants this, to add to it's ContentPane(s), not itself
   *   
   *   Hence the underscrore in the name .... child fields come looking for the setStatus function.
   *   Classes that want to implement setStatus, themselves, or in their children need to manually set it.
   *
   *   eg.
   *     this.setStatus = this._setStatus // TitlePane wants to implement this itself
   *   or
   *     page.setStatus = dojo.hitch(page, this._setStatus); // TabGroup wants it's pages to implement this
   */
  _setStatus: function(id, status) {
    if (!id || isNaN(status) || status < 0) return; // invalid params
    var widget = this.getStatusWidget(); // get the Widget to display status on
    if (!widget) return;
    
    // lazy initialise (being super-paranoid with the names of stuff we add to other widgets)
    if (!this._cforms_StatusRecord) {
      this._cforms_StatusRecord = {
        children: {},   // the ids of my children with status
        current: 0,     // my amalgamisted status (0: no status, 1: required field, 2: error field)
        counts: [0,0,0] // the number of children with each status	 
      };
    }

    // we keep track of the status of each of my child widgets
    var rec = this._cforms_StatusRecord; // shortcut
    var oldStatus = rec.children[id] || 0;
    if (oldStatus !== status) {
      // increment the count of fields with this status level, start with 1 if it has not been set before
      rec.counts[status] = isNaN(rec.counts[status]) ? 1 : rec.counts[status] +1;
      // decrement the count of fields with the previous status level for this field, start with 0 if it has not been set before
      rec.counts[oldStatus] = isNaN(rec.counts[oldStatus]) ? 0 : rec.counts[oldStatus] -1;
      // keep the current status level of this child
      rec.children[id] = status;
    }
    
    // find the highest status level of my children
    var newStatus = rec.counts.length -1;
    for (; newStatus > -1; newStatus--) { // iterate backwards through the array
      if (rec.counts[newStatus] && rec.counts[newStatus] > 0) break;
    }
    
    // set the new status on myself
    if (newStatus !== rec.current) { // if it has changed
      rec.current = newStatus; // remember the new state
      var statusNode = widget.statusNode || widget.focusNode || widget.domNode || widget; // the node that shows the status
      // update it's css class to show the new status
      // Get original (non state related, non baseClass related) class specified in template
      if(!("staticClass" in this)){
        this.staticClass = statusNode.className;
      }

      var classes = [ "cformsStatus" ];      
      // choose the CSS classes to add
      if (newStatus > 0) classes.push("cformsRequired");
      if (newStatus > 1) classes.push("cformsError");
  
      // set the CSS on the statusNode
      statusNode.className = this.staticClass + " " + classes.join(" ");
    }
  },
  
  /** 
   *   Supply the widget (descendant or self) that contains the 'statusNode' (via a template dojoAttachPoint)
   *   where the status is to be displayed. 
   *   NB. do not choose a node that is already being used by dojo for storing it's own dynamic
   *   status css classes (hover, mouseover, etc.) or the new ones will be lost.
   *   
   *   It is broken out like this so that NavGroup etc. may supply it's buttons instead of itself 
   *
   */
  getStatusWidget: function() {
    return this;
  }
  
});
