//
// JXForms support
//


/**
 * Creates a new JavaScript wrapper of a Form object
 * see org.apache.cocoon.components.xmlform.Form
 * @param id [String] unique form id
 * @param validatorNS [String] Namespace of validator
 * @param validatorDoc [String] Validator document
 * @param scope [String] either "request" or "session"
 */

function JXForm(id, validatorNS, validatorDoc, scope) {
    if (scope == "session") {
        cocoon.createSession();
    }
    this.cocoon = cocoon; // workaround for Rhino dynamic scope bug
    this.id = id;
    this.lastWebContinuation = null;
    this.validatorNS = validatorNS;
    this.validatorDoc = validatorDoc;
    this.submitId = undefined;
    this.rootContinuation = null;
    this.dead = false;
}

JXForm.jxpathContextFactory = Packages.org.apache.commons.jxpath.JXPathContextFactory.newInstance();

/**
 * Return the model object of this form
 * @return [Object] a Java bean, JavaScript, DOM, or JDOM object 
 */
JXForm.prototype.getModel = function() {
    return this.form.getModel();
}

/**
 * Return the id of the xf:submit element of the current form submission
 * @return [String] id attribute of the button that caused this form to be submitted
 */
JXForm.prototype.getSubmitId = function() {
    return this.submitId;
}

/**
 * Return the phase of the xf:submit element of the current form submission
 * @return [String] phase attribute of the button that caused this form to be submitted
 */
JXForm.prototype.getPhase = function() {
    return this.phase;
}

/**
 * Set the model object of this form
 * @param model [Object] Any Java bean, JavaScript, DOM, or JDOM object 
 */
JXForm.prototype.setModel = function(model) {
    this.form = 
       new Packages.org.apache.cocoon.components.jxforms.xmlform.Form(this.id, 
                                                               model);
    this.context = JXForm.jxpathContextFactory.newContext(null, model);
    this.form.setAutoValidate(false);
    if (this.validatorNS != undefined && this.validatorDoc != undefined) {
        this._setValidator(this.validatorNS, this.validatorDoc);
    }
}

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
    if (result instanceof WebContinuation) {
        this.form.clearViolations();
        return result;
    }
    return result.kont;
} 

JXForm.prototype._start = function(lastWebCont, timeToLive) {
    var k = new Continuation();
    var kont = new WebContinuation(this.cocoon, k, 
                                   lastWebCont, timeToLive);
    if (this.rootContinuation == null) {
        this.rootContinuation = kont;
    }
    return {kont: kont};
} 

/**
 * Adds a violation to this form
 * @param xpath [String] xpath location of field that contains invalid data
 * @param message [String] error message
 */
JXForm.prototype.addViolation = function(xpath, message) {
    var violation = 
       new Packages.org.apache.cocoon.components.jxforms.validation.Violation();
    violation.path = xpath;
    violation.message = message;
    var list = new java.util.LinkedList();
    list.add(violation);
    try {
        this.form.addViolations(list);
    } catch (e) {
        print(e);
        if (e instanceof java.lang.Throwable) {
            e.printStackTrace();
        }
    }
}

/**
 * Does this form have violations?
 * @return [Boolean] true if violations have been added to this form
 */
JXForm.prototype.hasViolations = function() {
    var set = this.form.violationsAsSortedSet;
    return set != null && set.size() > 0;
}

/**
 * Computes the value of an xpath expression against the model of this form
 * @param expr [String] xpath expression
 * @return [Object] result of computing <code>expr</code>
 */
JXForm.prototype.getValue = function(expr) {
    return this.context.getValue(expr);
}

/**
 * Returns an iterator over a nodeset value of an xpath expression evaluated 
 * against the model of this form
 * @param expr [String] xpath expression
 * @return [java.util.Iterator] representing a nodeset 
 */
JXForm.prototype.iterate = function(expr) {
    return this.context.iterate(expr);
}

JXForm.prototype._sendView = function(uri, lastWebCont, timeToLive) {
    var k = new Continuation();
    var wk = new WebContinuation(this.cocoon, k, lastWebCont, timeToLive);
    var bizData = this.form.getModel();
    if (bizData == undefined) {
        bizData = null;
    }
    this.lastWebContinuation = wk;
    this.cocoon.forwardTo("cocoon://" + 
                          this.cocoon.environment.getURIPrefix() + uri,
                          bizData, wk);
    suicide();
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
        if (this.cocoon.request == null) {
            // this continuation has been invalidated
            this.dead = true;
            handleInvalidContinuation();
            suicide();
        }
        this.form.remove(this.cocoon.environment.objectModel, this.id);
        this.form.save(this.cocoon.environment.objectModel, "request");
        var thisWebCont = this._sendView(uri, wk);
        // _sendView creates a continuation, the invocation of which
        // will return right here: it is used to implement 
        // <xf:submit continuation="forward">
        if (this.dead ||  this.cocoon.request == null) {
            // this continuation has been invalidated
            handleInvalidContinuation();
            suicide();
        }
        this.form.populate(this.cocoon.environment.objectModel);
        this.submitId = 
            this.cocoon.request.getAttribute("jxform-submit-id");
        this.phase = 
            this.cocoon.request.getAttribute("jxform-submit-phase");
        if (validator != undefined) {
            validator(this);
        }
        this.form.validate(this.phase);
        if (!this.hasViolations()) {
            this.lastWebContinuation = thisWebCont;
            break;
        }
    }
}

JXForm.prototype._setValidator = function(schNS, schDoc) {
    // if validator params are not specified, then
    // there is no validation by default
    if (schNS == null || schDoc == null) return null;
    var resolver =  this.cocoon.environment;
    var schemaSrc = resolver.resolveURI(schDoc);
    try {
        var is = Packages.org.apache.cocoon.components.source.SourceUtil.getInputSource(schemaSrc);
        var schf = Packages.org.apache.cocoon.components.jxforms.validation.SchemaFactory.lookup(schNS);
        var sch = schf.compileSchema(is);
        this.form.setValidator(sch.newValidator());
    } finally {
        resolver.release(schemaSrc);
    }
}

/**
 * Optionally sends final view to presentation pipeline but doesn't wait for 
 * submission and then destroys internal state of this form
 * @param uri [String] presentation pipeline uri
 */

JXForm.prototype.finish = function(uri) {
    if (uri != undefined) {
	this.form.remove(this.cocoon.environment.objectModel, this.id);
	this.form.save(this.cocoon.environment.objectModel, "request");
	this.cocoon.forwardTo("cocoon://" + 
			      this.cocoon.environment.getURIPrefix() + uri,
			      this.form.getModel(), 
			      null);
    }
    this.dead = true;
    if (this.rootContinuation != null) {
        this.rootContinuation.invalidate();
        this.rootContinuation = null;
        this.lastWebContinuation = null;
    }
}

/**
 * Entry point to a flow-based JXForm application. Replaces the functionality
 * of XMLForm actions.
 * @param application [String] Name of a JavaScript function that represents the page flow for a form
 * @param id [String] Form id
 * @param validator_ns [String] XML namespace of validator
 * @param validator_doc [String] Validator document
 * @param scope [String] one of "request" or "session"
 */

function jxForm(application, id, validator_ns, validator_doc, scope) {
    if (cocoon.request == null) {
        handleInvalidContinuation("");
        return;
    }
    function getCommand() {
        var enum_ = cocoon.request.parameterNames;
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
        cocoon.interpreter.handleContinuation(kontId, 
                                              null,
                                              cocoon.environment);
        return;
    } 
    if (id != null) {
        // Just start a new instance of the application
        var args = new Array(arguments.length - 5 + 1);
        args[0] = new JXForm(id, validator_ns, validator_doc, scope);
        for (var i = 5; i < arguments.length; i++) {
            args[i-4] = arguments[i];
        }
        this[application].apply(this, args);
    } else {
        handleInvalidContinuation(command);
    }
}

