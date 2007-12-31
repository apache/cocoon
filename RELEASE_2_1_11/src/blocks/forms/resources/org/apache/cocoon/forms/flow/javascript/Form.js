/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * @version $Id$
 */

// Revisit this class, so it gives access to more than the value.
defineClass("org.apache.cocoon.forms.flow.javascript.ScriptableWidget");

/**
 * Create a form from XML form definition, given as URI, Source or DOM
 */
function Form(formDefinition) {
    var formMgr = cocoon.getComponent(Packages.org.apache.cocoon.forms.FormManager.ROLE);
    try {
        this.form = formMgr.createForm(formDefinition);
    } finally {
        cocoon.releaseComponent(formMgr);
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
 * Get the actual form widget (the Java object)
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
    return this.sendFormAndWait(uri, viewdata, ttl);
}

/**
 * Show form statelessly, without creating a continuation.
 *
 * @parameter uri the page uri (like in cocoon.sendPageAndWait())
 * @parameter viewdata some data for the view (like in cocoon.sendPageAndWait()).
 *            The "{FormsPipelineConfig.CFORMSKEY}" and "locale" properties are added to this object.
 */
Form.prototype.sendForm = function(uri, viewdata) {
    viewdata = this.buildViewData(viewdata)
    cocoon.sendPage(uri, viewdata);

    // Clean up after sending the page
    if (this.cleanupHook) {
        this.cleanupHook(this);
    }

    FOM_Cocoon.suicide();
}

/**
 * Process stateless form submit.
 *
 * @parameter viewdata some data for the view (like in cocoon.sendPageAndWait()).
 *            The "{FormsPipelineConfig.CFORMSKEY}" and "locale" properties are added to this object.
 */
Form.prototype.processForm = function(viewdata) {
    viewdata = this.buildViewData(viewdata)

    var formContext = new Packages.org.apache.cocoon.forms.FormContext(cocoon.request, this.locale);

    // Prematurely add the viewdata as in the object model so that event listeners can use it
    // (the same is done by cocoon.sendPage())
    // FIXME : hack needed because FOM doesn't provide access to the object model
    var objectModel = org.apache.cocoon.components.ContextHelper.getObjectModel(this.avalonContext);
    org.apache.cocoon.components.flow.FlowHelper.setContextObject(objectModel, viewdata);

    if (this.restoreHook) {
        this.restoreHook(this);
    }

    var finished = this.form.process(formContext);

    if (finished) {
        this.isValid = this.form.isValid();
        var widget = this.form.getSubmitWidget();
        // Can be null on "normal" submit
        this.submitId = widget == null ? null : widget.getId();
    }

    return finished;
}

Form.prototype.buildViewData = function(viewdata) {
    if (!viewdata) {
        viewdata = new Object();
    }

    viewdata[Packages.org.apache.cocoon.forms.transformation.FormsPipelineConfig.CFORMSKEY] = this.form;

    if (this.locale == null) {
        this.locale = java.util.Locale.getDefault();
    }

    viewdata["locale"] = this.locale;

    return viewdata
}

/**
 * Same as showForm
 */
Form.prototype.sendFormAndWait = function(uri, viewdata, ttl) {
    var finished = false;

    var comingBack = false;
    var bookmark = cocoon.createWebContinuation(ttl);

    // Attach the form to the continuation so that we can access by just knowing the continuation id
    bookmark.setAttribute("form", this.form);

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
                // Ask the client to issue a new request reloading the whole page.
                // As we have nothing special to send back, so a header should be just what we need...
                // e.g. cocoon.response.setHeader("X-Cocoon-Ajax", "continue");
                //      cocoon.sendStatus(200);
                // ...but Safari doesn't consider empty responses (with content-length = 0) as
                // valid ones. So send a continue response by using directly the HttpResponse's
                // output stream. Avoiding this hack would require to put an additional pipeline
                // in the sitemap for just sending constant response, which isn't nice.
                cocoon.sendStatus(200);
                var httpResponse = objectModel.get(org.apache.cocoon.environment.http.HttpEnvironment.HTTP_RESPONSE_OBJECT);

                if (httpResponse) {
                    var text ="";
                    if (cocoon.request.getParameter("dojo.transport")=="iframe") {
                        //MSIE accepts only HTML content when using the iframe
                        //dojo transport, so we have to wrap everything into
                        //html as demonstrated by IframeTransport-bu-styling.xsl
                        httpResponse.setContentType("text/html");
                        text = "<html><head><title>Browser Update Data-Island</title></head><body>"
                             + "<form id='browser-update'>"
                             + "<textarea name='continue'></textarea>"
                             + "</form>"
                             + "</body></html>";
                    } else {
                        httpResponse.setContentType("text/xml");
                        text = "<?xml version='1.0'?><bu:document xmlns:bu='"
                             + org.apache.cocoon.ajax.BrowserUpdateTransformer.BU_NSURI
                             + "'><bu:continue/></bu:document>";
                    }
                    httpResponse.setContentLength(text.length);
                    httpResponse.writer.print(text);
                } else {
                    // Empty response
                    cocoon.response.setHeader("Content-Length", "0");
                }

                FOM_Cocoon.suicide();
            }

            return bookmark;
        }
    }

    comingBack = true;
    viewdata = this.buildViewData(viewdata)
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

/**
 * Create a binding from XML binding definition, given as URI, Source or DOM
 */
Form.prototype.createBinding = function(bindingDefinition) {
    var bindingManager = cocoon.getComponent(Packages.org.apache.cocoon.forms.binding.BindingManager.ROLE);
    try {
        this.binding = bindingManager.createBinding(bindingDefinition);
    } finally {
        cocoon.releaseComponent(bindingManager);
    }
}

Form.prototype.load = function(object) {
    if (this.binding == null) {
        throw new Error("Binding not configured for this form.");
    }
    this.form.informStartLoadingModel();
    this.binding.loadFormFromModel(this.form, object);
    this.form.informEndLoadingModel();
}

Form.prototype.save = function(object) {
    if (this.binding == null) {
        throw new Error("Binding not configured for this form.");
    }
    this.form.informStartSavingModel();
    this.binding.saveFormToModel(this.form, object);
    this.form.informEndSavingModel();
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
        // Disambiguate toSAX method: Pick the one with Source argument.
        Packages.org.apache.cocoon.components.source.SourceUtil["toSAX(org.apache.excalibur.source.Source,org.xml.sax.ContentHandler)"](source, this.getXML())
    } finally {
        if (source != null) {
            resolver.release(source);
        }
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
            transformer.setOutputProperty(Packages.javax.xml.transform.OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(Packages.javax.xml.transform.OutputKeys.METHOD, "xml");
            transformerHandler.setResult(new Packages.javax.xml.transform.stream.StreamResult(outputStream));
            this.getXML().toSAX(transformerHandler);
        } else {
            throw new Packages.org.apache.cocoon.ProcessingException("Cannot write to source " + uri);
        }

    } finally {
        if (source != null) {
            resolver.release(source);
        }
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
