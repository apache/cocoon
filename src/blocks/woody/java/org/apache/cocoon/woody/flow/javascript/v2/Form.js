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

/**
 * Woody JavaScript API
 */

defineClass("org.apache.cocoon.woody.flow.javascript.v2.ScriptableWidget");

/**
 * Create a Woody form, given the URI of its definition file
 */
function Form(uri) {
    var formMgr = null;
    var resolver = null;
    var src = null;
    try {
        var FormManager = 
            Packages.org.apache.cocoon.woody.FormManager;
        var SourceResolver = 
            Packages.org.apache.cocoon.environment.SourceResolver;
        formMgr = cocoon.getComponent(FormManager.ROLE);
        resolver = cocoon.getComponent(SourceResolver.ROLE);
        src = resolver.resolveURI(uri);
        var form = formMgr.createForm(src);
        this.binding = null;
        this.formWidget = new Widget(form);
        this.local = cocoon.createPageLocal();
    } finally {
        cocoon.releaseComponent(formMgr);
        if (src != null) resolver.release(src);
        cocoon.releaseComponent(resolver);
    }
}


/**
 * Get a Widget from the form.
 * If <code>name</code> is undefined, the form widget itself is returned.
 * Otherwise, the form's child widget of name <code>name</code> is returned.
 */
Form.prototype.getWidget = function(name) {
    var result;
    if (name == undefined) {
        result = this.formWidget;
    } else {
        result = this.formWidget.getWidget(name);
    }
    return result;
}

/**
 * Manages the display of a form and its validation.
 * @parameter uri the page uri (like in cocoon.sendPageAndWait())
 */

Form.prototype.showForm = function(uri, fun) {
    var FormContext = Packages.org.apache.cocoon.woody.FormContext;
    this.local.webContinuation = cocoon.createWebContinuation();
    // this is needed by the WoodyTemplateTransformer:
    var javaWidget = this.formWidget.unwrap();;
    this.formWidget["woody-form"] = javaWidget;
    cocoon.request.setAttribute("woody-form", javaWidget);
    var wk = cocoon.sendPageAndWait(uri, this.formWidget, fun);
    var formContext = 
        new FormContext(cocoon.request, javaWidget.getLocale());
    var userErrors = 0;
    this.formWidget.validationErrorListener = function(widget, error) {
        if (error != null) {
            userErrors++;
        }
    }
    var finished = javaWidget.process(formContext);
    if (this.onValidate) {
        this.onValidate(this);
    }
    if (!finished || userErrors > 0) {
        this.redisplay();
    }
    return wk;
}

Form.prototype.redisplay = function() {
    cocoon.continuation = this.local.webContinuation;
    this.local.webContinuation.continuation(this.local.webContinuation);
}

Form.prototype.createEmptySelectionList = function(message) {
    return new Packages.org.apache.cocoon.woody.datatype.EmptySelectionList(message);
}

Form.prototype.createBinding = function(bindingURI) {
    var bindingManager = null;
    var source = null;
    var resolver = null;
    try {
        var BindingManager = 
            Packages.org.apache.cocoon.woody.binding.BindingManager;
        var SourceResolver = 
            Packages.org.apache.cocoon.environment.SourceResolver;
        bindingManager = cocoon.getComponent(BindingManager.ROLE);
        resolver = cocoon.getComponent(SourceResolver.ROLE);
        source = resolver.resolveURI(bindingURI);
        this.binding = bindingManager.createBinding(source);
    } finally {
        if (source != null) {
            resolver.release(source);
        }
        cocoon.releaseComponent(bindingManager);
        cocoon.releaseComponent(resolver);
    }
}

Form.prototype.load = function(object) {
    if (this.binding == null) {
        throw new Error("Binding not configured for this form.");
    }
    this.binding.loadFormFromModel(this.formWidget.unwrap(), object);
}

Form.prototype.save = function(object) {
    if (this.binding == null) {
        throw new Error("Binding not configured for this form.");
    }
    this.binding.saveFormToModel(this.formWidget.unwrap(), object);
}
