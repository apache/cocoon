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

if (typeof cocoon == "undefined") cocoon = {};

cocoon.ajax = {};

/**
 * Create an XHR if the browser supports it.
 */
cocoon.ajax.newXMLHttpRequest = function () {
    if (window.XMLHttpRequest)
        return new XMLHttpRequest;
    else if (window.ActiveXObject)
        return new ActiveXObject("Microsoft.XMLHTTP");
}

//-------------------------------------------------------------------------------------------------

cocoon.ajax.DOMUtils = {
    // Stupid IE doesn't have these constants
    ELEMENT_NODE : 1,
    ATTRIBUTE_NODE : 2,
    TEXT_NODE : 3,
    CDATA_SECTION_NODE : 4,
    ENTITY_REFERENCE_NODE : 5,
    ENTITY_NODE : 6,
    PROCESSING_INSTRUCTION_NODE : 7,
    COMMENT_NODE : 8,
    DOCUMENT_NODE : 9,
    DOCUMENT_TYPE_NODE : 10,
    DOCUMENT_FRAGMENT_NODE : 11
}

/**
 * Get the first child element of an element, ignoring text nodes
 */
cocoon.ajax.DOMUtils.firstChildElement = function(element) {
    var nodes = element.childNodes;
    for (var i = 0; i < nodes.length; i++) {
        var node = nodes[i];
        if (node.nodeType == this.ELEMENT_NODE) {
            return node;
        }
    }
}

cocoon.ajax.DOMUtils.ScriptRegexp = '(?:<script.*?>)((\n|.)*?)(?:<\/script>)';
/**
 * Imports an element into a document, taking care of using the correct implementation
 * so that the browser interprets it as displayable XML.
 * Any <script> in the imported node are collected and evaluated after the import
 */
cocoon.ajax.DOMUtils.importNode = function(node, targetDoc) {
    var result;
    if(node.xml) {
        // IE
        var div = targetDoc.createElement("DIV");
        var text = node.xml;
        
        // Extract scripts
        var match    = new RegExp(cocoon.ajax.DOMUtils.ScriptRegexp, 'img');
        
        // Update screen
        div.innerHTML = text.replace(match, '');

        // And evaluate scripts with a small delay (looks like IE doesn't
        // immediately update the DOM after setting innerHTML?)
        var scripts  = text.match(match);
        if (scripts) {
	        setTimeout(function() {
	            var match = new RegExp(cocoon.ajax.DOMUtils.ScriptRegexp, 'im');
	            for (var i = 0; i < scripts.length; i++) {
	                eval(scripts[i].match(match)[1]);
	            }
	        }, 10);
	    }
        
        result = this.firstChildElement(div);
        
    } else {
        var scripts = new Array();
        result = this._importNode(node, targetDoc, scripts);
        for (var i = 0; i < scripts.length; i++) {
              eval(scripts[i]);
        }
    }
    return result;
}

/**
 * DOM implementation of importNode, recursively creating nodes.
 * Scripts are collected in the "scripts" parameter
 */
cocoon.ajax.DOMUtils._importNode = function(node, targetDoc, scripts) {
    switch(node.nodeType) {
        case this.ELEMENT_NODE:
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
                var imported = this.importNode(children[j], targetDoc);
                if (imported) element.appendChild(imported);
            }
            return element;
        break;
        
        case this.TEXT_NODE:
            return targetDoc.createTextNode(node.nodeValue);
        break;
        
        case this.CDATA_SECTION_NODE:
            return targetDoc.createTextNode(node.nodeValue);
        break;
    }
}

//-------------------------------------------------------------------------------------------------
// General purpose AJAX infrastructure to handle a BU ("browser update") response
//
// To implement a new system-wide instruction, add the corresponding function as a property of
// BrowserUpdate.handlers.
// To add a new handler for a given request, create a new BrowserUpdater and update its
// handlers property.
//-------------------------------------------------------------------------------------------------

cocoon.ajax.BrowserUpdater = function() {
    this.handlers = {};
};

cocoon.ajax.BrowserUpdater.highlight = null;

cocoon.ajax.BrowserUpdater.prototype.processResponse = function(doc, request) {
    var nodes = doc.documentElement.childNodes;
    for (var i = 0; i < nodes.length; i++) {
        var node = nodes[i];
        if (node.nodeType == cocoon.ajax.DOMUtils.ELEMENT_NODE) {
            var handler;
            if (node.localName) {
                handler = node.localName;
            } else {
                // No DOM2 support (IE6)
                handler = node.nodeName.replace(/.*:/, "");
            }
            var handlerFunc = this.handlers[handler] || cocoon.ajax.BrowserUpdater.handlers[handler];
            if (handlerFunc) {
                handlerFunc(node);
            } else {
                cocoon.ajax.BrowserUpdater.handleError("No handler found for element " + handler, request);
            }
        }
    }
}

/**
 * System-wide handlers
 */
cocoon.ajax.BrowserUpdater.handlers = {
    replace :  function(element) {
        var id = element.getAttribute("id");
        if (!id) {
           alert("no id found on update element");
           return;
        }    
        // Get the first child element (the first child may be some text!)
        var firstChild = cocoon.ajax.DOMUtils.firstChildElement(element);
    
        var oldElement = document.getElementById(id);
        
        if (!oldElement) {
            alert("no element '" + id + "' in source document");
            return;
        }
    
        var newElement = cocoon.ajax.DOMUtils.importNode(firstChild, document);
        // Don't update if we don't have a valid new element.
        if (newElement) {
	        // Warn: it's replace(new, old)!!
	        oldElement.parentNode.replaceChild(newElement, oldElement);
	        // Ensure the new node has the correct id
	        newElement.setAttribute("id", id);
	        
	        if (cocoon.ajax.BrowserUpdater.highlight) {
	           cocoon.ajax.BrowserUpdater.highlight(newElement);
	        }
	      }
    }
}

cocoon.ajax.BrowserUpdater.handleError = function(message, request) {
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
}

//-------------------------------------------------------------------------------------------------
// Fader used to highlight page areas that have been updated
// WARNING: don't rely too much on these effects, as they're very likely to be replaced
//          by some third party library in the near future
//-------------------------------------------------------------------------------------------------

/**
 * Create a fader that will progressively change an element's background color from
 * a given color back to its original color.
 *
 * @param elt the element to fade
 * @param color the starting color (default yellow)
 * @param duration the fade duration in msecs (default 1000)
 * @param fps the animation frames per seconds (default 25)
 */
cocoon.ajax.Fader = function(elt, color, duration, fps) {
   // Set default values
   if (!color) color = "#FFFF80"; // yellow
   if (!duration) duration = 1000; // 1 sec
   if (!fps) fps = 25; // 25 frames/sec
   
   this.element = elt;
   this.fromColor = cocoon.ajax.Fader.colorToRgb(color);
   this.toColor = cocoon.ajax.Fader.colorToRgb(cocoon.ajax.Fader.getBgColor(this.element));
   
   this.maxFrames = Math.round(fps * duration / 1000.0);
   this.delay = duration / this.maxFrames;
}

/**
 * Creates a default fader for a given element. This function can be used to set BrowserUpdate.highlight
 */
cocoon.ajax.Fader.fade = function(elt) {
   new cocoon.ajax.Fader(elt).start();
}

cocoon.ajax.Fader.prototype.start = function() {
   this.frame = 0;
   this._changeColor();
}

cocoon.ajax.Fader.prototype._changeColor = function() {
    if (this.frame < this.maxFrames) {
        // Schedule the next iteration right now to keep a more accurate timing
        var fader = this;
        setTimeout(function() {fader._changeColor();}, this.delay);
    }
    var newColor = new Array(3);
    for (var channel = 0; channel < 3; channel++) {
        newColor[channel] = Math.floor(
            this.fromColor[channel] * ((this.maxFrames - this.frame) / this.maxFrames) +
            this.toColor[channel] * (this.frame/this.maxFrames)
        );
    }

    this.frame++;
    var color = cocoon.ajax.Fader.rgbToColor(newColor[0], newColor[1], newColor[2]);
    this.element.style.backgroundColor = color;
}

/**
 * Converts a long hex "#RRGGBB" or short hex "#RGB" color as an array of 3 ints.
 * If neither pattern matches, returns defaultValue or 255,255,255 if none specified.
 */
cocoon.ajax.Fader.colorToRgb = function(hex, defaultValues) {
    var r = 255; // defaults if no match and no defaultValues specified
    var g = 255;
    var b = 255;

    if (defaultValues) {
        r = defaultValues[0];
        g = defaultValues[1];
        b = defaultValues[2];
    }
    var colors = hex.match(/^#(\d{2})(\d{2})(\d{2})$/);
    if (colors) {
        r = parseInt(colors[0]);
        g = parseInt(colors[1]);
        b = parseInt(colors[2]);
    } else if (colors = hex.match(/^#(\d)(\d)(\d)$/)) {
        r = parseInt(colors[0] + colors[0]);
        g = parseInt(colors[1] + colors[1]);
        b = parseInt(colors[2] + colors[2]);
    }
    return [r,g,b];
}

/** Converts rgb values to a "#RRGGBB" color */
cocoon.ajax.Fader.rgbToColor = function(r, g, b) {
    r = r.toString(16); if (r.length == 1) r = '0' + r;
    g = g.toString(16); if (g.length == 1) g = '0' + g;
    b = b.toString(16); if (b.length == 1) b = '0' + b;
    return "#" + r + g + b;
}

/** Get the background color of an element */
cocoon.ajax.Fader.getBgColor = function(elt) {
    while(elt) {
        var c;
        if (window.getComputedStyle) c = window.getComputedStyle(elt,null).getPropertyValue("background-color");
        if (elt.currentStyle) c = elt.currentStyle.backgroundColor;
        if ((c != "" && c != "transparent") || elt.tagName == "BODY") { break; }
        elt = elt.parentNode;
    }
    if (c == undefined || c == "" || c == "transparent" || c == "white") c = "#FFFFFF";

    var rgb = c.match(/rgb\s*\(\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})\s*\)/);
    if (rgb) return this.rgbToColor(parseInt(rgb[1]),parseInt(rgb[2]),parseInt(rgb[3]));
    return c;
}

//-------------------------------------------------------------------------------------------------
// Blinker used to highlight page areas that have been updated
//-------------------------------------------------------------------------------------------------

cocoon.ajax.Blinker = function(elt, color, hltDelay, normalDelay, blinks) {
    this.element = elt;
    if (!color) color = "#FFFF80"; // yellow
    if (!hltDelay) hltDelay = 100;
    if (!normalDelay) normalDelay = 100;
    if (!blinks) blinks = 2;
    
    this.hltColor = color;
    this.hltDelay = hltDelay;
    this.normalDelay = normalDelay;
    this.normalColor = cocoon.ajax.Fader.getBgColor(elt);
    this.maxBlinks = blinks * 2;
    this.blink = 0;
}

cocoon.ajax.Blinker.prototype.start = function() {
   this.blink = 0;
   this._doBlink();
}

cocoon.ajax.Blinker.blink = function(elt) {
   new cocoon.ajax.Blinker(elt).start();
}

cocoon.ajax.Blinker.prototype._doBlink = function() {
   var hlt = (this.blink % 2 == 0);
   this.element.style.backgroundColor = hlt ? this.hltColor : this.normalColor;;
   if (this.blink <= this.maxBlinks) {
      var blinker = this;
      setTimeout(function() {blinker._doBlink();}, hlt ? this.hltDelay : this.normalDelay);
   }
   this.blink++;
}

