/*
 * Copyright 1999-2005 The Apache Software Foundation
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
 * Runtime JavaScript library for Cocoon forms.
 *
 * @version $Id$
 */

//-------------------------------------------------------------------------------------------------
/**
 * The cocoon.forms object holds all forms-related properties and methods.
 */
if (typeof cocoon == "undefined") cocoon = {};

cocoon.forms = {
    ajax : false // default mode is full page update
};

/**
 * Get the form of an element
 */
cocoon.forms.getForm = function(element) {
    while(element != null && element.tagName != "FORM") {
        element = element.parentNode;
    }
    return element;
}

/**
 * Submits a form. If ajax mode is on and the browser is ajax-aware, the page isn't reloaded
 */
cocoon.forms.submitForm = function(element, name) {
    if (typeof(name) == "undefined") {
        name = element.name;
    }
    
    var form = this.getForm(element);
    if (form == null) {
        alert("Cannot find form for " + element);
    } else {
        form["forms_submit_id"].value = name;
        // FIXME: programmatically submitting the form doesn't trigger onsubmit ? (both in IE and Moz)
        forms_onsubmit();

        var req = this.ajax && cocoon.ajax.newXMLHttpRequest();
        if (req) {
            var query = this._buildQueryString(form, name);
            if (query) {
                // Provide feedback that something is happening.
                document.body.style.cursor = "wait";

                // The "ajax-action" attribute specifies an alternate submit location used in Ajax mode.
                // This allows to use Ajax in the portal where forms are normally posted to the portal URL.
                var uri = form.getAttribute("ajax-action");
                if (!uri) uri = form.action;
                if (uri == "") uri = document.location;

                req.open("POST", uri, true);
                req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                req.onreadystatechange = function() {
                    if (req.readyState == 4) {
                        cocoon.forms._handleBrowserUpdate(form, req);
                    }
                }
                req.send(query);
                // Reset submit-id
                form["forms_submit_id"].value = '';
                forms_onsubmitHandlers = new Array();            
            } else {
                // Some inputs are not ajax-compatible. Fall back to full page reload
                form.submit();
            }
        } else {
            // Non ajax-aware browser : regular submit
            form.submit();
        }
    }
}

// Override the default forms_submitForm
forms_submitForm = function() { cocoon.forms.submitForm.apply(cocoon.forms, arguments) };

/**
 * Build a query string with all form inputs
 */
cocoon.forms._buildQueryString = function(form, submitId) {
    // Indicate to the server that we're in ajax mode
    var result = "cocoon-ajax=true";
    // Iterate on all form controls
    for (var i = 0; i < form.elements.length; i++) {
        input = form.elements[i];
        if (typeof(input.type) == "undefined") {
        	// Skip fieldset
        	continue;
        }
        if (input.type == "submit" || input.type == "image" || input.type == "button") {
            // Skip buttons
            continue;
        }
        if ((input.type == "checkbox" || input.type == "radio") && !input.checked) {
            // Skip unchecked checkboxes and radio buttons
            continue;
        }
        if (input.type == "file") {
            // Can't send files in Ajax mode. Fall back to full page
            return null;
        }
        if (input.tagName.toLowerCase() == "select" && input.multiple) {
            var name = encodeURIComponent(input.name);
            var options = input.options;
            for (var zz = 0; zz < options.length; zz++) {
                if (options[zz].selected) {
                    result += "&" + name + "=" + encodeURIComponent(options[zz].value);
                }
            }
            // don't use the default fallback
            continue;
        }
        
        // text, passwod, textarea, hidden, single select
        result += "&" + encodeURIComponent(input.name) + "=" + encodeURIComponent(input.value);
    }
    return result;
}

/**
 * Redirect when interaction is finished with the current form
 */
cocoon.forms._continue = function(form) {
    if (form.method.toLowerCase() == "post") {
        // Create a fake form and post it
        var div = document.createElement("div");
        var content = "<form action='" + form.action + "' method='POST'>" +
                  "<input type='hidden' name='cocoon-ajax-continue' value='true'/>";
	    if (form.elements["continuation-id"]) {
	        content += "<input type='hidden' name='continuation-id' value='" +
	            form.elements["continuation-id"].value + "'/>";
	    }
	    content += "</form>";
	    div.innerHTML = content;
	    document.body.appendChild(div);
	    div.firstChild.submit();
    } else {	    
        // Redirect to the form's action URL
	    var contParam = '?cocoon-ajax-continue=true';
	    if (form.elements["continuation-id"]) {
	        contParam += "&continuation-id=" + form.elements["continuation-id"].value;
	    }
	    window.location.href = form.action + contParam;
	}
}

/**
 * Handle the server response
 */
cocoon.forms._handleBrowserUpdate = function(form, request) {
    // Restore normal cursor
    document.body.style.cursor = "auto";
    if (request.status == 200) {
        var xca;
        try { // An exception is thrown if header doesn't exist
            xca = request.getResponseHeader("X-Cocoon-Ajax");
        } catch (e) {
            // header doesn't exist
        }
        if (xca == "continue") {
            this._continue(form);
        } else {
            // Handle browser update directives
            var doc = request.responseXML;
            if (!doc) {
                cocoon.ajax.BrowserUpdater.handleError("No xml answer", request);
                return;
            }
            
            var updater = new cocoon.ajax.BrowserUpdater();
            updater.handlers['continue'] = function() { cocoon.forms._continue(form); }
            updater.processResponse(doc, request);
       }
    } else {
        cocoon.ajax.BrowserUpdater.handleError("Request failed - status=" + request.status, request);
    }
}
