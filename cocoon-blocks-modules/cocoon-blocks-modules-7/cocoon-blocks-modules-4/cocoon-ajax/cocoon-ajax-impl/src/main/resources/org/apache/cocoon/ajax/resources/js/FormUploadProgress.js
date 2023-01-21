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
 
 /*
    FormUploadProgress Widget
    
    This is a Dojo Widget for Cocoon that monitors the progress of uploads from a form.
    
    It works by sending periodical XMLHttp calls to Cocoon in the background, while the files are being uploaded.
    Cocoon responds with JSON data which is used to draw the progress bar and status strings.

    @version $Id$
    
 */
 
dojo.provide("cocoon.ajax.FormUploadProgress");
dojo.require("dojo.event");
dojo.require("dojo.widget.HtmlWidget");
dojo.require("dojo.lang.timing.Timer");

dojo.widget.defineWidget(
    "cocoon.ajax.FormUploadProgress",
    dojo.widget.HtmlWidget,
    {
        // override properties (these may be overridden by using the same-named attributes on the widget tag)
        background: "#BFCCDF",      // the colour of the progress bar
        color: "#336699",           // the colour of the text and border
        ready: "Ready.",            // Strings the widget needs, that may require external i18n transformation
        connecting: "Connecting ...",
        
        // widget API properties
        ns: "forms",
        widgetType: "FormUploadProgress",
        isContainer: true,
        templateString: '<div class="FormUpload">' +
                            '<div class="FormUploadUser" dojoAttachPoint="containerNode"></div>' + 
                            '<div class="FormUploadProgress" style="border:1px solid ${this.color};margin:2px;height: 16px;position:relative;">' +
                                '<div style="width:0px;background:${this.background};position:absolute;top:0;left:0;width:0;right:0;" class="bar" dojoAttachPoint="barNode">&nbsp;</div>' + 
                                '<div style="color:${this.color};position:absolute;top:0;left:0;bottom:0;right:0;background:none;padding:2px;font-size:10px;overflow:hidden;" class="data" dojoAttachPoint="msgNode">${this.ready}</div>' + 
                            '</div>' +
                         '</div>',
                        
        // DOM Nodes in the template
        containerNode: null,   // the node containing the user content
        msgNode: null,         // the node containing the progress status text
        barNode: null,         // the progress bar node
        
        // private properties
        _form: null,           // this widget's form
        _target: null,         // the form's *Form widget
        _timer: null,          // timer for periodical updates
        _busy: false,          // busy getting the status
        _delay: 2000,          // 2 secs between status calls
        
        // widget interface
        postCreate: function(){
            // work out where to attach the event listener
            this._form = dojo.dom.getFirstAncestorByTag(this.domNode, "form");
            if (this._form) {
                var dojoId = this._form.getAttribute("dojoWidgetId");
                var formWidget = dojo.widget.byId(dojoId);
                if (formWidget) { // use the *Form Widget if there is one
                    this._target = formWidget;
                    dojo.event.connect("after", this._target, "submit", this, "_startUpload");
                } else { // otherwise use the form itself
                    dojo.event.connect("after", this._form, "onsubmit", this, "_startUpload");
                }
            } else {
                throw new Error("The FormUploadProgress widget may only be used inside a form.");
            }
        },
    
        // widget interface
        destroy: function(){
            if (this._target) {
                dojo.event.disconnect("after", this._target, "submit", this, "_startUpload");
            } else if (this._form) {
                dojo.event.disconnect("after", this._form, "onsubmit", this, "_startUpload");
            }
        },

        // private widget funtions

        // event handler, should fire after the forms starts submitting
        _startUpload: function(event) {
            if (this._checkForActiveFile(this._form)) {
                this._timer = new dojo.lang.timing.Timer(this._delay);
                this._timer.onTick = dojo.lang.hitch(this, function() { this._getStatus() });
                this._timer.onStart = this._timer.onTick;
                this._timer.start();
                this.msgNode.innerHTML = this.connecting;
            }
        },

        // get the current upload status from Cocoon
        _getStatus: function() {
            if (this._busy) return; // only one request at a time
            dojo.io.bind({
                url: new dojo.uri.Uri("servlet:/system/upload/status"),
                mimetype: "text/json",
                handle: dojo.lang.hitch(this, function(type, data, evt) {
                    if (type == "load") {
                        this._update(data);
                        this._busy = false;
                    } else if (type == "error") {
                        dojo.debug("FormUploadProgress - status request failed");
                    }           
                })
            });
            this._busy = true;
        },

        // perform the display update
        _update: function(status) {
            if (!status) return;
            if (status.sent) {
                if (status.finished) {
                    this.barNode.style.width = "100%";
                    this._timer.stop();
                } else {
                    this.barNode.style.width = status.percent + "%";
                }               
            }
            if (status.message) this.msgNode.innerHTML = status.message;
        },

        // look to see if there are active (file has been chosen) file-upload fields in the form
        _checkForActiveFile: function(node) {
            var hasFile = false;
            var inputs = node.getElementsByTagName("input");
            dojo.lang.forEach(inputs, function(input){
                if(hasFile){ return; }
                if(input.getAttribute("type")=="file" && input.value){
                    hasFile = true;
                }
            });
            return hasFile;
        }

});

