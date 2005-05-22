/*
* Copyright 1999-2005 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

/**
 * Runtime JavaScript library for Cocoon forms.
 *
 * @version $Id$
 */

//-------------------------------------------------------------------------------------------------
/**
 * The CForms class holds all forms-related properties and methods.
 */

CForms = {
    ajax : false // default mode is full page update
};

/**
 * Submits a form. If ajax mode is on and the browser is ajax-aware, the page isn't reloaded
 */
CForms.submitForm = function(element, name) {
    if (name == undefined) {
        name = element.name;
    }
    
    var form = forms_getForm(element);
    if (form == null) {
        alert("Cannot find form for " + element);
    } else {
        form["forms_submit_id"].value = name;
        // FIXME: programmatically submitting the form doesn't trigger onsubmit ? (both in IE and Moz)
        forms_onsubmit();
        
        // Provide feedback that something is happening.
        document.body.style.cursor = "wait";
        
        var req = CForms.newXMLHttpRequest();
        if (req) {
            var uri = form.action;
            if ( uri == "" )
                uri = document.location;
                
            req.open("POST", uri, true);
            req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            // onreadystatechange must be all lower case (?)
            req.onreadystatechange = function() {
                if (req.readyState == 4) {
                    CForms._handleBrowserUpdate(form, req);
                }
            }
            req.send(CForms._buildQueryString(form, name));
            // Reset submit-id
            form["forms_submit_id"].value = '';
            forms_onsubmitHandlers = new Array();
            
        } else {
            // Non ajax-aware browser : regular submit
            form.submit();
        }
    }
}

// Override the default forms_submitForm
forms_submitForm = CForms.submitForm;

/**
 * Create an XHR if the browser supports it.
 */
CForms.newXMLHttpRequest = function () {
    if (this.ajax) {
        if (window.XMLHttpRequest)
            return new XMLHttpRequest;
        else if (window.ActiveXObject)
            return new ActiveXObject("Microsoft.XMLHTTP");
    }
}

/**
 * Build a query string with all form inputs
 */
CForms._buildQueryString = function(form, submitId) {
    // Indicate to the server that we're in ajax mode
    var result = "cocoon-ajax=true";
    // Iterate on all form controls
    for (var i = 0; i < form.elements.length; i++) {
        input = form.elements[i];
        if (input.type == "submit" || input.type == "image") {
            // Skip buttons
            continue;
        }
        if ((input.type == "checkbox" || input.type == "radio") && !input.checked) {
            // Skip unchecked checkboxes and radio buttons
            continue;
        }
        result += "&" + encodeURIComponent(input.name) + "=" + encodeURIComponent(input.value);
    }
    return result;
}

/**
 * Handle the server response
 */
CForms._handleBrowserUpdate = function(form, request) {
    // Restore normal cursor
    document.body.style.cursor = "auto";
    if (request.status == 200) {
        var xca;
        try { // An exception is thrown if header doesn't exist (at least in Moz)
            xca = request.getResponseHeader("X-Cocoon-Ajax");
        } catch (e) {
            // header doesn't exist
        }
        if (xca == "continue") {
            // Interaction with this page is finished: redirect the browser to the form's action URL
            // Get the continuation-id, if any.
            var contParam = '?cocoon-ajax-continue=true';
            if (form.elements["continuation-id"]) {
                contParam = "&continuation-id=" + form.elements["continuation-id"].value;
            }
            window.location.href = form.action + contParam;
        } else {
            // Handle browser update directives
            var doc = request.responseXML;
            if (!doc) {
                alert("no xml answer");
                return;
            }
        
           BrowserUpdate.processResponse(doc);
       }
    } else {
/*        var str = "";
        for(prop in request) {
           str += prop
           str += " = " 
           str += request[prop];
           str += '\n';
        }
        alert(str);
        alert(request.getAllResponseHeaders());
*/
        alert("request failed - status: " + request.status);
    }
}

//-------------------------------------------------------------------------------------------------

DOMUtils = {
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
DOMUtils.firstChildElement = function(element) {
    var nodes = element.childNodes;
    for (var i = 0; i < nodes.length; i++) {
        var node = nodes[i];
        if (node.nodeType == this.ELEMENT_NODE) {
            return node;
        }
    }
}

/**
 * Imports an element into a document, taking care of using the correct implementation
 * so that the browser interprets it as displayable XML
 */
DOMUtils.importNode = function(node, targetDoc) {
    if(node.xml) {
        // IE
        var div = targetDoc.createElement("DIV");
        div.innerHTML = node.xml;
        return this.firstChildElement(div);
    } else {
        return DOMUtils._importNode(node, targetDoc);
    }
}

/**
 * DOM implementation of importNode, recursively creating nodes
 */
DOMUtils._importNode = function(node, targetDoc) {
    switch(node.nodeType) {
        case this.ELEMENT_NODE:
            var element = targetDoc.createElement(node.nodeName);
            //var element = targetDoc.createElementNS(node.namespaceURI, node.nodeName);
            var attrs = node.attributes;
            for (var i = 0; i < attrs.length; i++) {
                attr = attrs[i];
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
// To implement a new instruction, add the corresponding function as a property of
// BrowserUpdate.handlers.
//-------------------------------------------------------------------------------------------------

BrowserUpdate = {};

BrowserUpdate.processResponse = function(doc) {
    var nodes = doc.documentElement.childNodes;
    for (var i = 0; i < nodes.length; i++) {
        var node = nodes[i];
        if (node.nodeType == DOMUtils.ELEMENT_NODE) {
            var handler;
            if (node.localName) {
                handler = node.localName;
            } else {
                // No DOM2 support (IE6)
                handler = node.nodeName.replace(/.*:/, "");
            }
            var handlerFunc = BrowserUpdate.handlers[handler];
            if (handlerFunc) {
                handlerFunc(node);
            } else {
                alert("no handler found for " + handler);
            }
        }
    }
}
BrowserUpdate.handlers = {
    replace :  function(element) {
        var id = element.getAttribute("id");
        if (!id) {
           alert("no id found on update element");
           return;
        }    
        // Get the first child element (the first child not may be some text!)
        var firstChild = DOMUtils.firstChildElement(element);
    
        var oldElement = document.getElementById(id);
        
        if (!oldElement) {
            alert("no element '" + id + "' in source document");
            return;
        }
    
        var newElement = DOMUtils.importNode(firstChild, document);
        
        // Warn: it's replace(new, old)!!
        oldElement.parentNode.replaceChild(newElement, oldElement);
        // Ensure the new node has the correct id
        newElement.setAttribute("id", id);
        
        if (BrowserUpdate.highlight) {
           BrowserUpdate.highlight(newElement);
        }
    }
}

//-------------------------------------------------------------------------------------------------
// Fader used to highlight page areas that have been updated
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
function Fader(elt, color, duration, fps) {
   // Set default values
   if (!color) color = "#FFFF80"; // yellow
   if (!duration) duration = 1000; // 1 sec
   if (!fps) fps = 25; // 25 frames/sec
   
   this.element = elt;
   this.fromColor = Fader.colorToRgb(color);
   this.toColor = Fader.colorToRgb(Fader.getBgColor(this.element));
   
   this.maxFrames = Math.round(fps * duration / 1000.0);
   this.delay = duration / this.maxFrames;
}

/**
 * Creates a default fader for a given element. This function can be used to set BrowserUpdate.highlight
 */
Fader.fade = function(elt) {
   new Fader(elt).start();
}

Fader.prototype.start = function() {
   this.frame = 0;
   this._changeColor();
}

Fader.prototype._changeColor = function() {
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
    var color = Fader.rgbToColor(newColor[0], newColor[1], newColor[2]);
    this.element.style.backgroundColor = color;
}

/** Converts a "#RRGGBB" color as an array of 3 ints */
Fader.colorToRgb = function(hex) {
    return [
        parseInt(hex.substr(1,2),16),
        parseInt(hex.substr(3,2),16),
        parseInt(hex.substr(5,2),16) ];
}

/** Converts rgb values to a "#RRGGBB" color */
Fader.rgbToColor = function(r, g, b) {
    r = r.toString(16); if (r.length == 1) r = '0' + r;
    g = g.toString(16); if (g.length == 1) g = '0' + g;
    b = b.toString(16); if (b.length == 1) b = '0' + b;
    return "#" + r + g + b;
}

/** Get the background color of an element */
Fader.getBgColor = function(elt) {
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

BrowserUpdate.highlight = Fader.fade;

//-------------------------------------------------------------------------------------------------
// Blinker used to highlight page areas that have been updated
//-------------------------------------------------------------------------------------------------

function Blinker(elt, color, hltDelay, normalDelay, blinks) {
    this.element = elt;
    if (!color) color = "#FFFF80"; // yellow
    if (!hltDelay) hltDelay = 100;
    if (!normalDelay) normalDelay = 100;
    if (!blinks) blinks = 2;
    
    this.hltColor = color;
    this.hltDelay = hltDelay;
    this.normalDelay = normalDelay;
    this.normalColor = Fader.getBgColor(elt);
    this.maxBlinks = blinks * 2;
    this.blink = 0;
}

Blinker.prototype.start = function() {
   this.blink = 0;
   this._doBlink();
}

Blinker.blink = function(elt) {
   new Blinker(elt).start();
}

Blinker.prototype._doBlink = function() {
   var hlt = (this.blink % 2 == 0);
   this.element.style.backgroundColor = hlt ? this.hltColor : this.normalColor;;
   if (this.blink <= this.maxBlinks) {
      var blinker = this;
      setTimeout(function() {blinker._doBlink();}, hlt ? this.hltDelay : this.normalDelay);
   }
   this.blink++;
}

