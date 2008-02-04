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
dojo.require("dojo.widget.HtmlWidget");
dojo.require("cocoon.forms.MultiValueEditor");
dojo.provide("cocoon.forms.MultiValueEditorWithSuggestion");

/**
 * A free-entry multivalue field editor.
 *
 * Some functionality that's not visible at first sight:
 *  - items can be moved around using ctrl+up and ctrl+down.
 *  - an item can be replaced/updated by pressing ctrl+enter in the input box
 */
dojo.widget.defineWidget("cocoon.forms.MultiValueEditorWithSuggestion",
    cocoon.forms.MultiValueEditor,
    function() {
    },
    // properties and methods
    {
        onchange: "",
        name: "",
        widgetsInTemplate: true,
        cformsIdPrefix: "id-prefix-not-set",
        dataUrl: "_cocoon/forms/suggest",
        styleClass: "",
        popupUri: "",
        popupSize: "",
        popupLinkText: "",
        resourcesUri: cocoon.resourcesUri,
        templatePath: cocoon.resourcesUri + "/forms/js/templates/MultiValueEditorWithSuggestion.html",

        _setUpDataUrl: function() {
            var dataUrl = this.dataUrl;
            if (!dataUrl || dataUrl == "") {
                dataUrl = "_cocoon/forms/suggest/" + this.widgetId + "?filter=%{searchString}";
            } else {
                var strings = dataUrl.split("?");
                if (strings.length > 1) {
                    if (strings[0] == "_cocoon/forms/suggest") {
                        strings[0] += "/" + this.widgetId;
                    }
                    dataUrl = strings[0] + "?" + strings[1] + "&";
                } else {
                    dataUrl += "?";
                }
                dataUrl += "filter=%{searchString}";
            }
            this.dataUrl = dataUrl;
        },

        _addOnSubmitHandler: function(parent) {
            var form = this._getForm(this);
            if (form != null) {
                dojo.event.connect("before", form,"onsubmit", this, "_selectAll");
             } else {
                 dojo.debug("MultiValueEditorWithSuggestion is not being added to a form -- no onSubmitHandler then.");
             }
        },

        postCreate: function() {
            this.entry.dataUrl = this.dataUrl;
            this.entry.dataProvider.searchUrl = this.dataUrl;
            dojo.event.connect("after", this.entry, "_handleKeyEvents", this, "_processInputKey");
        },

        _readData: function(origFrag) {
            var table = dojo.dom.getFirstChildElement(origFrag, "table");
            if (table != null) {
                var tbody = dojo.dom.firstElement(table, "tbody");
                if (tbody != null) {
                    var tr = dojo.dom.firstElement(tbody, "tr");
                    while (tr != null) {
                        var td = dojo.dom.firstElement(tr, "td");
                        var value = td != null ? dojo.dom.textContent(td) : null;
                        var text = td.nextSibling != null ? dojo.dom.textContent(td.nextSibling) : value;

                        if (value && text) {
                            this.select.addOption(value, text);
                        }
                        tr = dojo.dom.nextElement(tr, "tr");
                    }
                }
            } else {
                dojo.debug("MultiValueEditorWithSuggestion: no data table found");
            }
        },

        _processInputKey: function(event) {
            var k = dojo.event.browser.keys;
            switch (event.key){
                case k.KEY_ENTER:
                    if (this.entry.comboBoxSelectionValue.value && this.entry.comboBoxValue.value) {
                        this.select.addOption(this.entry.comboBoxSelectionValue.value, this.entry.comboBoxValue.value);
                    }
                    dojo.event.browser.stopEvent(event);
                    this.entry.setValue("");
                    this.entry.setSelectedValue("");
                    break;
            }
        },

        fillInTemplate: function(args, frag) {
            cocoon.forms.MultiValueEditor.superclass.fillInTemplate(this, args, frag);

            this._setUpDataUrl();

            if (!this.popupSize || this.popupSize == "") {
                this.popupSize = "400,450";
            }

            this.select.addOption = function(value, text) {
                var alreadyInList = false;
                for (var i = 0; this.options.length > i && !alreadyInList; i++) {
                    if (this.options[i].value == value) {
                        alreadyInList = true;
                    }
                }

                if (!alreadyInList) {
                    this.options[this.options.length] = new Option(text, value, true, true);
                }
            }

            dojo.event.connect(this.select, "onkeydown", this, "_processSelectKey");
            dojo.event.connect(this.select, "onchange", this, "_processSelectChange");

            dojo.event.connect(this.deleteButton, "onclick", this, "_deleteValues");
            dojo.event.connect(this.moveUpButton, "onclick", this, "_moveUp");
            dojo.event.connect(this.moveDownButton, "onclick", this, "_moveDown");

            if (this.linkButton) {
                if (this.popupUri != "" && this.popupLinkText != "") {
                    dojo.event.connect(this.linkButton, "onclick", this, "openPopup");
                } else {
                    dojo.dom.destroyNode(this.linkButton);
                }
            }

            dojo.event.connect(this, "addedTo", this, "_addOnSubmitHandler");
            this._readData(this.getFragNodeRef(frag));
        },

        openPopup: function() {
            if (!this.popupWindow) {
                this.popupWindow = new PopupWindow();
                var values = this.popupSize.split(",");
                this.popupWindow.setSize(parseInt(values[0]), parseInt(values[1]));
                this.popupWindow.setWindowProperties("scrollbars=yes,resizable=yes");
                this.popupWindow.name = this.widgetId;
                this.popupWindow.showPopup = __showPopup;
                if (this.popupUri.indexOf("?") == -1) {
                    this.popupUri += "?"
                } else {
                    this.popupUri += "&";
                }
                this.popupUri += "caller=" + this.widgetId +  ":input";
                this.popupWindow.setUrl(this.popupUri);
            }
            this.popupWindow.showPopup(this.linkButton.id);
        }
    }
);

function __showPopup(anchorname) {
    this.getXYPosition(anchorname);
    this.x += this.offsetX;
    this.y += this.offsetY;
    if (!this.populated && (this.contents != "")) {
        this.populated = true;
        this.refresh();
    }
    if (this.divName != null) {
        // Show the DIV object
        if (this.use_gebi) {
            document.getElementById(this.divName).style.left = this.x + "px";
            document.getElementById(this.divName).style.top = this.y + "px";
            document.getElementById(this.divName).style.visibility = "visible";
        } else if (this.use_css) {
            document.all[this.divName].style.left = this.x;
            document.all[this.divName].style.top = this.y;
            document.all[this.divName].style.visibility = "visible";
        } else if (this.use_layers) {
            document.layers[this.divName].left = this.x;
            document.layers[this.divName].top = this.y;
            document.layers[this.divName].visibility = "visible";
        }
    } else {
        if (this.popupWindow == null || this.popupWindow.closed) {
            // If the popup window will go off-screen, move it so it doesn't
            if (this.x < 0) {
                this.x = 0;
            }
            if (this.y < 0) {
                this.y = 0;
            }
            if (screen && screen.availHeight) {
                if ((this.y + this.height) > screen.availHeight) {
                    this.y = screen.availHeight - this.height;
                }
            }
            if (screen && screen.availWidth) {
                if ((this.x + this.width) > screen.availWidth) {
                    this.x = screen.availWidth - this.width;
                }
            }
            var avoidAboutBlank = window.opera || ( document.layers && !navigator.mimeTypes['*'] ) || navigator.vendor == 'KDE' || ( document.childNodes && !document.all && !navigator.taintEnabled );
            this.popupWindow = window.open(avoidAboutBlank?"":"about:blank","window_"+ this.name,this.windowProperties+",width="+this.width+",height="+this.height+",screenX="+this.x+",left="+this.x+",screenY="+this.y+",top="+this.y+"");
        }
        this.refresh();
	}
}