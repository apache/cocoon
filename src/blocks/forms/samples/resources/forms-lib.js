/*
* Copyright 1999-2004 The Apache Software Foundation
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
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: forms-lib.js,v 1.4 2004/05/11 22:50:25 joerg Exp $
 */

// Handlers that are to be called in the document's "onload" event
var forms_onloadHandlers = new Array();

function forms_onload() {
    for (var i = 0; i < forms_onloadHandlers.length; i++) {
        forms_onloadHandlers[i].forms_onload();
    }
    // Clear it (we no more need them)
    forms_onloadHandlers = null;
}

// Handlers that are to be called in form's "onsubmit" event
//FIXME: this single var implies only one form per page, and needs to be
//       visited if we decide to support several forms per page.
var forms_onsubmitHandlers = new Array();

function forms_onsubmit() {
    if (forms_onsubmitHandlers == null) {
        alert("onsubmit called twice!");
    }

    for (var i = 0; i < forms_onsubmitHandlers.length; i++) {
        forms_onsubmitHandlers[i].forms_onsubmit();
    }
    // clear it
    forms_onsubmitHandlers = null;
}

/**
 * Submit the form containing an element, also storing in the hidden
 * 'forms_submit_id' field the name of the element which triggered the submit.
 */
function forms_submitForm(element, name) {
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
        form.submit();
    }
}

/**
 * Crawl the parents of an element up to finding a form.
 */
function forms_getForm(element) {
    while(element != null && element.tagName != "FORM") {
        element = element.parentNode;
    }
    return element;
}

/**
 * Move a named element as an immediate child of the <body> element.
 * This is required for help popups inside <wi:group> tabs. The reason is that CSS positioning
 * properties ("left" and "top") on a block with a "position: absolute" are actually relative to
 * the nearest ancestor that has a position of "absolute", "relative" or "fixed".
 * See http://www.w3.org/TR/CSS21/visudet.html#containing-block-details $4
 */

function forms_moveInBody(element) {
    element.parentNode.removeChild(element);
    document.body.appendChild(element);
}

/**
 * Create a popup window for a named element.
 *
 * @param id the ID of the element to make a popup with.
 */
function forms_createPopupWindow(id) {
    var result = new PopupWindow(id);
    result.autoHide();
    // add to onload handlers
    result.forms_id = id;
    result.forms_onload = function() {
        forms_moveInBody(document.getElementById(this.forms_id));
    }
    forms_onloadHandlers.push(result);
    return result;
}


function forms_createOptionTransfer(id, submitOnChange) {
    var result = new OptionTransfer(id + ".unselected", id);
    result.setAutoSort(true);
    // add to onload handlers
    result.forms_id = id;
    result.forms_onload = function() {
        var form = forms_getForm(document.getElementById(this.forms_id));
        this.init(form);
        sortSelect(this.left);
        sortSelect(this.right);
    }
    result.submitOnChange = submitOnChange;
    result.forms_transferLeft = function() {
        this.transferLeft();
        if (this.submitOnChange) {
            forms_submitForm(document.getElementById(this.forms_id));
        }
    }
    result.forms_transferRight = function() {
        this.transferRight();
        if (this.submitOnChange) {
            forms_submitForm(document.getElementById(this.forms_id));
        }
    }
    result.forms_transferAllLeft = function() {
        this.transferAllLeft();
        if (this.submitOnChange) {
            forms_submitForm(document.getElementById(this.forms_id));
        }
    };
    result.forms_transferAllRight = function() {
        this.transferAllRight();
        if (this.submitOnChange) {
            forms_submitForm(document.getElementById(this.forms_id));
        }
    };
    forms_onloadHandlers.push(result);
    
    // add to onsubmit handlers
    result.forms_onsubmit = function() {
        // Select all options in the "selected" list to that
        // its values are sent.
        selectAllOptions(this.right);
    }
    forms_onsubmitHandlers.push(result);
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
        }
    }
    // Change state value
    if (state.length > 0) {
        document.forms[0][state].value = idx;
    }
}