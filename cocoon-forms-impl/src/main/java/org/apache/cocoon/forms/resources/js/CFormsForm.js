/*
 * Copyright 1999-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
dojo.require("cocoon.ajax.BUHandler");

/**
 * Dojo widget for forms, that handles the Ajax interaction with the server.
 *
 * @version $Id$
 */
// Extends the base DomWidget class. We don't need all the HtmlWidget stuff
// but need traversal of the DOM to build child widgets
cocoon.forms.CFormsForm = function() {
	dojo.widget.DomWidget.call(this);
};

dojo.inherits(cocoon.forms.CFormsForm, dojo.widget.DomWidget);

dojo.lang.extend(cocoon.forms.CFormsForm, {
	// Properties
	
	// Widget definition
	widgetType: "CFormsForm",
    isContainer: true,
    buildRendering: function(args, parserFragment, parentWidget) {
	
        // Magical statement to get the dom node, stolen in DomWidget
	    this.domNode = parserFragment["dojo:"+this.widgetType.toLowerCase()].nodeRef;

        this.id = this.domNode.getAttribute("id");

        this.domNode.setAttribute("dojoWidgetId", this.widgetId);

        dojo.event.connect("around", this.domNode, "onsubmit", this, "_browserSubmit");
        dojo.event.connect(this.domNode, "onclick", this, "_grabClickTarget");
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

        this.submit(target.name);
        // If real submit has to occur, it's taken care of in submit()
        return false;
    },

    /**
     * Submit the form, choosing automatically Ajax or fullpage mode depending on the
     * widgets in the form.
     *
     * @param name the name of the widget that triggered the submit (if any)
     * @param params an object containing additional parameters to be added to the
     *        query string (optional)
     */
    submit: function(name, params) {
        var form = this.domNode;
        
        var query = cocoon.forms.buildQueryString(form, name);
        if (!query) {
            if (params) alert("FIXME: handle additional params in CFormsForm.submit()");
            // Some inputs are not ajax-compatible. Fall back to full page reload
            form["forms_submit_id"].value = name;
            form.submit();
            return;
        }
        
        query += cocoon.forms.encodeParams(params, true);

        // Provide feedback that something is happening.
        document.body.style.cursor = "wait";

        // The "ajax-action" attribute specifies an alternate submit location used in Ajax mode.
        // This allows to use Ajax in the portal where forms are normally posted to the portal URL.
        var uri = form.getAttribute("ajax-action");
        if (!uri) uri = form.action;
        if (uri == "") uri = document.location;
        
        // FIXME: revisit with dojo.io.bind(), but need to see what happens if we say
        // mimetype="text/xml" and no XML is sent back
        var req = dojo.hostenv.getXmlhttpObject()

        req.open("POST", uri, true);
        req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        var thisWidget = this;
        req.onreadystatechange = function() {
            if (req.readyState == 4) {
                thisWidget._handleBrowserUpdate(req);
            }
        }
        req.send(query);
    },
    
    /**
     * Handle the server response
     */
    _handleBrowserUpdate: function(request) {
	    // Restore normal cursor
	    document.body.style.cursor = "auto";
        var updater = new cocoon.ajax.BUHandler();
	    if (request.status == 200) {
            // Handle browser update directives
            var doc = request.responseXML;
            if (!doc) {
                cocoon.ajax.BUHandler.handleError("No xml answer", request);
                return;
            }
            
            var thisWidget = this;
            updater.handlers['continue'] = function() { thisWidget._continue(); }
            updater.processResponse(doc, request);
	    } else {
	        updater.handleError("Request failed - status=" + request.status, request);
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

dojo.widget.tags.addParseTreeHandler("dojo:CFormsForm");
// Register this module as a widget package
dojo.widget.manager.registerWidgetPackage("cocoon.forms");

