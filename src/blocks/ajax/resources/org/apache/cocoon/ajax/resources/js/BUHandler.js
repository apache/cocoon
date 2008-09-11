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

/**
 * General purpose AJAX infrastructure to handle a BU (browser update) response
 *
 * To add a new handler for a given request, create a new BUHandler and update its
 * handlers property.
 *
 * @version $Id$
 */

dojo.provide("cocoon.ajax.BUHandler");

dojo.require("cocoon.ajax.common");
dojo.require("cocoon.ajax.insertion");

dojo.declare("cocoon.ajax.BUHandler", null, {
	highlight: null, // Default highlight effect (none)
	
    processResponse: function(doc) {
		var base = doc.documentElement;

		var nodes = [];
		if (base.nodeName.toLowerCase() == "bu:document") {
			nodes = base.childNodes;
		} else {
			base = dojo.byId("browser-update", doc) || doc.getElementsByTagName("form")[0];
			if (base) {
				nodes = base.childNodes;
			} else {
				this.handleError("No response data found", doc);
			}
		}
		for (var i = 0; i < nodes.length; i++) {
			var node = nodes[i];
			if (node.nodeType === 1 /*ELEMENT_NODE*/) {
				var handler = node.nodeName.replace(/.*:/, "").toLowerCase();
				if (handler == "textarea") handler = node.getAttribute("name");
				var handlerFunc = this.handlers[handler];
				if (handlerFunc) {
					handlerFunc(node);
				} else {
					this.handleError("No handler found for element " + handler, doc);
				}
			}
		}
	},

	handleError: function(message, response) {
		if (confirm(message + "\nShow server response?")) {
			var w = window.open(undefined, "Cocoon Error", "location=no,resizable=yes,scrollbars=yes");
			if (w == undefined) {
				alert("You must allow popups from this server to display the response.");
			} else {
				var doc = w.document;
				if (response.responseText) {
					doc.open();
					doc.write(response.responseText);
					doc.close();
				} else if (response.childNodes) {
					dojo.require("dojox.data.dom"); // NB. Marked as experimental
					dojox.data.dom.replaceChildren(doc,response.childNodes);
				}
			}
		}
	},

	handlers: {
		replace: function(element) {
			var id = element.getAttribute("id");
			if (!id) {
				alert("no id found on update element");
				return;
			}
			// Get the first child element (the first child may be some text!)
			//var firstChild = dojo.dom.getFirstChildElement(element);
			
			var firstChild = element.firstChild;
			while(firstChild && firstChild.nodeType != 1 /*ELEMENT_NODE*/){
				firstChild = firstChild.nextSibling;
			}
			
			if (!firstChild && element.nodeName.toLowerCase() == "textarea") {
				dojo.require("dojox.data.dom"); // NB. Marked as experimental
				firstChild = dojox.data.dom.createDocument(element.value).documentElement;
			}
			var oldElement = document.getElementById(id);

			if (!oldElement) {
				alert("no element '" + id + "' in source document");
				return;
			}
			var newElement = cocoon.ajax.insertion.replace(oldElement, firstChild);

			if (typeof(cocoon.ajax.BUHandler.highlight) == "function") {
				cocoon.ajax.BUHandler.highlight(newElement);
			}

			/* update the label */

		    var nextChild = firstChild.nextSibling;
            while (nextChild && nextChild.nodeType != 1 /*ELEMENT_NODE*/){
                nextChild = nextChild.nextSibling;
            }
            if (nextChild) {
              cocoon.ajax.BUHandler.Helper.replaceLabel(nextChild);
            }
		}
	}
});

cocoon.ajax.BUHandler.Helper = function() { };

dojo.mixin(cocoon.ajax.BUHandler.Helper, {
    replaceLabel: function(element) {
          var oldElement = document.getElementById(element.getAttribute("id"));
          if (oldElement) {
             var newElement = cocoon.ajax.insertion.replace(oldElement, element);
             if (typeof(cocoon.ajax.BUHandler.highlight) == "function") {
                cocoon.ajax.BUHandler.highlight(newElement);
             }
          }
       }
});
