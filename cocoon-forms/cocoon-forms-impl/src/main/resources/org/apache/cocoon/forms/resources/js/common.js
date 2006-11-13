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
    while(element != null && element.tagName != null && element.tagName.toLowerCase() != "form") {
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
