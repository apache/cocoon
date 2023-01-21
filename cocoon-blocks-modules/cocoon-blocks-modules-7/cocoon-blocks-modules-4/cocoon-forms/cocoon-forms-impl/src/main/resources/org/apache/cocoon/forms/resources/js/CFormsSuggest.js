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
dojo.provide("cocoon.forms.CFormsSuggest");
dojo.require("dojo.widget.ComboBox");

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

dojo.widget.defineWidget(
    "cocoon.forms.CFormsSuggest",
    dojo.widget.ComboBox, {

    // Widget definition
    ns: "forms",
    widgetType: "CFormsSuggest",
    // properties
    onchange: "",
    name: "",

    fillInTemplate: function(args, frag) {
        this.mode = "remote";
        var node = this.getFragNodeRef(frag);
        var form = cocoon.forms.getForm(node);
        this.form = form;
        var contId = form["continuation-id"].value;
        if (!contId) throw "Cannot find continuation Id";

        if (!this.dataUrl || this.dataUrl == "") {
            this.dataUrl = "_cocoon/forms/suggest?widget=" + node.getAttribute("name") +
                "&continuation-id=" + contId + "&filter=%{searchString}";
        }
        cocoon.forms.CFormsSuggest.superclass.fillInTemplate.call(this, args, frag);
        if (node.value) {
            // Get the suggestion text from the server
            this.getData(this, "_cocoon/forms/suggest?widget=" + node.getAttribute("name") + 
                    "&continuation-id=" + contId + "&filter=" + node.value + "&phase=init", node);
        } else {
            // Restore the initial value and the associated suggestion text, if any
            this.setValue(node.getAttribute("suggestion") ? node.getAttribute("suggestion") : node.value);
            this.setSelectedValue(node.value);
        }
    },

    onValueChanged: function(/*String*/ value){
        if (this.onchange == "cocoon.forms.submitForm(this)") {
            cocoon.forms.submitForm(this.domNode, this.name);
        }
    },

    getData: function(widget, url, node) {
        dojo.io.bind({
            url: url,
            load: dojo.lang.hitch(this, function(type, data, evt){ 
                if(!dojo.lang.isArray(data)){
                    var arrData = [];
                    for(var key in data){
                        arrData.push([data[key], key]);
                    }
                    data = arrData;
                }
                // suggestion text
                widget.setValue(data[0][0]);
                // numeric value
                widget.setSelectedValue(node.value);
            }),
            mimetype: "text/json"
        });
    }
});
