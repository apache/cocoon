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

defineClass("org.apache.cocoon.forms.flow.javascript.v3.ScriptableWidget");
importClass(Packages.org.apache.cocoon.forms.FormContext);

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
        this.form = form;
        this.model = new Widget(form);
        this.bindings_ = new Object();
        this.locale = java.util.Locale.getDefault();
    } finally {
        cocoon.releaseComponent(formMgr);
        if (src != null) resolver.release(src);
        cocoon.releaseComponent(resolver);
    }
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
 * Displays a form until it is valid.
 */
Form.prototype.showForm = function(uri, viewData, fun, ttl) {
    if (viewData == null)
        viewData = new Object();
    viewData["CocoonFormsInstance"] = this.form;

    var webCont;
    var finished = false;
    while (!finished) {
        webCont = cocoon.sendPageAndWait(uri, viewData, fun, ttl);
        finished = this.processSubmit();
    }
    return webCont;
}

Form.prototype.processSubmit = function() {
    var formContext = new FormContext(cocoon.request, this.locale);
    return this.form.process(formContext);
}

Form.prototype.createEmptySelectionList = function(message) {
    return new Packages.org.apache.cocoon.forms.datatype.EmptySelectionList(message);
}

Form.prototype.createBinding = function(arg1, arg2) {
    var name;
    var bindingURI;
    if (arguments.length == 2) {
        name = arg1;
        bindingURI = arg2;
    } else {
        name = "default";
        bindingURI = arg1;
    }
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
        this.bindings_[name] = bindingManager.createBinding(source);
    } finally {
        if (source != null) {
            resolver.release(source);
        }
        cocoon.releaseComponent(bindingManager);
        cocoon.releaseComponent(resolver);
    }
}

Form.prototype.load = function(arg1, arg2) {
    var name;
    var object;
    if (arguments.length == 2) {
        name == arg1;
        object = arg2;
    } else {
        name = "default";
        object = arg1;
    }
    if (this.bindings_[name] == null) {
        throw "Binding \"" + name + "\" not configured for this form.";
    }
    this.bindings_[name].loadFormFromModel(this.getWidget(), object);
}

Form.prototype.save = function(arg1, arg2) {
    var name;
    var object;
    if (arguments.length == 2) {
        name = arg1;
        object = arg2;
    } else {
        name = "default";
        object = arg1;
    }
    if (this.bindings_[name] == null) {
        throw "Binding \"" + name + "\" not configured for this form.";
    }
    this.bindings_[name].saveFormToModel(this.getWidget(), object);
}

Form.prototype.setAttribute = function(name, value) {
    this.form.setAttribute(name, value);
}

Form.prototype.getAttribute = function(name) {
    return this.form.getAttribute(name);
}

Form.prototype.removeAttribute = function(name) {
    this.form.removeAttribute(name);
}