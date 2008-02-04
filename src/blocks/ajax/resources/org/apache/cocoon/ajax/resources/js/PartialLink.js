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
dojo.provide("cocoon.ajax.PartialLink");

dojo.require("dojo.widget.DomWidget");
dojo.require("cocoon.ajax.common");

/**
 * Dojo widget for links that partially update the page.
 *
 * @version $Id$
 */

dojo.widget.defineWidget(
    "cocoon.ajax.PartialLink",
    dojo.widget.DomWidget,
 {
	// Properties
	href: "",
	target: "",

	// Widget definition
	ns: "forms",
	widgetType: "PartialLink",
    isContainer: false,
    preventClobber: true, // don't clobber our node

    fillInTemplate: function(args, frag) {

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
