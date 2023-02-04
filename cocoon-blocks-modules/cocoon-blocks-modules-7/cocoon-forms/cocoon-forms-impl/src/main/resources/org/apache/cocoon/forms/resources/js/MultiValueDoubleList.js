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
dojo.provide("cocoon.forms.MultiValueDoubleList");

/**
 * A free-entry multivalue field editor style double list.
 *
 * Some functionality that's not visible at first sight:
 *  - items can be moved around using ctrl+up and ctrl+down.
 *  - an item can be replaced/updated by pressing ctrl+enter in the input box
 */
dojo.widget.defineWidget(
    // widget name and class
    "cocoon.forms.MultiValueDoubleList",

    // superclass
    dojo.widget.HtmlWidget,

    function() {
    },
    // properties and methods
   {
        isContainer: false,

        cformsIdPrefix: "id-prefix-not-set",
        resourcesUri: cocoon.formsResourcesUri,
        templatePath: cocoon.formsResourcesUri + "/js/templates/MultiValueDoubleList.html",

        availableListLabel : "Available",
        selectedListLabel : "Selected",
        size: "5",
        styleClass : "",

        fillInTemplate: function(args, frag) {
            cocoon.forms.MultiValueDoubleList.superclass.fillInTemplate(this, args, frag);
            // Available list options.
            this.selectLeft.addOption = function(value, text) {
                var alreadyInList = false;
                for (var i = 0; this.options.length > i && !alreadyInList; i++) {
                    if (this.options[i].value == value) {
                        alreadyInList = true;
                    }
                }

                if (!alreadyInList) {
                    this.options[this.options.length] = new Option(text, value);
                }
            }
            // Selected list options.
            this.selectRight.addOption = function(value, text) {
                var alreadyInList = false;
                for (var i = 0; this.options.length > i && !alreadyInList; i++) {
                    if (this.options[i].value == value) {
                        alreadyInList = true;
                    }
                }

                if (!alreadyInList) {
                    this.options[this.options.length] = new Option(text, value);
                }
            }

            dojo.event.connect(this.selectRight, "ondblclick", this, "_transferLeft");
            dojo.event.connect(this.transferRight, "onclick", this, "_transferRight");
            dojo.event.connect(this.selectLeft, "ondblclick", this, "_transferRight");
            dojo.event.connect(this.transferAllRight, "onclick", this, "_transferAllRight");
            dojo.event.connect(this.transferLeft, "onclick", this, "_transferLeft");
            dojo.event.connect(this.transferAllLeft, "onclick", this, "_transferAllLeft");

            dojo.event.connect(this, "addedTo", this, "_addOnSubmitHandler");
            this._readData(this.getFragNodeRef(frag));

        },

        _addOnSubmitHandler: function(parent) {
            var form = this._getForm(this);
            if (form != null) {
                dojo.event.connect("before", form,"onsubmit", this, "_selectAll");
            } else {
                dojo.debug("MultiValueDoubleList is not being added to a form -- no onSubmitHandler then.");
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
                widget = widget.parent;
            } while (widget != null)
            return null;
        },

        _readData: function(origFrag) {
            // Read data from available list options.
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
                            this.selectLeft.addOption(value, text);
                        }
                        tr = dojo.dom.nextElement(tr, "tr");
                    }
                }
                // Read data from selected list options.
                var table1 = dojo.dom.getNextSiblingElement(table, "table");
                if (table1 != null) {
                    var tbody1 = dojo.dom.firstElement(table1, "tbody");
                    if (tbody1 != null) {
                        var tr1 = dojo.dom.firstElement(tbody1, "tr");
                        while (tr1 != null) {
                            var td1 = dojo.dom.firstElement(tr1, "td");
                            var value1 = td1 != null ? dojo.dom.textContent(td1) : null;
                            var text1 = td1.nextSibling != null ? dojo.dom.textContent(td1.nextSibling) : value1;

                            if (value1 && text1) {
                                this.selectRight.addOption(value1, text1);
                            }
                            tr1 = dojo.dom.nextElement(tr1, "tr");
                        }
                    }
                }
            } else {
                dojo.debug("MultiValueDoubleList: no data table found");
            }
        },

        _selectAll: function() {
            this._selectAllOptions(this.selectRight);
        },

        _transferLeft: function (event) {
            dojo.event.browser.stopEvent(event);
            this._moveSelectedOptions(this.selectRight, this.selectLeft);
            this._update();
        },

        _transferRight: function (event) {
            dojo.event.browser.stopEvent(event);
            this._moveSelectedOptions(this.selectLeft, this.selectRight);
            this._update();
        },

        _transferAllLeft: function (event) {
            dojo.event.browser.stopEvent(event);
            this._moveAllOptions(this.selectRight, this.selectLeft);
            this._update();
        },

        _transferAllRight: function (event) {
            dojo.event.browser.stopEvent(event);
            this._moveAllOptions(this.selectLeft, this.selectRight);
            this._update();
        },

        _update: function() {
            var removedLeft = new Object();
            var removedRight = new Object();
            var addedLeft = new Object();
            var addedRight = new Object();
            var newLeft = new Object();
            var newRight = new Object();
            var originalLeftValues = new Object();
            var originalRightValues = new Object();
            var delimiter = ",";
            var right = this.selectRight;
            var left = this.selectLeft;
            var removedLeftField = null;
            var removedRightField = null;
            var addedLeftField = null;
            var addedRightField = null;
            var newLeftField = null;
            var newRightField = null;
            for (var i = 0; i < left.options.length; i++) {
                var o = left.options[i];
                newLeft[o.value] = 1;
                if (typeof(originalLeftValues[o.value]) == "undefined") {
                    addedLeft[o.value] = 1;
                    removedRight[o.value] = 1;
                    }
                }
            for (var i = 0; i < right.options.length; i++) {
                var o = right.options[i];
                newRight[o.value] = 1;
                if (typeof(originalRightValues[o.value]) == "undefined") {
                    addedRight[o.value] = 1;
                    removedLeft[o.value] = 1;
                    }
                }
            if (removedLeftField != null) {
                removedLeftField.value = this._join(removedLeft, delimiter);
            }
            if (removedRightField != null) {
                removedRightField.value = this._join(removedRight, delimiter);
            }
            if (addedLeftField != null) {
                addedLeftField.value = this._join(addedLeft, delimiter);
            }
            if (addedRightField != null) {
                addedRightField.value = this._join(addedRight, delimiter);
            }
            if (newLeftField != null) {
                newLeftField.value = this._join(newLeft, delimiter);
            }
            if (newRightField != null) {
                newRightField.value = this._join(newRight, delimiter);
            }
        },

        _moveSelectedOptions: function (from, to) {
            if (arguments.length > 3) {
                var regex = arguments[3];
                if(regex != "") {
                    this._unSelectMatchingOptions(from,regex);
                }
            }
            for(var i = 0; i < from.options.length; i++) {
                var o = from.options[i];
                if(o.selected) {
                    to.options[to.options.length] = new Option( o.text, o.value, true, o.selected);
                }
            }
            for (var i = (from.options.length-1); i >= 0; i--) {
                var o = from.options[i];
                if(o.selected) {
                    from.options[i] = null;
                }
            }
            if ((arguments.length < 3) || (arguments[2] == true)) {
                this._sortSelect(from);
                this._sortSelect(to);
            }
            from.selectedIndex = -1;
            to.selectedIndex = -1;
        },

        _moveAllOptions: function (from, to) {
            this._selectAllOptions(from);
            if(arguments.length == 2) {
                this._moveSelectedOptions(from, to);
            } else if(arguments.length == 3) {
                this._moveSelectedOptions(from, to, arguments[2]);
            } else if(arguments.length == 4) {
                this._moveSelectedOptions(from, to, arguments[2], arguments[3]);
            }
        },

        _selectAllOptions: function (obj) {
            for(var i = 0; i < obj.options.length; i++) {
                obj.options[i].selected = true;
            }
        },

        _unSelectMatchingOptions: function (obj, regex) {
            this._selectUnselectMatchingOptions(obj, regex, "unselect", false);
        },

        _selectUnselectMatchingOptions: function(obj, regex, which, only) {
            if (window.RegExp) {
                var selected1;
                var selected2;
                if(which == "select"){
                    selected1 = true;
                    selected2 = false;
                } else if (which == "unselect") {
                    selected1 = false;
                    selected2 = true;
                } else {
                      return;
                }
                var re = new RegExp(regex);
                for (var i = 0;i < obj.options.length; i++) {
                    if(re.test(obj.options[i].text)) {
                        obj.options[i].selected = selected1;
                } else {
                    if(only == true) {
                        obj.options[i].selected = selected2;
                    }
                }
              }
           }
        },

        _join: function(o, delimiter) {
            var val;
            var str="";
            for(val in o) {
                if (str.length > 0) {
                    str = str + delimiter;
                }
                str = str + val;
            }
            return str;
        },

        _sortSelect: function(obj) {
            var o = new Array();
            if(obj.options == null) {
                return;
            }
            for(var i = 0; i < obj.options.length; i++) {
                o[o.length] = new Option( obj.options[i].text, obj.options[i].value, obj.options[i].defaultSelected, obj.options[i].selected) ;
             }
             if (o.length == 0) {
                 return;
             }
             o = o.sort( function(a,b) {
                            if ((a.text + "") < (b.text + "")) {
                                return -1;
                            }
                            if ((a.text + "") > (b.text + "")) {
                                return 1;
                            }
                            return 0;
                         } );
            for(var i = 0; i < o.length; i++) {
                obj.options[i] = new Option(o[i].text, o[i].value, o[i].defaultSelected, o[i].selected);
            }
        }

    }
);
