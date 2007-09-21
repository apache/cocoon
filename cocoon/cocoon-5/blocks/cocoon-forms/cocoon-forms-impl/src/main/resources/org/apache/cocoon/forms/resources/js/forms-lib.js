/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
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
 * NOTE: This file will be trimmed down to contain only the necessary
 *       features for dynamic behaviour on non Ajax-capable browsers.
 *       Advanced widgets such as double selection list and multivalue
 *       field will be refactored as Dojo widgets.
 * NOTE: (2.1.11) moving support for non-ajax forms to cocoon.forms.common and the SimpleForm Widget.
 *
 * This file has dependencies on cocoon.forms.common
 *    /_cocoon/resources/forms/js/common.js
 *
 * @version $Id$
 */


/**
  * Deprecated legacy functions
  * To be removed in 2.1.12(?) release
  */
forms_submitForm = function() { 
    if (dojo) dojo.debug("DEPRECATED: forms_submitForm(), use cocoon.forms.submitForm(element[, name, params]) instead");
    // we do not know which form is being submitted, let's at least try to find it.
    var form = document.getElementById("_cforms_default_form_"); // maybe forms_onsubmitHandlers.push has been called
    if (!form) {
        for (var i = 0; i < document.forms.length; i++) {
            if (document.forms[i].getAttribute("dojoWidgetId") != "") {
                form = document.forms[i];
                form.setAttribute("id", "_cforms_default_form_"); // just in case we get here again ...
            }
            if (form) break;
        }
    }
    if (!form) { // last ditch attempt, maybe dojo is not being used
        form = document.forms[0];
    }
    cocoon.forms.submitForm.apply(cocoon.forms, [form]); 
}

forms_getForm = function(element) { 
    if (dojo) dojo.debug("DEPRECATED: forms_getForm(element), use cocoon.forms.getForm(element) instead");
    cocoon.forms.getForm.apply(cocoon.forms, arguments); 
}

forms_onsubmit = function() { 
    if (dojo) dojo.debug("DEPRECATED: forms_onsubmit(), use cocoon.forms.callOnSubmitHandlers(form) instead");
    var form = document.getElementById("_cforms_default_form_");
    cocoon.forms.callOnSubmitHandlers.apply(cocoon.forms, [form]); 
}

forms_onload = function() { 
    if (dojo) dojo.debug("DEPRECATED: forms_onload(), use cocoon.forms.callOnLoadHandlers() instead");
    cocoon.forms.callOnLoadHandlers.apply(cocoon.forms, arguments);
}

var forms_onloadHandlers = new Array();
forms_onloadHandlers.push = function(handler) {
    if (dojo) dojo.debug("DEPRECATED: forms_onloadHandlers.push(handler), use cocoon.forms.addOnLoadHandler(handler) instead");
    cocoon.forms.addOnLoadHandler.apply(cocoon.forms, arguments) 
}

var forms_onsubmitHandlers = new Array();
forms_onsubmitHandlers.push = function(handler) {
    if (dojo) dojo.debug("DEPRECATED: forms_onsubmitHandlers.push(handler), use cocoon.forms.addOnSubmitHandler(form, handler) instead");
    // we do not know which form the handler is from, let's at least try to find it.
    var form = document.getElementById("_cforms_default_form_"); // maybe we did this before
    if (!form) {
        for (var i = 0; i < document.forms.length; i++) {
            if (document.forms[i].getAttribute("dojoWidgetId") != "") {
                form = document.forms[i];
                form.setAttribute("id", "_cforms_default_form_"); // make it easier to find is there are more submit handlers
            }
            if (form) break;
        }
    }
    if (!form) { // last ditch attempt, maybe dojo is not being used
        form = document.forms[0];
        if (form) form.setAttribute("id", "_cforms_default_form_");
    }
    cocoon.forms.addOnSubmitHandler.apply(cocoon.forms, [form, handler]); 
}


// TODO: Not called
function forms_moveInBody(element) {
    element.parentNode.removeChild(element);
    document.body.appendChild(element);
}

function forms_createOptionTransfer(id, submitOnChange) {
    var result = new OptionTransfer(id + ".unselected", id);
    result.setAutoSort(true);
    // add to onload handlers
    result.forms_id = id + ":input";
    result.forms_onload = function() {
        var form = cocoon.forms.getForm(document.getElementById(this.forms_id));
        this.init(form);
        sortSelect(this.left);
        sortSelect(this.right);
    }
    result.submitOnChange = submitOnChange;
    result.forms_transferLeft = function() {
        this.transferLeft();
        if (this.submitOnChange) {
            cocoon.forms.submitForm(document.getElementById(this.forms_id));
        }
    }
    result.forms_transferRight = function() {
        this.transferRight();
        if (this.submitOnChange) {
            cocoon.forms.submitForm(document.getElementById(this.forms_id));
        }
    }
    result.forms_transferAllLeft = function() {
        this.transferAllLeft();
        if (this.submitOnChange) {
            cocoon.forms.submitForm(document.getElementById(this.forms_id));
        }
    };
    result.forms_transferAllRight = function() {
        this.transferAllRight();
        if (this.submitOnChange) {
            cocoon.forms.submitForm(document.getElementById(this.forms_id));
        }
    };
    cocoon.forms.addOnLoadHandler(result);
    
    // add to onsubmit handlers
    result.forms_onsubmit = function() {
        // Select all options in the "selected" list to that
        // its values are sent.
        selectAllOptions(this.right);
    }
    cocoon.forms.addOnSubmitHandler(document.getElementById(id), result);
    return result;
}


/**
 * Show a tab in a <wi:group>
 *
 * @param tabgroup (string) name of the <wi:group>
 * @param idx (integer) index of the selected tab
 * @param length (integer) total number of tabs
 * @param state (string, optional) name of the input storing the tabgroup state
 */
function forms_showTab(tabgroup, idx, length, state) {
    // Change state value
    if (state.length > 0) {
        document.getElementById(state).value = idx;
    }
    for (var i = 0; i < length; i++) {
        // Change tab status (selected/unselected)
        var tab = document.getElementById(tabgroup + "_tab_" + i);
        if (tab != null) {
            tab.className = (i == idx) ? 'forms-tab forms-activeTab': 'forms-tab';
        }
        // Change tab content visibilty
        var tabitems = document.getElementById(tabgroup + "_items_" + i);
        if (tabitems != null) {
            tabitems.style.display = (i == idx) ? '' : 'none';
            // execute event handler if any
            if (i == idx &&
                    window.onTabShownHandlers != null &&
                    window.onTabShownHandlers[tabgroup] != null) {
                var onShowHandler = window.onTabShownHandlers[tabgroup][tabgroup + "_items_" + i];
                if (onShowHandler != null) {
                    eval(onShowHandler);
                }
            }
        }
    }
}