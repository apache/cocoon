/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
dojo.provide("cocoon.forms.SimpleForm");
dojo.require("dojo.event");
dojo.require("dojo.widget.DomWidget");
dojo.require("cocoon.forms.common");


/**
 * Dojo widget for Cocoon Forms, that handles full-page submits to the server.
 *
 * Extends the base DomWidget class. We don't need all the HtmlWidget stuff
 * but need traversal of the DOM to build child widgets
 *
 * NOTE: Introduced in 2.1.11, replaces functionality in forms-lib.js
 *
 * @version $Id$
 */

dojo.widget.defineWidget(
    "cocoon.forms.SimpleForm",
    dojo.widget.DomWidget,
    {

    // widget properties
    ns: "forms",
    widgetType: "SimpleForm",
    isContainer: true,
    preventClobber: true, // don't clobber our form node (this.domNode)
        
    // widget interface
    fillInTemplate: function(args, frag) {
        this.id = this.domNode.getAttribute("id"); 
        if (!this.id) dojo.debug("WARNING: IDs on forms are now required, this form may not work properly.");
        this.domNode.setAttribute("dojoWidgetId", this.widgetId); // mark this node as a widget impl of a form
        dojo.event.connect("around", this.domNode, "onsubmit", this, "_browserSubmit");
        dojo.event.connect(this.domNode, "onclick", this, "_grabClickTarget"); 
    },
    
    /** 
     * Connected to the 'onclick' event to capture the clicked element 
     * Keep targets of onclick so that we can know what input triggered the submit
     * (the event in onsubmit() is the HTMLFormElement).
     */
    _grabClickTarget: function(event) {
        this.lastClickTarget = dojo.html.getEventTarget(event);
    },

    /** 
     * Connected to the forms 'onsubmit' event 
     * called when the user clicks a submit input 
     * Calls the user's optional @onsubmit handler on the form tag
     * If real submit has to occur, it's taken care of in this.submit()
     * We always return false, to stop the native submit from running
     *
     */
    _browserSubmit: function(onSubmitEvent) {
        if (onSubmitEvent.proceed() == false) { 
            // onsubmit handlers stopped submission
            return false;
        }
        this.submit(this.lastClickTarget && this.lastClickTarget.name); // submit the form
        return false;
    },

    /**
     * Submit the form, full page request.
     *
     * @param name the name of the widget that triggered the submit (if any)
     * @param params an object containing additional parameters to be added to the request (optional)
     *
     */
    submit: function(name, params) {
        dojo.debug("SimpleForm.submit");
        if (!params) params = {};
        cocoon.forms.fullPageSubmit(this.domNode, name, params);
        // Toggle the click target off, so it does not get resubmitted if another submit is fired before this has finished
        if (this.domNode[name]) this.domNode[name].disabled = true;
    }
});
