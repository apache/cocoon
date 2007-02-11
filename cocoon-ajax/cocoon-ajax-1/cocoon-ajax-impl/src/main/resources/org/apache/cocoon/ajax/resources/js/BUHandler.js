/*
 * Copyright 2006 The Apache Software Foundation.
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

/**
 * General purpose AJAX infrastructure to handle a BU ("browser update") response
 *
 * To add a new handler for a given request, create a new BUHandler and update its
 * handlers property.
 *
 * @version $Id$
 */

dojo.require("dojo.dom");
dojo.require("dojo.html");
dojo.require("cocoon.ajax.insertion");
dojo.provide("cocoon.ajax.BUHandler");

cocoon.ajax.BUHandler = function() { };

cocoon.ajax.BUHandler.fade = function(node) {
    dojo.require("dojo.fx.*");
    dojo.fx.highlight(element, dojo.graphics.color.hex2rgb("#ffc"), 700, 300);
}

dojo.lang.extend(cocoon.ajax.BUHandler, {
    // Default highlight effect
    highlight: null,
    
    processResponse: function(doc, request) {
	    var nodes = doc.documentElement.childNodes;
	    for (var i = 0; i < nodes.length; i++) {
	        var node = nodes[i];
	        if (node.nodeType == dojo.dom.ELEMENT_NODE) {
	            var handler;
	            if (node.localName) {
	                handler = node.localName;
	            } else {
	                // No DOM2 support (IE6)
	                handler = node.nodeName.replace(/.*:/, "");
	            }
	            var handlerFunc = this.handlers[handler];
	            if (handlerFunc) {
	                handlerFunc(node);
	            } else {
	                this.handleError("No handler found for element " + handler, request);
	            }
	        }
	    }
	},
	
	handleError: function(message, request) {
	    if (confirm(message + "\nShow server response?")) {
	        var w = window.open(undefined, "Cocoon Error", "location=no,resizable=yes,scrollbars=yes");
	        if (w == undefined) {
	            alert("You must allow popups from this server to display the response.");
	        } else {
		        var doc = w.document;
		        doc.open();
		        doc.write(request.responseText);
		        doc.close();
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
	        var firstChild = dojo.dom.getFirstChildElement(element);
	    
	        var oldElement = document.getElementById(id);
	        
	        if (!oldElement) {
	            alert("no element '" + id + "' in source document");
	            return;
	        }
	        
	        var newElement = cocoon.ajax.insertion.replace(oldElement, firstChild);
	        
	        if (this.highlight) {
	           this.highlight(newElement);
	        }
		}
    }
});