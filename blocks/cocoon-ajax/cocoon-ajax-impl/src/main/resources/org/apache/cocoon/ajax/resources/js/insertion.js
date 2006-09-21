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
 * Implementation of Prototype's Insertion class with Dojo
 *
 * @version $Id$
 */

dojo.provide("cocoon.ajax.insertion");
dojo.require("dojo.dom");

cocoon.ajax.insertion = {};

dojo.lang.mixin(cocoon.ajax.insertion, {

	/**
	 * Inserts before the reference node
	 */
    before: function (refElt, content) {
        return cocoon.ajax.insertionHelper.insert(refElt, content, function(refElt, newElt) {
            refElt.parentNode.insertBefore(newElt, refElt);
        });
    },

	/**
	 * Inserts after the reference node
	 */
    after: function (refElt, content) {
        return cocoon.ajax.insertionHelper.insert(refElt, content, function(refElt, newElt) {
            // There's no node.insertAfter...
            if (refElt.nextSibling) {
                refElt.parentNode.insertBefore(newElt, refElt.nextSibling);
            } else {
                refElt.parentNode.appendChild(newElt);
            }
        });
    },

    /**
     * Inserts at the top of the reference node children (i.e. as the first child)
     */
    top: function (refElt, content) {
        return cocoon.ajax.insertionHelper.insert(refElt, content, function(refElt, newElt) {
            if (refElt.firstChild) {
                refElt.insertBefore(newElt, refElt.firstChild);
            } else {
                refElt.appendChild(newElt);
            }
        })
    },

    /**
     * Inserts at the bottom of the reference node children (i.e. as the last child)
     */
    bottom: function (refElt, content) {
        return cocoon.ajax.insertionHelper.insert(refElt, content, function(refElt, newElt) {
            refElt.appendChild(newElt);
        })
    },

    /**
     * Inserts as the contents of the reference node (i.e. replaces all children)
     */
    inside: function (refElt, content) {
        return cocoon.ajax.insertionHelper.insert(refElt, content, function(refElt, newElt) {
            // Destroy and remove all children
            while (refElt.hasChildNodes()) {
                var firstChild = refElt.firstChild;
                if (firstChild.nodeType == dojo.dom.ELEMENT_NODE) {
                    cocoon.ajax.insertionHelper.destroy(firstChild);
                }
                refElt.removeChild(firstChild);
            }

            // Insert the new one
            refElt.appendChild(newElt);
        })
    },

    /**
     * Replaces the reference element
     */
    replace: function (refElt, content) {
        return cocoon.ajax.insertionHelper.insert(refElt, content, function(refElt, newElt) {
            refElt.parentNode.replaceChild(newElt, refElt);
            cocoon.ajax.insertionHelper.destroy(refElt);
        })
    }
});

cocoon.ajax.insertionHelper = {};

dojo.lang.mixin(cocoon.ajax.insertionHelper, {
	/**
	 * Imports an element into a document, taking care of using the correct implementation
	 * so that the browser interprets it as displayable XML.
	 *
	 * Any <script> in the imported node are collected and for post-import evaluation
	 * FIXME: only inline script are considered, but not <script src="">
	 *
	 * @return { element: <em>imported_element</em>, scripts: <em>array_of_script_text</em> }
	 */
    importNode: function(node, targetDoc) {
	    if(node.xml || dojo.lang.isString(node)) {
	        // IE or text
	        var text = dojo.lang.isString(node) ? node : node.xml;
	        var div = targetDoc.createElement("DIV");

	        // Code below heavily inspired by the Prototype library
	        var scriptExpr = "(?:<script.*?>)((\n|\r|.)*?)(?:<\/script>)";
            var textWithoutScripts = text.replace(new RegExp(scriptExpr, 'img'), '');

            // Internet Explorer can't handle empty <textarea .../> elements, so replace
            // them with <textarea></textarea>
            var textareaExpr = "(<textarea[^<]*)\/>";
            var textAreaMatches = textWithoutScripts.match(textareaExpr);
            var textFixed = textWithoutScripts.replace(new RegExp(textareaExpr, 'img'), "$1></textarea>");

            // Update screen with removed scripts
	        div.innerHTML = textFixed;

	        var matchAll = new RegExp(scriptExpr, 'img');
	        var matchOne = new RegExp(scriptExpr, 'im');
	        var allMatches = text.match(matchAll);
	        var scripts = new Array();
	        if (allMatches) {
		        for (var i = 0; i < allMatches.length; i++) {
		            scripts.push(allMatches[i].match(matchOne)[1]);
		        }
		    }
	        return { element: dojo.dom.getFirstChildElement(div), scripts: scripts };

	    } else {
	        var scripts = new Array();
	        var element = this._importDomNode(node, targetDoc, scripts);
	        return { element: element, scripts: scripts }
	    }
	},

	/**
	 * DOM implementation of importNode, recursively creating nodes.
	 * Scripts are collected in the "scripts" parameter
	 */
	_importDomNode: function(node, targetDoc, scripts) {
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
	                var imported = this._importDomNode(children[j], targetDoc, scripts);
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
	},

	_runScripts: function(imported) {
        // Evaluate scripts
        for (var i = 0; i < imported.scripts.length; i++) {
            eval(imported.scripts[i]);
        }
	},

    insert: function(refElt, content, insertFunc) {
        refElt = dojo.byId(refElt, content);
        var imports = this.importNode(content, refElt.ownerDocument);
        insertFunc(refElt, imports.element);
        this._runScripts(imports);
        return this.parseDojoWidgets(imports.element);
    },

    destroy: function(element) {
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
	                this.destroy(child);
	            }
	        }
	    }
	},

	parseDojoWidgets: function(element) {
	    // Find a parent widget (if any) so that Dojo can maintain its widget tree
	    var parentWidget = this.findParentWidget(element);
		var parser = new dojo.xml.Parse();

		// FIXME: parser.parseElement() parses the _children_ of an element, whereas we want here
		// the element itself to be parsed. Parsing its parent is not an option as we don't want
		// to parse the siblings. So place it in a temporary div that we'll trash afterwards.
		var div = document.createElement("DIV");
		element.parentNode.replaceChild(div, element);
		div.appendChild(element);
		var frag = parser.parseElement(div, null, true);
		dojo.widget.getParser().createComponents(frag, parentWidget);
		// Get again the first child of the div, which may no more be the original one
		// if it's a widget
		element = div.firstChild;
		div.parentNode.replaceChild(element, div);
		parentWidget && parentWidget.onResized();

		return element;
	},

	findParentWidget: function(element) {
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
});