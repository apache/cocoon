/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
defineClass("org.apache.cocoon.components.jxforms.flow.javascript.JXForm");
//
// JXForms support
//

JXForm.suicide = new Continuation();

/**
 * Creates a new web continuation
 * @param lastWebCont [WebContinuation] previous web continuation
 * @param timeToLive [Number] expiration time for this continuation in milliseconds
 * @return [WebContinuation] a new WebContinuation instance
 */
JXForm.prototype.start = function(lastWebCont, timeToLive) {
    var result = this._start(lastWebCont, timeToLive);
    // 
    // _start() will return an Object when it's called
    // the first time. However, when its Continuation is invoked it
    // will return a WebContinuation instead. In the latter case
    // we're going back to the previous page: so 
    // clear the current page's violations before showing the previous page.
    // Without this, violations from the current page will
    // incorrectly be displayed on the previous page.
    if (result instanceof FOM_WebContinuation) {
        this.clearFormViolations();
        return result;
    }
    return result.kont;
} 

JXForm.prototype._start = function(lastWebCont, timeToLive) {
    var k = new Continuation();
    var kont = this.makeWebContinuation(k, lastWebCont, timeToLive);
    if (this.rootContinuation == null) {
        this.rootContinuation = kont;
    }
    return {kont: kont};
} 


JXForm.prototype._sendView = function(uri, lastWebCont, timeToLive) {
    var k = new Continuation();
    var wk = this.makeWebContinuation(k, lastWebCont, timeToLive);
    var bizData = this.getModel();
    if (bizData == undefined) {
        bizData = null;
    }
    this.forwardTo(uri, bizData, wk);
    JXForm.suicide();
}

/**
 * Sends view to presentation pipeline and waits for subsequent submission.
 * Automatically resends view if validation fails.
 * Creates two continuations: one immediately before the page is sent 
 * and one immediately after. These are used to implement automated support
 * for back/forward navigation in the form. When you move forward in the
 * form the second continuation is invoked. When you move back from the
 * following page the first continuation is invoked.
 * @param uri [String] presentation pipeline resource identifier of "view"
 * @param validator [Function] optional function invoked to perform validation
 */
JXForm.prototype.sendView = function(uri, validator) {
    var lastWebCont = this.lastWebContinuation;
    // create a continuation, the invocation of which will resend
    // the page: this is used to implement <xf:submit continuation="back">
    var wk = this.start(lastWebCont);
    while (true) {
        this.removeForm();
        this.saveForm();
        var thisWebCont = this._sendView(uri, wk);
        // _sendView creates a continuation, the invocation of which
        // will return right here: it is used to implement 
        // <xf:submit continuation="forward">
        this.populateForm();
        var phase = cocoon.request.getAttribute("jxform-submit-phase");
        if (validator != undefined) {
            validator(this);
        }
        this.validateForm(phase);
        if (!this.hasViolations()) {
            this.lastWebContinuation = thisWebCont;
            break;
        }
    }
}


JXForm.prototype.finish = function(uri) {
    if (uri != undefined) {
        this.removeForm();
        this.saveForm();
        this.forwardTo(uri, this.getModel(), null);
    }
    if (this.rootContinuation != null) {
        this.rootContinuation.invalidate();
        this.rootContinuation = null;
        this.lastWebContinuation = null;
    }
}

/**
 * Entry point to a flow-based JXForm application. 
 * @param application [String] Name of a JavaScript function that represents the page flow for a form
 * @param id [String] Form id
 * @param validator_ns [String] XML namespace of validator
 * @param validator_doc [String] Validator document
 * @param scope [String] one of "request" or "session"
 */

function jxform(application, id, validator_ns, validator_doc) {
    if (this[application] == undefined || 
	!(this[application] instanceof Function)) {
      throw new Packages.org.apache.cocoon.ResourceNotFoundException("Function \"javascript:"+application+"()\" not found");
    }
    function getCommand() {
        var enum_ = cocoon.request.getParameterNames();
        var command = undefined;
        while (enum_.hasMoreElements()) {
            var paramName = enum_.nextElement();
            // search for the command
            if (paramName.startsWith(Packages.org.apache.cocoon.Constants.ACTION_PARAM_PREFIX)) {
                command =
                    paramName.substring(Packages.org.apache.cocoon.Constants.ACTION_PARAM_PREFIX.length(), paramName.length());
                break;
            }
        }
        // command encodes the continuation id for "back" or "next" actions
        return command;
    }
    var command = getCommand();
    if (command != undefined) {
        // invoke a continuation 
        // command looks like kontId:phase:id
        var kontId = command;
        var izer = new java.util.StringTokenizer(command, ":");
        if (izer.countTokens() == 3) {
            kontId = izer.nextToken();
            var phase = izer.nextToken();
            var id = izer.nextToken();
            cocoon.request.setAttribute("jxform-submit-phase", phase);
            cocoon.request.setAttribute("jxform-submit-id", id);
        }
        JXForm.handleContinuation(kontId, cocoon);
        return;
    } 
    // Just start a new instance of the application
    this[application].apply(this, 
			    [new JXForm(id, validator_ns, validator_doc, undefined)]);
}
