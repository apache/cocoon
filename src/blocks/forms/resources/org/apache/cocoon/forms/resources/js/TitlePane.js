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
dojo.provide("cocoon.forms.TitlePane");
dojo.require("dijit.TitlePane");
dojo.require("cocoon.forms.common");
dojo.require("cocoon.forms._StatusMixin");

/**
 * CForms TitlePane Widget.
 * A pane with a title on top, that can _optionally_ be opened or collapsed.
 *  Collapsability may be controlled by adding @collapsable="true|false"
 *  Because the content may be hidden, we show child widget status in the title
 *
 * eg: <fi:group>
 *         <fi:label>Pane Title</fi:label>
 *
 * NOTE: introduced in 2.1.12
 *
 * @version $Id$
 */
 
 /*
 
    TODO: don't like the status marker right-aligned, put it next to the title
 
 */
 
dojo.declare("cocoon.forms.TitlePane", [dijit.TitlePane, cocoon.forms._StatusMixin], {

  collapsable: false, // default to not allow collapsable (principle of least surprise?)

  // adding a place for the status marker to dijit.TitlePane's template
	templateString:"<div class=\"${baseClass}\">\n\t<div dojoAttachEvent=\"onclick:toggle,onkeypress: _onTitleKey,onfocus:_handleFocus,onblur:_handleFocus\" tabindex=\"0\"\n\t\t\twaiRole=\"button\" class=\"dijitTitlePaneTitle\" dojoAttachPoint=\"titleBarNode,focusNode,statusNode\">\n\t\t<div class=\"dijitReset dijitValidationIconText\">${_cforms_statusMarker}</div><div dojoAttachPoint=\"arrowNode\" class=\"dijitInline dijitArrowNode\"><span dojoAttachPoint=\"arrowNodeInner\" class=\"dijitArrowNodeInner\"></span></div>\n\t\t<div dojoAttachPoint=\"titleNode\" class=\"dijitTitlePaneTextNode\"></div></div>\n\t<div class=\"dijitTitlePaneContentOuter\" dojoAttachPoint=\"hideNode\">\n\t\t<div class=\"dijitReset\" dojoAttachPoint=\"wipeNode\">\n\t\t\t<div class=\"dijitTitlePaneContentInner\" dojoAttachPoint=\"containerNode\" waiRole=\"region\" tabindex=\"-1\">\n\t\t\t\t<!-- nested divs because wipeIn()/wipeOut() doesn't work right on node w/padding etc.  Put padding on inner div. -->\n\t\t\t</div>\n\t\t</div>\n\t</div>\n</div>\n",


  postMixInProperties: function() {
    // TODO: why does this not call super class?
    var script = this.srcNodeRef.getAttribute("onShow"); // optional user script
    if (script) this.onShow = dojo.hitch(this, function() { eval(script); }); // run the script in the context of this pane
    this._cforms_statusMarker = cocoon.forms.defaults.statusMark; // the error marker
    this.setStatus = this._setStatus; // make myself the status handler
  },

  // hide collapse control if it is unwanted
  postCreate: function() {
    this.inherited(arguments);
    if (!this.collapsable) this.arrowNode.style.visibility = "hidden"; 
  },
  
  // only switch between opened and closed state if allowed
	toggle: function(){ 
		if (this.collapsable) this.inherited(arguments);
	},

  // collapsability may be set programatically (woo!)
  toggleCollapsable: function () { 
    this.collapsable = !this.collapsable;
    this.arrowNode.style.visibility = this.collapsable ? "visible" : "hidden";
  }
	
});

