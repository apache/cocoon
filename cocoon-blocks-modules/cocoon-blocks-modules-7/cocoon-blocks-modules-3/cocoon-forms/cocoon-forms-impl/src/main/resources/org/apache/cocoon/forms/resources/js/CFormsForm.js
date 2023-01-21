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
dojo.provide("cocoon.forms.CFormsForm");
dojo.require("dojo.event");
dojo.require("dojo.widget.DomWidget");
dojo.require("cocoon.forms.common");
dojo.require("cocoon.ajax.BUHandler");


/**
 * DEPRECATED: 2.1.11, in favour of SimpleForm and AjaxForm
 *
 * Dojo widget for forms, that handles the Ajax interaction with the server.
 *
 * Extends the base DomWidget class. We don't need all the HtmlWidget stuff
 * but need traversal of the DOM to build child widgets
 *
 * @version $Id$
 */

dojo.widget.defineWidget(
    "cocoon.forms.CFormsForm",
    dojo.widget.DomWidget,
    {

    // properties
    ns: "forms",
    widgetType: "CFormsForm",
    isContainer: true,
        
    // Widget definition
    buildRendering: function(args, frag) {
        this.domNode = this.getFragNodeRef(frag);
        this.id = this.domNode.getAttribute("id");
        this.domNode.setAttribute("dojoWidgetId", this.widgetId);
        dojo.event.connect("around", this.domNode, "onsubmit", this, "_browserSubmit");
        dojo.event.connect(this.domNode, "onclick", this, "_grabClickTarget");
        dojo.debug("DEPRECATED: cocoon.forms.CFormsForm - Please use cocoon.forms.AjaxForm instead.");
    },

    _grabClickTarget: function(event) {
        // Keep targets of onclick so that we can know what input triggered the submit
        // (the event in onsubmit() is the HTMLFormElement).
        this.lastClickTarget = dojo.html.getEventTarget(event);
    },

    /** Connected to the forms 'onsubmit' event, called when the user clicks a submit input */
    _browserSubmit: function(invocation) {
        if (invocation.proceed() == false) {
            // onsubmit handlers stopped submission
            return false;
        }
        var event = invocation.args[0] || window.event;
        // Interestingly, FF provides the explicitOriginalTarget property that can avoid
        // grabClickTarget above, but avoid browser specifics for now.
        var target = /*event.explicitOriginalTarget ||*/ this.lastClickTarget;
        this.submit(target && target.name);
        // If real submit has to occur, it's taken care of in submit()
        return false;
    },

    /**
     * Submit the form, choosing the right transport depending on the widgets in the form.
     *
     * @param name the name of the widget that triggered the submit (if any)
     * @param params an object containing additional parameters to be added to the
     *              query string (optional)
     */
    submit: function(name, params) {
        var form = this.domNode;                                /* the form node */
        var thisWidget = this;                                  /* closure magic for the callback handler */
        var mimetype = "text/xml";                              /* the default mime-type */
        if (!params) params = {};                               /* create if not passed */
        
        // Provide feedback that something is happening.
        document.body.style.cursor = "wait";
        
        // The "ajax-action" attribute specifies an alternate submit location used in Ajax mode.
        // This allows to use Ajax in the portal where forms are normally posted to the portal URL.
        var uri = form.getAttribute("ajax-action");
        if (!uri) uri = form.action;
        if (uri == "") uri = document.location;

        form["forms_submit_id"].value = name;                   /* name of the button doing the submit */
        params["cocoon-ajax"] = true;                           /* tell Cocoon we want AJAX-style browser updates */
        if (dojo.io.formHasFile(form)) {                        /* check for file-upload fields */
            if (dojo.render.html.safari) {                      /* poor old safari, hopefully Apple will fix this soon, it works in the nightly builds of WebKit (2006-10-11) */
                form.submit();                                  /* do a full-page submit */
                return;
            }
            dojo.require("dojo.io.IframeIO");                   /* using IframeIO as we have file-upload fields */
            mimetype = "text/html";                             /* a different mime-type is required for IframeIO */
        }

        dojo.io.bind({
            url: uri,
            handle: function(type, data, evt) { thisWidget._handleBrowserUpdate(thisWidget, name, type, data, evt) },
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
     * Handle the server response
     */
    _handleBrowserUpdate: function(widget, name, type, data, evt) {
        // Restore normal cursor
        document.body.style.cursor = "auto";
        // Attempt to re-enable the click target
        if (this.domNode[name]) this.domNode[name].disabled = false;
        
        var updater = new cocoon.ajax.BUHandler();
        if (type == "load") {
            // Handle browser update directives
            if (!data) {
                cocoon.ajax.BUHandler.handleError("No xml answer", data);
                return;
            }
            updater.handlers['continue'] = function() { widget._continue(); }
            updater.processResponse(data, evt);
        } else if (type == "error") {
            updater.handleError("Request failed", data);
        } else {
            // umm, how did we get here ?
        }
    },
    
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

// override built-in dojo function, we do not care about 'file' fields that are disabled
dojo.io.checkChildrenForFile = function(node) {
    var hasFile = false;
    var inputs = node.getElementsByTagName("input");
    dojo.lang.forEach(inputs, function(input){
        if(hasFile){ return; }
        if(input.getAttribute("type")=="file" && !input.disabled){
            hasFile = true;
        }
    });
    return hasFile;
}
