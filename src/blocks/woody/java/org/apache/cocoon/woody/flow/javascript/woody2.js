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
 * Implementation of the Woody/FlowScript integration.
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: woody2.js,v 1.12 2004/03/18 14:01:44 joerg Exp $
 */

// Revisit this class, so it gives access to more than the value.
defineClass("org.apache.cocoon.woody.flow.javascript.ScriptableWidget");

/**
 * Create a Woody form, given the URI of its definition file
 */
function Form(uri) {
    var formMgr = null;
    var resolver = null;
    var src = null;
    try {
        formMgr = cocoon.getComponent(Packages.org.apache.cocoon.woody.FormManager.ROLE);
        resolver = cocoon.getComponent(Packages.org.apache.cocoon.environment.SourceResolver.ROLE);
        src = resolver.resolveURI(uri);
        this.form = formMgr.createForm(src);
        this.binding = null;
        this.validator = null;
        this.eventHandler = null;
        // TODO : do we keep this ?
        this.formWidget = new Widget(this.form);
        
    } finally {
        cocoon.releaseComponent(formMgr);
        if (src != null) resolver.release(src);
        cocoon.releaseComponent(resolver);
    }
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
 * Manages the display of a form and its validation.
 *
 * This uses some additionnal propertied on the form object :
 * - "locale" : the form locale (default locale is used if not set)
 * - "validator" : additional validation function. This function receives
 *   the form object as parameter and should return a boolean indicating
 *   if the form handling is finished (true) or if the form should be
 *   redisplayed again (false)
 *
 * On return, the calling code can check some properties to know the form result :
 * - "isValid" : true if the form was sucessfully validated
 * - "submitId" : the id of the widget that triggered the form submit (can be null)
 *
 * @parameter uri the page uri (like in cocoon.sendPageAndWait())
 * @parameter bizdata some business data for the view (like in cocoon.sendPageAndWait()).
 *            The "woody-form" and "locale" properties are added to this object.
 */
Form.prototype.showForm = function(uri, bizData) {

    if (bizData == undefined) bizData = new Object();
    bizData[Packages.org.apache.cocoon.woody.transformation.WoodyPipelineConfig.WOODY_FORM] = this.form;

    if (this.locale == null)
        this.locale = java.util.Locale.getDefault();
    bizData["locale"] = this.locale;
    
    // Keep the first continuation that will be created as the result of this function
    var result = null;

    var finished = false;
    this.isValid = false;

    do {
        var k = cocoon.sendPageAndWait(uri, bizData);
        if (result == null) result = k;
        
        var formContext = 
            Packages.org.apache.cocoon.woody.flow.javascript.WoodyFlowHelper.getFormContext(cocoon, this.locale);

        // Prematurely add the bizData as a request attribute so that event listeners can use it
        // (the same is done by cocoon.sendPage())
        cocoon.request.setAttribute(Packages.org.apache.cocoon.components.flow.FlowHelper.CONTEXT_OBJECT, bizData);

        finished = this.form.process(formContext);
        
        // Additional flow-level validation
        if (finished && this.form.isValid()) {
            if (this.validator == null) {
              this.isValid = true;
            } else {
              this.isValid = this.validator(this.form, bizData);
              finished = this.isValid;
            }
        }
        
        // FIXME: Theoretically, we should clone the form widget (this.form) to ensure it keeps its
        // value with the continuation. We don't do it since there should me not much pratical consequences
        // except a sudden change of repeaters whose size changed from a continuation to another.
        
    } while(!finished);

    var widget = this.form.getSubmitWidget();
    // Can be null on "normal" submit
    this.submitId = widget == null ? null : widget.getId(); 
    
    return result;
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

Form.prototype.setAttribute = function(name, value) {
    this.form.setAttribute(name, value);
}

Form.prototype.getAttribute = function(name) {
    return this.form.getAttribute(name);
}

Form.prototype.removeAttribute = function(name) {
    this.form.removeAttribute(name);
}

function woody() {
    var form = new Form(cocoon.parameters["form-definition"]);

    var args = [form];

    // set the binding on the form if there's any
    var bindingURI = cocoon.parameters["bindingURI"];
    if (bindingURI != null) {
        form.createBinding(bindingURI);
    }

    var funcName = cocoon.parameters["function"];
    var func = this[funcName];

    if (!func) {
        throw "Function \"" + funcName + "\" is not defined.";
    } else if (!(func instanceof Function)) {
        throw "\"" + funcName + "\" is not a function.";
    }

    func.apply(this, args);
}
