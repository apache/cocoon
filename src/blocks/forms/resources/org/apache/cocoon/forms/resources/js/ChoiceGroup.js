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
dojo.provide("cocoon.forms.ChoiceGroup");

dojo.require("dijit.Menu");
dojo.require("dijit.form.Button");
dojo.require("dijit.layout.StackContainer");
dojo.require("cocoon.forms._GroupMixin");

/**
 * CForms ChoiceGroup Widget.
 * A set of panes that are switched between using a drop-down menu        
 *
 * eg: <fi:group>
 *         <fi:styling type="choice" . . . />
 *
 * NOTE: introduced in 2.1.12
 *
 * @version $Id$
 */
 
 /*
 
    TODO: Fix appalling behaviour of window re-size!!!
    TODO: Status not showing
          Not sure _setStatus being called on this
    
 */
 
 
 
dojo.declare("cocoon.forms.ChoiceGroup", [dijit.layout.StackContainer, dijit._Templated, cocoon.forms._GroupMixin], {	

	_controllerWidget: "cocoon.forms.ChoiceController", // the controller to use.

  // template for menu and content, ripped from TitlePane
  templateString: "<div class=\"dijitTitlePane forms-choice-group\">\n\t<div tabindex=\"0\" waiRole=\"button\" class=\"dijitTitlePaneTitle\" dojoAttachPoint=\"focusNode\">\n\t\t<div dojoAttachPoint=\"controllerNode\" class=\"dijitTitlePaneTextNode\"></div>\n\t</div>\n\t<div class=\"dijitTitlePaneContentOuter\">\n\t\t<div class=\"dijitReset\">\n\t\t\t<div class=\"dijitTitlePaneContentInner\" dojoAttachPoint=\"containerNode\" waiRole=\"region\" tabindex=\"-1\">\n\t\t\t</div>\n\t\t</div>\n\t</div>\n</div>\n", 

  // create the controller
	postCreate: function(){	
		this.inherited(arguments);
		// create the choice list that will have a menu item for each panel
		var Controller = dojo.getObject(this._controllerWidget);
		this.controller = new Controller({
			id: this.id + "_controller",
			doLayout: this.doLayout,
			containerId: this.id
		}, this.controllerNode);
	},
	
  // wire up the controller and its menu items
	startup: function(){
		if(this._started){ return; }
		this.controller.startup();
		this.inherited(arguments);
  },

	destroy: function(){
		if(this.controller){
			this.controller.destroy();
		}
		this.inherited(arguments);
	},
	  
	// supply the relevant MenuItem to draw status on
  getStatusWidget: function() {
    return this.controlButton ? this.controlButton : this;
  }

});

/**
 * CForms ChoiceController Widget.
 * A drop-down menu for choosing between panes
 *
 * NOTE: introduced in 2.1.12
 */
dojo.declare("cocoon.forms.ChoiceController", [dijit.layout.StackController], {

  // Add the DropDownButton and it's Menu to the domNode
  buildRendering: function(){		
    this.inherited(arguments); // call my super class
    this.menu = new dijit.Menu({ });
    this.menu.domNode.style.display="none";
    this.button = new dijit.form.DropDownButton({dropDown: this.menu});
    this.domNode.appendChild(this.button.domNode);
  },

  // Add a MenuItem to the Menu for a page
  onAddChild: function(/*Widget*/ page, /*Integer?*/ insertIndex){
    var menuItem = new cocoon.forms.StatusMenuItem({
        label: page.title,
        onClick: dojo.hitch(this, "onButtonClick", page)
    });
    this.menu.addChild(menuItem);
    dijit.setWaiRole((menuItem.focusNode || menuItem.domNode), "tab"); // ??
    this.pane2button[page] = menuItem;
		page.controlButton = menuItem;
    if(!this._currentChild){
      this._currentChild = page;
    }
  },

  // Set the DropDownButton's label to the selected pane title, execute the pane's optional onShow script
  onSelectChild: function(/*Widget*/ page){
    if(!page){ return; }
    this.button.setLabel(page.title);
    var container = dijit.byId(this.containerId);
    dijit.setWaiState(container.containerNode || container.domNode, "labelledby", this.button.id);    
    var script = page.domNode.getAttribute("onShow");
    if (script) dojo.hitch(page, function() { eval(script); })(); 
  },
  
  // clean up on unload
  destroy: function() {
    if (this.button) this.button.destroy();
    this.inherited(arguments);
  }

});

/**
 * CForms StatusMenuItem Widget.
 * A dijit.MenuItem that shows a status mark
 *
 * NOTE: introduced in 2.1.12
 */
dojo.declare("cocoon.forms.StatusMenuItem", [dijit.MenuItem], {
	
	// copy of the dijit.MenuItem template, adding statusMarker
	templateString:
		 '<tr class="dijitReset dijitMenuItem" '
		+'dojoAttachEvent="onmouseenter:_onHover,onmouseleave:_onUnhover,ondijitclick:_onClick">'
		+'<td class="dijitReset"><div class="dijitMenuItemIcon ${iconClass}" dojoAttachPoint="iconNode"></div></td>'
		+'<td tabIndex="-1" class="dijitReset dijitMenuItemLabel" dojoAttachPoint="containerNode,focusNode" waiRole="menuitem"></td>'
		+'<td dojoAttachPoint="statusNode"><div class="dijitReset dijitValidationIconText">${_cforms_statusMarker}</div></td>'
		+'<td class="dijitReset" dojoAttachPoint="arrowCell">'
			+'<div class="dijitMenuExpand" dojoAttachPoint="expand" style="display:none">'
			+'<span class="dijitInline dijitArrowNode dijitMenuExpandInner">+</span>'
			+'</div>'
		+'</td>'
		+'</tr>',
	
	// get my marker
  postMixInProperties: function() {
    this._cforms_statusMarker = cocoon.forms.defaults.statusMark;
    this.inherited(arguments); 
  }

});

