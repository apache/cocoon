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

dojo.require("dijit._Widget");
dojo.require("cocoon.forms.common");
dojo.requireLocalization("dijit", "loading", null, "ar,cs,da,de,el,es,fi,fr,he,hu,it,ja,ko,nb,nl,pl,pt,pt-pt,ru,sv,tr,ROOT,zh,zh-tw"); // bummer!


/**
 * Dojo widget for Cocoon Forms, that handles full-page submits to the server.
 *
 * Extends dijit._Widget.
 *
 * NOTE: Introduced in 2.1.11, replaces functionality in forms-lib.js
 *
 * @version $Id$
 */

dojo.declare("cocoon.forms.SimpleForm", dijit._Widget, {
    
    duration: 2000, // default fade-in duration in milliseconds
    loadingMessage: "<span class='cformsLoading dijitContentPaneLoading'>${loadingState}</span>", 
    
    buildRendering: function() {
        this.inherited(arguments);
        // if the form is invisible (there was @class="fadeIn" on the form in the Template), draw the loader animation
        if (dojo.hasClass(this.domNode, "fadeIn")) {
            dojo.require("dojo.i18n");
            dojo.require("dojo.string");
            var messages = dojo.i18n.getLocalization("dijit", "loading", this.lang);
		    this.loadingMessage = dojo.string.substitute(this.loadingMessage, messages);
            this.loadingNode = dojo.doc.createElement('div');
            this.loadingNode.innerHTML = this.loadingMessage;
            this.domNode.parentNode.appendChild(this.loadingNode);
        }
    },
    
    /** 
     *  dijit.Widget interface
     *  Hook up our events and onSubmit handler
     */
    postCreate: function() {
        this.inherited(arguments);
        this.id = this.domNode.getAttribute("id"); // why do we need to do this?
        if (!this.id && console) console.warn("WARNING: IDs on forms are now required, this form may not work properly.");
        // make getDescendants() work
        if(!this.containerNode) this.containerNode = this.domNode; 
        // set the form's onsubmit handler to call this.submit
        // pick up the User's optional onSubmit handler (Added in the CForms Template)
        this.userOnSubmit = this.domNode["onsubmit"]; 
        
        // replace the onSubmit handler with our own
        this.domNode["onsubmit"] = dojo.hitch(this, function() {
            // submit the form using the cocoon.forms.*Form Widget's handlers
            this.submit(this.lastClickTarget && this.lastClickTarget.name); 
            return false; // no further submits
        });
        // capture the click target
        this.connect(this.domNode, "onclick", "_grabClickTarget"); 
    },
    
    /** 
     *  dijit.Widget interface
     *  Reveal the form if it was rendered hidden (there was @class="fadeIn" on the form in the Template)
     */
    startup: function() {
        this.inherited(arguments);
        var loading = this.loadingNode;
        dojo.fadeIn({
            node:this.domNode, 
            duration: this.duration,
            onEnd: function() { if (loading) loading.parentNode.removeChild(loading); }
        }).play(); // ready to reveal the form in all it's glory
    },
    
    /** 
     * Connected to the 'onclick' event to capture the clicked element 
     * Keep targets of onclick so that we can know what input triggered the submit
     * (the event in onsubmit() is the HTMLFormElement).
     */
    _grabClickTarget: function(event) {
        this.lastClickTarget = event.target;
    },
    
    getOnSubmitTopic: function() {
        return this.id + "_cforms_onSubmit";
    },

    getAfterSubmitTopic: function() {
        return this.id + "_cforms_afterSubmit";
    },

    /**
     * Submit the form, full page request.
     *
     * @param name the name of the widget that triggered the submit (if any)
     * @param params an object containing additional parameters to be added to the request (optional)
     *
     */
    submit: function(name, params) {
        // Widgets subscribing to this topic get called first, but cannot stop form submit
        dojo.publish(this.getOnSubmitTopic()); 
        // The user supplied onSubmit handler may stop submit (only for full page submits)
        if (dojo.isFunction(this.userOnSubmit) ) {
            if (this.userOnSubmit() === false) return false; 
        }
        cocoon.forms.fullPageSubmit(this.domNode, name, params || {});
        // Toggle the click target off, so it does not get resubmitted if another submit is fired before this has finished
        if (this.domNode[name]) this.domNode[name].disabled = true;
        dojo.publish(this.getAfterSubmitTopic()); 
    }
});
