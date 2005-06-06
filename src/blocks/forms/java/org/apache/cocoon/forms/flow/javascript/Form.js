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
function Form(formDefinition) {
    var formMgr = null;
    var resolver = null;
    var src = null;
    var xmlAdapter = null;
    try {
        formMgr = cocoon.getComponent(Packages.org.apache.cocoon.forms.FormManager.ROLE);
        if ((typeof formDefinition) == "string" || formDefinition instanceof String) {
            resolver = cocoon.getComponent(Packages.org.apache.cocoon.environment.SourceResolver.ROLE);
            src = resolver.resolveURI(formDefinition);
            this.form = formMgr.createForm(src);
        } else {
            this.form = formMgr.createForm(formDefinition)
        }
    } finally {
        cocoon.releaseComponent(formMgr);
        if (src != null) resolver.release(src);
        cocoon.releaseComponent(resolver);
    }
    this.binding = null;
    this.eventHandler = null;
    // FIXME : hack needed because FOM doesn't provide access to the context
    this.avalonContext = formMgr.getAvalonContext();
    // TODO : do we keep this ?
    this.formWidget = new Widget(this.form);
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
 * This uses some additionnal properties on the form object :
 * - "locale" : the form locale (default locale is used if not set)
 * - "cleanupHook": a function called after having sent the page displaying the form. This is equivalent
 *       to the "fun" argument of sendPageAndWait(), which allows to perform some cleanup when the pipeline
 *       has been processed. The function is called with a single parameter which is the form it is attached to.
 * - "restoreHook": a function called before processing the form when it has been submitted by
 *       the browser. This allows to restore some environment that is needed by the form processing.
 *       The function is called with a single parameter which is the form it is attached to.
 *
 * On return, the calling code can check some properties to know the form result :
 * - "isValid" : true if the form was sucessfully validated
 * - "submitId" : the id of the widget that triggered the form submit (can be null)
 *
 * @parameter uri the page uri (like in cocoon.sendPageAndWait())
 * @parameter viewdata some data for the view (like in cocoon.sendPageAndWait()).
 *            The "{FormsPipelineConfig.CFORMSKEY}" and "locale" properties are added to this object.
 * @parameter ttl the time to live of the continuation used to display the form
 */
Form.prototype.showForm = function(uri, viewdata, ttl) {

    if (viewdata == undefined) viewdata = new Object();
    viewdata[Packages.org.apache.cocoon.forms.transformation.FormsPipelineConfig.CFORMSKEY] = this.form;

    if (this.locale == null)
        this.locale = java.util.Locale.getDefault();
    viewdata["locale"] = this.locale;

    var finished = false;

    var comingBack = false;
    var bookmark = cocoon.createWebContinuation(ttl);

    if (comingBack) {
        // We come back to the bookmark: process the form
        
        if (finished && cocoon.request.getParameter("cocoon-ajax-continue") != null) {
            // A request with this parameter is sent by the client upon receiving the indication
            // that Ajax interaction on the form is finished (see below).
            // We also check "finished" to ensure we won't exit showForm() because of some
            // faulty or hacked request. It's set to false, this will simply redisplay the form.
            return bookmark;
        }
        
	    if (this.restoreHook) {
	        this.restoreHook(this);
	    }
        var formContext = new Packages.org.apache.cocoon.forms.FormContext(cocoon.request, this.locale);

        // Prematurely add the viewdata as in the object model so that event listeners can use it 	 
        // (the same is done by cocoon.sendPage()) 	 
        // FIXME : hack needed because FOM doesn't provide access to the object model 	 
        var objectModel = org.apache.cocoon.components.ContextHelper.getObjectModel(this.avalonContext); 	 
        org.apache.cocoon.components.flow.FlowHelper.setContextObject(objectModel, viewdata); 	 

        finished = this.form.process(formContext);
        if (finished) {
            this.isValid = this.form.isValid();
            var widget = this.form.getSubmitWidget();
            // Can be null on "normal" submit
            this.submitId = widget == null ? null : widget.getId();
            
            if (cocoon.request.getParameter("cocoon-ajax") != null) {
                // Ask the client to load the page
                cocoon.response.setHeader("X-Cocoon-Ajax", "continue");
                cocoon.response.setHeader("Content-Length", "0");
                cocoon.sendStatus(200);
                FOM_Cocoon.suicide();
            }
            
            return bookmark;
        }
    }
    comingBack = true;
    cocoon.sendPage(uri, viewdata, bookmark);
    
    // Clean up after sending the page
    if (this.cleanupHook) {
        this.cleanupHook(this);
    }
    
    FOM_Cocoon.suicide();
}

Form.prototype.setValidationError = function(error) {
    this.form.setValidationError(error);
}

Form.prototype.getValidationError = function() {
    return this.form.getValidationError();
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
    // get the form definition
    var def = cocoon.parameters["definitionURI"];
    if (def == null) {
        if (cocoon.parameters["form-definition"] != null) {
            cocoon.log.warn("the form-definition parameter in handleForm has changed to definitionURI");
            def = cocoon.parameters["form-definition"];
        } else {
            throw "Definition not configured for this form.";
        }
    }
    // create the Form
    var form = new Form(def);
    // set the binding on the form if there is one
    var bindingURI = cocoon.parameters["bindingURI"];
    if (bindingURI != null) {
        form.createBinding(bindingURI);
    }
    // get the function to call to handle the form
    var funcName = cocoon.parameters["function"];
    var func = this[funcName];
    // check the function exists
    if (!func) {
        throw "Function \"" + funcName + "\" is not defined.";
    } else if (!(func instanceof Function)) {
        throw "\"" + funcName + "\" is not a function.";
    }
    // call the function
    func.apply(this, [form]);
}
