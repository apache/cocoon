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
dojo.provide("cocoon.forms.MultiValueEditor");

/**
 * A free-entry multivalue field editor.
 *
 * Some functionality that's not visible at first sight:
 *  - items can be moved around using ctrl+up and ctrl+down.
 *  - an item can be replaced/updated by pressing ctrl+enter in the input box
 */
dojo.widget.defineWidget(
    // widget name and class
    "cocoon.forms.MultiValueEditor",

    // superclass
    dojo.widget.HtmlWidget,

    function() {
    },

    // properties and methods
    {
        isContainer: false,

        cformsIdPrefix: "id-prefix-not-set",

        resourcesUri: cocoon.resourcesUri, // to make this available to the template

        templatePath: cocoon.resourcesUri + "/forms/js/templates/MultiValueEditor.html",

        fillInTemplate: function(args, frag) {
            cocoon.forms.MultiValueEditor.superclass.fillInTemplate(this, args, frag);

            dojo.event.connect(this.entry, "onkeypress", this, "_processInputKey");
            dojo.event.connect(this.select, "onkeydown", this, "_processSelectKey");
            dojo.event.connect(this.select, "onchange", this, "_processSelectChange");

            dojo.event.connect(this.deleteButton, "onclick", this, "_deleteValues");
            dojo.event.connect(this.moveUpButton, "onclick", this, "_moveUp");
            dojo.event.connect(this.moveDownButton, "onclick", this, "_moveDown");

            dojo.event.connect(this, "addedTo", this, "_addOnSubmitHandler");

            this._readData(this.getFragNodeRef(frag));
        },

        /**
         * Reads the data for the select-list from a table in the original widget-defining element.
         */
        _readData: function(origFrag) {
            var table = dojo.dom.getFirstChildElement(origFrag, "table");
            if (table != null) {
                var tbody = dojo.dom.firstElement(table, "tbody");
                if (tbody != null) {
                    var tr = dojo.dom.firstElement(tbody, "tr");
                    while (tr != null) {
                        var td = dojo.dom.firstElement(tr, "td");
                        if (td != null) {
                            var text = dojo.dom.textContent(td);
                            this.select.options[this.select.options.length] = new Option(text, text);
                        }
                        tr = dojo.dom.nextElement(tr, "tr");
                    }
                }
            } else {
                dojo.debug("MultiValueEditor: no data table found");
            }
        },

        _addOnSubmitHandler: function(parent) {
            var form = this._getForm(this);
            if (form != null) {
                var onSubmitHandler = {};
                onSubmitHandler.forms_onsubmit = dojo.lang.hitch(this, "_selectAll");
                cocoon.forms.addOnSubmitHandler(form, onSubmitHandler);
            } else {
                dojo.debug("MultiValueEditor is not being added to a form -- no onSubmitHandler then.");
            }
        },

        /**
         * Finds the HTML form to which a widget belongs.
         * The widget is passed as an argument since this might become a generic
         * utility function outside of this wiget.
         */
        _getForm: function(widget) {
            do {
                if (widget.domNode != null && widget.domNode.tagName != null && widget.domNode.tagName.toLowerCase() == "form") {
                    return widget.domNode;
                }
                widget = widget.parent; // parent property points to the parent widget
            } while (widget != null)
            return null;
        },

        /**
         * Key event handler for keypresses in the input box.
         */
        _processInputKey: function(event) {
            if (event.keyCode == 13 || event.keyCode == 10) {
                dojo.event.browser.stopEvent(event);
                var entry = this.entry;
                var select = this.select;
                var newItem = entry.value;
                if (newItem == null || newItem == "")
                    return;
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
            }
        },

        /**
         * Key event handler for keypresses in the select list.
         */
        _processSelectKey: function(event) {
            // 46 = delete key
            if (event.keyCode == 46) {
                dojo.event.browser.stopEvent(event);
                this._deleteValues();
            } else if (event.ctrlKey && event.keyCode == 38) {
                dojo.event.browser.stopEvent(event);
                // key up = 38
                this._moveUp();
            } else if (event.ctrlKey && event.keyCode == 40) {
                dojo.event.browser.stopEvent(event);
                // key down = 40
                this._moveDown();
            }
        },

        _deleteValues: function(event) {
            if (event) dojo.event.browser.stopEvent(event);
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
        },

        _processSelectChange: function(event) {
            if (event) dojo.event.browser.stopEvent(event);
            var options = this.select.options;
            for (var i = 0; i < options.length; i++) {
                if (options[i].selected) {
                    this.entry.value = options[i].value;
                    break;
                }
            }
        },

        _moveUp: function(event) {
            if (event) dojo.event.browser.stopEvent(event);
            var options = this.select.options;
            if (options.length == 0)
                return;
            if (options[0].selected)
                return;

            for (var i = 0; i < options.length; i++) {
                if (options[i].selected) {
                    var prev = this._cloneOption(options[i - 1]);
                    var current = this._cloneOption(options[i]);
                    options[i - 1] = current;
                    options[i] = prev;
                }
            }
        },


        _cloneOption: function(option) {
            return new Option(option.text, option.value, false, option.selected);
        },

        _moveDown: function(event) {
            if (event) dojo.event.browser.stopEvent(event);
            var options = this.select.options;
            if (options.length == 0)
                return;
            if (options[options.length - 1].selected)
                return;

            for (var i = options.length - 1; i >= 0; i--) {
                if (options[i].selected) {
                    var next = this._cloneOption(options[i + 1]);
                    var current = this._cloneOption(options[i]);
                    options[i + 1] = current;
                    options[i] = next;
                }
            }
        },

        _selectAll: function() {
            var options = this.select.options;
            for (var i = 0; i < options.length; i++) {
                options[i].selected = true;
            }
        }
    }
);