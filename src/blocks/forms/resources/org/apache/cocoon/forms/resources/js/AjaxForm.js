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


dojo.declare("cocoon.forms.AjaxForm", cocoon.forms.SimpleForm, {

    /**
     * Submit the form via Ajax.
     * Choose the right transport depending on the widgets in the form and the browser's capabilities.
     *
     * @param name the name of the widget that triggered the submit (if any)
     * @param content an object containing additional parameters to be added to the form data (optional)
     */
    submit: function(name, content) {
        // Provide feedback that something is happening.
        document.body.style.cursor = "wait";
        // Call child Widget's onSubmit function
        dojo.publish(this.getOnSubmitTopic()); 
        // The "ajax-action" attribute specifies an alternate submit location used in Ajax mode.
        // This allows to use Ajax in the portal where forms are normally posted to the portal URL.
        var url = this.domNode.getAttribute("ajax-action") || this.domNode.action || document.location;
        content = content || {};
        // ROFL, we are using every type of delimiter in standard CForms fields, legacy legacy :) (JQ)
        content["forms_submit_id"] = name;                      /* name of the button doing the submit */
        content["cocoon-ajax"] = true;                          /* tell Cocoon we want AJAX-style browser updates */
        content["dojo.transport"] = "xmlhttp";                  /* tell Cocoon we are using xmlhttp transport */
        // prepare the basic IOArgs
        var xhrArgs = {
            url: url,
            content: content,
            form: this.domNode,
            handleAs: "xml",
            handle: dojo.hitch(this, function(response, ioArgs) { this._handleBrowserUpdate(this, name, response, ioArgs); })
        };
        // choose a transport
        if (this.checkForActiveFile(this.domNode)) {            /* check for file-upload fields */
            dojo.require("dojo.io.iframe");                     /* using IframeIO as we have file-upload fields */
            xhrArgs.handleAs = "html";                          /* a different mime-type is required for IframeIO */
            xhrArgs.content["dojo.transport"] = "iframe";       /* a different transport id is required for IframeIO */
            dojo.io.iframe.send(xhrArgs);                       /* send the AJAX Request via iframe IO */
            // TODO: weird bug with Safari, does not send 'cocoon-ajax' or 'dojo.transport' the second time the form is posted
        } else {
            xhrArgs.contentType = "application/x-www-form-urlencoded; charset=UTF-8";
            dojo.xhrPost(xhrArgs);                              /* send the AJAX Request via xhr IO */
        }
        // Toggle the click target off, so it does not get resubmitted if another submit is fired before this has finished
        // NB. This must be done after the form is assembled by dojo, or certain onChange handlers may fail
        // Avoid the use of this.lastClickTarget as it may already be out of date
        if (this.domNode[name]) { this.domNode[name].disabled = true; }
        dojo.publish(this.getAfterSubmitTopic());
    },

    /**
     * Handle the server's BrowserUpdate response.
     * Update the part of the form referenced by ids in the reponse.
     */
    _handleBrowserUpdate: function(widget, name, response, ioArgs) {
        _response = response; // debug
        // Restore normal cursor
        document.body.style.cursor = "auto";
        // Attempt to re-enable the click target
        if (this.domNode[name]) this.domNode[name].disabled = false;
        // get a BrowsewrUpdateHandler which will replace the updated parts of the form
        var updater = new cocoon.ajax.BUHandler();
        if (response instanceof Error && response.dojoType == "timeout") {
            updater.handleError("Request timed out", response);
        } else if (response instanceof Error) {
            updater.handleError("Request failed", response);
        } else if (!response) {
            cocoon.ajax.BUHandler.handleError("No xml answer");
        } else {
            // add the continue handler for CForms
            updater.handlers['continue'] = function() { widget._continue(); };
            // Handle browser update directives
            updater.processResponse(response);
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
    },
    
    checkForActiveFile: function(node) {
        var hasFile = false;
        var inputs = node.getElementsByTagName("input");
        dojo.forEach(inputs, function(input){
            if(hasFile){ return; }
            if(input.getAttribute("type")=="file" && !input.disabled && input.value){
                hasFile = true;
            }
        });
        return hasFile;
    }
});
