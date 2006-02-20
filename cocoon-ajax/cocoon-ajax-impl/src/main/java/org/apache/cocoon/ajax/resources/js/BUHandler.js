/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
dojo.provide("cocoon.ajax.BUHandler");

cocoon.ajax.BUHandler = function() { };

/**
 * Imports an element into a document, taking care of using the correct implementation
 * so that the browser interprets it as displayable XML.
 *
 * Any <script> in the imported node are collected and for post-import evaluation
 * FIXME: only inline script are considered, but not <script src="">
 *
 * @return { element: <em>imported_element</em>, scripts: <em>array_of_script_text</em> }
 */
cocoon.ajax.BUHandler.importNode = function(node, targetDoc) {
    if(node.xml) {
        // IE
        var div = targetDoc.createElement("DIV");
        var text = node.xml;
        
        // Code below heavily inspired by the Prototype library
        var scriptExpr = "(?:<script.*?>)((\n|\r|.)*?)(?:<\/script>)";
        
        // Update screen with removed scripts
        div.innerHTML = text.replace(new RegExp(scriptExpr, 'img'), '');;
        
        var matchAll = new RegExp(scriptExpr, 'img');
        var matchOne = new RegExp(scriptExpr, 'im');
        var allMatches = text.match(matchAll);
        var scripts = new Array();
        for (var i = 0; i < allMatches.length; i++) {
            scripts.push(allMatches[i].match(matchOne)[1]);
        }
        return { element: dojo.dom.getFirstChildElement(div), scripts: scripts };
        
    } else {
        var scripts = new Array();
        var element = cocoon.ajax.BUHandler._DOMimportNode(node, targetDoc, scripts);
        return { element: element, scripts: scripts }
    }
}
	
/**
 * DOM implementation of importNode, recursively creating nodes.
 * Scripts are collected in the "scripts" parameter
 */
cocoon.ajax.BUHandler._DOMimportNode = function(node, targetDoc, scripts) {
    switch(node.nodeType) {
        case dojo.dom.ELEMENT_NODE:
            if (node.nodeName.toLowerCase() == "script") {
                // Collect scripts
                scripts.push(node.firstChild && node.firstChild.nodeValue);
                return;
            }
            var element = targetDoc.createElement(node.nodeName);
            //var element = targetDoc.createElementNS(node.namespaceURI, node.nodeName);
            var attrs = node.attributes;
            for (var i = 0; i < attrs.length; i++) {
                var attr = attrs[i];
                element.setAttribute(attr.nodeName, attr.nodeValue);
                //element.setAttributeNS(attr.namespaceURI, attr.nodeName, attr.nodeValue);
            }
            var children = node.childNodes;
            for (var j = 0; j < children.length; j++) {
                var imported = cocoon.ajax.BUHandler._DOMimportNode(children[j], targetDoc, scripts);
                if (imported) element.appendChild(imported);
            }
            return element;
        break;
        
        case dojo.dom.TEXT_NODE:
            return targetDoc.createTextNode(node.nodeValue);
        break;
        
        case dojo.dom.CDATA_SECTION_NODE:
            return targetDoc.createTextNode(node.nodeValue);
        break;
    }
}

cocoon.ajax.BUHandler._destroyDojoWidgets = function(element) {
    var widget = dojo.widget.byNode(element);
    if (widget) {
        // Dojo will destroy all its children
        widget.destroy(true, true);
    } else {
        // Recurse until we eventually find a widget
        var children = element.childNodes;
        for (var i = 0; i < children.length; i++) {
            var child = children[i];
            if (child.nodeType == dojo.dom.ELEMENT_NODE) {
                cocoon.ajax.BUHandler._destroyDojoWidgets(child);
            }
        }
    }
}

cocoon.ajax.BUHandler._parseDojoWidgets = function(element) {
    // Find a parent widget (if any) so that Dojo can maintain its widget tree
    var parentWidget = cocoon.ajax.BUHandler._findParentWidget(element);
	var parser = new dojo.xml.Parse();

	// FIXME: parser.parseElement() parses the _children_ of an element, whereas we want here
	// the element itself to be parsed. Parsing its parent is not an option as we don't want
	// to parse the siblings. So place it in a temporary div that we'll trash afterwards.
	var div = document.createElement("DIV");
	element.parentNode.replaceChild(div, element);
	div.appendChild(element);
	var frag = parser.parseElement(div, null, true);
	dojo.widget.getParser().createComponents(frag, parentWidget);
	div.parentNode.replaceChild(element, div);
	parentWidget && parentWidget.onResized();
}

cocoon.ajax.BUHandler._findParentWidget = function(element) {
    var parent = element.parentNode;
    var widget;
    while (parent && !widget) {
        var widget = dojo.widget.byNode(parent);
        if (widget) {
            return widget;
        }
        parent = parent.parentNode;
    }
}

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
	        var w = window.open(undefined, "Cocoon Error", "location=no");
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

	        var imported = cocoon.ajax.BUHandler.importNode(firstChild, document);
	        var newElement = imported.element;
	        // Don't update if we don't have a valid new element.
	        if (newElement) {
		        // Warn: it's replace(new, old)!!
		        oldElement.parentNode.replaceChild(newElement, oldElement);
		        // Ensure the new node has the correct id
		        newElement.setAttribute("id", id);
		        
	            // Destroy Dojo widgets associated with the old element
	            cocoon.ajax.BUHandler._destroyDojoWidgets(oldElement);

		        // Evaluate scripts
		        for (var i = 0; i < imported.scripts.length; i++) {
		            eval(imported.scripts[i]);
		        }
		        
		        // Parse dojo widgets for the new element
		        cocoon.ajax.BUHandler._parseDojoWidgets(newElement);
		        
		        // Reload it as the widgetization may have changed it
		        newElement = document.getElementById(id);
		        
		        if (this.highlight) {
		           this.highlight(newElement);
		        }
		    }
		}
    }
});