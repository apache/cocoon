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
 * Utility functions for CForm handling.
 *
 * These scripts will use Dojo if it is avalable, but should work without.
 *
 * NOTE: (2.1.11) Functionality from forms-lib.js has been refactored, namespaced and moved here
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
 * Get the parent form of an element
 *
 * NOTE: Introduced in 2.1.11, replaces forms_getForm
 */
cocoon.forms.getForm = function(element) {
    while(element != null && element.tagName != null && element.tagName.toLowerCase() != "form") {
        element = element.parentNode;
    }
    return element;
}

/**
 * Submits a form.
 * This function is designed to submit the form when called from scripts (onchange handlers etc.)
 *
 * NOTE: Introduced in 2.1.11, replaces forms_submitForm
 *
 * @param element   the DOM Node that is triggering this submit
 * @param name      (Optional) tell CForms the name of the submitting element
 * @param params    (Optional) an Associative Array of parameters to add to the submitted form
 *
 */
cocoon.forms.submitForm = function(element, name, params) {
    var form = this.getForm(element);
    if (form == null) {
        alert("Cannot find form for " + element);
        return;
    }

    if (!name) name = element.name;

    var dojoId = form.getAttribute("dojoWidgetId");
    if (dojoId) {
        // Delegate to the SimpleForm or AjaxForm widget
        dojo.widget.byId(dojoId).submit(name, params);
    } else {
        // Regular submit. There is no *Form widget available

        // A form's onsubmit is only called when submit is triggered by user action, but not when
        // called by a script. So call it now, cancelling the submit if it returns false
        if (!form.onsubmit || form.onsubmit() != false) {     // call the user's onSubmit handler
            cocoon.forms.fullPageSubmit(form, name, params);
        }
    }
}

/**
 * Internal function
 * Submits a form using a full page submit
 *
 * @param form      the form DOM Node that is being submitted
 * @param name      tell CForms the name of the submitting widget
 * @param params    an Associative Array of parameters to add to the submitted form
 *
 */
cocoon.forms.fullPageSubmit = function(form, name, params) {
    // Send the identifier of the widget that triggered the submit
    params["forms_submit_id"] = name;
    // call CForm's onSubmit handlers - allow them to stop the form by returning false
    if (cocoon.forms.callOnSubmitHandlers(form)) {   
        for (var param in params) { // add extra params to the form
            var input = form[param] || document.createElement("input");
            input.setAttribute("type", "hidden");
            input.setAttribute("name", param);
            input.setAttribute("value", params[param]);
            if (!form[param]) form.appendChild(input);
        }
        form.submit();
    }
}

/**
 * onLoad Handlers
 * Manage functions that should be called when the page has loaded
 *
 * NOTE: Introduced in 2.1.11, replaces forms_onloadHandlers
 */
cocoon.forms.onLoadHandlers = new Array();

/**
 * add an onLoad Hander
 *
 * NOTE: Introduced in 2.1.11, replaces forms_onloadHandlers.push
 */
cocoon.forms.addOnLoadHandler = function(handler) {
    if (handler && typeof(handler.forms_onload) == "function") {
        cocoon.forms.onLoadHandlers.push(handler);
    }
}

/**
 * call the onLoad Handlers (typically this function is passed to dojo.addOnLoad)
 *
 * NOTE: Introduced in 2.1.11, replaces forms_onloadHandlers.push
 */
cocoon.forms.callOnLoadHandlers = function() {
    for (var i = 0; i < cocoon.forms.onLoadHandlers.length; i++) {
        cocoon.forms.onLoadHandlers[i].forms_onload();
    }
    // Reset it (we do not need them anymore)
    cocoon.forms.onLoadHandlers = new Array();
}

/**
 * onSubmit Handlers
 * Manage functions that should be called before the form is submitted
 * If an onSubmit Handler returns false, the form will not be submitted
 * NB. onSubmit Handlers are not currently called for Ajax forms
 *
 * NOTE: Introduced in 2.1.11, replaces forms_onsubmitHandlers.push
 */
cocoon.forms.onSubmitHandlers = {};

/**
 * add an onSubmit Handler
 *
 * NOTE: Introduced in 2.1.11, replaces forms_onsubmitHandlers.push
 *
 * @param element  the form control adding the handler
 * @param handler  the handler
 */
cocoon.forms.addOnSubmitHandler = function(element, handler) {
    if (handler && typeof(handler.forms_onsubmit) == "function") {
        var form = this.getForm(element);
        if (form) {
            var id = form.getAttribute("id");
            if (id) {
                if (!cocoon.forms.onSubmitHandlers[id]) cocoon.forms.onSubmitHandlers[id] = new Array();
                cocoon.forms.onSubmitHandlers[id].push(handler);
            } else {
                if (dojo) dojo.debug("WARNING: SubmitHandler not added. There is no id attribute on your form.");
            }
        }
    }
}

/**
 * call the onSubmit Handlers
 *
 * NOTE: Introduced in 2.1.11, replaces forms_onsubmit
 *
 * @param form      the form (DOMNode) being submitted
 */
cocoon.forms.callOnSubmitHandlers = function(form) {
    var id = form.getAttribute("id");
    if (cocoon.forms.onSubmitHandlers == null) {
        // Form already submited, but the new page is not yet loaded. This can happen when
        // the focus is in an input with an "onchange" and the user clicks on a submit button.
        return false;
    }
    if (cocoon.forms.onSubmitHandlers[id] == null) {
        // When addOnSubmitHandler has never been called, there will be no submit handlers
        return true;
    }
    for (var i = 0; i < cocoon.forms.onSubmitHandlers[id].length; i++) {
        if (cocoon.forms.onSubmitHandlers[id][i].forms_onsubmit() == false) {
            // handler cancels the submit
            return false;
            // TODO: should we allow all onsubmithandlers to be called, but then return the aggregate result ?
            //  (bruno): I don't think so, the first cancel operation should cancel it completely (esp. if this
            //           might be the result of some user interaction)
        }
    }
    // clear it
    // TODO: if AjaxForm were to start calling submit handlers, this would need to change
    cocoon.forms.onSubmitHandlers[id] = null;
    return true;
}




