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

/**
 * Cocoon Forms JavaScript API
 */

defineClass("org.apache.cocoon.forms.flow.javascript.v2.ScriptableWidget");

/**
 * Create a form, giving either:
 *   - the URI of its definition file
 *   - an fd:form element in the form of a org.w3c.dom.Element
 */
function Form(formDefinition) {
    var formMgr = null;
    var resolver = null;
    var src = null;
    try {
        var FormManager = 
            Packages.org.apache.cocoon.forms.FormManager;
        var SourceResolver = 
            Packages.org.apache.cocoon.environment.SourceResolver;
        formMgr = cocoon.getComponent(FormManager.ROLE);
        var form;
        if ((typeof formDefinition) == "string" || formDefinition instanceof String) {
            resolver = cocoon.getComponent(SourceResolver.ROLE);
            src = resolver.resolveURI(formDefinition);
            form = formMgr.createForm(src);
        } else {
            form = formMgr.createForm(formDefinition)
        }
        this.binding_ = null;
        this.formWidget_ = new Widget(form);
        this.local_ = cocoon.createPageLocal();
        this.locale = java.util.Locale.getDefault();
    } finally {
        cocoon.releaseComponent(formMgr);
        if (src != null) resolver.release(src);
        cocoon.releaseComponent(resolver);
    }
}

/**
 * Get the actual Form-Widget (the Java object)
 */
Form.prototype.getWidget = function() {
    return this.form;
}

/**
 * Get a Widget from the form.
 * If <code>name</code> is undefined, the form widget itself is returned.
 * Otherwise, the form's child widget of name <code>name</code> is returned.
 */
Form.prototype.lookupWidget = function(name) {
    var result;
    if (name == undefined) {
        result = this.formWidget_;
    } else {
        result = this.formWidget_.lookupWidget(name);
    }
    return result;
}

/**
 * Sets the point in your script that will be returned to when the form is 
 * redisplayed. If setBookmark() is not called, this is implicitly set to 
 * the beginning of showForm().
 */
Form.prototype.setBookmark = function() {
    return (this.local_.webContinuation = cocoon.createWebContinuation());
}

/**
 * Returns the bookmark continuation associated with this form, or undefined
 * if setBookmark() has not been called.
 */
Form.prototype.getBookmark = function() {
    return this.local_.webContinuation;
}

/**
 * Manages the display of a form and its validation.
 * @parameter uri the page uri (like in cocoon.sendPageAndWait())
 * @parameter fun optional function which will be executed after pipeline processing. Useful for releasing resources needed during pipeline processing but which should not become part of the continuation
 * @parameter ttl Time to live (in milliseconds) for the continuation created
 * @returns The web continuation associated with submitting this form
 */
Form.prototype.showForm = function(uri, fun, ttl) {
    if (!this.getBookmark()) {
        this.setBookmark();
    }
    var FormContext = Packages.org.apache.cocoon.forms.FormContext;
    // this is needed by the FormTemplateTransformer:
    var javaWidget = this.formWidget_.unwrap();;
    this.formWidget_["CocoonFormsInstance"] = javaWidget;
    cocoon.request.setAttribute(Packages.org.apache.cocoon.forms.transformation.CFORMSKEY, javaWidget);
    var wk = cocoon.sendPageAndWait(uri, this.formWidget_, fun, ttl); 
    var formContext = 
        new FormContext(cocoon.request, this.locale);
    var userErrors = 0;
    this.formWidget_.validationErrorListener = function(widget, error) {
        if (error != null) {
            userErrors++;
        }
    }
    var finished = javaWidget.process(formContext);
    if (this.onValidate) {
        this.onValidate(this);
    }
    if (!finished || userErrors > 0) {
        cocoon.continuation = this.local_.webContinuation;
        this.local_.webContinuation.continuation(this.local_.webContinuation);
    }
    return wk;
}

Form.prototype.createEmptySelectionList = function(message) {
    return new Packages.org.apache.cocoon.forms.datatype.EmptySelectionList(message);
}

Form.prototype.createBinding = function(bindingURI) {
    var bindingManager = null;
    var source = null;
    var resolver = null;
    try {
        var BindingManager = 
            Packages.org.apache.cocoon.forms.binding.BindingManager;
        var SourceResolver = 
            Packages.org.apache.cocoon.environment.SourceResolver;
        bindingManager = cocoon.getComponent(BindingManager.ROLE);
        resolver = cocoon.getComponent(SourceResolver.ROLE);
        source = resolver.resolveURI(bindingURI);
        this.binding_ = bindingManager.createBinding(source);
    } finally {
        if (source != null) {
            resolver.release(source);
        }
        cocoon.releaseComponent(bindingManager);
        cocoon.releaseComponent(resolver);
    }
}

Form.prototype.load = function(object) {
    if (this.binding_ == null) {
        throw new Error("Binding not configured for this form.");
    }
    this.binding_.loadFormFromModel(this.formWidget_.unwrap(), object);
}

Form.prototype.save = function(object) {
    if (this.binding_ == null) {
        throw new Error("Binding not configured for this form.");
    }
    this.binding_.saveFormToModel(this.formWidget_.unwrap(), object);
}
