/*
 * Copyright 1999-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
dojo.provide("cocoon.ajax.PartialLink");

dojo.require("dojo.widget.DomWidget");
dojo.require("dojo.io");
dojo.require("cocoon.ajax.common");

/**
 * Dojo widget for links that partially update the page.
 *
 * @version $Id$
 */

cocoon.ajax.PartialLink = function() {
	dojo.widget.DomWidget.call(this);
};

dojo.inherits(cocoon.ajax.PartialLink, dojo.widget.DomWidget);

dojo.lang.extend(cocoon.ajax.PartialLink, {
	// Properties
	href: "",
	target: "",
	
	// Widget definition
	widgetType: "PartialLink",
    isContainer: true,
    
    buildRendering: function(args, parserFragment, parentWidget) {

        // Magical statement to get the dom node, stolen in DomWidget
	    this.domNode = parserFragment["dojo:"+this.widgetType.toLowerCase()].nodeRef;

	    if (this.target.indexOf("#") < 0) {
	        dojo.debug("PartialLink: wrong value for 'target' attribute: " + this.target);
	        return;
	    }
	    
	    dojo.event.connect(this.domNode, "onclick", this, "onClick");
    },
    
    onClick: function(event) {
        event.preventDefault();
        var _this = this;
        cocoon.ajax.update(_this.href, _this.target);
    }
});

dojo.widget.tags.addParseTreeHandler("dojo:PartialLink");
// Register this module as a widget package
dojo.widget.manager.registerWidgetPackage("cocoon.ajax");

