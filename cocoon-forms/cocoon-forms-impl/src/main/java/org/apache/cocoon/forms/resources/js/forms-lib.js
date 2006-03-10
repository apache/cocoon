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
 * NOTE: This file will be trimmed down to contain only the necessary
 *       features for dynamic behaviour on non Ajax-capable browsers.
 *       Advanced widgets such as double selection list and multivalue
 *       field will be refactored as Dojo widgets.
 *
 * @version $Id$
 */

// Handlers that are to be called in the document's "onload" event
var forms_onloadHandlers = new Array();

function forms_onload() {
    for (var i = 0; i < forms_onloadHandlers.length; i++) {
        forms_onloadHandlers[i].forms_onload();
    }
    // Reset it (we no more need them)
    forms_onloadHandlers = new Array();
}

// Handlers that are to be called in form's "onsubmit" event
// FIXME: this single var implies only one form per page, and needs to be
//       visited if we decide to support several forms per page.
var forms_onsubmitHandlers = new Array();

function forms_onsubmit() {
    if (forms_onsubmitHandlers == null) {
        // Form already submited, but the new page is not yet loaded. This can happen when
        // the focus is in an input with an "onchange" and the user clicks on a submit button.
        return false;
    }

    for (var i = 0; i < forms_onsubmitHandlers.length; i++) {
        if (forms_onsubmitHandlers[i].forms_onsubmit() == false) {
            // handler cancels the submit
            return false;
        	}
    }
    // clear it
    forms_onsubmitHandlers = null;
    return true;
}

/**
 * Submit the form containing an element, also storing in the hidden
 * 'forms_submit_id' field the name of the element which triggered the submit.
 */
function oldforms_submitForm(element, name) {
    name = name || element.name;
    
    var form = forms_getForm(element);
    if (form == null) {
        alert("Cannot find form for " + element);
    } else {
        form["forms_submit_id"].value = name;
        if (!form.onsubmit || form.onsubmit() != false) {
            form.submit();
        }
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
    result.forms_id = id + ":input";
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

/**
 * FormsMultiValueEditor is the implementation of the free-form multivalue field editor.
 */
function FormsMultiValueEditor(id) {
    this.select = document.getElementById(id + ":input");
    this.entry = document.getElementById(id + ":entry");
    var self = this;
    this.entry.onkeypress = function(event) { return self.processInputKey(event); };
    this.select.onkeydown = function(event) { return self.processSelectKey(event); };
    this.select.onchange = function(event) { return self.processSelectChange(event); };

    var deleteEl = document.getElementById(id + ":delete");
    deleteEl.onclick = function() { self.deleteValues(); return false; };

    var upEl = document.getElementById(id + ":up");
    upEl.onclick = function() { self.moveUp(); return false; };

    var downEl = document.getElementById(id + ":down");
    downEl.onclick = function() { self.moveDown(); return false; };

    var onsubmitHandler = new Object();
    onsubmitHandler.forms_onsubmit = function () {
        self.selectAll();
    }
    forms_onsubmitHandlers.push(onsubmitHandler);
}

/**
 * Key event handler for keypresses in the input box.
 */
FormsMultiValueEditor.prototype.processInputKey = function(event) {
    if (event == null) event = window.event; // Internet Explorer
    if (event.keyCode == 13 || event.keyCode == 10) {
        var entry = this.entry;
        var select = this.select;
        var newItem = entry.value;
        if (newItem == null || newItem == "")
            return false;
        // if ctrl+enter is pressed, the first selected item is replaced with the new value
        // (otherwise, the new value is appended at the end of the list)
        var replace = event.ctrlKey;
        var newItemPos = -1;
        for (var i = 0; i < select.options.length; i++) {
            if (select.options[i].selected && replace && newItemPos == -1)
                newItemPos = i;
            select.options[i].selected = false;
        }
        if (newItemPos == -1)
            newItemPos = select.options.length;
        select.options[newItemPos] = new Option(newItem, newItem, false, true);
        entry.value = "";
        return false;
    } else {
        return true;
    }
}

/**
 * Key event handler for keypresses in the select list.
 */
FormsMultiValueEditor.prototype.processSelectKey = function(event) {
    if (event == null) event = window.event; // Internet Explorer
    // 46 = delete key
    if (event.keyCode == 46) {
        this.deleteValues();
        return false;
    } else if (event.ctrlKey && event.keyCode == 38) {
        // key up = 38
        this.moveUp();
        return false;
    } else if (event.ctrlKey && event.keyCode == 40) {
        // key down = 40
        this.moveDown();
        return false;
    }
}


FormsMultiValueEditor.prototype.deleteValues = function() {
    var options = this.select.options;
    var i = 0;
    var lastRemovedItem = -1;
    while (i < options.length) {
        if (options[i].selected) {
            options[i] = null;
            lastRemovedItem = i;
        } else {
             i++;
        }
    }

    if (lastRemovedItem != -1) {
        if (options.length > lastRemovedItem) {
            options[lastRemovedItem].selected = true;
        } else if (lastRemovedItem - 1 >= 0) {
            options[lastRemovedItem - 1].selected = true;
        }
    }
}

FormsMultiValueEditor.prototype.processSelectChange = function() {
    var options = this.select.options;
    for (var i = 0; i < options.length; i++) {
        if (options[i].selected) {
            this.entry.value = options[i].value;
            break;
        }
    }
}

FormsMultiValueEditor.prototype.moveUp = function() {
    var options = this.select.options;
    if (options.length == 0)
        return;
    if (options[0].selected)
        return;

    for (var i = 0; i < options.length; i++) {
        if (options[i].selected) {
            var prev = this.cloneOption(options[i - 1]);
            var current = this.cloneOption(options[i]);
            options[i - 1] = current;
            options[i] = prev;
        }
    }
}

FormsMultiValueEditor.prototype.cloneOption = function(option) {
    return new Option(option.text, option.value, false, option.selected);
}

FormsMultiValueEditor.prototype.moveDown = function() {
    var options = this.select.options;
    if (options.length == 0)
        return;
    if (options[options.length - 1].selected)
        return;

    for (var i = options.length - 1; i >= 0; i--) {
        if (options[i].selected) {
            var next = this.cloneOption(options[i + 1]);
            var current = this.cloneOption(options[i]);
            options[i + 1] = current;
            options[i] = next;
        }
    }
}

FormsMultiValueEditor.prototype.selectAll = function() {
    var options = this.select.options;
    for (var i = 0; i < options.length; i++) {
        options[i].selected = true;
    }
}

