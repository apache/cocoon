/**
 * Runtime JavaScript library for Woody.
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: woody-lib.js,v 1.2 2003/11/19 16:54:02 sylvain Exp $
 */

// Handlers that are to be called in the document's "onload" event
var woody_onloadHandlers = new Array();

function woody_onload() {
    for (var i = 0; i < woody_onloadHandlers.length; i++) {
        woody_onloadHandlers[i].woody_onload();
    }
    // Clear it (we no more need them)
    woody_onloadHandlers = null;
}

// Handlers that are to be called in form's "onsubmit" event
//FIXME: this single var implies only one woody form per page, and needs to be
//       visited if we decide to support several forms per page.
var woody_onsubmitHandlers = new Array();

function woody_onsubmit() {
    if (woody_onsubmitHandlers == null) {
        alert("onsubmit called twice!");
    }

    for (var i = 0; i < woody_onsubmitHandlers.length; i++) {
        woody_onsubmitHandlers[i].woody_onsubmit();
    }
    // clear it
    woody_onsubmitHandlers = null;
}

/**
 * Submit the form containing an element, also storing in the hidden
 * 'woody_submit_id' field the name of the element which triggered the submit.
 */
function woody_submitForm(element, name) {
    if (name == undefined) {
        name = element.name;
    }
    
    var form = woody_getForm(element);
    if (form == null) {
        alert("Cannot find form for " + element);
    } else {
        form["woody_submit_id"].value = name;
        // FIXME: programmatically submitting the form doesn't trigger onsubmit ? (both in IE and Moz)
        woody_onsubmit();
        form.submit();
    }
}

/**
 * Crawl the parents of an element up to finding a form.
 */
function woody_getForm(element) {
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
 * See http://www.w3.org/TR/CSS21/visudet.html#containing-block-details �4
 */

function woody_moveInBody(element) {
    element.parentNode.removeChild(element);
    document.body.appendChild(element);
}

/**
 * Create a popup window for a named element.
 *
 * @param id the ID of the element to make a popup with.
 */
function woody_createPopupWindow(id) {
    var result = new PopupWindow(id);
    result.autoHide();
    // add to onload handlers
    result.woody_id = id;
    result.woody_onload = function() {
        woody_moveInBody(document.getElementById(this.woody_id));
    }
    woody_onloadHandlers.push(result);
    return result;
}


function woody_createOptionTransfer(id) {
    var result = new OptionTransfer(id + ".unselected", id);
    result.setAutoSort(true);
    // add to onload handlers
    result.woody_id = id;
    result.woody_onload = function() {
        var form = woody_getForm(document.getElementById(this.woody_id));
        this.init(form);
        sortSelect(this.left);
        sortSelect(this.right);
    }
    woody_onloadHandlers.push(result);
    
    // add to onsubmit handlers
    result.woody_onsubmit = function() {
        // Select all options in the "selected" list to that
        // its values are sent.
        selectAllOptions(this.right);
    }
    woody_onsubmitHandlers.push(result);
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
function woody_showTab(tabgroup, idx, length, state) {
    for (var i = 0; i < length; i++) {
        // Change tab status (selected/unselected)
        var tab = document.getElementById(tabgroup + "_tab_" + i);
        if (tab != null) {
            tab.className = (i == idx) ? 'woody-tab woody-activeTab': 'woody-tab';
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
