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
dojo.provide("cocoon.forms.TabGroup");

dojo.require("dijit.layout.TabContainer");
dojo.require("cocoon.forms.common");
dojo.require("cocoon.forms._GroupMixin");

/**
 * CForms TabGroup Widget.
 * A set of panes that are switched between using tabs        
 *
 * If this container has no absolute height set via css,
 * it attempts to size itself to the vertical height of the first tab pane
 *
 * eg: <fi:group>
 *         <fi:styling type="tabs" . . . />
 *
 * NOTE: introduced in 2.1.12
 *
 * @version $Id$
 */
dojo.declare("cocoon.forms.TabGroup", [dijit.layout.TabContainer, cocoon.forms._GroupMixin], {	

  // the controller to use.
	_controllerWidget: "cocoon.forms._TabController", 
	
	// TODO: bottom border missing in FireFox
	
	// if there was no absolute height in the style, size the TabContainer by the foremost Tab
  layout: function(){ 
    this.inherited(arguments);
    if (this.domNode.clientHeight == 0 && this.selectedChildWidget) {
      var high = 0, border = /* eek! */3;
      if (this.tabPosition == "top" || this.tabPosition == "bottom") {
        high = this.selectedChildWidget.domNode.scrollHeight + this.tablist.domNode.clientHeight;
      } else {
        high = Math.max(this.selectedChildWidget.domNode.scrollHeight, this.tablist.domNode.clientHeight);
      }
      if (high > 0) this.domNode.style.height = high + border + "px";
    }
  },
  
  getStatusWidget: function() {
    return this.controlButton ? this.controlButton : this;
  }
  
});

/**
 * CForms TabController Widget.
 * A container of tab-buttons, one for each page
 *
 * NOTE: introduced in 2.1.12
 */
dojo.declare("cocoon.forms._TabController", [dijit.layout.TabController], {
  
  //	The name of the tab widget to create to correspond to each page
	buttonWidget: "cocoon.forms._TabButton", 	

  // execute user script, where 'this' is the page widget
	onSelectChild: function(page) {
    this.inherited(arguments);
    var script = page.domNode.getAttribute("onShow");
    if (script) dojo.hitch(page, function() { eval(script); })(); // TODO: find a better way
	}
	
	/* TODO: see if you can get this working .......
	    trying to allow resize on each tab select (like the old one, except I did not like that behaviour ..... )
	onSelectChild: function(page) {
    this.inherited(arguments);
    var container = dijit.byId(this.containerId);
      console.debug("_TabController.onSelectChild " + container.domNode.clientHeight);
    
    if (container.domNode.clientHeight == 0 && this._currentChild) {
      var high = 0, border = 3;
      if (this.tabPosition == "top" || this.tabPosition == "bottom") {
        high = this._currentChild.domNode.scrollHeight + this.domNode.clientHeight;
      } else {
        high = Math.max(this._currentChild.domNode.scrollHeight, this.domNode.clientHeight);
      }
      if (high > 0) this.container.domNode.style.height = high + border + "px";
    }
  }  */

});

/**
 * CForms _TabButton Widget.
 * A dijit.layout._TabButton that shows a status mark
 *
 * NOTE: introduced in 2.1.12
 */
dojo.declare("cocoon.forms._TabButton", [dijit.layout._TabButton], {

	templateString:"<div waiRole=\"presentation\" dojoAttachEvent='onclick:onClick,onmouseenter:_onMouse,onmouseleave:_onMouse'>\n    <div waiRole=\"presentation\" class='dijitTabInnerDiv' dojoAttachPoint='innerDiv'>\n        <div waiRole=\"presentation\" class='dijitTabContent' dojoAttachPoint='tabContent,statusNode'>\n          <span class=\"dijitReset dijitValidationIconText\">${_cforms_statusMarker}</span><span dojoAttachPoint='containerNode,focusNode' class='tabLabel'>${!label}</span>\n          <span dojoAttachPoint='closeButtonNode' class='closeImage' dojoAttachEvent='onmouseenter:_onMouse, onmouseleave:_onMouse, onclick:onClickCloseButton' stateModifier='CloseButton'>\n              <span dojoAttachPoint='closeText' class='closeText'>x</span>\n          </span>\n        </div>\n    </div>\n</div>\n",

  // get my status mark
  postMixInProperties: function() {
    this._cforms_statusMarker = cocoon.forms.defaults.statusMark;
    this.inherited(arguments); 
  }

});