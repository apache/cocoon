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
dojo.provide("cocoon.forms.AjaxForm");
dojo.require("cocoon.forms.SimpleForm");
dojo.require("cocoon.ajax.BUHandler");


/**
 * Dojo widget for CForms, that handles the Ajax interaction with the server.
 *
 * Extends cocoon.forms.SimpleForm with Ajax behaviour
 *
 * NOTE: Introduced in 2.1.11
 *
 * @version $Id$
 */

dojo.widget.defineWidget(
    "cocoon.forms.AjaxForm",
    cocoon.forms.SimpleForm,
    {

    // widget properties
    widgetType: "AjaxForm",

    /**
     * Submit the form via Ajax.
     * Choose the right transport depending on the widgets in the form and the browser's capabilities.
     *
     * @param name the name of the widget that triggered the submit (if any)
     * @param params an object containing additional parameters to be added to the form data (optional)
     */
    submit: function(name, params) {
        var form = this.domNode;                                /* the form node */
        var mimetype = "text/xml";                              /* the default mime-type */
        if (!params) params = {};                               /* create if not passed */
        
        // TODO: should CForm's onSubmit handlers be called for Ajax events ?
        //if (cocoon.forms.callOnSubmitHandlers(form)) == false) return; /* call CForm's onSubmit handlers */
        
        // Provide feedback that something is happening.
        document.body.style.cursor = "wait";
        
        // The "ajax-action" attribute specifies an alternate submit location used in Ajax mode.
        // This allows to use Ajax in the portal where forms are normally posted to the portal URL.
        var uri = form.getAttribute("ajax-action");
        if (!uri) uri = form.action;
        if (uri == "") uri = document.location;

        params["forms_submit_id"] = name;                       /* name of the button doing the submit */
        params["cocoon-ajax"] = true;                           /* tell Cocoon we want AJAX-style browser updates */
        if (dojo.io.formHasFile(form)) {                        /* check for file-upload fields */
            dojo.require("dojo.io.IframeIO");                   /* using IframeIO as we have file-upload fields */
            mimetype = "text/html";                             /* a different mime-type is required for IframeIO */
        }

        dojo.io.bind({
            url: uri,
            handle: dojo.lang.hitch(this, function(type, data) { this._handleBrowserUpdate(this, name, type, data) }),
            method: "post",
            mimetype: mimetype,                                 /* the mimetype of the response */
            content: params,                                    /* add extra params to the form */
            formNode: form,                                     /* the form */
            sendTransport: true                                 /* tell cocoon what transport we are using */
        });
        // Toggle the click target off, so it does not get resubmitted if another submit is fired before this has finished
        // NB. This must be done after the form is assembled by dojo, or certain onChange handlers may fail
        // Avoid the use of widget.lastClickTarget as it may already be out of date
        if (form[name]) form[name].disabled = true;
    },
        
    /**
     * Handle the server's BrowserUpdate response.
     * Update the part of the form referenced by ids in the reponse.
     */
    _handleBrowserUpdate: function(widget, name, type, data) {
        // Restore normal cursor
        document.body.style.cursor = "auto";
        // Attempt to re-enable the click target
        if (this.domNode[name]) this.domNode[name].disabled = false;
        // get a BrowsewrUpdateHandler which will replace the updated parts of the form
        var updater = new cocoon.ajax.BUHandler();
        if (type == "load") {
            if (!data) {
                cocoon.ajax.BUHandler.handleError("No xml answer", data);
                return;
            }
            // add the continue handler for CForms
            updater.handlers['continue'] = function() { widget._continue(); } 
            // Handle browser update directives
            updater.processResponse(data);
        } else if (type == "error") {
            updater.handleError("Request failed", data);
        } else {
            dojo.debug("WARNING: dojo.io.bind returned an unhandled state : " + type);
        }
    },
    
    /**
     * Handle the server continue message.
     * The server is signalling in a BrowserUpdate response that the CForm is finished.
     * Return an acknowledgement to the continuation so cocoon.sendForm may complete.
     */
    _continue: function() {
        var form = this.domNode;
        if (form.method.toLowerCase() == "post") {
            // Create a fake form and post it
            var div = document.createElement("div");
            var content = "<form action='" + form.action + "' method='POST'>" +
                                "<input type='hidden' name='cocoon-ajax-continue' value='true'/>";
            if (form.elements["continuation-id"]) {
                content += "<input type='hidden' name='continuation-id' value='" +
                        form.elements["continuation-id"].value + "'/>";
            }
            content += "</form>";
            div.innerHTML = content;
            document.body.appendChild(div);
            div.firstChild.submit();
        } else {            
            // Redirect to the form's action URL
            var contParam = '?cocoon-ajax-continue=true';
            if (form.elements["continuation-id"]) {
                contParam += "&continuation-id=" + form.elements["continuation-id"].value;
            }
            window.location.href = form.action + contParam;
        }
    }
});

/**
 * Override built-in dojo function, we do not care about 'file' fields that are disabled.
 * We overide because dojo will call this during form submission and we do not want it
 * to be bothered by disabled file fields. Hopefully this can be added to dojo .....
 */
dojo.io.checkChildrenForFile = function(node) {
    var hasFile = false;
    var inputs = node.getElementsByTagName("input");
    dojo.lang.forEach(inputs, function(input){
        if(hasFile){ return; }
        if(input.getAttribute("type")=="file" && !input.disabled && input.value){
            hasFile = true;
        }
    });
    return hasFile;
}
