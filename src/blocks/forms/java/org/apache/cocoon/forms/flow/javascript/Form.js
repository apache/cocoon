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
 * Implementation of the Cocoon Forms/FlowScript integration.
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version $Id$
 */

// Revisit this class, so it gives access to more than the value.
defineClass("org.apache.cocoon.forms.flow.javascript.ScriptableWidget");

/**
 * Create a form, given the URI of its definition file
 */
function Form(uri) {
    var formMgr = null;
    var resolver = null;
    var src = null;
    var xmlAdapter = null;
    try {
        formMgr = cocoon.getComponent(Packages.org.apache.cocoon.forms.FormManager.ROLE);
        resolver = cocoon.getComponent(Packages.org.apache.cocoon.environment.SourceResolver.ROLE);
        src = resolver.resolveURI(uri);
        this.form = formMgr.createForm(src);
        this.binding = null;
        this.eventHandler = null;
        // FIXME : hack needed because FOM doesn't provide access to the context
        this.avalonContext = formMgr.getAvalonContext();
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
 * Get the actual Form-Widget (the Java object)
 */
Form.prototype.getWidget = function(name) {
    if (name != undefined) {
        throw "getWidget(id) has been deprecated.\n" +
              "Consider using getChild(id) or lookupWidget(path) instead."
    }
    return this.form;
}

/**
 * Get a child Widget (the java object) from the form via its <code>id</code>.
 */
Form.prototype.getChild = function(id) {
    return this.form.getChild(id);
}

/**
 * Get a Widget (the java object) from the form via its <code>path</code>.
 */
Form.prototype.lookupWidget = function(path) {
    return this.form.lookupWidget(path);
}

/**
 * Manages the display of a form and its validation.
 *
 * This uses some additionnal propertied on the form object :
 * - "locale" : the form locale (default locale is used if not set)
 *
 * On return, the calling code can check some properties to know the form result :
 * - "isValid" : true if the form was sucessfully validated
 * - "submitId" : the id of the widget that triggered the form submit (can be null)
 *
 * @parameter uri the page uri (like in cocoon.sendPageAndWait())
 * @parameter bizdata some business data for the view (like in cocoon.sendPageAndWait()).
 *            The "{FormsPipelineConfig.CFORMSKEY}" and "locale" properties are added to this object.
 */
Form.prototype.showForm = function(uri, bizData) {

    if (bizData == undefined) bizData = new Object();
    bizData[Packages.org.apache.cocoon.forms.transformation.FormsPipelineConfig.CFORMSKEY] = this.form;

    if (this.locale == null)
        this.locale = java.util.Locale.getDefault();
    bizData["locale"] = this.locale;

    // Keep the first continuation that will be created as the result of this function
    var result = null;

    var finished = false;
    this.isValid = false;

    // FIXME: Remove check for removed syntax later.
    if (this.validator != undefined) {
        throw "Forms do not support custom javascript validators anymore. Declare your validators in the form model file.";
    }

    // Fire any events pending from binding, etc.
    this.form.fireEvents();

    do {
        var k = cocoon.sendPageAndWait(uri, bizData);
        if (result == null) result = k;

        var formContext = new Packages.org.apache.cocoon.forms.FormContext(cocoon.request, this.locale);

        // Prematurely add the bizData as in the object model so that event listeners can use it
        // (the same is done by cocoon.sendPage())
        // FIXME : hack needed because FOM doesn't provide access to the object model
        var objectModel = org.apache.cocoon.components.ContextHelper.getObjectModel(this.avalonContext);
        org.apache.cocoon.components.flow.FlowHelper.setContextObject(objectModel, bizData);

        finished = this.form.process(formContext);
        if (finished) {
            this.isValid = this.form.isValid();
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
        bindingManager = cocoon.getComponent(Packages.org.apache.cocoon.forms.binding.BindingManager.ROLE);
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

Form.prototype.getXML = function() {
    if (this.xmlAdapter == null)
        this.xmlAdapter = new Packages.org.apache.cocoon.forms.util.XMLAdapter(this.form);
    return this.xmlAdapter;
}

Form.prototype.loadXML = function(uri) {
    var source = null;
    var resolver = null;
    try {
        resolver = cocoon.getComponent(Packages.org.apache.cocoon.environment.SourceResolver.ROLE);
        source = resolver.resolveURI(uri);
        Packages.org.apache.cocoon.components.source.SourceUtil.toSAX(source, this.getXML());
    } finally {
        if (source != null)
            resolver.release(source);
        cocoon.releaseComponent(resolver);
    }
}

Form.prototype.saveXML = function(uri) {
    var source = null;
    var resolver = null;
    var outputStream = null;
    try {
        resolver = cocoon.getComponent(Packages.org.apache.cocoon.environment.SourceResolver.ROLE);
        source = resolver.resolveURI(uri);

        var tf = Packages.javax.xml.transform.TransformerFactory.newInstance();

        if (source instanceof Packages.org.apache.excalibur.source.ModifiableSource
            && tf.getFeature(Packages.javax.xml.transform.sax.SAXTransformerFactory.FEATURE)) {

            outputStream = source.getOutputStream();
            var transformerHandler = tf.newTransformerHandler();
            var transformer = transformerHandler.getTransformer();
            transformer.setOutputProperty(Packages.javax.xml.transform.OutputKeys.INDENT, "true");
            transformer.setOutputProperty(Packages.javax.xml.transform.OutputKeys.METHOD, "xml");
            transformerHandler.setResult(new Packages.javax.xml.transform.stream.StreamResult(outputStream));
            this.getXML().toSAX(transformerHandler);
        } else {
            throw new Packages.org.apache.cocoon.ProcessingException("Cannot write to source " + uri);
        }

    } finally {
        if (source != null)
            resolver.release(source);
        cocoon.releaseComponent(resolver);
        if (outputStream != null) {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (error) {
                cocoon.log.error("Could not flush/close outputstream: " + error);
            }
        }
    }
}

function handleForm() {
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
