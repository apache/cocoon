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
dojo.provide("cocoon.forms._GroupMixin");
dojo.require("cocoon.forms._StatusMixin");

/**
 * Common functions and properties for CForms Groups.
 *
 * Use this mixin for groups that may persist their state
 * and groups that want to show the status of their children
 * eg. a TabGroup that can show the same tab after a submit
 * and the child status in it's tabs
 *
 * NOTE: introduced in 2.1.12
 *
 * @version $Id$
 */
dojo.declare("cocoon.forms._GroupMixin", [cocoon.forms._StatusMixin], {

  stateId: "", // the ID of my state widget (set from an Attribute)
  //autoSize: true, // TODO: should tab pages be resized when activated?
  
  // Widget interface - find the optional state widget (to remember the tab state across submits)
	postCreate: function(){
    this.inherited(arguments);	       
    this._cforms_stateWidget = dojo.byId(this.stateId);
  },

  // Widget interface - make getDescendants() work
  buildRendering: function(){
    this.inherited(arguments);
    if(!this.containerNode) this.containerNode = this.domNode; 
  },
  
  // dijit.layout.StackContainer interface - keep the state widget in sync with the selection state
  selectChild: function(page){
    this.inherited(arguments);	       
		if (this.selectedChildWidget && this._cforms_stateWidget){
      this._cforms_stateWidget.value = dojo.indexOf(this.getChildren(), this.selectedChildWidget);
		}
	},

  // dijit.layout.StackContainer interface - provide the page container with a setStatus function
  _setupChild: function(page){
    this.inherited(arguments);	   // TODO: should this have been missing intentionally?     
    page.setStatus = dojo.hitch(page, this._setStatus); // default _setStatus provided by _StatusMixin
    page.getStatusWidget = dojo.hitch(page, this.getStatusWidget); // default _setStatus provided by _StatusMixin
    return page;
  }

});
