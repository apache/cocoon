/*
 * Copyright 2005 The Apache Software Foundation.
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
 * Utility functions for form handling.
 *
 * @version $Id$
 */
// Can be loaded either through "cocoon.forms" or "cocoon.forms.common"
// or individually out of Dojo.
if (dojo) {
    dojo.provide("cocoon.forms");
    dojo.provide("cocoon.forms.common");
} else {
    cocoon = cocoon || {};
    cocoon.forms = cocoon.forms || {};
}

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
    var form = this.getForm(element);
    if (form == null) {
        alert("Cannot find form for " + element);
        return;
    }

    if (!name) name = element.name;

	var dojoId = form.getAttribute("dojoWidgetId");
    if (dojoId) {
        // Delegate to the CFormsForm widget
        dojo.widget.byId(dojoId).submit(name);

    } else {
        // Regular submit. How old-fashioned :-)
        
        // Send the identifier of the widget that triggered the submit
        form["forms_submit_id"].value = name;

        // A form's onsubmit is only called when submit is triggered by user action, but not when
        // called by a script. So don't forget to call it, cancelling the submit if (and only if)
        // it returns false
	    if (!form.onsubmit || form.onsubmit() != false) {
	        form.submit();
	    }
	}
}

// Override the default forms_submitForm
forms_submitForm = function() { cocoon.forms.submitForm.apply(cocoon.forms, arguments) };

/**
 * Build a query string with all form inputs.
 *
 * @parameter form the form element
 * @parameter submitId the ID of the widget that submitted the form
 * @return the query string for the form, or null if some inputs cannot be
 *         send in Ajax mode (e.g. file inputs)
 */
cocoon.forms.buildQueryString = function(form, submitId) {
    // Indicate to the server that we're in ajax mode
    var result = "cocoon-ajax=true";
    
    // If the form has a forms_submit_id input, use it to avoid sending the value twice
    if (form["forms_submit_id"]) {
        form["forms_submit_id"].value = submitId;
    } else {
        if (submitId) result += "&forms_submit_id=" + submitId;
    }

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
 * Encode an object as querystring parameters.
 *
 * @parameter params the object to encode
 * @isAppending if true, "&" is prepended to the result
 * @return the querystring
 */
cocoon.forms.encodeParams = function(params, isAppending) {
    if (!params) return "";
    var result = "";
    var sep = isAppending ? "&" : "";
    for (var name in params) {
        result += sep + encodeURIComponent(name) + "=" + encodeURIComponent(params[name]);
        sep = "&";
    }
    return result;
}
