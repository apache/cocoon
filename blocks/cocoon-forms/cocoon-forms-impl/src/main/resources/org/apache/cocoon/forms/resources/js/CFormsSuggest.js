/*
 * Copyright 1999-2006 The Apache Software Foundation.
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
dojo.provide("cocoon.forms.CFormsSuggest");
dojo.require("dojo.widget.html.ComboBox");

/**
 * Dojo widget for suggestion-lists. Extends Dojo's ComboBox widget.
 * Suggestion lists are fetched from the server at the "_cocoon/forms/suggest"
 * URL.
 *
 * The following parameters are passed to that URL:
 * - widget: the widget's name for which to provide suggestions
 * - continuation-id: the continuation id, used to find the form
 * - filter: what the user already typed, used to filter suggestions
 *
 * The response must be a JSON array of suggestions, each item being itself
 * a 2-item array containing a label (displayed) and a value (not displayed but
 * sent back to the server as the "{widget}_selected parameter".
 *
 * @version $Id$
 */

cocoon.forms.CFormsSuggest = function() {
    dojo.widget.html.ComboBox.call(this);
    this.widgetType = "CFormsSuggest";
}

dojo.inherits(cocoon.forms.CFormsSuggest, dojo.widget.html.ComboBox);

dojo.lang.extend(cocoon.forms.CFormsSuggest, {
    fillInTemplate: function(args, frag) {
        this.mode = "remote";
        var node = frag["dojo:"+this.widgetType.toLowerCase()]["nodeRef"];
        var form = cocoon.forms.getForm(node);
        var contId = form["continuation-id"].value;
        if (!contId) throw "Cannot find continuation Id";
        
        if (!this.dataUrl || this.dataUrl == "") {
            this.dataUrl = "_cocoon/forms/suggest?widget=" + node.getAttribute("name") +
                "&continuation-id=" + contId + "&filter=%{searchString}";
        }
        dojo.widget.html.ComboBox.prototype.fillInTemplate.apply(this, arguments);
        // Restore the initial value and the associated suggestion text, if any
        this.setValue(node.getAttribute("suggestion") ? node.getAttribute("suggestion") : node.value);
        this.setSelectedValue(node.value);
    }
})

dojo.widget.tags.addParseTreeHandler("dojo:CFormsSuggest");
// Register this module as a widget package
dojo.widget.manager.registerWidgetPackage("cocoon.forms");
