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

/* @version $Id$ */


Cocoon.Ajax.BrowserUpdater = Class.create();
Object.extend(Object.extend(Cocoon.Ajax.BrowserUpdater.prototype, Ajax.Request.prototype), {
  initialize: function(url, options) {
    this.transport = Ajax.getTransport();
    this.setOptions(options);

    var onComplete = this.options.onComplete || Prototype.emptyFunction;
    this.options.onComplete = (function() {
    	if (this.checkContinue(this.options.form)) {
				this.updateContent();
				onComplete(this.transport);
      }
    }).bind(this);

    this.request(url);
  },

  updateContent: function() {
		var doc = this.transport.responseXML;
    if (doc) {
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
								var handlerFunc = Cocoon.Ajax.BrowserUpdater.Handlers[handler];
								if (handlerFunc) {
										handlerFunc(node);
								} else {
										this.handleError("No handler found for element " + handler, this.transport);
										return;
								}
						}
				}
    } else {
    	this.handleError("No xml answer", this.transport);
      return;
    }

    if (this.responseIsSuccess()) {
      if (this.onComplete) {
        setTimeout((function() {this.onComplete(this.transport)}).bind(this), 10);
      }
			document.body.style.cursor = "auto";
			self.status = "Update Complete";
    }
  },
  
  checkContinue: function(form) {
  	if (!form) return true;
		var xca;
		try { // An exception is thrown if header doesn't exist (at least in Moz)
				xca = this.transport.getResponseHeader("X-Cocoon-Ajax");
		} catch (e) {
				// header doesn't exist
		}
		if (xca == "continue") {
			// Interaction with this page is finished: redirect the browser to the form's action URL
			// Get the continuation-id, if any.
			var contParam = '?cocoon-ajax-continue=true';
			if (form.elements && form.elements["continuation-id"]) {
					contParam += "&continuation-id=" + form.elements["continuation-id"].value;
			}
			window.location.href = form.action + contParam;
			return false;
    } else {
			return true;
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
	}
	
});

Cocoon.Ajax.BrowserUpdater.Handlers = {
    replace :  function(element) {
        var id = element.getAttribute("id"); // the id from the incoming element
        if (!id) {
           alert("no id found on update element");
           return;
        }    
        var oldElement = $(id); // the element that is being replaced
        if (!oldElement) {
            alert("no element '" + id + "' in source document");
            return;
        }
        // Get the first child element (the first child may be some text!)
        var content = DOMUtils.firstChildElement(element); // the element to replace with
        var insertion = new Cocoon.Ajax.Insertion.Replace(oldElement, content);
        
        var newElement = $(id);
				// Ensure the new node has the correct id
        newElement.setAttribute("id", id);

        if (Cocoon.Ajax.BrowserUpdater.highlight) {
           new Cocoon.Ajax.BrowserUpdater.highlight(newElement);
        }
    }

}

Cocoon.Ajax.BrowserUpdater.highlight = Effect.Highlight;

Cocoon.Ajax.Insertion = new Object();
Cocoon.Ajax.Insertion.Replace = Class.create();
Cocoon.Ajax.Insertion.Replace.prototype = Object.extend(new Abstract.Insertion(''), {

  initialize: function(element, content) {
  	this.element = $(element);
    this.content = content;
    this.insertContent();
  },
  
  insertContent: function() {
  	if (typeof this.content == "string") {
  		var newDiv = document.createElement('div');
  		newDiv.innerHTML = this.content;
  		var newElement = newDiv.firstChild;
  		this.element.parentNode.replaceChild(newElement, this.element);
  	} else { // hopefully 'content' is a DOM Fragment
			var newElement = DOMUtils.importNode(this.content, document);
			this.element.parentNode.replaceChild(newElement, this.element);
		}
  }

});
