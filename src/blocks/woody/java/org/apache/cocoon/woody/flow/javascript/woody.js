/*
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/

defineClass("org.apache.cocoon.woody.flow.javascript.ScriptableWidget");
defineClass("org.apache.cocoon.woody.flow.javascript.Woody");

Woody.suicide = new Continuation();

function Form(formDef) {
    var formMgr = null;
    var resolver = null;
    var src = null;
    try {
        formMgr = cocoon.getComponent(Packages.org.apache.cocoon.woody.FormManager.ROLE);
        resolver = cocoon.getComponent(Packages.org.apache.cocoon.environment.SourceResolver.ROLE);
        src = resolver.resolveURI(formDef);
        this.form = formMgr.createForm(src);
        this.formWidget = new Widget(this.form);
        this.lastWebContinuation = null;
        this.rootContinuation = null;
        this.binding = null;
        this.woody = new Woody();
    } finally {
        cocoon.releaseComponent(formMgr);
        if (src != null)
            resolver.release(src);
        cocoon.releaseComponent(resolver);
    }
}

Form.prototype.start = function(lastWebCont, timeToLive) {
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
        return result;
    }
    return result.kont;
} 

Form.prototype._start = function(lastWebCont, timeToLive) {
    var k = new Continuation();
    var kont = this.woody.makeWebContinuation(k, lastWebCont, timeToLive);
    if (this.rootContinuation == null) {
        this.rootContinuation = kont;
    }
    return {kont: kont};
} 

Form.prototype.getModel = function() {
    return this.formWidget;
}

/**
 * Get a Widget (the java object) from the form.
 * If <code>name</code> is undefined, the form widget itself is returned.
 * Otherwise, the form's child widget of name <code>name</code> is returned.
 */
Form.prototype.getWidget = function(name) {
    if (name == undefined) {
        return this.form;
    } else {
        return this.form.getWidget(name);
    }
}

/**
 * @deprecated use showForm() instead
 */
Form.prototype.show = function(uri, validator, locale) {
    var lastWebCont = this.lastWebContinuation;
    // create a continuation, the invocation of which will resend
    // the page: this will be used to implement automated "back"
    // navigation
    var wk = this.start(lastWebCont);

    if (locale == null)
        locale = java.util.Locale.getDefault();

    while (true) {
        if (cocoon.request == null) {
            // this continuation has been invalidated
            this.dead = true;
            handleInvalidContinuation();
            Woody.suicide();
        }
        var thisWebCont = this._show(uri, locale, wk, {});
        // _show creates a continuation, the invocation of which
        // will return right here: it is used to implement 
        // automated "next" navigation
        if (this.dead ||  cocoon.request == null) {
            // this continuation has been invalidated
            handleInvalidContinuation();
            suicide();
        }

        var formContext = 
            new Packages.org.apache.cocoon.woody.FormContext(this.woody.request, locale);
        var finished = this.form.process(formContext);
        var evt = formContext.getActionEvent();
        if (evt != null) {
            this.submitId = String(evt.getActionCommand());
        } else {
            this.submitId = undefined;
        }
        // If either validation was successfull or there was an event, call the validator
        if ((finished ||this.submitId != null) && validator != undefined) {
            finished = validator(this);
        }
        if (finished) {
            break;
        }
    }
}

/**
 * Manages the display of a form and its validation.
 *
 * This uses some additionnal propertied on the form object :
 * - "locale" : the form locale (default locale is used if not set)
 * - "validator" : additional validation function. This function receives
 *   the form object as parameter and should return a boolean indicating
 *   if the form handling is finished (true) or if the form should be
 *   redisplayed again (false)
 *
 * @parameter uri the page uri (like in cocoon.sendPageAndWait())
 * @parameter bizdata some business data for the view (like in cocoon.sendPageAndWait()).
 *            The "woody-form" and "locale" properties are added to this object.
 */
Form.prototype.showForm = function(uri, bizdata) {
    var lastWebCont = this.lastWebContinuation;
    // create a continuation, the invocation of which will resend
    // the page: this will be used to implement automated "back"
    // navigation
    var wk = this.start(lastWebCont);

    if (this.locale == null)
        this.locale = java.util.Locale.getDefault();

    while (true) {
        if (cocoon.request == null) {
            // this continuation has been invalidated
            this.dead = true;
            handleInvalidContinuation();
            Woody.suicide();
        }
        var thisWebCont = this._show(uri, this.locale, wk, undefined, bizdata == undefined ? new Object() : bizdata);
        // _show creates a continuation, the invocation of which
        // will return right here: it is used to implement 
        // automated "next" navigation
        if (this.dead ||  cocoon.request == null) {
            // this continuation has been invalidated
            handleInvalidContinuation();
            suicide();
        }

        var formContext = 
            new Packages.org.apache.cocoon.woody.FormContext(this.woody.request, this.locale);
        var finished = this.form.process(formContext);
        var evt = formContext.getActionEvent();
        if (evt != null) {
            this.submitId = String(evt.getActionCommand());
        } else {
            this.submitId = undefined;
        }
        // If either validation was successfull or there was an event, call the validator
        if ((finished ||this.submitId != null) && this.validator != undefined) {
            finished = this.validator(this);
        }
        if (finished) {
            break;
        }
    }
}

Form.prototype._show = function(uri, locale, lastWebCont, timeToLive, bizData) {
    var k = new Continuation();
    var wk = this.woody.makeWebContinuation(k, lastWebCont, timeToLive);
    bizData["woody-form"] = this.form;
    bizData["locale"] = locale;
    //var bizData = { "woody-form": this.form, "locale" : locale };
    this.lastWebContinuation = wk;
    this.woody.forwardTo(uri, bizData, wk);
    Woody.suicide();
}

Form.prototype.finish = function() {
    this.rootContinuation.invalidate();
    this.rootContinuation = null;
    this.lastWebContinuation = null;
    this.form = null;
    this.formWidget = null;
}

Form.prototype.getSubmitId = function() {
    return this.submitId;
}

Form.prototype.createBinding = function(bindingURI) {
    var bindingManager = null;
    var source = null;
    var resolver = null;
    try {
        bindingManager = cocoon.getComponent(Packages.org.apache.cocoon.woody.binding.BindingManager.ROLE);
        resolver = cocoon.getComponent(Packages.org.apache.cocoon.environment.SourceResolver.ROLE);
        source = resolver.resolveURI(bindingURI);
        this.binding = bindingManager.createBinding(source);
    } finally {
        if (source != null)
            resolver.release(source);
        cocoon.releaseComponent(bindingManager);
        cocoon.releaseComponent(resolver);
    }
}

Form.prototype.load = function(object) {
    if (this.binding == null)
        throw new Error("Binding not configured for this form.");
    this.binding.loadFormFromModel(this.form, object);
}

Form.prototype.save = function(object) {
    if (this.binding == null)
        throw new Error("Binding not configured for this form.");
    this.binding.saveFormToModel(this.form, object);
}

function woody(form_function, form_definition) {
    var form = new Form(form_definition);
    var args = new Array(arguments.length - 2 + 1);
    args[0] = form;
    for (var i = 2; i < arguments.length; i++) {
        args[i-1] = arguments[i];
    }

    // set the binding on the form if there's any
    var bindingURI = cocoon.parameters["bindingURI"];
    if (bindingURI != null)
        form.createBinding(bindingURI);

    this[form_function].apply(this, args);
}
