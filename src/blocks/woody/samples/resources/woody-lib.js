/**
 * Runtime JavaScript library for Woody.
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: woody-lib.js,v 1.1 2003/11/18 22:45:28 sylvain Exp $
 */

var woody_initHandlers = new Array();

function woody_init() {
    for (var i = 0; i < woody_initHandlers.length; i++) {
        woody_initHandlers[i].handle();
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
        form.submit();
    }
}

/**
 * Move a named element as an immediate child of the <body> element.
 * This is required for help popups inside <wi:group> tabs. The reason is that CSS positioning
 * properties ("left" and "top") on a block with a "position: absolute" are actually relative to
 * the nearest ancestor that has a position of "absolute", "relative" or "fixed".
 * See http://www.w3.org/TR/CSS21/visudet.html#containing-block-details §4
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
    woody_initHandlers.push(new woody_moveInBodyHandler(id));
    var result = new PopupWindow(id);
    result.autoHide();
    return result;
}

function woody_moveInBodyHandler(id) {
    this.id = id;
    this.handle = function() {
        var element = document.getElementById(this.id);
        element.parentNode.removeChild(element);
        document.body.appendChild(element);
    }
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
